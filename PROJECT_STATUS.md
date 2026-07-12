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
- [x] `Process.execute()` 动作链正确串联
- [x] `Process.setResourcePack/setRequireResources` 正常存储
- [x] `IResource` 继承 `IData` 解决 `toJsonString()` 编译错误
- [x] `Constant.java` 构造器参数修复
- [x] `BatchStatus` 补充 `RELEASED` (GMP放行节点)
- [x] `IAction` 增加 `getImportance()` 消除孤儿方法

### 待进行

- [ ] GraalJS 沙箱实际集成
- [ ] ScriptExecutor (Nashorn) 与 IScriptEngine (GraalJS) 双轨统一
- [ ] 空壳类补充
- [ ] 单元测试覆盖

---

## easy-factory-common（已完成）

- [x] `IData` + `IExpand` + `IDataExpand` 接口体系
- [x] `Data` / `DataExpand` 抽象基类
- [x] `CollectionUtil` 工具类

---

## 业务模块状态（全部骨架编译通过）

| 模块 | 状态 | 核心模型 | 核心Service接口 |
|------|------|---------|---------------|
| `mes` | 骨架 | MesWorkOrder | WorkOrderService |
| `qms` | 骨架 | InspectionOrder | InspectionService |
| `plm` | 骨架 | ProcessTemplate | BlueprintService |
| `equip` | 骨架 | Equipment(IMachineModel) | EquipmentService |
| `lims` | 骨架 | Formula | FormulaService |
| `erp` | 骨架 | MaterialCache | ErpAdapterService |
| `iot` | 骨架 | DeviceConnection | IotGatewayService |
| `eam` | 骨架 | Asset | EamService |
| `mps` | 骨架 | ProductionPlan | MpsService |
| `aps` | 骨架 | Schedule | ApsService |
| `wms` | 骨架 | Storage, Receipt | WmsService |
| `andon` | 骨架 | AndonCall | AndonService |
| `bi` | 骨架 | KpiSnapshot | DashboardService |
| `scm` | 骨架 | Supplier | ScmService |
| `dms` | 骨架 | Document | DmsService |

---

## 模块依赖全景

```
                      ┌─────────────────────┐
                      │  easy-factory-web    │  统一门户
                      └──────────┬──────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
    ┌─────▼─────┐         ┌─────▼─────┐          ┌─────▼─────┐
    │   MES     │────────▶│   QMS     │          │   Andon   │
    └─────┬─────┘         └─────┬─────┘          └─────┬─────┘
          │                      │                      │
    ┌─────▼─────┐         ┌─────▼─────┐          ┌─────▼─────┐
    │   WMS     │         │   LIMS    │          │   EAM     │
    └─────┬─────┘         └─────┬─────┘          └─────┬─────┘
          │                      │                      │
    ┌─────▼─────┐                │                ┌─────▼─────┐
    │   SCM     │                │                │   DMS     │
    └───────────┘                │                └───────────┘
                                 │
               ┌─────────────────┼─────────────────┐
               │                 │                 │
         ┌─────▼─────┐    ┌─────▼─────┐     ┌─────▼─────┐
         │   Equip   │    │   IoT     │     │   ERP     │
         └───────────┘    └───────────┘     └───────────┘
               │
         ┌─────▼─────┐    ┌─────▼─────┐
         │   MPS     │───▶│   APS     │
         └───────────┘    └───────────┘
               │
         ┌─────▼─────┐
         │   BI      │  只读消费所有模块数据
         └───────────┘
                                 │
                      ┌──────────▼──────────┐
                      │  easy-factory-core  │
                      │     领域内核         │
                      └─────────────────────┘
```

---

## 实施顺序（详见 docs/implementation/roadmap.md）

| 阶段 | 模块 | 并行度 | 关键依赖 |
|------|------|--------|---------|
| Phase 1 | erp, iot, plm, equip, scm, dms | 6并行 | 无（仅依赖 core） |
| Phase 2 | lims, wms, mps | 3并行 | erp/plm/scm |
| Phase 3 | **mes**, aps | 串行 | plm+equip+wms+lims |
| Phase 4 | qms, andon, eam | 2并行 | mes(+iot) |
| Phase 5 | bi, web, test | 3并行 | 全部 |

> **mes 是集成枢纽，预计占总工作量 40%+**

---

## 更新记录

| 日期 | 内容 |
|------|------|
| 2026-07-12 | 初始创建，core 接口体系完成 |
| 2026-07-12 | 8个业务模块骨架创建，编译通过 |
| 2026-07-12 | 移除 easy-factory-db（持久化回归各模块） |
| 2026-07-12 | 全部 15 个业务模块骨架创建 + 编译测试全通过（20模块总计） |
