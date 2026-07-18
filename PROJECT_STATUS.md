# 项目工作状态

> 独立于 CLAUDE.md，专供各模块会话追踪进度。
> CLAUDE.md 保持稳定，本文件频繁更新。

## easy-factory-core

### 已完成 (v0.1.2)

- [x] Core 接口体系：`IResource(extends IData)` → `IResourceModel`, `IAction(getImportance)` → `IActionModel`
- [x] **Equip 模块接口回归**：`IEquipment`(extends IMachine+ILifecycle), `IEquipmentParameter`(含 isInControl default), `IEquipmentRecipe` + `IRecipePhase`, `IOEMetrics`(A×P×Q)
- [x] **EquipEventTypes** 迁入 core `com.byz.factory.factory`（与 PlmEventTypes 同级）
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
| `equip` | 约定完成 | Equipment + EquipmentParameter + EquipmentRecipe + OEMetrics + EquipEventTypes | EquipmentService (16方法) |
| `lims` | 骨架 | Formula | FormulaService |
| `erp` | 约定已建立 | MaterialCache, InventorySnapshot, Transaction(TransactionStatus) | ErpAdapterService |
| `iot` | 约定完成 | DeviceConnection + TagValue + Command(CommandStatus) + AlarmEvent + IotEventTypes | IotGatewayService (13方法) |
| `eam` | 约定已建立 | Asset, MaintenanceOrder, CalibrationRecord | EamService |
| `mps` | 骨架 | ProductionPlan | MpsService |
| `aps` | 骨架 | Schedule | ApsService |
| `wms` | 骨架 | Storage, Receipt | WmsService |
| `andon` | 骨架 | AndonCall | AndonService |
| `bi` | 骨架 | KpiSnapshot | DashboardService |
| `scm` | 约定已建立 | Supplier, PurchaseOrder(PurchaseOrderStatus) | ScmService |
| `dms` | 约定已建立 | Document + ApprovalWorkflow + ApprovalStep + DmsEventTypes | DmsService (19方法) |

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

## Equip 模块约定（2026-07-18 建立，2026-07-19 补充，2026-07-19 审核通过）

### 设计定位

Equip 管理生产设备的工艺参数、设备配方、运行状态和 OEE 效率。设备是 core 中 `Resource (group=Machine)` 的具体化和管理系统，向上供应 MES 设备状态、IoT 参数下发、BI 效率看板所需数据。

### Core 层依赖（供 Equip 引用，2026-07-19 回归）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `batch/MachineStatus.java` | IDLE→RUNNING\|SETUP\|MAINTENANCE / RUNNING→IDLE\|FAULT / FAULT→MAINTENANCE→IDLE |
| 接口 | `batch/IEquipment.java` | ⭐ 设备台账抽象 = IMachine + ILifecycle<MachineStatus> + 台账字段；供 EAM/MES/APS/Andon 引用 |
| 接口 | `batch/IEquipmentParameter.java` | ⭐ 工艺参数抽象：设定值/实际值/控制限 + isInControl() default；供 IEquipmentBinding/IEquipmentAction/EngineeringBOM 迁移 |
| 接口 | `batch/IEquipmentRecipe.java` | ⭐ 设备配方抽象 + IRecipePhase 内嵌接口；供 EngineeringBOM/PLM/IoT 引用 |
| 接口 | `batch/IOEMetrics.java` | ⭐ OEE 指标抽象：A×P×Q；供 BI/APS/FactoryCapacityProfile 引用 |
| 事件常量 | `event/types/EquipEventTypes.java` | ⭐ 9 个领域事件常量（与 PlmEventTypes 同包，统一在 `com.byz.factory.event.types`） |
| 接口 | `process/action/IEquipmentAction.java` | 设备动作桥接，参数下发 + 状态检查 + OEE 记录 |

### Equip 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Equipment.java` | `AbstractResourceItem` | `IEquipment`（⭐ = IMachine + ILifecycle<MachineStatus> + 台账字段） | 设备台账，含 7 个业务便捷方法 + IExpand 文档 |
| `model/EquipmentParameter.java` | `BaseEntity` | `IEquipmentParameter` | 工艺参数：设定值 + 实际值 + 控制限；`isInControl()` 由接口 default 提供 |
| `model/EquipmentRecipe.java` | `BaseEntity` | `IEquipmentRecipe` | 设备配方：阶段化参数集(RecipePhase implements IRecipePhase) + 版本管理 |
| `model/OEMetrics.java` | `BaseEntity` | `IOEMetrics` | OEE 指标：A×P×Q 计算；工厂方法 `of()` + `toPercentString()` |
| `event/EquipEventTypes.java` | — | — | **已删除**（迁移至 core `com.byz.factory.event.types.EquipEventTypes`） |
| `service/EquipmentService.java` | — | — | 16 方法：台账 + 状态 + 配方 + 参数 + OEE |

