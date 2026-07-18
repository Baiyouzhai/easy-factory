# CLAUDE.md

## 项目概述

**easy-factory** 是一个基于 DDD（领域驱动设计）的制造系统平台。核心思想：以 `easy-factory-core` 为领域内核，定义工序(Process)、动作(Action)、资源(Resource)、蓝图(Blueprint) 等统一语言，衍生 MES/QMS/PLM/LIMS/Equip/IoT/ERP 子系统。

### 核心领域概念

```
Factory(工厂) → Process(工序) → Action(动作) ─→ Resource(资源)
                                    │              ├── Personnel(人)
                                    │              ├── Machine(机)
                                    │              ├── Material(料)
                                    │              ├── Method(法)
                                    │              ├── Environment(环)
                                    │              └── Product(产品)
                                    │
                              Blueprint(蓝图) = 产品的工序路线
                              Batch(批次) = 制造追溯的基本单元
```

### 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 (LTS) |
| 框架 | Spring Boot | 3.x (web模块) |
| 构建 | Maven | 3.9+ |
| 脚本引擎 | Nashorn (当前) → GraalJS (计划) | 15.4 |
| JSON | fastjson2 | 2.0.53 |
| 工具 | Lombok, HuTool | — |

## 构建命令

```bash
# 编译全项目
mvn clean compile

# 运行测试
mvn test

# 跳过测试打包
mvn clean package -DskipTests

# 启动 web 模块
cd easy-factory-web && mvn spring-boot:run

# 仅编译 core 模块
mvn -pl easy-factory-core compile
```

## 模块结构

```
easy-factory/
├── easy-factory-common/    ← 基础数据层：IData, IExpand, JSON序列化
├── easy-factory-core/      ← ★ 领域内核：所有接口、枚举、抽象类
├── easy-factory-test/      ← 测试存根
├── easy-factory-web/       ← Spring Boot Web 门户（存根）
└── docs/                   ← 协作文档
    ├── architecture/       ← 总体设计 + 模块设计 + 贯穿示例 + 数据模型
    ├── implementation/     ← 开发指南 + 编码规范
    ├── ai-collaboration/   ← AI 协作指南
    └── feedback/           ← 问题反馈模板
```

### 模块依赖

```
common ← core ← (mes, qms, plm, equip, lims, erp, iot) ← web
```

### 包命名

```
com.byz.factory.<module>.<layer>

module: common | core | mes | qms | plm | equip | lims | erp | iot | web
layer:  model | data | design | script | service | repository | web | config | exception
```

## 关键设计模式

### 1. 接口-实现分离

所有领域概念在 core 中以接口定义（`IProcess`, `IAction`, `IResource`），业务模块提供实现。接口之间形成清晰的继承层次：

```
IResource → IResourceModel → IMaterialsModel / IMachineModel / IPersonnelModel
IAction   → IActionModel
```

### 2. IExpand 扩展属性

`IExpand` 接口让领域对象携带动态属性。不同子系统挂不同字段，无需修改 core：

```java
// key 命名: <module>.<property>
process.set("mes.workOrderId", "WO-001");
process.set("qms.inspectionLevel", "NORMAL");
process.set("plm.version", "2.1.0");
```

### 3. 脚本化动作

每个 `IActionModel` 可携带 JS 脚本，由 `IScriptEngine` 在沙箱中执行。脚本必须含元数据头：

```javascript
/**
 * @id          qms.quality-gate.v1
 * @name        质量门禁判定
 * @version     1.0.0
 * @module      qms
 */
function execute(context) {
    // context.process / context.inputResources / context.services
}
```

脚本存放：`easy-factory-core/src/main/resources/scripts/<module>/`，索引：`registry.json`。

### 4. 资源 4M1E + Product 分类

`Dict.SourceGroup`: `Personnel | Machine | Material | Method | Environment | Product`

### 5. 动作语义

`Dict.Execute`: `Nothing | Create | Add | Use | Change | Convert | Split | Combine | Transfer | Hold`

### 6. 质量内嵌

质量不是独立系统——检验动作长在工序里，通过 `Control.Interrupt` 中断异常流程。

### 7. 生命周期状态机

`ILifecycle<Status>` 接口为所有带状态的实体提供标准化状态转换校验：
```java
public enum WorkOrderStatus implements ILifecycle.StatusEnum {
    CREATED, RELEASED, IN_PROGRESS, COMPLETED, CLOSED;
    public Set<WorkOrderStatus> allowedTransitions() { ... }
}
```

### 8. 领域事件

