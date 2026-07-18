# easy-factory-core — 领域内核

## 模块定位

定义制造系统的统一领域语言。所有接口、枚举、抽象类的所在地，是整个平台的契约层。

## 包结构

```
com.byz.factory
├── model/         — 核心领域接口与实现
├── data/          — 枚举字典、基础DTO、序列号生成器
├── design/        — 产品蓝图、产品信息、资源估算
├── manufacture/   — 产品模型、已用资源
├── plan/          — 生产计划
├── testing/       — 可制造性检查
├── script/        — 脚本化动作
└── exception/     — 领域异常
```

## 核心领域模型

### 工厂 (IFactory / Factory)

```java
public interface IFactory {
    String getCode();
    String getName();
    List<IProcess> getProcesses();
    List<IActionModel> getActions();  // 汇总所有工序的动作
}
```

工厂是工序的容器，代表一个可执行的生产系统。

### 工序 (IProcess / Process)

```java
public interface IProcess {
    String getCode();
    String getName();
    int getOrder();
    List<IActionModel> getActions();
    IResourcePack getResourcePack();
    List<IResourceModel> requireResources();
    IResourcePack execute(IResourceModel... inputResources);
}
```

工序按 `order` 排序，包含一组有序动作。`execute()` 遍历动作链表，传递输入资源，累积输出资源。

### 动作 (IActionModel / Action)

```java
public interface IActionModel {
    String getCode();
    String getName();
    int getOrder();
    Dict.Importance getImportance();
    String getScript();
    List<IResourceModel> requireResources();
    IResourcePack execute(IProcess process, IResourceModel... inputResources);
}
```

关键设计：
- `importance`: Optional（可选动作，可跳过）/ Require（必要动作）
- `script`: JavaScript 脚本字符串，如果存在则通过 ScriptExecutor 执行
- `execute()`: 如果有脚本，调用脚本；否则返回工序的 resourcePack

### 资源 (IResourceModel / ResourceModel)

```java
public interface IResourceModel {
    String getName();
    Dict.SourceGroup getGroup();
    Dict.SourceType getType();
    BigDecimal getNumber();
    void add(BigDecimal number);
    void use(BigDecimal number);      // 消耗，有负余额保护
    boolean isEmpty();
}
```

按 4M1E (人机料法环) 分类，所有数量操作均在 `ResourceModel` 中有负余额保护。

**子类型接口：**
- `IMaterialsModel` — 物料 (group = Material)
- `IMachineModel` — 机器 (group = Machine)
- `IPersonnelModel` — 人员 (group = Personnel)

### 资源包 (IResourcePack / ResourcePack)

```java
public interface IResourcePack {
    List<IResourceModel> getResources();
    void merge(List<IResourceModel> resources);
    void compress();                  // 按组+名称去重，汇总数量
    List<IResourceModel> requireResources();
    IResourcePack copy();
    boolean isEmpty();
}
```

资源包即 BOM / 配方组分。核心操作 `compress()` 将相同 (group, name) 的资源合并，数量累加。

## 数据字典 (Dict)

```
Dict.Execute:   Nothing | Create | Add | Use | Change | Convert
Dict.Control:   Default | Interrupt | Count | Timing | Repeat
Dict.Importance: Optional | Require
Dict.SourceGroup: Other | Personnel | Machine | Material | Method | Environment
Dict.SourceType:  Other | Machine
```

## 蓝图与产品

### IBlueprint — 产品工序定义

```java
public interface IBlueprint {
    List<IProcess> getProcesses();
}
```

### IProductInfoModel — 产品信息

```java
public interface IProductInfoModel {
    String getName();
    IBlueprint getBlueprint();
}
```

### IEstimateResourceModel — 资源估算

```java
public interface IEstimateResourceModel extends IResourceModel {
    BigDecimal getEstimateNumber();
    BigDecimal getDeviation();
    BigDecimal getMinNumber();
    BigDecimal getMaxNumber();
}
```

用于工序设计阶段的资源定额估算（理论用量、偏差、最小-最大范围）。

## 可制造性检查

### IProductChecker / ProductChecker

```java
public interface IProductChecker {
    IProductFactoryResult check(IProductInfoModel product, IFactory factory);
}
```

验证给定工厂是否能生产指定产品：
- 比对产品蓝图的工序列表与工厂的工序列表
- 返回逐工序的通过/失败结果

## 脚本执行器

### ScriptExecutor

```java
public class ScriptExecutor {
    public Object execute(String script, String functionName, Object... args);
}
```

使用 IScriptEngine（当前 Nashorn 实现，后续迁移到 GraalJS）在运行时编译并调用 JS 函数。动作脚本的标准签名为：

```javascript
function execute(process, inputResources) {
    // 自定义生产逻辑
    return process.getResourcePack();
}
```

## 脚本引擎

### IScriptEngine 抽象

```java
public interface IScriptEngine {
    CompiledScript compile(String scriptId, String source);
    Object execute(CompiledScript compiled, ScriptContext context);
    Object eval(String source, ScriptContext context);
    String getName();
    boolean isSandboxed();
}
```

实现类: `GraalScriptEngine` (推荐), `NashornScriptEngine` (过渡), `J2V8ScriptEngine` (备选)

### 安全沙箱