### 约定规则

1. **状态机** — 使用 core 的 `MachineStatus`（实现 `ILifecycle.StatusEnum`），不再定义内部 Status 枚举；Equipment 实现 `ILifecycle<MachineStatus>` 获得 `transition()` 校验
2. **模型继承** — Equipment 继承 `AbstractResourceItem`（保留资源数量操作 + 4M1E 分类）+ 实现 `IMachine` + `ILifecycle<MachineStatus>`；与 EAM 的 `BaseLifecycleEntity` 模式有所不同，因为设备同时是资源。其余模型（EquipmentParameter, EquipmentRecipe, OEMetrics）均继承 `BaseEntity`
3. **IExpand 约定** — `equip.status` / `equip.oee` / `equip.lastMaintenanceDate` / `equip.nextMaintenanceDate` / `equip.parameters` 等动态属性挂载在资源扩展属性中
4. **业务便捷方法** — Equipment 对外的语义化方法封装 `transition()` + `markUpdated()`，调用方不直接操作 `setStatus()`；方法：`startProduction()` / `stopProduction()` / `startSetup()` / `completeSetup()` / `reportFault()` / `startMaintenance()` / `completeMaintenance()`
5. **领域事件** — 事件常量在 `EquipEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定；Equipment 业务方法内部可发布对应事件（通过 `DomainEventPublisher`）
6. **配方版本** — EquipmentRecipe 通过 `version` 字符串管理版本（默认 "1.0.0"），`bumpVersion()` 执行 MINOR 递增（1.0.0 → 1.1.0）；RecipePhase 为静态内部类，三参数构造默认 rampRate=0
7. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换（正常+故障恢复+换型）、非法转换、业务便捷方法（正常+非法）、参数控制限、配方阶段、OEE 计算、事件命名约定
8. **待实现** — EquipmentService 实现类、IEquipmentAction 桥接、IoT 参数实时监控、OEE 六大损失采集、REST 控制器

### 高层级审核结论（2026-07-19）

9. **IEquipmentBinding 迁移** — ✅ B 方案（渐进迁移），String equipmentCode 保持为松耦合胶水；`EquipmentFound` 可新增可选 `IEquipment` 字段
10. **EngineeringBOM.EquipmentAssignment** — ✅ A+B 方案，新增可选的 `recipeCode`+`phaseName`，不为空时从 Equip 读参数，为空时走副本 fallback
11. **FactoryCapacityProfile** — ✅ B 方案（双轨并存），保持静态 OEE 因子为规划默认值，新增 `IOEMetrics` 重载做执行后修正
12. **EventTypes 统一位置** — ✅ 迁移到 `com.byz.factory.event.types` 包（与 event 族同居）

---
## PLM 模块约定（2026-07-18 建立，2026-07-19 两次补充）

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `factory/BlueprintStatus.java` | DRAFT→UNDER_REVIEW→APPROVED→RELEASED→OBSOLETED |
| 接口 | `factory/IBlueprintDiffer.java` | 蓝图版本差异比较器，供 DMS/APS 引用 |
| 记录类 | `factory/BlueprintDiff.java` | 差异报告：DiffType × DiffDimension |
| 事件常量 | `event/types/PlmEventTypes.java` | 8 个 PLM 领域事件类型常量，供 MES/DMS/APS/ERP/WMS 订阅 |
| 版本策略 | `shared/IVersionStrategy.java` | 版本递增策略接口（⭐ PLM/DMS/LIMS 共用） |
| 版本值对象 | `shared/SemanticVersion.java` | MAJOR.MINOR.PATCH 语义版本 |
| 版本枚举 | `shared/BumpType.java` | MAJOR / MINOR / PATCH |
| 策略实现 | `shared/SemanticVersionStrategy.java` | 语义版本递增 |
| 策略实现 | `shared/IncrementalVersionStrategy.java` | 简单整数递增（DMS 适用） |

### PLM 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/ProcessTemplate.java` | `BaseEntity` | `HasVersion` | 工艺模板，含 activate()/obsolete() |
| `model/Blueprint.java` | `BaseLifecycleEntity<BlueprintStatus>` | `IBlueprint` | 蓝图核心，含 5 业务方法 + 版本策略 + 领域事件发布 |
| `model/ProcessParameter.java` | `BaseEntity` | `IProcessParameter` | 工艺参数：4 种数据类型 × 3 种控制方式 × 3 级重要性 |
| `model/ChangeRequest.java` | `BaseLifecycleEntity<ChangeRequestStatus>` | — | ECR/ECO 变更请求，6 状态 + 6 业务方法 |
| `bom/BOMType.java` | — | — | EBOM / PBOM / MBOM 枚举 + 转化链 |
| `bom/BOMConversionRequest.java` | — | — | BOM 转化请求 record（源→目标+蓝图+工厂） |
| `bom/BOMConversionResult.java` | — | — | 转化结果（产物BOM+规则+告警） |
| `bom/IBOMConversionRule.java` | — | — | 转化规则契约（⭐ Common 实现） |
| `service/BlueprintDifferImpl.java` | — | `IBlueprintDiffer` | 差异比较器，工序/参数/资源三维度 |
| `service/BlueprintService.java` | — | — | 30+ 方法：生命周期 + 参数 + 版本 + BOM + 可制造性 + 变更管理 |

