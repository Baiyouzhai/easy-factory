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
| `iot` | 骨架 | DeviceConnection | IotGatewayService |
| `eam` | 约定已建立 | Asset, MaintenanceOrder, CalibrationRecord | EamService |
| `mps` | 骨架 | ProductionPlan | MpsService |
| `aps` | 骨架 | Schedule | ApsService |
| `wms` | 骨架 | Storage, Receipt | WmsService |
| `andon` | 骨架 | AndonCall | AndonService |
| `bi` | 骨架 | KpiSnapshot | DashboardService |
| `scm` | 约定已建立 | Supplier, PurchaseOrder(PurchaseOrderStatus) | ScmService |
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

## Equip 模块约定（2026-07-18 建立，2026-07-19 补充）

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
| 事件常量 | `factory/EquipEventTypes.java` | ⭐ 9 个领域事件常量（与 PlmEventTypes 同级）；供 MES/Andon/EAM/IoT/BI 订阅 |
| 接口 | `process/action/IEquipmentAction.java` | 设备动作桥接，参数下发 + 状态检查 + OEE 记录 |

### Equip 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Equipment.java` | `AbstractResourceItem` | `IMachine`, `ILifecycle<MachineStatus>` | 设备台账，含 7 个业务便捷方法 + IExpand 文档 |
| `model/EquipmentParameter.java` | `BaseEntity` | — | 工艺参数：设定值 + 实际值 + 控制限 + 控制方式；含 `isInControl()` / `updateActual()` |
| `model/EquipmentRecipe.java` | `BaseEntity` | — | 设备配方：阶段化参数集(RecipePhase) + 版本管理；含 `addPhase()` / `bumpVersion()` |
| `model/OEMetrics.java` | `BaseEntity` | — | OEE 指标：A×P×Q 计算；工厂方法 `of()` + `toPercentString()` |
| `event/EquipEventTypes.java` | — | — | 9 个领域事件常量，含其他模块订阅指南（MES/Andon/EAM/IoT/BI） |
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

---
## PLM 模块约定（2026-07-18 建立，2026-07-19 两次补充）

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `factory/BlueprintStatus.java` | DRAFT→UNDER_REVIEW→APPROVED→RELEASED→OBSOLETED |
| 接口 | `factory/IBlueprintDiffer.java` | 蓝图版本差异比较器，供 DMS/APS 引用 |
| 记录类 | `factory/BlueprintDiff.java` | 差异报告：DiffType × DiffDimension |
| 事件常量 | `factory/PlmEventTypes.java` | 8 个 PLM 领域事件类型常量，供 MES/DMS/APS/ERP/WMS 订阅 |
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

## 更新记录

| 日期 | 内容 |
|------|------|
| 2026-07-19 | **Equip → Core 回归**：5 个接口回归 core(IEquipment/IEquipmentParameter/IEquipmentRecipe/IOEMetrics) + EquipEventTypes 迁入 core；equip 模型全部实现新接口；跨模块影响分析文档 → `docs/architecture/modules/equip-core-regression.md`；待高层级审核 |
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
