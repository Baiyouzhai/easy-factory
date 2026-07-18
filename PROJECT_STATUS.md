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
| `plm` | 约定已建立 | ProcessTemplate, Blueprint, ProcessParameter | BlueprintService |
| `equip` | 约定已建立 | Equipment(IMachine + ILifecycle&lt;MachineStatus&gt;) | EquipmentService |
| `lims` | 骨架 | Formula | FormulaService |
| `erp` | 骨架 | MaterialCache | ErpAdapterService |
| `iot` | 骨架 | DeviceConnection | IotGatewayService |
| `eam` | 约定已建立 | Asset, MaintenanceOrder, CalibrationRecord | EamService |
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

---

## EAM 模块约定（2026-07-18 建立）

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `batch/IAsset.java` | 资产抽象，供 Equip/Andon/ERP/DMS 引用 |
| 接口 | `batch/IMaintenanceOrder.java` | 维护工单抽象，供 Andon 引用 |
| 接口 | `batch/ICalibrationRecord.java` | 校准记录抽象，供 DMS 引用 |
| 状态枚举 | `batch/MaintenanceOrderStatus.java` | OPEN→IN_PROGRESS→COMPLETED→VERIFIED; +CANCELLED |
| 值枚举 | `batch/MaintenanceType.java` | PREVENTIVE / CORRECTIVE / PREDICTIVE / CALIBRATION |
| 值枚举 | `batch/MaintenancePriority.java` | LOW / MEDIUM / HIGH / EMERGENCY |
| 值枚举 | `batch/CalibrationResult.java` | PASS / FAIL / ADJUSTED |
| 值枚举 | `batch/CalibrationType.java` | INTERNAL / EXTERNAL |

### 约定规则

1. **跨模块接口** — 被其他模块引用的 EAM 实体在 core 定义接口，放在 `com.byz.factory.batch`
2. **状态枚举** — 所有枚举放 core；状态机枚举实现 `ILifecycle.StatusEnum`；纯值枚举为 plain enum
3. **模型继承** — 有状态实体继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录继承 `BaseEntity` + 实现 core 接口
4. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述

---

## Equip 模块约定（2026-07-18 建立）

### Core 层依赖（供 Equip 引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `batch/MachineStatus.java` | IDLE→RUNNING\|SETUP\|MAINTENANCE / RUNNING→IDLE\|FAULT / FAULT→MAINTENANCE→IDLE |
| 接口 | `resource/IMachine.java` | 设备资源抽象，group 固定为 Machine |
| 接口 | `process/action/IEquipmentAction.java` | 设备动作桥接，参数下发 + 状态检查 + OEE 记录 |

### 约定规则

1. **状态机** — 使用 core 的 `MachineStatus`（实现 `ILifecycle.StatusEnum`），不再定义内部 Status 枚举；Equipment 实现 `ILifecycle<MachineStatus>` 获得 `transition()` 校验
2. **模型继承** — Equipment 继承 `AbstractResourceItem`（保留资源数量操作 + 4M1E 分类）+ 实现 `IMachine` + `ILifecycle<MachineStatus>`；与 EAM 的 `BaseLifecycleEntity` 模式有所不同，因为设备同时是资源
3. **IExpand 约定** — `equip.status` / `equip.oee` / `equip.lastMaintenanceDate` / `equip.nextMaintenanceDate` 等动态属性挂载在资源扩展属性中
4. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖正常转换路径、故障恢复路径、换型路径、非法转换校验
5. **待实现模型** — EquipmentParameter（工艺参数）、EquipmentRecipe（设备配方）、OEMetrics（OEE 指标）后续按架构文档迭代

---
## PLM 模块约定（2026-07-18 建立）

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `factory/BlueprintStatus.java` | DRAFT→UNDER_REVIEW→APPROVED→RELEASED→OBSOLETED，含 DRAFT 回退和 OBSOLETED 废弃路径 |
| 接口 | `factory/IBlueprintDiffer.java` | 蓝图版本差异比较器，供 DMS/APS 引用 |
| 记录类 | `factory/BlueprintDiff.java` | 差异报告：DiffType(ADDED/REMOVED/MODIFIED/REORDERED) × DiffDimension(PROCESS/PARAMETER/RESOURCE) |

### PLM 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/ProcessTemplate.java` | `BaseEntity` | `HasVersion` | 工艺模板，status(DRAFT/ACTIVE/OBSOLETED)，含参数集合 |
| `model/Blueprint.java` | `BaseLifecycleEntity<BlueprintStatus>` | `IBlueprint` | 蓝图核心实体，完整状态机 |
| `model/ProcessParameter.java` | `BaseEntity` | `IProcessParameter` | 工艺参数：NUMERIC/BOOLEAN/ENUM/TEXT + MANUAL/AUTO/SEMI_AUTO + CRITICAL/IMPORTANT/NORMAL |

### 约定规则

1. **跨模块接口** — PLM 实体通过 core `factory/` 包中的接口暴露（`IBlueprint` 已存在，`IBlueprintDiffer` 本次新增）；其他模块通过接口引用 Blueprint，无需直接依赖 PLM
2. **状态枚举** — `BlueprintStatus` 放 core `factory/` 包，实现 `ILifecycle.StatusEnum`；ProcessTemplate 用简单 String status（DRAFT/ACTIVE/OBSOLETED），无需状态机
3. **模型继承** — 有状态实体（Blueprint）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录（ProcessParameter）和模板（ProcessTemplate）继承 `BaseEntity`
4. **IExpand 约定** — `plm.version` / `plm.status` / `plm.author` / `plm.approver` / `plm.releaseDate` / `plm.effectiveDate` / `plm.changeReason` 挂载在 Blueprint 扩展属性中
5. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖正常转换路径、驳回路径、非法转换校验、终端状态、canTransition 检查
6. **三阶段管道** — `CustomerSpec → EngineeringBOM → Blueprint(RELEASED)` — 前两步在 PLM 内完成，第三步交付 MES
7. **待实现模型** — BOMConverter（EBOM→PBOM→MBOM）、BlueprintDiffer 实现、工艺参数标准库后续按架构文档迭代

---

## 更新记录

| 日期 | 内容 |
|------|------|
| 2026-07-18 | PLM 模块初步约定建立：BlueprintStatus 状态枚举 + IBlueprintDiffer + BlueprintDiff + 3 个模型 + BlueprintService + 15 个测试 |
| 2026-07-18 | Equip 模块初步约定建立：升级 MachineStatus 状态机 + 10 个测试 + 扩展字段 |
| 2026-07-18 | EAM 模块初步约定建立：3 个 core 接口 + 5 个枚举 + 3 个模型 + 服务接口 + 测试 |
| 2026-07-12 | 全部 15 个业务模块骨架创建 + 编译测试全通过（20模块总计） |
| 2026-07-12 | 移除 easy-factory-db（持久化回归各模块） |
| 2026-07-12 | 8个业务模块骨架创建，编译通过 |
| 2026-07-12 | 初始创建，core 接口体系完成 |