### 约定规则

1. **跨模块接口** — PLM 实体通过 core `factory/` 包接口暴露；其他模块通过接口引用 Blueprint，无需直接依赖 PLM
2. **状态枚举** — `BlueprintStatus` 放 core；`ChangeRequestStatus` 在 PLM 内（DMS 通过事件感知状态变更）
3. **模型继承** — 有状态实体继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录和模板继承 `BaseEntity`
4. **IExpand 约定** — Blueprint 的 `plm.*`、ProcessTemplate 的 `plm.template.*`、ChangeRequest 的 `plm.cr.*` 挂载在扩展属性中
5. **业务便捷方法** — 模型对外的语义化方法封装状态转换，内部调用 transition() + markUpdated()；调用方不直接操作 setStatus()
6. **领域事件驱动** — 状态变更时自动通过 `DomainEventPublisher` 发布事件；MES/DMS/APS/ERP/WMS 订阅相关事件类型（常量在 `PlmEventTypes`）
7. **版本管理** — 蓝图默认使用 `SemanticVersionStrategy(BumpType.MINOR)`，可注入自定义 `IVersionStrategy`；DMS 文档可用 `IncrementalVersionStrategy`
8. **BOM 转化** — 契约模型（`BOMConversionRequest/Result` + `IBOMConversionRule`）在 PLM；规则引擎（`IRuleEngine`）委托 Common 模块实现
9. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述
10. **三阶段管道** — `CustomerSpec → EngineeringBOM → Blueprint(RELEASED)` → MES
11. **待实现** — BlueprintService 实现类、BOM 规则引擎（Common）、工艺参数标准库

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(PlmEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `plm.blueprint.submitted` | **DMS** | 触发审批流文档 |
| `plm.blueprint.approved` | **DMS** | 审批完成归档 |
| `plm.blueprint.released` | **MES, APS, WMS** | MES 获取工艺路线 / APS 更新排程 / WMS 预置储位 |
| `plm.blueprint.obsoleted` | **MES, APS** | 标记蓝图不可用 |
| `plm.bom.transformed` | **ERP** | 同步物料需求到采购计划 |
| `plm.change.requested` | **DMS** | 触发变更审批流 |

---

## ERP 模块约定（2026-07-18 建立）

### 设计定位

ERP 模块是**外部系统的适配层**，不是领域模型层。其他模块通过 `IResourceItem`/`IMaterial` 使用物料数据，通过 `ErpAdapterService` 调用库存/回传操作。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `batch/IErpTransaction.java` | 事务回传记录抽象，供 MES/WMS/LIMS 创建事务时编译期引用 |
| 状态枚举 | `batch/TransactionStatus.java` | PENDING→SENT→CONFIRMED / SENT→FAILED→PENDING / PENDING→CANCELLED；实现 `ILifecycle.StatusEnum` |
| 值枚举 | `batch/TransactionType.java` | GOODS_ISSUE / GOODS_RECEIPT / TRANSFER |

### ERP 枚举（非跨模块，留在 erp）

| 文件 | 位置 | 说明 |
|------|------|------|
| `ErpMaterialType.java` | erp/model | ERP 物料类型：ROH / HALB / FERT（SAP 特有概念，映射到 core `Dict.SourceType`） |

### ERP 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/MaterialCache.java` | `BaseEntity` | — | 物料主数据本地缓存，提供 `toResourceItem()` 映射为 core `IResourceItem` + `mapUom()` 单位映射 |
| `model/InventorySnapshot.java` | `BaseEntity` | — | 库存快照，按物料+工厂+库位+批次记录库存状态 |
| `model/Transaction.java` | `BaseLifecycleEntity<TransactionStatus>` | `IErpTransaction` | 事务回传记录，带完整状态机 + 便捷方法（markSent/markConfirmed/markFailed/retry/cancel） |

### 约定规则

1. **适配器模式** — ERP 是外部系统适配层，不定义 core 接口；内部模型继承 `BaseEntity`/`BaseLifecycleEntity`；对外的契约通过 `ErpAdapterService` 接口暴露
2. **模型继承** — 有状态实体（Transaction）继承 `BaseLifecycleEntity<S>`，通过便捷方法封装状态转换；无状态记录（MaterialCache, InventorySnapshot）继承 `BaseEntity`
3. **IExpand 约定** — `erp.materialType` / `erp.batchManaged` / `erp.shelfLife` / `erp.ghsClass` / `erp.sourceSystem` / `erp.lastSyncTime` 挂载在 MaterialCache 上；ERP 单位编码 → core `UOM` 枚举映射通过 `mapUom()` 完成
4. **物料映射** — `MaterialCache.toResourceItem()` 将 ERP 物料映射为 core `IResourceItem`：code→name, group→Material, ErpMaterialType→SourceType
5. **事件驱动同步** — 库存变更通过 `DomainEventPublisher` 广播，ERP 适配器异步订阅回传；生产执行不因 ERP 回传失败而中断
6. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换（正常/失败/重试/取消/终态）、映射正确性、默认值
7. **待实现模型** — 同步策略（全量/增量）、事务队列持久化、具体 ERP 对接适配器（SAP/Oracle/用友/金蝶）后续按架构文档迭代

---

## SCM 模块约定（2026-07-19 建立）

### 设计定位

SCM 管理上游供应链——供应商主数据、采购订单、来料计划、供应商绩效。是 WMS 的上游驱动模块——采购订单驱动收货，来料计划驱动入库准备。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `batch/ISupplier.java` | 供应商抽象，供 WMS/QMS/ERP 引用 |
| 接口 | `batch/IPurchaseOrder.java` | 采购订单抽象（含 IPurchaseOrderItem 嵌套接口），供 WMS/ERP 引用 |
| 状态枚举 | `batch/PurchaseOrderStatus.java` | DRAFT→APPROVED→SENT→RECEIVING→COMPLETED；+CANCELLED；实现 `ILifecycle.StatusEnum` |
| 值枚举 | `batch/SupplierStatus.java` | ACTIVE / INACTIVE / BLACKLISTED |

### SCM 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Supplier.java` | `BaseEntity` | `ISupplier` | 供应商实体，双维度分离：qualification(资质,String) × status(运营,SupplierStatus)，各自独立变化 |
| `model/PurchaseOrder.java` | `BaseLifecycleEntity<PurchaseOrderStatus>` | `IPurchaseOrder` | 采购订单核心实体，完整状态机 + 明细管理 + 总金额计算 |
| `model/PurchaseOrderItem.java` | — | `IPurchaseOrder.IPurchaseOrderItem` | 采购明细行，含收货累加（receive/receivedQty/remaining/isFullyReceived） |

### 约定规则

1. **跨模块接口** — SCM 实体通过 core `batch/` 包中的接口暴露（`ISupplier`、`IPurchaseOrder`）；其他模块通过接口引用 Supplier/PurchaseOrder，无需直接依赖 SCM
2. **状态枚举** — `PurchaseOrderStatus` 放 core `batch/` 包，实现 `ILifecycle.StatusEnum`；`SupplierStatus` 为纯值枚举（状态变更由管理操作驱动，不走状态机）
3. **模型继承** — 有状态实体（PurchaseOrder）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录（Supplier）继承 `BaseEntity` + 实现 core 接口
4. **IExpand 约定** — `scm.category` / `scm.qualification` / `scm.leadTime` / `scm.onTimeRate` / `scm.qualityRate` / `scm.contacts` 挂载在 Supplier 上；`scm.poNo` / `scm.supplierCode` / `scm.approvedBy` / `scm.expectedDelivery` 挂载在 PurchaseOrder 上
5. **跨模块协作** — SCM→WMS（来料计划触发收货）、SCM←WMS（收货结果更新 PO 进度）、SCM→ERP（采购成本→应付）、SCM→QMS（供应商批次→来料检 IQC）
6. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换、退货、非法转换、终态、采购明细/收货逻辑
7. **Supplier 双维度分离** — `qualification`（资质，String）和 `status`（运营，SupplierStatus）是两个正交维度，各自独立变化。qualification 用 String 预留对接外部 OA/ERP 时扩展中间态（如 EXPIRING）；status 用枚举直接建模 ACTIVE/INACTIVE/BLACKLISTED 三类运营态。示例：已合格的供应商可因付款纠纷暂停合作（INACTIVE），恢复后可因质量事故拉黑（BLACKLISTED）——两个维度互不绑定
8. **待实现模型** — InboundPlan（来料计划）、NotificationService（仓库通知）、SupplierScoring（供应商评分）后续按架构文档迭代

---

## IoT 模块约定（2026-07-19 建立）

### 设计定位

IoT 是 **设备层的适配和采集中枢**——连接 PLC/SCADA/DCS 等工业控制系统，将设备数据转化为 core 领域模型的实时状态。IoT 向上为 Equip（参数实时值）、MES（设备状态）、QMS（SPC 数据源）、Andon（报警触发）提供数据支撑，向下通过协议适配器统一 OPC UA / Modbus / MQTT / Siemens S7 / HTTP 等工业协议。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `batch/IDeviceConnection.java` | ⭐ 设备连接抽象：设备编码 + 协议 + 端点 + 采集间隔 + 在线判定 default；供 Equip/MES/Andon 引用 |
| 接口 | `batch/ITagValue.java` | ⭐ 采集标签值抽象 + TagQuality 内嵌枚举(GOOD/BAD/UNCERTAIN)；供 Equip/QMS/MES 引用 |
| 接口 | `batch/ICommand.java` | ⭐ 设备指令抽象 + CommandType(SET_PARAM/START/STOP/READ/ACK_ALARM) + CommandPriority(HIGH/NORMAL/LOW) 内嵌枚举；供 Equip/MES/Andon 引用 |
| 接口 | `batch/IAlarmEvent.java` | ⭐ 报警事件抽象 + AlarmSeverity(INFO/WARNING/CRITICAL/EMERGENCY) 内嵌枚举；供 QMS/Andon/MES/EAM 引用 |
| 状态枚举 | `batch/CommandStatus.java` | QUEUED→SENT→ACKNOWLEDGED→COMPLETED；SENT/ACKNOWLEDGED→FAILED；QUEUED→CANCELLED；实现 ILifecycle.StatusEnum |
| 事件常量 | `event/types/IotEventTypes.java` | ⭐ 9 个领域事件常量（与 EquipEventTypes/PlmEventTypes 同包 `com.byz.factory.event.types`） |
| 动作接口 | `process/action/IDataCollectionAction.java` | ⭐ IoT 数据采集动作桥接（先前已存在；DataTag + AlarmRule + DataCollectionResult） |

### IoT 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/DeviceConnection.java` | `BaseEntity` | `IDeviceConnection` | 设备连接管理：心跳检测 + 超时判定 + authConfig + IExpand |
| `model/TagValue.java` | `BaseEntity` | `ITagValue` | 标签采集值：原始值→工程值换算(multiplier×offset) + 3种质量标准 + 3个工厂方法(of/bad/uncertain) |
| `model/Command.java` | `BaseLifecycleEntity<CommandStatus>` | `ICommand` | 设备指令：完整状态机 + 5个业务便捷方法(markSent/markAcknowledged/markCompleted/markFailed/cancel) |
| `model/AlarmEvent.java` | `BaseEntity` | `IAlarmEvent` | 报警事件：确认/恢复 + 联动动作(addAction) + 2个查询方法(isAcknowledged/isResolved) |

### 服务接口

| 文件 | 说明 |
|------|------|
| `service/IotGatewayService.java` | 13 方法：设备注册(register/unregister/getConnection/getOnlineDevices) + 数据采集(readTags/startPolling/stopPolling/isPolling) + 指令下发(sendCommand/getCommandStatus) + 报警管理(getActiveAlarms×2/acknowledgeAlarm) |
| `service/IProtocolAdapter.java` | 6 方法：connect/disconnect/isConnected/read/write/subscribe；目标实现 OPC UA(Eclipse Milo)/Modbus/MQTT/S7/HTTP |

### 约定规则

1. **跨模块接口** — IoT 实体通过 core `batch/` 包中的接口暴露（`IDeviceConnection`、`ITagValue`、`ICommand`、`IAlarmEvent`）；其他模块通过接口引用 IoT 数据，无需直接依赖 IoT 模块
2. **状态机** — `CommandStatus` 放 core `batch/` 包中，实现 `ILifecycle.StatusEnum`；Command 继承 `BaseLifecycleEntity<CommandStatus>` 获得 `transition()` 校验
3. **模型继承** — 有状态实体（Command）继承 `BaseLifecycleEntity<CommandStatus>` + 实现 core 接口；无状态记录（DeviceConnection, TagValue, AlarmEvent）继承 `BaseEntity` + 实现 core 接口
4. **IExpand 约定** — `iot.protocol` / `iot.endpoint` / `iot.connectionStatus` / `iot.lastHeartbeat` 挂载在 DeviceConnection 上；`iot.tag` / `iot.interval` 挂载在 Resource 上供 TagFilter 提取；`iot.alarm.*` 挂载在 AlarmEvent 上
5. **业务便捷方法** — Command 对外的语义化方法封装状态转换，调用方不直接操作 `setStatus()`；方法：`markSent()` / `markAcknowledged()` / `markCompleted()` / `markFailed()` / `cancel()`；AlarmEvent 提供 `acknowledge()` / `resolve()` / `addAction()`；DeviceConnection 提供 `heartbeat()` / `isTimedOut()`
6. **领域事件** — 事件常量在 `IotEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定；IoT 模型业务方法内部通过 `DomainEventPublisher` 发布对应事件
7. **协议适配器** — `IProtocolAdapter` 定义统一的协议抽象（connect/disconnect/isConnected/read/write/subscribe）；具体协议实现在 IoT 模块内部，不暴露到 core
8. **数据质量** — TagValue 采用 OPC UA 三态质量模型（GOOD/BAD/UNCERTAIN），3 个工厂方法覆盖常见场景（`of`=正常值 / `bad`=传感器故障 / `uncertain`=超量程）
9. **报警联动** — CRITICAL/EMERGENCY 级别报警触发后，通过 `DomainEventPublisher` 通知 Andon（创建呼叫）、MES（暂停工序）、QMS（发起偏差）、EAM（紧急维护工单）
10. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换（正常+非法+终态）、业务便捷方法、事件命名约定、接口契约
11. **待实现** — IotGatewayService 实现类、5 种协议适配器实现（OPC UA/Modbus/MQTT/S7/HTTP）、IDataCollectionAction 桥接实现、时序数据库集成（InfluxDB/TimescaleDB）、REST 控制器、报警联动引擎

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(IotEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `iot.device.connected` | **Equip** | 同步设备在线状态 |
| `iot.device.disconnected` | **Equip, MES** | Equip 标记离线 / MES 暂停关联工单 |
| `iot.tag.collected` | **Equip, QMS** | Equip 更新参数实际值 / QMS SPC 分析 |
| `iot.command.sent` | **Equip** | 确认指令已下发 |
| `iot.command.completed` | **Equip, MES** | 确认指令执行完成 |
| `iot.command.failed` | **Equip, MES** | 指令失败处理 |
| `iot.alarm.triggered` | **QMS, Andon, MES, EAM** | QMS 发起偏差 / Andon 触发呼叫 / MES 暂停工序 / EAM 生成维护工单 |
| `iot.alarm.acknowledged` | **Andon** | 取消安灯呼叫 |
| `iot.alarm.resolved` | **QMS, MES, EAM** | QMS 关闭偏差 / MES 恢复工序 / EAM 关闭维护工单 |

---

## DMS 模块约定（2026-07-19 建立）

### 设计定位

DMS 管理制造运营中的 GMP 受控文档——SOP、批记录、检验报告、偏差报告、CAPA 报告、验证文件、校准证书。DMS 是 GMP 合规的底层基础设施：所有 GxP 文件需要版本控制、审批流和审计追踪。向上为 LIMS/QMS/PLM/EAM 提供文档审批和归档能力。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `batch/DocumentStatus.java` | DRAFT→UNDER_REVIEW→APPROVED→EFFECTIVE→OBSOLETED；+REJECTED；实现 `ILifecycle.StatusEnum` |
| 值枚举 | `batch/DocumentCategory.java` | SOP / BATCH_RECORD / INSPECTION / DEVIATION / CAPA / VALIDATION / CALIBRATION |
| 接口 | `batch/IDocument.java` | ⭐ 文档抽象：编号+标题+类别+版本+状态+生效日期+复审周期；供 LIMS/QMS/PLM/EAM 编译期引用 |
| 事件常量 | `event/types/DmsEventTypes.java` | ⭐ 8 个领域事件常量（与 PlmEventTypes/EquipEventTypes/IotEventTypes 同包 `com.byz.factory.event.types`） |

### DMS 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Document.java` | `BaseLifecycleEntity<DocumentStatus>` | `IDocument` | 文档核心实体：完整状态机 + 8 个业务便捷方法 + 版本管理(IncrementalVersionStrategy) + 领域事件发布 + IExpand 文档 |
| `model/ApprovalWorkflow.java` | `BaseEntity` | — | 审批工作流：多步骤审批流程 + 进度追踪 + 完成判定(isApproved/isRejected) |
| `model/ApprovalStep.java` | — | — | 审批步骤：角色→人→决定(APPROVED/REJECTED/NEEDS_REVISION/PENDING) + 意见 + 时间戳 |

### 服务接口

| 文件 | 说明 |
|------|------|
| `service/DmsService.java` | 19 方法：文档 CRUD(createDraft/getDocument/listDocuments/searchDocuments/updateContent) + 审批生命周期(submitForReview/approve/reject/makeEffective/obsolete/supersede) + 审批流管理(createApprovalWorkflow/addApprovalStep/executeApprovalStep/getApprovalWorkflow) + 审计追踪(recordAudit/getAuditTrails) + 合规签名(signDocument) |

### 约定规则

1. **跨模块接口** — DMS 实体通过 core `batch/` 包中的接口暴露（`IDocument`）；其他模块通过接口引用 Document，无需直接依赖 DMS 模块
2. **状态机** — `DocumentStatus` 放 core `batch/` 包中，实现 `ILifecycle.StatusEnum`；Document 继承 `BaseLifecycleEntity<DocumentStatus>` 获得 `transition()` 校验；状态流转：DRAFT→UNDER_REVIEW→APPROVED→EFFECTIVE→OBSOLETED，驳回路径 UNDER_REVIEW→REJECTED→DRAFT
3. **模型继承** — 有状态实体（Document）继承 `BaseLifecycleEntity<DocumentStatus>` + 实现 core 接口；无状态流程记录（ApprovalWorkflow）继承 `BaseEntity`
4. **IExpand 约定** — `dms.version` / `dms.category` / `dms.author` / `dms.approvedBy` / `dms.effectiveDate` / `dms.reviewCycle` / `dms.nextReviewDate` / `dms.obsoleteReason` / `dms.rejectionReason` 挂载在 Document 上；`dms.wf.*` 挂载在 ApprovalWorkflow 上
5. **业务便捷方法** — Document 对外的语义化方法封装状态转换，调用方不直接操作 `setStatus()`；方法：`submitForReview()` / `approve(approvedBy)` / `reject(reason)` / `resubmit()` / `makeEffective(date)` / `obsolete(reason)` / `supersede(reason)` / `createNewVersion(version)`
6. **领域事件** — 事件常量在 `DmsEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定；Document 业务方法内部通过 `DomainEventPublisher` 发布对应事件
7. **版本管理** — Document 使用 `IncrementalVersionStrategy`（简单整数递增：1→2→3…）；`bumpVersion()` 执行递增；`supersede()` = 作废旧版 → 创建新版草稿
8. **审计追踪** — DMS 直接使用 core 的 `AuditTrail` record（entityType/entityId/action/operator/timestamp/before/after/reason），遵循 ALCOA+ 原则
9. **电子签名** — DMS 审批流使用 core 的 `IElectronicSignature` + `SignatureMeaning`（REVIEWED/APPROVED/VERIFIED），满足 21 CFR Part 11 三要素
10. **审批流** — ApprovalWorkflow 管理多步骤顺序审批，`executeCurrentStep()` 执行当前待审批步骤，支持 APPROVED/REJECTED/NEEDS_REVISION 三种决定
11. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换（正常+驳回+重新提交）、版本管理、事件发布、审批流、状态机枚举
12. **待实现** — DmsService 实现类、`IComplianceAction` 实现（清场+复核+签名）、AOP 审计切面、文档模板引擎、REST 控制器

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(DmsEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `dms.document.submitted` | **QMS** | 偏差/CAPA 报告进入审批流 |
| `dms.document.approved` | **QMS, Equip** | QMS 同步报告审批状态 / Equip 更新设备验证状态 |
| `dms.document.rejected` | **QMS** | 同步驳回状态，通知报告发起人修改 |
| `dms.document.effective` | **LIMS, PLM, EAM** | LIMS 更新报告生效状态 / PLM 同步 SOP 版本 / EAM 更新设备校准日期 |
| `dms.document.obsoleted` | **PLM** | 标记旧版 SOP 不可用 |
| `dms.document.expired` | **LIMS, QMS, EAM** | 通知复审逾期 |
| `dms.document.versioned` | **PLM** | 同步文档版本变更 |
| `dms.approval.step.completed` | — | 审批步骤完成通知（模块内部使用） |

---

## 更新记录

| 日期 | 内容 |
|------|------|
| 2026-07-19 | **DMS 模块约定完成**：3 个 core 接口/枚举(IDocument/DocumentCategory/DocumentStatus扩展) + DmsEventTypes(8事件) + 3 个模型(Document + ApprovalWorkflow + ApprovalStep) + DmsService 扩展(5→19方法) + 30 个测试 |
| 2026-07-19 | **IoT 模块约定完成**：5 个 core 接口/枚举(IDeviceConnection/ITagValue/ICommand/IAlarmEvent/CommandStatus) + IotEventTypes(9事件) + 4 个模型 + 2 个服务接口(IotGatewayService 13方法/IProtocolAdapter) + 46 个测试 |
| 2026-07-19 | **Equip → Core 回归审核通过**：4 个议题决议（IEquipmentBinding渐进迁移/BOM引用Recipe双轨/FactoryCapacityProfile双轨/EventTypes迁至event.types包）；详见 `docs/architecture/modules/equip-core-regression.md` 五-六章 |
| 2026-07-19 | **Equip → Core 回归**：5 个接口回归 core(IEquipment/IEquipmentParameter/IEquipmentRecipe/IOEMetrics) + EquipEventTypes 迁入 core；equip 模型全部实现新接口；跨模块影响分析文档 → `docs/architecture/modules/equip-core-regression.md` |
| 2026-07-19 | Equip 模块约定补充：Equipment 业务便捷方法 + EquipmentParameter + EquipmentRecipe + OEMetrics + EquipEventTypes + EquipmentService 扩展(4→16方法) + 38 个测试 |
| 2026-07-19 | PLM 模块约定第二次补充：BOM转化模型 + ChangeRequest + 版本管理抽象(core) + 领域事件契约 + Common规则引擎需求 |
| 2026-07-19 | PLM 模块约定第一次补充：Blueprint 业务便捷方法 + ProcessTemplate 便捷方法 + BlueprintDifferImpl + IExpand 文档 + 31 个测试 |
| 2026-07-19 | SCM 模块初步约定建立：4 个 core 接口/枚举 + 3 个模型 + ScmService 扩展 + 39 个测试 |
| 2026-07-18 | ERP 模块初步约定建立：3 个枚举 + 3 个模型 + ErpAdapterService 扩展 + 13 个测试 |
| 2026-07-18 | PLM 模块初步约定建立：BlueprintStatus 状态枚举 + IBlueprintDiffer + BlueprintDiff + 3 个模型 + BlueprintService + 15 个测试 |
| 2026-07-18 | Equip 模块初步约定建立：升级 MachineStatus 状态机 + 10 个测试 + 扩展字段 |
| 2026-07-18 | EAM 模块初步约定建立：3 个 core 接口 + 5 个枚举 + 3 个模型 + 服务接口 + 测试 |
| 2026-07-12 | 全部 15 个业务模块骨架创建 + 编译测试全通过（20模块总计） |
| 2026-07-12 | 移除 easy-factory-db（持久化回归各模块） |
| 2026-07-12 | 8个业务模块骨架创建，编译通过 |
| 2026-07-12 | 初始创建，core 接口体系完成 |