默认全部禁止，只开放 ScriptContext 中显式绑定的白名单 API：
- ❌ Java 类访问、IO、线程、进程、JNI
- ✅ 仅能调用预授权的 API 表面对象

### 脚本标准化

每个脚本必须使用 JSDoc 元数据头：
```javascript
/**
 * @id          qms.quality-gate.v1
 * @name        质量门禁判定
 * @version     1.0.0
 * @module      qms
 */
```

## 新增接口

### IBatch — 批次概念

```java
public interface IBatch {
    String getBatchNo();
    String getProductCode();
    IBlueprint getBlueprint();
    BigDecimal getBatchSize();
    BigDecimal getActualYield();
    BatchStatus getStatus();
    List<? extends ITraceable> getTraceRecords();
}
```

### ITraceable — 追溯接口

每个资源操作回答：谁、什么时候、在哪个批、哪个工序、做了什么。

### ILifecycle — 状态机

```java
public interface ILifecycle<S extends ILifecycle.StatusEnum> {
    S getStatus();
    void transition(S target);    // 校验合法性后转换
    boolean canTransition(S target);
}
```

### IDomainEvent — 领域事件

```java
public interface IDomainEvent {
    String getEventId();
    String getEventType();        // 如 "mes.process.started"
    String getSource();
    Object getPayload();
}
```

`DomainEventPublisher` 提供静态的 publish/subscribe 机制，支持模块间异步解耦通信。

### IWorkOrder — 工单抽象

MES 的核心实体：工单号、产品、蓝图、批次、工厂、状态、时间节点。

### IInspectionOrder — 检验指令

QMS 的核心实体：检验编号、检验类型(IQC/IPQC/FQC/OQC)、检验方案、项目进度。

## 已修复问题 (v0.1.1)

1. ~~`IAction` 接口未定义~~ ✅ `com.byz.factory.model.IAction`
2. ~~`GroupType` 枚举未定义~~ ✅ 统一为 `Dict.SourceGroup`，增加 `Product`
3. ~~`IResource` 接口未定义~~ ✅ `com.byz.factory.model.IResource` (extends IData)
4. ~~HuTool `StrUtil` 依赖~~ ✅ `script/Action` 改用 JDK `String.isBlank()`
5. ~~Constant.java 构造器参数不匹配~~ ✅ 修复构造参数 + 独立 setScript
6. ~~ActionGroup 继承错误的 Action~~ ✅ 改为继承 `model.Action`
7. ~~Process.execute() 动作链输出不传递~~ ✅ 前一个动作的输出作为下一个动作的输入
8. ~~Process.setResourcePack/setRequireResources 未实现~~ ✅ 字段存储
9. ~~Action.execute() 空实现~~ ✅ 按 executeType 路由资源处理
10. ~~BatchStatus 缺少 RELEASED~~ ✅ 已补充 APPROVED→RELEASED→ARCHIVED
11. ~~script.Action 的 getImportance 孤儿方法~~ ✅ IAction 接口增加 getImportance()

## 已知待解决问题

1. 部分类为空壳：`ProductFactoryResult`、`ProcessRoute`、`BillOfMaterial`（待业务填充）
2. GraalJS 沙箱待 GraalVM SDK 依赖到位后真正启用（POM 中已预留注释）
3. 脚本引擎 ScriptExecutor (Nashorn) 与 IScriptEngine (GraalJS) 双轨并存，待统一
4. `IProductChecker.check()` 返回 null，待实现实际检查逻辑
5. 无正式单元测试覆盖

## AI 协作建议（2026-07-18）

以下为本次设计评审后，建议在后续会话中优先处理的事项：

### 优先级 P0 — 阻塞性问题

1. **修复脚本引擎空指针** — `ScriptEngines.getDefault()` 返回 null，导致 `IActionModel.execute()` 脚本路径崩溃。建议：在 `IActionModel.execute()` 默认实现中增加 null 检查，引擎不可用时静默回退到 executeType 分支。

2. **修复 DomainEventPublisher.subscribePrefix()** — Javadoc 声称前缀匹配，实际做精确匹配。应遍历订阅者 key，用 `startsWith()` 匹配。同时补充 `unsubscribe()` 方法。

3. **更新已知待解决问题清单** — 部分条目已过期：
   - `ProcessRoute` 空壳 → `IProcessRoute` 已删除，但 ProcessRoute 类仍保留
   - `BillOfMaterial` 空壳 → 已接口化 `IBillOfMaterial`
   - `IProductChecker.check()` 返回 null → 确认是否仍存在
   - 无单元测试 → 现有 70 个测试，需更新描述

### 优先级 P1 — 增强健壮性

4. **Action 子接口桥接** — `IEquipmentAction.executeEquipment()` 等 5 个子接口方法与 `IAction.execute()` 标准路径无连接。建议在每个子接口中提供 `default execute()` 桥接实现，先调用 `IActionModel.super.execute()`，再调用子接口专用方法。

5. **GraalJS 依赖** — POM 中已注释。要么正式启用（解决依赖下载问题），要么标注为长期 TODO 并更新文档说明当前状态。

6. **补充缺失的实现** — `IBlueprint` 是引用最频繁的接口（被 IBatch、IWorkOrder、所有 operation 计算器使用），但仍无具体实现类。建议在 core 或 test 模块提供一个 `SimpleBlueprint` 实现。

