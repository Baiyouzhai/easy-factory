# 项目工作状态

> 独立于 CLAUDE.md，专供各模块会话追踪进度。
> CLAUDE.md 保持稳定，本文件频繁更新。

## easy-factory-core

### 已完成 (v0.1.1)

- [x] Core 接口体系：`IResource(extends IData)` → `IResourceModel`, `IAction(getImportance)` → `IActionModel`
- [x] 4M1E+Product 资源分类 (`Dict.SourceGroup`)
- [x] 10 种动作语义枚举 + `Action.execute()` 按 executeType 路由资源处理
- [x] `IScriptEngine` 抽象 + `GraalScriptEngine` 沙箱骨架
- [x] `IBatch` / `ITraceable` / `BatchStatus`(含RELEASED) 批次追溯
- [x] `ILifecycle<S>` 通用状态机 + `IDomainEvent` 领域事件
- [x] `IWorkOrder` / `IInspectionOrder` 跨模块抽象
- [x] `ScriptMetadata` + `ScriptRegistry` + 4 个示例脚本 + `registry.json`
- [x] `ActionGroup` 正确继承 `model.Action` 并实现复合执行
- [x] `Process.execute()` 动作链正确串联（前一个动作输出 → 下一个动作输入）
- [x] `Process.setResourcePack/setRequireResources` 正常存储
- [x] `IResource` 继承 `IData` 解决 `toJsonString()` 编译错误
- [x] `Constant.java` 构造器参数修复
- [x] `ActionGroup` 继承链修正（`data.Action` → `model.Action`）
- [x] `BatchStatus` 补充 `RELEASED` (GMP放行节点)
- [x] `IAction` 增加 `getImportance()` 消除 `script.Action` 孤儿方法

### 待进行

- [ ] GraalJS 沙箱实际集成（POM 依赖已预留注释，`GraalScriptEngine` 骨架就绪）
- [ ] ScriptExecutor (Nashorn) 与 IScriptEngine (GraalJS) 双轨统一
- [ ] 空壳类补充：
  - `BillOfMaterial.java` — 作为 `IResourcePack` 子接口
  - `ProcessRoute.java` / `IProcessRoute.java` — 工序列表包装
  - `ProductFactoryResult.java` / `ProductProcessResult.java` — 检查结果字段
  - `IProductChecker.check()` — 返回 null，待实现
- [ ] 单元测试覆盖

### 已知技术债务

- 三个同名 `Action` 类 (`data.Action`, `model.Action`, `script.Action`) — 建议 `script.Action` 重命名为 `ScriptAction`
- `model.Action extends data.Action` 继承了 `executeType` 字段，但 model 也声明了同名字段（Lombok @Data 叠加手动字段）
- `data.Action` 作为父类其 `@Data` 会自动生成 getter/setter，但 `model.Action` 手动声明了 `code/name` 导致字段隐藏

---

## easy-factory-common

### 已完成
- [x] `IData` + `IExpand` + `IDataExpand` 接口体系
- [x] `Data` / `DataExpand` 抽象基类
- [x] `CollectionUtil` 工具类

### 待进行
- 无明确计划

---

## 待建模块

| 模块 | 状态 | 优先级 | 说明 |
|------|------|--------|------|
| `easy-factory-db` | 未开始 | P1 | 持久化模块（JPA/MyBatis-Plus） |
| `easy-factory-mes` | 未开始 | P1 | 工单/工序流转/报工/追溯 |
| `easy-factory-qms` | 未开始 | P2 | 检验/偏差/CAPA/SPC |
| `easy-factory-web` | 存根 | P2 | Spring Boot 聚合门户 |
| `easy-factory-plm` | 未开始 | P3 | 工艺路线/BOM转化/版本管理 |
| `easy-factory-equip` | 未开始 | P3 | 设备台账/配方/OEE |
| `easy-factory-lims` | 未开始 | P3 | 配方版本/称量/批记录 |
| `easy-factory-iot` | 未开始 | P3 | 协议适配/数据采集/指令下发 |
| `easy-factory-erp` | 未开始 | P3 | 主数据同步/库存/事务回传 |

---

## 更新记录

| 日期 | 内容 |
|------|------|
| 2026-07-12 | 初始创建，从 CLAUDE.md 迁移工作状态 |