`IDomainEvent` + `DomainEventPublisher` 提供模块间异步解耦通信机制。
典型事件：`ProcessStarted`, `ActionExecuted`, `ProcessInterrupted`, `DeviationResolved`, `BatchCompleted`。

### 9. 跨模块抽象

- `IWorkOrder` — MES 工单抽象（工单号/产品/蓝图/批次/状态）
- `IInspectionOrder` — QMS 检验指令抽象（检验类型/方案/进度）
- `IBatch` — 批次追溯核心（批号/收率/状态/追溯记录）
- `ITraceable` — 追溯接口（谁/何时/在哪批/做了什么）

## 编码约定

### 命名

- 接口：`I` 前缀（`IProcess`, `IActionModel`）
- 实现类：接口名去 I（`Process`, `Action`）
- 枚举：名词（`Dict.SourceGroup`, `BatchStatus`）
- 异常：`Exception` 后缀（`ActionException`, `ResourceException`）
- 抽象基类：`Data`, `DataExpand`

### 方法

- 查询：`get/find/query/is/has`
- 命令：`create/add/update/delete/remove/execute`
- 转换：`to/from/convert/map`

### BigDecimal

```java
// ✅ compareTo 比较
if (number.compareTo(BigDecimal.ZERO) > 0) { }

// ❌ 不要 equals（会考虑 scale）
new BigDecimal("0.00").equals(BigDecimal.ZERO)  // false!

// ✅ new BigDecimal(String) 或 BigDecimal.valueOf()
new BigDecimal("10.50")

// ❌ new BigDecimal(double)
new BigDecimal(10.50)  // 精度丢失
```

### 空值处理

- 可能为空的返回值用 `Optional`
- 空对象用哨兵（`EmptyAction.Instance`, `EmptyResourcePack.Instance`）
- 集合返回空集合而非 null

### 测试：Given-When-Then

```java
@Test
void methodName_condition_expectedResult() {
    // Given
    // When
    // Then
}
```

## 文档索引

处理任何任务前，先阅读相关文档：

| 任务类型 | 必读文档 |
|---------|---------|
| 了解全局 | `docs/architecture/overview.md` |
| 开发新模块 | `docs/architecture/modules/<module>.md` |
| 理解全流程 | `docs/architecture/walkthrough.md` |
| 数据库设计 | `docs/architecture/data-model.md` |
| 编码风格 | `docs/implementation/conventions.md` |
| 环境/构建 | `docs/implementation/guide.md` |
| 与 AI 协作 | `docs/ai-collaboration/guide.md` |
| **追踪进度** | **[PROJECT_STATUS.md](PROJECT_STATUS.md)** — 各模块工作状态 |

## Git 工作流

```
main                  ← 稳定版本
├── develop           ← 开发主分支
│   ├── feature/xxx   ← 功能分支
│   ├── fix/xxx       ← 修复分支
│   └── docs/xxx      ← 文档分支
└── release/x.x.x     ← 发布分支
```

### 提交信息格式

```
<type>(<scope>): <subject>

type:  feat | fix | docs | style | refactor | test | chore
scope: common | core | mes | qms | plm | equip | lims | erp | iot | web

示例:
feat(core): 添加 IBatch 批次接口
fix(core): 修复 IAction 未定义引用
docs(walkthrough): 添加阿莫西林贯穿示例
```

## 常见任务指南

### 创建新业务模块

1. 复制 `docs/architecture/modules/mes.md` 作为模板编写模块设计
2. 创建模块 POM，依赖 `easy-factory-core`
3. 在父 POM `<modules>` 中注册
4. 实现 core 接口（model 层），使用 IExpand 挂载扩展属性
5. 实现 Service 和 Repository
6. 编写单元测试

### 修复 core 层

1. 改接口前，搜索所有引用处（`extends/implements`）
2. 优先添加 default 方法而非破坏性修改
3. 同步更新 `docs/architecture/modules/core.md` 中的"已知问题"列表
4. 同步更新 `PROJECT_STATUS.md` 中的 easy-factory-core 工作状态

### 编写动作脚本

1. 在 `easy-factory-core/src/main/resources/scripts/<module>/` 创建 `.js` 文件
2. 脚本头写完整的 `@id @name @version @module @description` 元数据
3. 入口函数签名: `function execute(context) { ... }`
4. 只能通过 `context.services` 访问预授权 API
5. 在 `registry.json` 中注册
6. 沙箱内测试执行验证

### 添加新文档

1. 在 `docs/README.md` 添加导航链接
2. 更新 `docs/architecture/overview.md` 中相关章节
3. 同步到 CLAUDE.md 的"文档索引"
