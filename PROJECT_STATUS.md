# 项目工作状态

> 独立于 CLAUDE.md，专供各模块会话追踪进度。
> CLAUDE.md 保持稳定，本文件频繁更新。

## easy-factory-core (v1.0)

### 核心定义统计

| 类别 | 数量 | 说明 |
|------|------|------|
| 接口 | 60+ | process/resource/factory/batch/equip/scm/lims/wms/iot/eam/mps/dms/erp 跨模块契约 |
| 状态枚举 | 20 | 全部实现 ILifecycle.StatusEnum |
| 纯值枚举 | 10 | SupplierStatus/MaterialStatus/MaintenanceType 等 |
| 事件常量类 | 12 | MesEventTypes/EquipEventTypes/PlmEventTypes/LimsEventTypes/MpsEventTypes/DmsEventTypes/IotEventTypes/ApsEventTypes/QmsEventTypes/AndonEventTypes/CrmEventTypes/EamEventTypes |
| 操作分析模型 | 15+ | ProcessRouteMatcher/BottleneckDetector/ProductionLeadTime 等 |
| 测试 | 600+ | core 182 + 各模块集成测试 |
| 总计 | 91 | 详见 domain-model.md v5 |

### 已完成（截至 2026-07-19）

- [x] 动作语义: 10 种 Execute 枚举 + Action/ActionGroup/ActionLibrary
- [x] 资源模型: 4M1E+Product 全接口 (IMachine/IMaterial/IPersonnel/IMethod/IEnvironment) + IResourcePack
- [x] 物理模型: IFactory/IWorkstation/IEquipmentBinding/IProductionLine/LineNode/LineConnection/StationType
- [x] 蓝图三阶段: CustomerSpec → EngineeringBOM → WorkOrder (Blueprint 冻结)
- [x] 批次追溯: IBatch (含 WIP 位置+物料谱系) / ITraceable (before/after 快照)
- [x] 20 个状态枚举全部实现 (跨 10 个模块包)
- [x] 23 个跨模块接口 (equip/scm/lims/wms/iot/eam/mps/dms/erp 领域包)
- [x] 10 个事件类型常量类 (event/types/，含 QmsEventTypes + AndonEventTypes + 跨模块订阅指南)
- [x] 生命周期: ILifecycle<S> 泛型状态机 + BaseLifecycleEntity
- [x] 领域事件: IDomainEvent + DomainEventPublisher (前缀匹配+unsubscribe)
- [x] 审计追踪: IAuditable + AuditTrail record + IElectronicSignature + SignatureMeaning
- [x] 操作分析: IMatchingStrategy/ProcessRouteMatcher/BottleneckDetector/ProductionLeadTime/ResourceRequirementExploder
- [x] 时间模型: ITimed 5层接口 + ActionDuration/GmpActionDuration/PreciseActionDuration
- [x] 计量单位: UOM 枚举 (KG/G/L/PCS/CELSIUS/PERCENT 等)
- [x] 工艺参数: IProcessParameter (code/name/uom/targetValue/上限/下限)
- [x] 脚本引擎: IScriptEngine 抽象 + IActionModel.execute() 空指针保护
- [x] 哨兵对象: EmptyAction/EmptyResourcePack
- [x] 仓储抽象: IRepository + IPageRequest/IPageResult + Sort
- [x] 死代码清理: script/Action.java / IProcessRoute / ProcessCompatibilityChecker 已删除
- [x] 拼写修正: Noting → Nothing
- [x] 计算器泛型化: Map<String, ActionDuration> → Map<String, ? extends ITimed>
- [x] IResourcePack.copy() 默认不再返回 null
- [x] require() 接口与实现默认值一致 (false)
- [x] formatDuration() 7处重复代码提取到 ReportPrinter
- [x] BillOfMaterial → IBillOfMaterial 接口化
- [x] Dict.SourceType 从2值扩展到18值

### 待进行

- [ ] GraalJS 沙箱实际集成（POM 依赖已注释，待网络就绪）
- [ ] ScriptExecutor (Nashorn) 与 IScriptEngine (GraalJS) 双轨统一
- [ ] IAction 子接口桥接默认实现 (IEquipmentAction.execute() → IActionModel.super.execute() + 子系统专用逻辑)
- [ ] IBlueprint 公共实现类（当前引用方各自创建匿名内部类）
- [ ] DomainEventPublisher 异步分发升级

---

## easy-factory-common（已完成）

- [x] `IData` + `IExpand` + `IDataExpand` 接口体系
- [x] `Data` / `DataExpand` 抽象基类
- [x] `CollectionUtil` 工具类

---

## 业务模块状态（全部骨架编译通过）

| 模块 | 状态 | 核心模型 | 核心Service接口 |
|------|------|---------|---------------|
| `mes` | 约定已建立，Service实现完成 | MesWorkOrder + ProcessRecord + ActionRecord | WorkOrderService (11方法) + WorkOrderServiceImpl (内存实现) |
| `qms` | 约定已建立 | InspectionOrder + InspectionPlan + InspectionRecord + Deviation + Capa + QualityAction + QmsEventTypes | InspectionService (12方法) + DeviationService (13方法) + CapaService (10方法) + InspectionServiceImpl + DeviationServiceImpl + CapaServiceImpl |
| `plm` | 约定已建立 | ProcessTemplate, Blueprint, ProcessParameter | BlueprintService |
| `equip` | 约定完成 | Equipment + EquipmentParameter + EquipmentRecipe + OEMetrics + EquipEventTypes | EquipmentService (16方法) |
| `lims` | 约定已建立 + 持久化 | Formula + FormulaPhase + WeighingTask + WeighingItem + BatchRecord + LimsEventTypes | FormulaService (10方法) + WeighingTaskService (7方法) + BatchRecordService (8方法) |
| `erp` | 约定已建立 | MaterialCache, InventorySnapshot, Transaction(TransactionStatus) | ErpAdapterService |
| `iot` | 约定完成+持久化 | DeviceConnection + TagValue + Command(CommandStatus) + AlarmEvent + IotEventTypes + 4Repository + IotGatewayServiceImpl | IotGatewayService (13方法) |
| `eam` | 约定完成 | Asset + MaintenanceOrder + CalibrationRecord + EamEventTypes | EamService (18方法) |
| `mps` | 持久化完成 | ProductionPlan(JPA) + DemandSource(JPA) + DemandSourceType + CapacityCheck + MpsEventTypes + 2 Repository + MpsServiceImpl | MpsService (14方法) |
| `aps` | 约定已建立 | Schedule + ScheduledTask + ResourceCalendar + RescheduleTrigger + SchedulingRule + ApsServiceImpl + ApsEventTypes + IOptimizationEngine | ApsService (5方法) |
| `wms` | 约定已建立 | Storage, Receipt, PickingTask, InventorySnapshot | WmsService (20方法) |
| `andon` | 约定已建立 | AndonCall(AndonStatus) + EscalationRule + AndonDashboard + AndonServiceImpl + AndonCallRepository + EscalationRuleRepository | AndonService (19方法) |
| `bi` | **持久化完成** | KpiSnapshot + ProductionDashboard + QualityDashboard + OeeDashboard + InventoryDashboard + KpiType + DashboardPeriod + DashboardServiceImpl + KpiSnapshotRepository | DashboardService (17方法) |
| `scm` | 约定已建立 | Supplier, PurchaseOrder(PurchaseOrderStatus) | ScmService |
| `crm` | 约定已建立 | Customer + SalesOrder(SalesOrderStatus) + Complaint(ComplaintStatus) + SalesOrderItem + CrmEventTypes | CrmService (25方法) |
| `dms` | **持久化完成** | Document + ApprovalWorkflow + ApprovalStep + DmsEventTypes | DmsService (19方法) + DmsServiceImpl |

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

## 活跃任务（2026-07-19 池→线固化实施列表）

> 任务按会话分配。每个任务的"完成"标准 = 编译通过 + 测试通过 + 本文件更新记录。

## 会话任务卡（2026-07-19）

> 编号 = 会话简称 + 序号。完成标准：编译通过 + 测试通过 + 更新本文件记录。

### core（含 factory/physical 子包 — 物理层模型）

| # | 任务 |
|---|------|
| C1 | **IWorkstation.findCapableEquipment(actionCode)** — 确认按 actionCode 返回设备列表，按 isPrimary() 降序排列（Primary 在前）。验收：同一 actionCode 可查到多台设备 + 排序正确 ✅ 已有实现 |
| C2 | **IProductionLine / IWorkstation** — 确认"产线=有序工位序列，工位=设备绑定集合"。验收：与 overview.md §3 一致 ✅ 已完成 |
| C3 | **IBlueprint** — 确认 Action 中无设备引用字段（设备是绑定层的事）。验收：遍历所有 Action，无一引用 Equipment ✅ 已完成 |
| C4 | **GroupType 残留** — 确认清除完毕。验收：grep -r "GroupType" 零结果 ✅ 已完成 |
| C5 | **Physical 无 Equip 依赖** — 确认 Physical 包不 import Equip。验收：无业务模块 import ✅ 已完成 |
| C6 | **IProductionLine 实现** — addWorkstation → 工位按 LineNode 有序排列。验收：按顺序遍历工位 ✅ 已完成 |
| C7 | **IWorkstation 实现** — 管理一组 IEquipmentBinding。验收：findBinding(actionCode) → List ✅ 已完成 |
| C8 | **IEquipmentBinding 实现** — actionCode + equipmentCode + priority + parameters。验收：1:N 绑定 + 按 priority 排序 ✅ 已完成 |
| C9 | **IEquipmentBinding 预留 setup/cleanup** — 接口增加 `getSetupMinutes()` / `getCleanupMinutes()` 可选方法（default 返回 0）。APS 排程时消费，MES 执行时忽略。验收：接口定义，不影响现有编译 ✅ 已完成 |
| C10 | **IEquipmentBinding 序列约束三档防呆** — `SequenceMatch` 枚举(EXACT/FLEXIBLE/REJECTED)。EXACT=顺序一致直接通过；FLEXIBLE=动作集合匹配但顺序不同，允许但标记 setup 惩罚；REJECTED=设备不支持某动作，门禁拒绝。`getSupportedSequences()` default 空=跳过检查。验收：枚举定义 + `matchSequence(List<String>)` 方法 + 绑定创建时校验 ✅ 已完成 |

### plm — 设计态（池）

| # | 任务 |
|---|------|
| P1 | Blueprint 的 Action 通过 requireResources() 描述能力需求，不绑定设备编码 |
| P2 | Blueprint 版本发布流程：DRAFT→UNDER_REVIEW→APPROVED→RELEASED→OBSOLETED。验收：5 状态全过 |
| P3 | BlueprintService.publishToMes(blueprint, factoryCode) — 发布时携带工厂绑定配置 |

### equip — 设备层

| # | 任务 |
|---|------|
| E1 | Equipment.getStatus() → MachineStatus 枚举。验收：MES 可按 code 查询 |
| E2 | Equipment.occupy() / release() — 被 MES 调用。验收：occupy→RUNNING, release→IDLE |
| E3 | 设备绑定配置数据结构 — 可按 factoryCode + actionCode 返回绑定列表 |

### mes — 执行态（走固化线）

| # | 任务 |
|---|------|
| M1 | 工单创建时冻结 Blueprint 版本号，运行时按版本号获取 Blueprint |
| M2 | Action 执行：查 IEquipmentBinding → Primary 可用用 Primary，不可用 fallback Backup |
| M3 | 全故障处理：通知 Andon → 暂停工序 → INTERRUPTED |
| M4 | 每个 Action 后生成 ActionRecord(ITraceable) — 操作人/设备/时间/快照 |

### lims — 配方系统

| # | 任务 |
|---|------|
| L1 | Formula 关联 Blueprint 版本（按 blueprintCode+version），称量任务按工单创建 |
| L2 | WeighingTask 状态流转：PENDING→WEIGHING→VERIFIED→COMPLETE。验收：5 状态全过 |
| L3 | 称量防错脚本集成 — 天平读数 vs 配方量偏差校验 |

### wms — 仓储管理

| # | 任务 |
|---|------|
| W1 | Receipt 状态流转：PENDING→PARTIAL→COMPLETED→CLOSED。验收：4 状态 + 明细管理 |
| W2 | PickingTask 按 MES 工单创建 — FIFO/FEFO 批次选择 |
| W3 | InventorySnapshot — 在手/已分配/可用/待检/不合格。验收：负数防护 |

### iot — 设备互联

| # | 任务 |
|---|------|
| I1 | DeviceConnection 心跳检测 + 超时判定 |
| I2 | TagValue 采集 — OPC UA 三态质量模型，3 个工厂方法(of/bad/uncertain) |
| I3 | IProtocolAdapter 接口 — connect/disconnect/read/write/subscribe |

### erp — 资源计划集成

| # | 任务 |
|---|------|
| R1 | MaterialCache.toResourceItem() — ERP 物料→core IResourceItem 映射 |
| R2 | Transaction 回传队列 — PENDING→SENT→CONFIRMED/FAILED 状态流转 |
| R3 | ErpAdapterService 库存查询 — queryStock(materialCode, plantCode) |

### eam — 资产管理

| # | 任务 |
|---|------|
| A1 | Asset 状态流转：IDLE→IN_USE→UNDER_MAINTENANCE→SCRAPPED。验收：4 状态全过 |
| A2 | MaintenanceOrder — 关联 Asset + EquipmentCode + 停机时长 + 备件清单 |
| A3 | CalibrationRecord — 校准结果 + 下次校准日期 + 到期提醒 |

### mps — 主生产计划

| # | 任务 |
|---|------|
| S1 | ProductionPlan 状态流转：DRAFT→APPROVED→RELEASED→IN_PROGRESS→COMPLETED→CLOSED |
| S2 | DemandSource 注册 — SALES_ORDER/FORECAST/SAFETY_STOCK/MANUAL 四种来源 |
| S3 | 粗产能检查 — 使用 FactoryCapacityProfile + BottleneckDetector |

### aps — 高级排程

| # | 任务 |
|---|------|
| K1 | Schedule 状态流转：DRAFT→OPTIMIZED→DISPATCHED→IN_PROGRESS→COMPLETED |
| K2 | SchedulingRule 排程引擎 — EDD/SPT/CR 三种策略 + 可组合 Comparator |
| K3 | ResourceCalendar — 可用窗口 + 产能 + canFit/overlapMinutes |

### qms — 质量管理

| # | 任务 |
|---|------|
| Q1 | InspectionOrder 创建 + 状态流转：PENDING→IN_PROGRESS→PASSED/FAILED→CLOSED |
| Q2 | 质量门禁判定脚本 — 放行/让步/拒收 + 偏差触发 |
| Q3 | SPC 数据订阅 — 消费 iot.tag.collected → 控制限判定 |

### andon — 安灯系统

| # | 任务 | 状态 |
|---|------|------|
| N1 | AndonCall 触发 + 状态流转：OPEN→ACKNOWLEDGED→RESOLVED/ESCALATED→CLOSED | ✅ 已完成 |
| N2 | 逐级上报规则 — 静态配置数据库表，操作工→班组长→车间主任→厂长 | ✅ 已完成 |
| N3 | 多源事件订阅 — mes.process.interrupted + equip.fault + qms.spc.outOfControl | ✅ 约定已建立（AndonEventTypes 7事件 + 订阅指南） |

### dms — 文档管理

| # | 任务 |
|---|------|
| D1 | Document 状态流转：DRAFT→UNDER_REVIEW→APPROVED→EFFECTIVE→OBSOLETED |
| D2 | ApprovalWorkflow — 多步骤审批流 + 进度追踪 + 完成判定 |
| D3 | 审计追踪 — AuditTrail 记录 entityType/entityId/action/operator/before/after |

### scm — 供应链

| # | 任务 |
|---|------|
| U1 | Supplier 双维度 — qualification(资质) + status(运营态) 各自独立变化 |
| U2 | PurchaseOrder 状态流转：DRAFT→APPROVED→SENT→RECEIVING→COMPLETED |
| U3 | PO 明细收货 — receive/receivedQty/remaining/isFullyReceived |

### bi — 看板报表

| # | 任务 |
|---|------|
| B1 | 生产看板 — MES 工单进度 + 计划完成率 |
| B2 | 质量看板 — QMS 一次合格率 + 偏差统计 |
| B3 | OEE 看板 — Equip 可用率×性能率×质量率 |

### crm — 客户关系（协作层）

| # | 任务 |
|---|------|
| CR1 | Customer 注册 — code/name/industry/GMP审计状态。验收：Customer 实体可查询 |
| CR2 | SalesOrder 状态流转 — 订单确认→排程→生产中→发货→完成 |
| CR3 | Complaint → QMS CAPA 闭环 — 投诉创建后通知 QMS 创建偏差 |

---

## 实施顺序（详见 docs/implementation/roadmap.md）

| 阶段 | 模块 | 并行度 | 关键依赖 |
|------|------|--------|---------|
| Phase 1 | erp, iot, plm, equip, scm, dms | 6并行 | 无（仅依赖 core） |
| Phase 2 | lims, wms, mps | 3并行 | erp/plm/scm |
| Phase 3 | **mes**, aps | 串行 | plm+equip+wms+lims |
| Phase 4 | qms, andon, eam | 2并行 | mes(+iot) |
| Phase 5 | bi, web, test | 4并行 | 全部 |

> **Phase 5 测试模块已完成**：42 个跨模块集成测试覆盖全部15个业务模块。

> **mes 是集成枢纽，预计占总工作量 40%+**

---

---

## EAM 模块约定（2026-07-18 建立，2026-07-25 补充）

> **包位置说明**: 以下文件位于 core `com.byz.factory.eam` 包（按制造职能分包），非 `com.byz.factory.batch`。
> 事件常量位于 core `com.byz.factory.event.types`。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `eam/IAsset.java` | 资产抽象，供 Equip/Andon/ERP/DMS 引用 |
| 接口 | `eam/IMaintenanceOrder.java` | 维护工单抽象，供 Andon 引用 |
| 接口 | `eam/ICalibrationRecord.java` | 校准记录抽象，供 DMS 引用 |
| 状态枚举 | `eam/MaintenanceOrderStatus.java` | OPEN→IN_PROGRESS→COMPLETED→VERIFIED; +CANCELLED |
| 值枚举 | `eam/MaintenanceType.java` | PREVENTIVE / CORRECTIVE / PREDICTIVE / CALIBRATION |
| 值枚举 | `eam/MaintenancePriority.java` | LOW / MEDIUM / HIGH / EMERGENCY |
| 值枚举 | `eam/CalibrationResult.java` | PASS / FAIL / ADJUSTED |
| 值枚举 | `eam/CalibrationType.java` | INTERNAL / EXTERNAL |
| 事件常量 | `event/types/EamEventTypes.java` | ⭐ 6 个领域事件常量（与 PlmEventTypes/EquipEventTypes 同级）；供 Andon/DMS/MES/ERP 订阅 |

### EAM 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Asset.java` | `BaseLifecycleEntity<AssetStatus>` | `IAsset` | 资产实体，含 5 个业务便捷方法 + IExpand 文档 |
| `model/MaintenanceOrder.java` | `BaseLifecycleEntity<MaintenanceOrderStatus>` | `IMaintenanceOrder` | 维护工单，含 4 个业务便捷方法(自动记录时间/成本) + IExpand |
| `model/CalibrationRecord.java` | `BaseEntity` | `ICalibrationRecord` | 校准记录，含 3 个便捷方法(recordPass/recordFail/recordAdjusted) + IExpand |
| `service/EamService.java` | — | — | 18 方法：资产查询+生命周期 + 维护工单+查询 + 校准+查询+到期预警 |

### 约定规则

1. **跨模块接口** — 被其他模块引用的 EAM 实体在 core 定义接口，放在 `com.byz.factory.eam`
2. **状态枚举** — 所有枚举放 core；状态机枚举实现 `ILifecycle.StatusEnum`；纯值枚举为 plain enum
3. **模型继承** — 有状态实体继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录继承 `BaseEntity` + 实现 core 接口
4. **业务便捷方法** — Asset/ MaintenanceOrder 对外语义化方法封装 `transition()` + `markUpdated()`，调用方不直接操作 `setStatus()`；MaintenanceOrder 的 `startWork()` 自动记录 `actualStart`，`completeWork()` 自动记录 `actualEnd`/downtime/cost/technician
5. **领域事件** — 事件常量在 `EamEventTypes` 统一管理（`event/types/`）；Andon 订阅维护开始/完成，DMS 订阅校准记录，MES 订阅资产报废/维护开始
6. **IExpand 约定** — Asset: `eam.*` 前缀；MaintenanceOrder: `eam.mo.*` 前缀；CalibrationRecord: `eam.cal.*` 前缀
7. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；29 个测试覆盖构造/底层转换/业务方法/非法操作/IExpand/事件命名
8. **待实现** — EamService 实现类、维护计划自动生成、备件管理、到期预警自动通知

---

## Equip 模块约定（2026-07-18 建立，2026-07-19 补充，2026-07-19 审核通过）

### 设计定位

Equip 管理生产设备的工艺参数、设备配方、运行状态和 OEE 效率。设备是 core 中 `Resource (group=Machine)` 的具体化和管理系统，向上供应 MES 设备状态、IoT 参数下发、BI 效率看板所需数据。

### Core 层依赖（供 Equip 引用，2026-07-19 回归）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `equip/MachineStatus.java` | IDLE→RUNNING\|SETUP\|MAINTENANCE / RUNNING→IDLE\|FAULT / FAULT→MAINTENANCE→IDLE |
| 接口 | `equip/IEquipment.java` | ⭐ 设备台账抽象 = IMachine + ILifecycle<MachineStatus> + 台账字段；供 EAM/MES/APS/Andon 引用 |
| 接口 | `equip/IEquipmentParameter.java` | ⭐ 工艺参数抽象：设定值/实际值/控制限 + isInControl() default；供 IEquipmentBinding/IEquipmentAction/EngineeringBOM 迁移 |
| 接口 | `equip/IEquipmentRecipe.java` | ⭐ 设备配方抽象 + IRecipePhase 内嵌接口；供 EngineeringBOM/PLM/IoT 引用 |
| 接口 | `equip/IOEMetrics.java` | ⭐ OEE 指标抽象：A×P×Q；供 BI/APS/FactoryCapacityProfile 引用 |
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
| **持久化层（2026-07-25）** | | | |
| `service/impl/EquipmentServiceImpl.java` | — | `EquipmentService` | 构造器注入 4 Repository + @Transactional + DomainEventPublisher 事件发布 |
| `repository/EquipmentRepository.java` | `JpaRepository` | — | 按 code/status/category/location 查询 |
| `repository/EquipmentParameterRepository.java` | `JpaRepository` | — | 按 equipmentCode+paramCode 查询 |
| `repository/EquipmentRecipeRepository.java` | `JpaRepository` | — | 按 code/equipmentCode/productCode 查询 |
| `repository/OEMetricsRepository.java` | `JpaRepository` | — | 按设备+周期查询 + 最近一次 OEE |
| `ddl/V1.3__equip.sql` | — | — | 5 张表：equip_equipment/equip_parameter/equip_recipe/equip_recipe_phase/equip_oee_metrics |
| **测试** | | | |
| `EquipModuleTest.java` | — | — | 43 个单元测试 |
| `repository/EquipmentRepositoryTest.java` | — | — | 4 个集成测试：保存查询/按状态/按类别/按位置 |
| `repository/OEMetricsRepositoryTest.java` | — | — | 2 个集成测试：周期查询/最近 OEE |

### 约定规则

1. **状态机** — 使用 core 的 `MachineStatus`（实现 `ILifecycle.StatusEnum`），不再定义内部 Status 枚举；Equipment 实现 `ILifecycle<MachineStatus>` 获得 `transition()` 校验
2. **模型继承** — Equipment 继承 `AbstractResourceItem`（保留资源数量操作 + 4M1E 分类）+ 实现 `IMachine` + `ILifecycle<MachineStatus>`；与 EAM 的 `BaseLifecycleEntity` 模式有所不同，因为设备同时是资源。其余模型（EquipmentParameter, EquipmentRecipe, OEMetrics）均继承 `BaseEntity`
3. **IExpand 约定** — `equip.status` / `equip.oee` / `equip.lastMaintenanceDate` / `equip.nextMaintenanceDate` / `equip.parameters` 等动态属性挂载在资源扩展属性中
4. **业务便捷方法** — Equipment 对外的语义化方法封装 `transition()` + `markUpdated()`，调用方不直接操作 `setStatus()`；方法：`startProduction()` / `stopProduction()` / `startSetup()` / `completeSetup()` / `reportFault()` / `startMaintenance()` / `completeMaintenance()`
5. **领域事件** — 事件常量在 `EquipEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定；Equipment 业务方法内部可发布对应事件（通过 `DomainEventPublisher`）
6. **配方版本** — EquipmentRecipe 通过 `version` 字符串管理版本（默认 "1.0.0"），`bumpVersion()` 执行 MINOR 递增（1.0.0 → 1.1.0）；RecipePhase 为静态内部类，三参数构造默认 rampRate=0
7. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换（正常+故障恢复+换型）、非法转换、业务便捷方法（正常+非法）、参数控制限、配方阶段、OEE 计算、事件命名约定
8. **已完成** — ✅ EquipmentService 实现类 + JPA Repository + DDL + 集成测试（2026-07-25）。**待实现** — IEquipmentAction 桥接、IoT 参数实时监控、OEE 六大损失采集、REST 控制器

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
11. **待实现** — BOM 规则引擎（Common）、工艺参数标准库

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
| 接口 | `erp/IErpTransaction.java` | 事务回传记录抽象，供 MES/WMS/LIMS 创建事务时编译期引用 |
| 状态枚举 | `erp/TransactionStatus.java` | PENDING→SENT→CONFIRMED / SENT→FAILED→PENDING / PENDING→CANCELLED；实现 `ILifecycle.StatusEnum` |
| 值枚举 | `erp/TransactionType.java` | GOODS_ISSUE / GOODS_RECEIPT / TRANSFER |

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
| 接口 | `scm/ISupplier.java` | 供应商抽象，供 WMS/QMS/ERP 引用 |
| 接口 | `scm/IPurchaseOrder.java` | 采购订单抽象（含 IPurchaseOrderItem 嵌套接口），供 WMS/ERP 引用 |
| 接口 | `scm/IInboundPlan.java` | 来料计划抽象，供 WMS 引用 |
| 状态枚举 | `scm/PurchaseOrderStatus.java` | DRAFT→APPROVED→SENT→RECEIVING→COMPLETED；+CANCELLED |
| 状态枚举 | `scm/InboundPlanStatus.java` | CREATED→NOTIFIED→RECEIVING→COMPLETED；+CANCELLED |
| 值枚举 | `scm/SupplierStatus.java` | ACTIVE / INACTIVE / BLACKLISTED |
| 事件常量 | `event/types/ScmEventTypes.java` | 16 个事件类型：SUPPLIER_* (6) + PO_* (6) + INBOUND_* (2) + PREFIX |

### SCM 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Supplier.java` | `BaseEntity` | `ISupplier` | 供应商实体，双维度分离 |
| `model/PurchaseOrder.java` | `BaseLifecycleEntity<PurchaseOrderStatus>` | `IPurchaseOrder` | 采购订单，完整状态机 + @OneToMany 级联明细 |
| `model/PurchaseOrderItem.java` | —（`@Entity`） | `IPurchaseOrder.IPurchaseOrderItem` | 采购明细行，收货累加 |
| `model/InboundPlan.java` | `BaseLifecycleEntity<InboundPlanStatus>` | `IInboundPlan` | 来料计划，通知仓库→收货→完成 |

### 约定规则

1. **跨模块接口** — SCM 实体通过 core `scm/` 包中的接口暴露（`ISupplier`、`IPurchaseOrder`）；其他模块通过接口引用 Supplier/PurchaseOrder，无需直接依赖 SCM
2. **状态枚举** — `PurchaseOrderStatus` 放 core `scm/` 包，实现 `ILifecycle.StatusEnum`；`SupplierStatus` 为纯值枚举（状态变更由管理操作驱动，不走状态机）
3. **模型继承** — 有状态实体（PurchaseOrder）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录（Supplier）继承 `BaseEntity` + 实现 core 接口
4. **IExpand 约定** — `scm.category` / `scm.qualification` / `scm.leadTime` / `scm.onTimeRate` / `scm.qualityRate` / `scm.contacts` 挂载在 Supplier 上；`scm.poNo` / `scm.supplierCode` / `scm.approvedBy` / `scm.expectedDelivery` 挂载在 PurchaseOrder 上
5. **跨模块协作** — SCM→WMS（来料计划触发收货）、SCM←WMS（收货结果更新 PO 进度）、SCM→ERP（采购成本→应付）、SCM→QMS（供应商批次→来料检 IQC）
6. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换、退货、非法转换、终态、采购明细/收货逻辑
7. **Supplier 双维度分离** — `qualification`（资质，String）和 `status`（运营，SupplierStatus）是两个正交维度，各自独立变化。qualification 用 String 预留对接外部 OA/ERP 时扩展中间态（如 EXPIRING）；status 用枚举直接建模 ACTIVE/INACTIVE/BLACKLISTED 三类运营态。示例：已合格的供应商可因付款纠纷暂停合作（INACTIVE），恢复后可因质量事故拉黑（BLACKLISTED）——两个维度互不绑定
8. **持久化** — JPA 注解直接加在 Model 类上（`@Entity @Table`）；Supplier 用 `@Enumerated(STRING)` 持久化 status；PurchaseOrder 通过 `@OneToMany(cascade=ALL, orphanRemoval=true)` 级联持久化明细行；枚举字段映射到 VARCHAR 列
9. **服务实现** — `ScmServiceImpl`（@Transactional）含完整生命周期：注册/审核/暂停/恢复供应商 + 创建/审批/发送/收货/取消订单 + 来料计划管理 + 状态自动推进
10. **待实现** — NotificationService（仓库通知）、SupplierScoring（供应商评分）后续迭代

### 持久化产出

| 类型 | 文件 | 说明 |
|------|------|------|
| DDL | `ddl/V1.2__scm.sql` | 4 表：supplier + purchase_order + purchase_order_item + inbound_plan |
| Repo | `repository/SupplierRepository.java` | 6 查询 |
| Repo | `repository/PurchaseOrderRepository.java` | 4 查询 |
| Repo | `repository/InboundPlanRepository.java` | 5 查询 |
| Service | `service/ScmServiceImpl.java` | @Transactional，3 个 repo 注入，完整 CRUD |
| 测试 | `repository/ScmRepositoryTest.java` | 23 个 @DataJpaTest |
| 测试 | `service/ScmServiceImplTest.java` | 18 个 @DataJpaTest + @Import |
| Repository | `repository/SupplierRepository.java` | 6 个查询方法（findByCode/category/status/qualification/组合查询） |
| Repository | `repository/PurchaseOrderRepository.java` | 4 个查询方法（findByPoNo/supplierCode/status/approvedBy） |
| 测试 | `repository/ScmRepositoryTest.java` | 23 个 @DataJpaTest 集成测试：Supplier(10) + PO(11) + 综合(2) |

---

## IoT 模块约定（2026-07-19 建立）

### 设计定位

IoT 是 **设备层的适配和采集中枢**——连接 PLC/SCADA/DCS 等工业控制系统，将设备数据转化为 core 领域模型的实时状态。IoT 向上为 Equip（参数实时值）、MES（设备状态）、QMS（SPC 数据源）、Andon（报警触发）提供数据支撑，向下通过协议适配器统一 OPC UA / Modbus / MQTT / Siemens S7 / HTTP 等工业协议。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `iot/IDeviceConnection.java` | ⭐ 设备连接抽象：设备编码 + 协议 + 端点 + 采集间隔 + 在线判定 default；供 Equip/MES/Andon 引用 |
| 接口 | `iot/ITagValue.java` | ⭐ 采集标签值抽象 + TagQuality 内嵌枚举(GOOD/BAD/UNCERTAIN)；供 Equip/QMS/MES 引用 |
| 接口 | `iot/ICommand.java` | ⭐ 设备指令抽象 + CommandType(SET_PARAM/START/STOP/READ/ACK_ALARM) + CommandPriority(HIGH/NORMAL/LOW) 内嵌枚举；供 Equip/MES/Andon 引用 |
| 接口 | `iot/IAlarmEvent.java` | ⭐ 报警事件抽象 + AlarmSeverity(INFO/WARNING/CRITICAL/EMERGENCY) 内嵌枚举；供 QMS/Andon/MES/EAM 引用 |
| 状态枚举 | `iot/CommandStatus.java` | QUEUED→SENT→ACKNOWLEDGED→COMPLETED；SENT/ACKNOWLEDGED→FAILED；QUEUED→CANCELLED；实现 ILifecycle.StatusEnum |
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

1. **跨模块接口** — IoT 实体通过 core `iot/` 包中的接口暴露（`IDeviceConnection`、`ITagValue`、`ICommand`、`IAlarmEvent`）；其他模块通过接口引用 IoT 数据，无需直接依赖 IoT 模块
2. **状态机** — `CommandStatus` 放 core `iot/` 包中，实现 `ILifecycle.StatusEnum`；Command 继承 `BaseLifecycleEntity<CommandStatus>` 获得 `transition()` 校验
3. **模型继承** — 有状态实体（Command）继承 `BaseLifecycleEntity<CommandStatus>` + 实现 core 接口；无状态记录（DeviceConnection, TagValue, AlarmEvent）继承 `BaseEntity` + 实现 core 接口
4. **IExpand 约定** — `iot.protocol` / `iot.endpoint` / `iot.connectionStatus` / `iot.lastHeartbeat` 挂载在 DeviceConnection 上；`iot.tag` / `iot.interval` 挂载在 Resource 上供 TagFilter 提取；`iot.alarm.*` 挂载在 AlarmEvent 上
5. **业务便捷方法** — Command 对外的语义化方法封装状态转换，调用方不直接操作 `setStatus()`；方法：`markSent()` / `markAcknowledged()` / `markCompleted()` / `markFailed()` / `cancel()`；AlarmEvent 提供 `acknowledge()` / `resolve()` / `addAction()`；DeviceConnection 提供 `heartbeat()` / `isTimedOut()`
6. **领域事件** — 事件常量在 `IotEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定；IoT 模型业务方法内部通过 `DomainEventPublisher` 发布对应事件
7. **协议适配器** — `IProtocolAdapter` 定义统一的协议抽象（connect/disconnect/isConnected/read/write/subscribe）；具体协议实现在 IoT 模块内部，不暴露到 core
8. **数据质量** — TagValue 采用 OPC UA 三态质量模型（GOOD/BAD/UNCERTAIN），3 个工厂方法覆盖常见场景（`of`=正常值 / `bad`=传感器故障 / `uncertain`=超量程）
9. **报警联动** — CRITICAL/EMERGENCY 级别报警触发后，通过 `DomainEventPublisher` 通知 Andon（创建呼叫）、MES（暂停工序）、QMS（发起偏差）、EAM（紧急维护工单）
10. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换（正常+非法+终态）、业务便捷方法、事件命名约定、接口契约
11. **待实现** — 5 种协议适配器实现（OPC UA/Modbus/MQTT/S7/HTTP）、IDataCollectionAction 桥接实现、时序数据库集成（InfluxDB/TimescaleDB）、REST 控制器、报警联动引擎
12. **持久化（2026-07-25 完成）** — 4 个 Model 全部添加 JPA 注解（@Entity/@Table/@Column/@Enumerated/@Convert）；3 个 JSON 属性转换器（JsonMapConverter/JsonListConverter/JsonValueConverter）；4 个 Repository 接口（DeviceConnection/TagValue/Command/AlarmEvent）；IotGatewayServiceImpl 完整实现（设备连接+数据采集+指令下发+报警管理+协议适配器管理+领域事件发布）；DDL V1.5__iot.sql（PostgreSQL 16）；17 个 Repository 集成测试 + 46 个单元测试 = 63 测试全部通过

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

## DMS 模块约定（2026-07-19 建立，2026-07-19 持久化层完成）

### 设计定位

DMS 管理制造运营中的 GMP 受控文档——SOP、批记录、检验报告、偏差报告、CAPA 报告、验证文件、校准证书。DMS 是 GMP 合规的底层基础设施：所有 GxP 文件需要版本控制、审批流和审计追踪。向上为 LIMS/QMS/PLM/EAM 提供文档审批和归档能力。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 事件常量 | `event/types/DmsEventTypes.java` | ⭐ 8 个领域事件常量（与 PlmEventTypes/EquipEventTypes/IotEventTypes 同包 `com.byz.factory.event.types`） |

> **注意**：`DocumentStatus`、`DocumentCategory`、`IDocument` 按项目约定（"内部枚举放模块内"）从 core `batch/` 迁入 DMS 模块 `com.byz.factory.dms` 包，不暴露为跨模块接口。

### DMS 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Document.java` | `BaseLifecycleEntity<DocumentStatus>` | `IDocument` | 文档核心实体：`@Entity` JPA 映射 + 完整状态机 + 8 个业务便捷方法 + 版本管理(IncrementalVersionStrategy) + 领域事件发布 + IExpand 文档 |
| `model/ApprovalWorkflow.java` | `BaseEntity` | — | 审批工作流：`@Entity` JPA 映射 + `@OneToMany` ApprovalStep + 进度追踪 + 完成判定 |
| `model/ApprovalStep.java` | — | — | 审批步骤：`@Entity` JPA 映射 + 角色→人→决定(APPROVED/REJECTED/NEEDS_REVISION/PENDING) + 意见 + 时间戳 |

### DMS 内部枚举/接口

| 文件 | 说明 |
|------|------|
| `DocumentStatus.java` | 7 状态枚举：DRAFT→UNDER_REVIEW→APPROVED→EFFECTIVE→OBSOLETED；+REJECTED；实现 `ILifecycle.StatusEnum` |
| `DocumentCategory.java` | 7 类别：SOP / BATCH_RECORD / INSPECTION / DEVIATION / CAPA / VALIDATION / CALIBRATION |
| `IDocument.java` | 文档内部契约接口 |

### 持久化层

| 文件 | 说明 |
|------|------|
| `repository/DocumentRepository.java` | extends JpaRepository：findByDocumentCode / findByCategory / findByStatus / findByTitleContainingIgnoreCase |
| `repository/ApprovalWorkflowRepository.java` | extends JpaRepository：findByDocumentCode |
| `ddl/V1.6__dms.sql` | 3 表：dms_document + dms_approval_workflow + dms_approval_step（含索引） |

### 服务接口/实现

| 文件 | 说明 |
|------|------|
| `service/DmsService.java` | 19 方法：文档 CRUD(createDraft/getDocument/listDocuments/searchDocuments/updateContent) + 审批生命周期(submitForReview/approve/reject/makeEffective/obsolete/supersede) + 审批流管理(createApprovalWorkflow/addApprovalStep/executeApprovalStep/getApprovalWorkflow) + 审计追踪(recordAudit/getAuditTrails) + 合规签名(signDocument) |
| `service/impl/DmsServiceImpl.java` | `@Service @Transactional` 构造器注入 Repository：读写操作 + 审计追踪记录；（Model 层内嵌事件发布，Impl 不重复发布） |

### 约定规则

1. **内部枚举/接口** — `DocumentStatus`、`DocumentCategory`、`IDocument` 按项目约定"内部枚举放模块内"，放在 DMS 模块 `com.byz.factory.dms` 包，不暴露为跨模块接口
2. **状态机** — `DocumentStatus` 实现 `ILifecycle.StatusEnum`；Document 继承 `BaseLifecycleEntity<DocumentStatus>` 获得 `transition()` 校验；状态流转：DRAFT→UNDER_REVIEW→APPROVED→EFFECTIVE→OBSOLETED，驳回路径 UNDER_REVIEW→REJECTED→DRAFT
3. **模型继承** — 有状态实体（Document）继承 `BaseLifecycleEntity<DocumentStatus>` + 实现 DMS 内部 `IDocument`；无状态流程记录（ApprovalWorkflow）继承 `BaseEntity`
4. **JPA 映射** — Model 直接加 `@Entity @Table @Column @Enumerated`，不新建单独 Entity 类；ApprovalWorkflow.steps 用 `@OneToMany(cascade=ALL, orphanRemoval=true)` + `@JoinColumn(name="workflow_id")`
5. **IExpand 约定** — `dms.version` / `dms.category` / `dms.author` / `dms.approvedBy` / `dms.effectiveDate` / `dms.reviewCycle` / `dms.nextReviewDate` / `dms.obsoleteReason` / `dms.rejectionReason` 挂载在 Document 上；`dms.wf.*` 挂载在 ApprovalWorkflow 上
6. **业务便捷方法** — Document 对外的语义化方法封装状态转换，调用方不直接操作 `setStatus()`；方法：`submitForReview()` / `approve(approvedBy)` / `reject(reason)` / `resubmit()` / `makeEffective(date)` / `obsolete(reason)` / `supersede(reason)` / `createNewVersion(version)`
7. **领域事件** — 事件常量在 core `DmsEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定；Document 业务方法内部通过 `DomainEventPublisher` 发布对应事件
8. **版本管理** — Document 使用 `IncrementalVersionStrategy`（简单整数递增：1→2→3…）；`bumpVersion()` 执行递增；`supersede()` = 作废旧版 → 创建新版草稿
9. **审计追踪** — DMS 直接使用 core 的 `AuditTrail` record；ServiceImpl 在写操作中记录审计追踪
10. **电子签名** — 使用 core 的 `IElectronicSignature` + `SignatureMeaning`（REVIEWED/APPROVED/VERIFIED）
11. **审批流** — ApprovalWorkflow 管理多步骤顺序审批；步骤拒绝/需修订立即终止流程
12. **Service 实现** — `@Service @Transactional` 构造器注入 Repository；Model 层内嵌事件发布（历史遗留），ServiceImpl 不重复发布事件
13. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；34 个测试覆盖构造、状态转换、版本管理、事件发布、审批流、状态机
14. **待实现** — `IComplianceAction` 实现（清场+复核+签名）、AOP 审计切面、文档模板引擎、REST 控制器

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

## LIMS 模块约定（2026-07-19 建立）

### 设计定位

LIMS 管理产品配方（物料组成）、配料称量和批记录。配方本质是 core 的 `IResourcePack` + 版本管理 + 投料阶段划分；称量任务关联工单和批次，通过允差校验实现防错；批记录是 GMP 合规的核心文档，记录从投料到产出的完整制造过程。LIMS 向上为 MES（配方下发/物料信息）、QMS（称量偏差→偏差处理）、DMS（批记录归档）、ERP（物料消耗同步）提供支撑。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `lims/FormulaStatus.java` | DRAFT→APPROVED→ACTIVE→RETIRED；APPROVED 可退回 DRAFT |
| 状态枚举 | `lims/WeighingTaskStatus.java` | PENDING→WEIGHING→VERIFIED→COMPLETE |
| 状态枚举 | `lims/BatchRecordStatus.java` | IN_PROGRESS→REVIEW→APPROVED→ARCHIVED；REVIEW 可退回 IN_PROGRESS |
| 接口 | `lims/IFormula.java` | ⭐ 配方抽象 + IFormulaPhase 嵌套接口；供 MES/QMS/ERP 编译期引用 |
| 接口 | `lims/IWeighingTask.java` | ⭐ 称量任务抽象 + IWeighingItem 嵌套接口；供 MES/IoT/QMS 编译期引用 |
| 接口 | `lims/IBatchRecord.java` | ⭐ 批记录抽象；供 MES/QMS/DMS 编译期引用 |
| 事件常量 | `event/types/LimsEventTypes.java` | ⭐ 9 个领域事件常量（与 DmsEventTypes/EquipEventTypes/IotEventTypes/PlmEventTypes 同包 `com.byz.factory.event.types`） |

### LIMS 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Formula.java` | `BaseLifecycleEntity<FormulaStatus>` | `IFormula, HasVersion` | 配方核心实体：完整状态机 + 6 个业务便捷方法 + 语义版本管理(SemanticVersionStrategy) + 领域事件发布 + FormulaPhase 内部类 + IExpand 文档 |
| `model/WeighingTask.java` | `BaseLifecycleEntity<WeighingTaskStatus>` | `IWeighingTask` | 称量任务：3 个业务便捷方法 + 偏差检测(getDeviatedItems) + 领域事件发布 |
| `model/WeighingItem.java` | — | `IWeighingTask.IWeighingItem` | 称量明细：recordWeighing() + getDeviationPercent() + isOutOfTolerance() |
| `model/BatchRecord.java` | `BaseLifecycleEntity<BatchRecordStatus>` | `IBatchRecord` | 批记录：4 个业务便捷方法 + 3 个记录添加方法 + 领域事件发布 + IExpand 文档 |

### 服务接口

| 文件 | 说明 |
|------|------|
| `service/FormulaService.java` | 10 方法：create/getByCode/list/submitForApproval/approve/activate/retire/releaseVersion/createWeighingTask/getFormulaByProductAndVersion |
| `service/WeighingTaskService.java` | 7 方法：create/getByCode/listByWorkOrder/startWeighing/recordWeighingItem/verify/complete |
| `service/BatchRecordService.java` | 8 方法：create/getByBatchNo/listByWorkOrder/submitForReview/approve/reject/archive/addDeviation |

### 脚本

| 文件 | ID | 说明 |
|------|-----|------|
| `scripts/lims/weighing_check.js` | `lims.weighing-check.v1` | 称量防错（从 MES 迁移至 LIMS）：对比天平读数与配方量，超差时拒绝并记录偏差 |
| `scripts/lims/batch_record_generate.js` | `lims.batch-record-generate.v1` | 批记录生成：收集称量/工序/检验数据，计算收率，生成 GMP 批记录 |

### 约定规则

1. **跨模块接口** — LIMS 实体通过 core `lims/` 包中的接口暴露（`IFormula`、`IWeighingTask`、`IBatchRecord`）；其他模块通过接口引用配方/称量/批记录，无需直接依赖 LIMS 模块
2. **状态机** — `FormulaStatus`、`WeighingTaskStatus`、`BatchRecordStatus` 放 core `lims/` 包中，实现 `ILifecycle.StatusEnum`；Formula/WeighingTask/BatchRecord 继承 `BaseLifecycleEntity<S>` 获得 `transition()` 校验
3. **模型继承** — 有状态实体（Formula, WeighingTask, BatchRecord）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态明细（WeighingItem）实现 core 嵌套接口
4. **IExpand 约定** — `lims.formulaCode` / `lims.formulaVersion` / `lims.batchSize` / `lims.status` / `lims.effectiveDate` / `lims.expiryDate` 挂载在 Formula 上；`lims.weighing.formulaCode` / `lims.weighing.workOrderId` / `lims.weighing.batchNo` 挂载在 WeighingTask 上；`lims.batch.*` 挂载在 BatchRecord 上；物料级 `lims.phase` / `lims.additionOrder` / `lims.toleranceMin` / `lims.toleranceMax` 挂载在 Resource 上
5. **业务便捷方法** — Formula: `submitForApproval()` / `approve(approvedBy)` / `activate()` / `reject(reason)` / `retire(reason)` / `bumpVersion(BumpType)` / `addPhase(phase)`；WeighingTask: `startWeighing()` / `verify()` / `completeWeighing()` / `addItem(item)` + 查询 `getDeviatedItems()`；BatchRecord: `submitForReview()` / `approve(reviewedBy)` / `reject(reason)` / `archive()` + 记录添加 `addProcessRecord/addWeighingTask/addDeviation`
6. **领域事件** — 事件常量在 `LimsEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定；业务方法内部通过 `DomainEventPublisher` 发布对应事件
7. **版本管理** — Formula 使用 `SemanticVersionStrategy(BumpType.MINOR)` 进行语义版本管理（默认 0.1.0）；`bumpVersion(BumpType)` 支持 MAJOR/MINOR/PATCH 三种递增；`createNewVersion(newVersion)` = 基于当前配方克隆新版本草稿
8. **FormulaPhase 内部类** — 投料阶段作为 Formula 的静态内部类（参照 EquipmentRecipe.RecipePhase 模式），含 phaseNo/phaseName/actions/conditions
9. **WeighingItem 独立类** — 称量项目为独立类实现 `IWeighingTask.IWeighingItem`（参照 PurchaseOrderItem 模式），含 `recordWeighing()` + `getDeviationPercent()` + `isOutOfTolerance()` 三个业务方法
10. **BatchRecord 记录引用** — processRecords/weighingTasks/inspectionRecords/deviations 存储为 `List<String>`（引用键），避免与 MES/QMS 的直接对象依赖
11. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换（正常+非法+终态）、业务便捷方法、事件发布、IExpand、查询方法
12. **待实现** — FormulaService 实现类、WeighingTaskService 实现类、BatchRecordService 实现类、IoT 天平集成、REST 控制器、称量防错与 ScriptEngine 桥接

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(LimsEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `lims.formula.activated` | **MES** | 配方激活后同步更新工单可用配方 |
| `lims.formula.retired` | **MES** | 标记旧版配方不可用于新工单 |
| `lims.weighing.task_created` | **IoT** | 激活对应天平数据采集 |
| `lims.weighing.completed` | **MES, QMS, ERP** | MES 接收物料信息 / QMS 称量偏差→偏差处理 / ERP 同步物料消耗 |
| `lims.batch_record.created` | **DMS** | 批记录生成后归档至文档管理系统 |
| `lims.batch_record.approved` | **QMS** | 关闭关联偏差 |
| `lims.batch_record.archived` | **DMS** | 确认归档完成 |

---

## MPS 模块约定（2026-07-19 建立，2026-07-25 持久化完成）

### 设计定位

MPS（主生产计划）根据销售订单、预测需求和库存水平，生成中长期的主生产计划。MPS 是 MES 的上游——MPS 告诉 MES"何时生产多少什么产品"，MES 负责"如何生产"。MPS 同时是 APS 的输入来源，经过 APS 的精细排程后形成可执行的设备级分钟级计划。粗产能检查（RCCP）利用 core 的 `FactoryCapacityProfile` + `BottleneckDetector` 进行可行性校验。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `mps/IProductionPlan.java` | ⭐ 生产计划抽象 + IPlanItem 嵌套接口；供 MES/APS/ERP 编译期引用 |
| 接口 | `mps/IDemandSource.java` | ⭐ 需求来源抽象；供 ERP/SCM/APS 编译期引用 |
| 事件常量 | `event/types/MpsEventTypes.java` | ⭐ 8 个领域事件常量（与 DmsEventTypes/EquipEventTypes/IotEventTypes/PlmEventTypes/LimsEventTypes 同包 `com.byz.factory.event.types`） |

### MPS 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/ProductionPlan.java` | `BaseLifecycleEntity<ProductionPlanStatus>` | `IProductionPlan` | ⭐ JPA: @Entity @Table("mps_production_plan")；完整状态机 + 6 个业务便捷方法 + 领域事件发布 + PlanItem(@Embeddable @ElementCollection) + IExpand 文档 |
| `model/DemandSource.java` | `BaseEntity` | `IDemandSource` | ⭐ JPA: @Entity @Table("mps_demand_source")；销售订单/预测/安全库存/手工录入 + 4 个业务方法 + 3 个查询方法 |
| `model/DemandSourceType.java` | — | — | 需求来源枚举：SALES_ORDER / FORECAST / SAFETY_STOCK / MANUAL |
| `model/CapacityCheck.java` | record | — | 粗产能检查结果值对象：PASS/WARNING/FAIL + 3 个工厂方法 + isAcceptable() |

### 持久化层

| 文件 | 说明 |
|------|------|
| `repository/ProductionPlanRepository.java` | JPA Repository：findByPlanNo / findByPeriodType / findByStatus / findByPeriodTypeAndStatus / findByApprovedBy |
| `repository/DemandSourceRepository.java` | JPA Repository：findByReferenceNo / findByProductCode / findBySourceType / findByProductCodeAndSourceType |
| `ddl/V1.9__mps.sql` | PostgreSQL DDL：mps_production_plan + mps_plan_item + mps_demand_source（3 表 + 6 索引） |

### 服务层

| 文件 | 说明 |
|------|------|
| `service/MpsService.java` | 14 方法接口：计划 CRUD(4) + 生命周期(6) + 需求管理(2) + RCCP(1) |
| `service/impl/MpsServiceImpl.java` | ⭐ 完整实现：构造器注入 Repository + @Transactional + 状态变更后事件发布；checkCapacity 集成 FactoryCapacityProfile 简化产能估算 |

### 约定规则

1. **跨模块接口** — MPS 实体通过 core `mps/` 包中的接口暴露（`IProductionPlan`、`IDemandSource`）；MES 通过 IProductionPlan 获取工单来源计划，APS 通过 IProductionPlan 获取排程输入，ERP 通过 IDemandSource 提供需求数据。PlanItem 实现 IPlanItem 嵌套接口，模式与 IPurchaseOrder.IPurchaseOrderItem 一致
2. **状态机** — 使用 core 的 `ProductionPlanStatus`（实现 `ILifecycle.StatusEnum`，DRAFT→APPROVED→RELEASED→IN_PROGRESS→COMPLETED→CLOSED；驳回路径 APPROVED→DRAFT）；`transition()` 自动校验非法转换，CLOSED 为终态不可再转换
3. **模型继承** — 有状态实体（ProductionPlan）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录（DemandSource）继承 `BaseEntity` + 实现 core 接口；CapacityCheck 为不可变值 record
4. **IExpand 约定** — `mps.periodType` / `mps.periodStart` / `mps.periodEnd` / `mps.approvedBy` / `mps.totalQuantity` / `mps.itemCount` 挂载在 ProductionPlan 上；`mps.sourceType` / `mps.referenceNo` / `mps.customer` 挂载在 DemandSource 上
5. **业务便捷方法** — ProductionPlan 对外的语义化方法封装状态转换 + 事件发布，调用方不直接操作 `setStatus()`；方法：`approve(approvedBy)` / `reject()` / `release()` / `start()` / `complete()` / `close()`；DemandSource 提供 `register(sourceType, customer)` / `register(sourceType)` / `setPriority(priority)`
6. **领域事件** — 事件常量在 `MpsEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定；ProductionPlan 业务方法内部通过 `DomainEventPublisher` 发布对应事件；`mps.plan.released` 事件是 MPS→MES 的关键契约（MES 订阅后为每个 PlanItem 生成工单）
7. **粗产能检查（RCCP）** — `checkCapacity` 应利用 core 的 `FactoryCapacityProfile`（工厂产能画像）和 `BottleneckDetector`（瓶颈识别）进行计算；CapacityCheck 为结果值对象，提供 `pass/warning/fail` 三个工厂方法和 `isAcceptable()` 查询
8. **PlanItem 模式** — PlanItem 为 ProductionPlan 的静态内部类（@Data + 全参构造），实现 IPlanItem 接口；使用 Lombok @Data 生成 getter（非 record 的 component 访问器），确保与 IPlanItem 接口的 getter 方法签名匹配
9. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；65 个单元测试覆盖构造、状态转换（正常+驳回+非法+终态+全生命周期）、业务便捷方法、PlanItem 增删、查询方法、DemandSource 注册/查询、枚举、CapacityCheck、事件命名约定、IExpand；26 个集成测试（H2 + @DataJpaTest）覆盖 JPA 映射、Repository 查询、Service 完整生命周期和异常路径
10. **持久化** — ✅ ProductionPlan/DemandSource 添加 @Entity + @Table + @Column JPA 注解；PlanItem 使用 @Embeddable + @ElementCollection；Repository extends JpaRepository；构造器注入；MpsTestConfig 提供测试 Spring 上下文
11. **待实现** — Rest 控制器、BottleneckDetector 深度集成（当前为简化产能估算）、需求优先级自动排序算法

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(MpsEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `mps.plan.released` | **MES** | 已发布计划触发生成工单 |
| `mps.plan.created` / `.approved` / `.released` | **APS** | 更新排程输入数据 |
| `mps.plan.started` / `.completed` / `.closed` | **BI** | 生产计划执行看板 |
| `mps.demand.registered` | **APS, BI** | 需求变更影响排程和统计 |
| `mps.capacity.checked` | **APS, BI** | 产能分析结果供排程优化和看板展示 |

---

## WMS 模块约定（2026-07-19 建立）

### 设计定位

WMS 管理物料/成品的仓储运作——收货、上架、拣料、发运、线边仓、库存盘点。WMS 是 MES 和 LIMS 的物料来源——没有仓储，投料和称量在真空中操作。向上为 MES（物料配送）、LIMS（称量批次）、QMS（来料检验）、ERP（库存过账）、SCM（采购收货）提供仓储数据支撑。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `wms/IStorage.java` | ⭐ 库位抽象：编码+仓库+区域+货架+层+位+存储类型+容量；供 MES/SCM 引用 |
| 接口 | `wms/IReceipt.java` | ⭐ 收货单抽象 + IReceiptItem 内嵌接口；供 SCM/QMS 引用 |
| 接口 | `wms/IPickingTask.java` | ⭐ 拣料任务抽象 + IPickingTaskItem 内嵌接口；供 MES/LIMS 引用 |
| 接口 | `wms/IInventorySnapshot.java` | ⭐ 库存快照抽象：在手/已分配/可用/待检/不合格+有效期；供 ERP/MES 引用 |
| 状态枚举 | `wms/PickingTaskStatus.java` | PENDING→IN_PROGRESS→PICKED→DELIVERED；+CANCELLED；实现 ILifecycle.StatusEnum |
| 值枚举 | `wms/StorageType.java` | AMBIENT / COLD / FROZEN / HAZARDOUS |
| 值枚举 | `wms/PickingType.java` | FULL / STAGED / JIT |
| 值枚举 | `wms/MaterialStatus.java` | QUARANTINE / RELEASED / REJECTED（用于 ReceiptItem 和库存物料质量状态） |

### WMS 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Storage.java` | `BaseEntity` | `IStorage` | 库位实体：层次结构(仓库→区域→货架→层→位) + StorageType + 容量 + IExpand 文档 |
| `model/Receipt.java` | `BaseLifecycleEntity<ReceiptStatus>` | `IReceipt` | 收货单：完整状态机 + 5 业务便捷方法(receive/complete/close/addItem/acceptItem/rejectItem) + ReceiptItem 内部类 + IExpand 文档 |
| `model/PickingTask.java` | `BaseLifecycleEntity<PickingTaskStatus>` | `IPickingTask` | 拣料任务：完整状态机 + 5 业务便捷方法(start/completePicking/deliver/cancel/addItem/pickItem) + PickingTaskItem 内部类 + IExpand 文档 |
| `model/InventorySnapshot.java` | `BaseEntity` | `IInventorySnapshot` | 库存快照：8 业务方法(addStock/removeStock/allocate/deallocate/quarantine/release/reject/count) + 可用量动态计算 + 负数防护 + IExpand 文档 |

### 服务接口

| 文件 | 说明 |
|------|------|
| `service/WmsService.java` | 20 方法：收货管理(createReceipt/acceptAndPutaway/completeReceipt/closeReceipt/findReceipt) + 拣料管理(createPickingTask/startPicking/completePicking/deliverPicking/cancelPicking/findPickingTask) + 库存管理(queryAvailableStock/getInventorySnapshot/getInventorySnapshots/allocateStock/deallocateStock) + 盘点管理(createCountTask) |
| `service/impl/WmsServiceImpl.java` | `@Service @Transactional` 构造器注入 Repository：读写操作 + DomainEventPublisher 发布领域事件；（20 个 Service 方法全部实现） |

### Repository

| 文件 | 说明 |
|------|------|
| `repository/StorageRepository.java` | findByCode / findByWarehouse / findByWarehouseAndZone / findByStorageType |
| `repository/ReceiptRepository.java` | findByCode / findByReferenceNo / findBySupplierCode / findByStatus |
| `repository/PickingTaskRepository.java` | findByCode / findByWorkOrderNo / findByStatus / findByWorkOrderNoAndStatus |
| `repository/InventorySnapshotRepository.java` | findByCode / findByMaterialCodeAndBatchNoAndLocationCode / findByMaterialCodeAndBatchNo / findByMaterialCode / findByLocationCode / findByStatus / sumAvailableStock(@Query) |

### DDL

| 文件 | 表数 | 说明 |
|------|------|------|
| `ddl/V1.8__wms.sql` | 6 | wms_storage / wms_receipt + wms_receipt_item / wms_picking_task + wms_picking_task_item / wms_inventory_snapshot |

### 约定规则

1. **跨模块接口** — WMS 实体通过 core `wms/` 包中的接口暴露（`IStorage`、`IReceipt`、`IPickingTask`、`IInventorySnapshot`）；其他模块通过接口引用仓储数据，无需直接依赖 WMS 模块
2. **状态机** — `ReceiptStatus`（已有）、`PickingTaskStatus`（新增）放 core `wms/` 包中，实现 `ILifecycle.StatusEnum`；Receipt/PickingTask 继承 `BaseLifecycleEntity<S>` 获得 `transition()` 校验
3. **模型继承** — 有状态实体（Receipt, PickingTask）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录（Storage, InventorySnapshot）继承 `BaseEntity` + 实现 core 接口
4. **JPA 映射** — Model 直接加 `@Entity @Table @Column @Enumerated`，不新建单独 Entity 类；Receipt.items 用 `@OneToMany(cascade=ALL, orphanRemoval=true)` + `@JoinColumn(name="receipt_id")`；PickingTask.items 用 `@OneToMany(cascade=ALL, orphanRemoval=true)` + `@JoinColumn(name="task_id")`；ReceiptItem/PickingTaskItem 为 `@Entity` 内部类 + `@NoArgsConstructor`
5. **IExpand 约定** — `wms.*` 前缀挂载在对应模型上
6. **业务便捷方法** — Receipt: receive/complete/close/addItem/acceptItem/rejectItem/isFullyProcessed；PickingTask: start/completePicking/deliver/cancel/addItem/pickItem/isFullyPicked；InventorySnapshot: addStock/removeStock/allocate/deallocate/quarantine/release/reject/count + 负数防护
7. **Service 实现** — `@Service @Transactional` 构造器注入 Repository；读方法 `@Transactional(readOnly = true)`；状态变更后 `DomainEventPublisher.publish(IDomainEvent.of(...))`
8. **Storage 无生命周期** — 库位不需要状态机，不实现 ILifecycle；locationCode 映射到继承的 code
9. **InventorySnapshot 无生命周期** — 库存快照是时点数据记录；availableQty 由 @Transient getter 动态计算
10. **ReceiptItem/PickingTaskItem 模式** — 明细项为静态内部 @Entity 类（@Data + 无参/全参构造），实现对应的嵌套接口
11. **sourceType 保留 String** — 对接不同外部系统时灵活，不限定枚举值
12. **测试规范** — Given-When-Then + `@DisplayName` 中文描述；33 个测试覆盖构造、状态转换、业务方法、库存操作、可用量计算、负数防护、接口契约、枚举验证
13. **待实现** — REST 控制器、条码/RFID 扫码（IoT 集成）、库存事务回传 ERP、批次策略引擎（FIFO/FEFO/LEFO）、线边仓管理

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(WmsEventTypes.XXX, handler)` 订阅。事件常量待后续阶段创建。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `wms.receipt.created` | **QMS, SCM** | QMS 创建来料检任务 / SCM 更新 PO 收货状态 |
| `wms.receipt.completed` | **ERP, SCM** | ERP 库存过账 / SCM 更新 PO 完成状态 |
| `wms.receipt.closed` | **ERP** | ERP 应收对账 |
| `wms.item.accepted` | **QMS** | 来料检放行 → 库存可用 |
| `wms.item.rejected` | **SCM** | 通知供应商退货 |
| `wms.picking.created` | **MES, LIMS** | MES 追踪物料配送进度 / LIMS 准备称量 |
| `wms.picking.delivered` | **MES, LIMS** | MES 确认投料就绪 / LIMS 开始称量 |
| `wms.inventory.changed` | **ERP, MES** | ERP 同步库存 / MES 更新物料可用性 |

---

## MES 模块约定（2026-07-19 建立）

### 设计定位

MES（制造执行系统）是 Phase 3 的**集成枢纽**——基于 core 的工序/动作模型，实现工单管理、工序流转、动作级报工和生产追溯。MES 是事件的主要生产者（工单下达→APS排程、工序中断→QMS检验、工序开始→Equip状态更新），通过领域事件与各模块松耦合协作。

### 设计裁定（design-decisions.md §2.1-2.4）

| 裁定 | 条款 | 执行 |
|------|------|------|
| 工单与蓝图 | §2.1 | 运行时按版本号解析 + 工单发布时冻结 blueprintVersion |
| 报工粒度 | §2.2 | 动作级——每次 Action 执行生成 ActionRecord |
| 并行工序 | §2.3 | ExecutionMode.PARALLEL 标记 |
| QMS 中断 | §2.4 | Control.Interrupt + 领域事件双向通信 |

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `batch/ProcessStatus.java` | PENDING→IN_PROGRESS→COMPLETED / IN_PROGRESS→INTERRUPTED→IN_PROGRESS(恢复)；+SKIPPED+CANCELLED；实现 ILifecycle.StatusEnum（放 batch/ 包，与 WorkOrderStatus 同包） |
| 事件常量 | `event/types/MesEventTypes.java` | ⭐ 10 个领域事件常量（与 PlmEventTypes/EquipEventTypes 等同包 `com.byz.factory.event.types`） |

### MES 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/MesWorkOrder.java` | `BaseLifecycleEntity<WorkOrderStatus>` | `IWorkOrder` | ⭐ 工单核心实体：完整状态机 + 6 个业务便捷方法(release/start/complete/close/cancel) + 蓝图版本冻结(design-decisions.md §2.1) + 配方/设备配方关联 + IExpand 文档 |
| `model/ProcessRecord.java` | `BaseLifecycleEntity<ProcessStatus>` | — | 工序记录：完整状态机 + 6 个业务便捷方法(start/interrupt/resume/complete/skip/cancel) + ActionRecord 列表管理 + QMS 中断支持 |
| `model/ActionRecord.java` | `BaseEntity` | `ITraceable` | ⭐ 动作记录（设计裁定 §2.2 动作级报工）：before/after 快照 + 追溯字段 + 无独立状态机(生命周期由 ProcessRecord 驱动) |

### 服务接口

| 文件 | 说明 |
|------|------|
| `service/WorkOrderService.java` | 11 方法：工单生命周期(create/release/start/complete/close) + 工序流转(executeProcess/reportAction) + QMS中断恢复(resumeProcess) + 查询(findByWorkOrderNo/findProcessRecords/findActionRecords) |

### 约定规则

1. **跨模块接口** — MES 核心实体通过 core `batch/` 包中已有接口暴露（IBatch/ITraceable/IWorkOrder 放 batch/；ProcessStatus 放 batch/）（`IWorkOrder`）；ProcessRecord 为 MES 内部模型（其他模块通过事件感知工序状态变更）；ActionRecord 实现 `ITraceable` 供 LIMS/QMS 追溯
2. **状态机** — `WorkOrderStatus`（已有）放 core `batch/`；`ProcessStatus`（新增）放 core `batch/`；均实现 `ILifecycle.StatusEnum`；MesWorkOrder/ProcessRecord 继承 `BaseLifecycleEntity<S>` 获得 `transition()` 校验
3. **模型继承** — 有状态实体（MesWorkOrder, ProcessRecord）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录（ActionRecord）继承 `BaseEntity` + 实现 `ITraceable`
4. **IExpand 约定** — `mes.workOrderNo` / `mes.productCode` / `mes.blueprintVersion` / `mes.formulaCode` / `mes.recipeCode` / `mes.operator` / `mes.batchNo` 挂载在 MesWorkOrder 上；`mes.processRecord.workOrderNo` / `mes.processRecord.processCode` / `mes.processRecord.operator` / `mes.processRecord.interruptedBy` / `mes.processRecord.interruptReason` 挂载在 ProcessRecord 上；`mes.actionRecord.processRecordId` / `mes.actionRecord.actionCode` / `mes.actionRecord.workOrderNo` / `mes.actionRecord.batchNo` / `mes.actionRecord.operator` / `mes.actionRecord.result` 挂载在 ActionRecord 上
5. **业务便捷方法** — MesWorkOrder: `release(blueprint,operator,plannedStart,plannedEnd,factoryCode)` / `start()` / `complete()` / `close()` / `cancel()` / `isBlueprintVersionFrozen()`；ProcessRecord: `start(operator)` / `interrupt(by,reason)` / `resume()` / `complete()` / `skip()` / `cancel()` / `addAction(ar)`；ActionRecord: `complete(result,beforeSnapshot,afterSnapshot,remark)` / `recordSnapshot(before,after)`
6. **领域事件** — 事件常量在 `MesEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定（10 个事件：工单 5 + 工序 4 + 动作 1）；**模型层不发布事件**（design-decisions.md §1.1），由 Service 实现类统一负责
7. **蓝图版本冻结** — 工单下达时冻结 Blueprint 版本号到 `blueprintVersion` 字段（design-decisions.md §2.1）；运行时按 `blueprintCode + blueprintVersion` 从 PLM 获取 Blueprint；蓝图后续变更不影响已发布工单
8. **动作级报工** — 每次 Action 执行后生成一条 `ActionRecord`，实现 `ITraceable` 的 before/after 快照（design-decisions.md §2.2）；ProcessRecord 是工序级聚合视图
9. **QMS 中断流程** — MES 执行到检验动作 → `Control.Interrupt` → 暂停工序（IN_PROGRESS→INTERRUPTED）→ 发布 `mes.process.interrupted` → QMS 订阅创建 InspectionOrder → QMS 判定后回调 `resumeProcess()` 恢复（design-decisions.md §2.4）；预留超时自动升级为 Andon 异常
10. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖 MesWorkOrder 构造+状态转换+蓝图版本冻结+非法转换、ProcessRecord 状态流转+QMS中断+设备故障中断、ActionRecord 与 ITraceable 一致性、ProcessStatus 枚举定义、MesEventTypes 事件常量命名约定、完整链路综合场景
11. **已完成** — WorkOrderServiceImpl（内存存储，事件发布，蓝图版本冻结，动作级报工，QMS中断/恢复，interruptProcess钩子）+ BlueprintProvider 解耦接口 + WorkOrderServiceImplTest（28个测试）
12. **待实现** — 工序流转引擎（ExecutionMode.PARALLEL 并发支持）、REST 控制器、与 PLM/Equip/WMS/LIMS 的集成适配器、Andon 超时升级、数据库持久化替代内存存储

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(MesEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `mes.workorder.created` | **BI** | 工单创建统计 |
| `mes.workorder.released` | **APS, WMS** | APS 触发排程 / WMS 触发拣料任务 |
| `mes.workorder.started` | **Equip, LIMS** | Equip 更新设备状态 / LIMS 准备配方称量 |
| `mes.workorder.completed` | **BI, ERP** | BI 生产看板 / ERP 成品入库 |
| `mes.workorder.closed` | **BI** | BI 工单关闭统计 |
| `mes.process.started` | **Equip, QMS** | Equip 更新设备占用 / QMS 按需创建检验 |
| `mes.process.completed` | **BI** | BI 工序进度看板 |
| `mes.process.interrupted` | **QMS, Andon** | QMS 创建检验单 / Andon 超时升级 |
| `mes.process.resumed` | **QMS, Andon** | QMS 关闭检验 / Andon 取消呼叫 |
| `mes.action.completed` | **LIMS** | 批记录实时写入（边做边记） |

---

## APS 模块约定（2026-07-19 建立）

### 设计定位

APS（高级排程系统）在 MPS 确定"生产什么、生产多少、何时交付"之后，负责"在哪个设备上、按什么顺序、什么时间段"来执行。APS 的输出直接驱动 MES 的工单下发，是计划→执行的最后一环。

### 设计裁定（design-decisions.md §2.5-2.6, §4.11）

| 裁定 | 条款 | 执行 |
|------|------|------|
| 排程算法 | §2.5 | 规则式起步（EDD/SPT/CR），预留 IOptimizationEngine 接口 |
| 重排程触发 | §2.6 | 事件驱动增量（订阅 mes.workorder.released / equip.fault.reported / scm.receipt.delayed） |
| 约束类型 | §4.11 | 设备容量超限→硬约束；人员资质不匹配→硬约束；交期延误→软约束 |

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 事件常量 | `event/types/ApsEventTypes.java` | ⭐ 5 个领域事件常量（与 PlmEventTypes/MesEventTypes/MpsEventTypes 等同包 `com.byz.factory.event.types`） |
| 接口 | `operation/common/IOptimizationEngine.java` | ⭐ 优化引擎抽象接口 — 规则式实现（EDD/SPT/CR）和约束求解（OR-Tools/OptaPlanner）的统一插拔点；含 ruleBased() 工厂方法 |

### APS 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Schedule.java` | `BaseLifecycleEntity<ScheduleStatus>` | — | 排程方案核心实体：完整状态机 + 6 个业务便捷方法(optimize/dispatch/start/complete/cancel/addTask) + ScheduledTask 静态内部类 + IExpand 文档 |
| `model/Schedule.ScheduledTask` | — | — | 排程任务内嵌类：工单号+工序+设备+人员+起止时间+工时+前置依赖+TaskStatus |
| `model/ResourceCalendar.java` | `BaseEntity` | — | 资源日历：可用窗口+产能+canFit/overlapMinutes/getAvailableMinutes |
| `model/RescheduleTrigger.java` | `BaseEntity` | — | 重排程触发记录：scheduleCode+triggerEvent+processed+markProcessed() |
| `model/TaskStatus.java` | — | — | 任务状态枚举：SCHEDULED/DISPATCHED/IN_PROGRESS/COMPLETED/CANCELLED |
| `model/ResourceType.java` | — | — | 资源类型枚举：MACHINE/PERSONNEL（映射自 Dict.SourceGroup 子集） |
| `service/SchedulingRule.java` | — | — | 排程规则引擎：EDD/SPT/CR 三种 Comparator + of(strategy) 工厂方法 + sort() 就地排序 |
| `service/impl/ApsServiceImpl.java` | — | `ApsService` | ⭐ 排程服务实现：ConcurrentHashMap 内存存储 + IDomainEvent 事件发布 + 排程编号自增 + query-only getSchedule；createSchedule/optimize/dispatch/reschedule/getSchedule 全部实现 |

### 服务接口

| 文件 | 说明 |
|------|------|
| `service/ApsService.java` | 5 方法：createSchedule(planNo,factoryCode) / optimize(scheduleCode,strategy) / dispatch(scheduleCode) / reschedule(scheduleCode,triggerEvent) / getSchedule(scheduleCode) |

### 约定规则

1. **跨模块接口** — APS 的核心状态枚举（ScheduleStatus）放 core `batch/` 包中（批次追溯域统一管理）；事件常量（ApsEventTypes）放 core `event/types/` 包中；Schedule 模型不暴露 core 接口（其他模块通过事件感知排程变更，不做编译期引用）
2. **状态机** — 使用 core 的 `ScheduleStatus`（实现 `ILifecycle.StatusEnum`，DRAFT→OPTIMIZED→DISPATCHED→IN_PROGRESS→COMPLETED；任意非终态→CANCELLED）；`transition()` 自动校验非法转换
3. **模型继承** — 有状态实体（Schedule）继承 `BaseLifecycleEntity<ScheduleStatus>`；无状态记录（ResourceCalendar, RescheduleTrigger）继承 `BaseEntity`
4. **IExpand 约定** — `aps.planNo` / `aps.strategy` / `aps.optimizationGoal` / `aps.scheduledDate` / `aps.horizonStart` / `aps.horizonEnd` 挂载在 Schedule 上；`aps.resourceCode` / `aps.resourceType` / `aps.availableFrom` / `aps.availableTo` 挂载在 ResourceCalendar 上
5. **业务便捷方法** — Schedule: `optimize(strategy)` / `dispatch()` / `start()` / `complete()` / `cancel()` / `addTask(task)`；ResourceCalendar: `canFit(start,end)` / `overlapMinutes(start,end)` / `getAvailableMinutes()` / `getCapacityMinutes()`；RescheduleTrigger: `markProcessed()`
6. **领域事件** — 事件常量在 `ApsEventTypes` 中统一管理，遵循 `{module}.{entity}.{past_tense}` 命名约定；**模型层不发布事件**（design-decisions.md §1.1），由 Service 实现类统一负责
7. **排程规则引擎** — `SchedulingRule` 实现 EDD（最早交期优先）、SPT（最短工时优先）、CR（关键比率优先）三种经典规则；每种规则返回 Comparator<ScheduledTask>，规则可组合；预留 IOptimizationEngine 接口供后续接入 OR-Tools/OptaPlanner（design-decisions.md §2.5）
8. **ScheduledTask 内嵌类** — 排程任务作为 Schedule 的静态内部类（@Data），含全参构造和无参构造；predecessors 存储前置任务编码列表；status 使用 TaskStatus 枚举；getTotalDurationMin() 返回换型+加工总分钟数
9. **ResourceCalendar 产能约束** — canFit() 检查任务时间是否在可用窗口内（硬约束）；overlapMinutes() 计算重叠时间；getCapacityMinutes() 返回产能分钟数（capacityHours × 60）
10. **重排程触发** — RescheduleTrigger 记录事件驱动的重排程触发（mes.workorder.released / equip.fault.reported / scm.receipt.delayed）；全量重排程由计划员手动触发（API 调用），不走此记录（design-decisions.md §2.6）
11. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；覆盖构造、状态转换（正常+非法+终态+全生命周期+取消路径）、TaskStatus/ResourceType 枚举、ScheduledTask 构造+getTotalDurationMin、ResourceCalendar 可用时间计算+canFit+overlapMinutes、RescheduleTrigger 构造+markProcessed、排程规则 EDD/SPT/CR 正确性、ApsEventTypes 事件命名约定+PREFIX、ApsService 接口契约、ScheduleStatus 枚举覆盖、综合集成场景
12. **待实现** — OR-Tools/OptaPlanner 约束求解引擎接入（IOptimizationEngine 已预留）、MPS→APS 计划接收集成、APS→MES 下发集成、资源日历数据库持久化、甘特图数据输出、REST 控制器
13. **服务实现** — ApsServiceImpl 采用 ConcurrentHashMap 内存存储（Phase 3 "先内存实现再持久化"原则）；排程编号自动生成（SCH-yyMMdd-xxxx）；dispatch 时自动计算排程范围（horizonStart/horizonEnd）；reschedule 创建 RescheduleTrigger 记录并发布事件后标记 processed；服务层统一负责事件发布（不内嵌 Model）

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(ApsEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `aps.plan.received` | **MPS** | 确认 MPS 计划已被 APS 接收 |
| `aps.schedule.created` | **MES, BI** | MES 预知排程创建 / BI 排程统计 |
| `aps.schedule.released` | **MES, Equip** | MES 根据排程任务下发工单 / Equip 锁定设备占用时段 |
| `aps.task.delayed` | **BI, Andon** | BI 交期延误统计 / Andon 异常预警 |
| `aps.reschedule.triggered` | **MES, BI** | MES 同步更新工单计划 / BI 排程变更统计 |

---

## QMS 模块约定（2026-07-24 建立）

### 设计定位

QMS（质量管理系统）基于 core 的工序/动作模型，在制造过程中内嵌质量管理。核心功能：检验工序管理、SPC 统计过程控制、偏差/CAPA 处理、质量门禁。设计理念：**质量长在工序里，不是贴在产品上。**

### 设计裁定（design-decisions.md §3.1-3.3）

| 裁定 | 条款 | 执行 |
|------|------|------|
| 检验方案存储 | §3.1 | 结构化字段为主，IExpand 为辅 |
| SPC 控制限 | §3.2 | 双模式：固定值（IProcessParameter）+ 自动计算（IoT 历史数据） |
| 偏差处理流程 | §3.3 | 自动暂停 + 人工介入（QA 判定） |

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `batch/IInspectionRecord.java` | 检验记录抽象，供 MES/LIMS/DMS 引用 |
| 接口 | `batch/IDeviation.java` | 偏差抽象，供 MES/Andon/DMS/EAM 引用 |
| 接口 | `batch/ICapa.java` | CAPA 抽象，供 DMS/Andon/MES 引用 |
| 值枚举 | `batch/InspectionType.java` | IQC / IPQC / FQC / OQC |
| 值枚举 | `batch/DeviationSeverity.java` | MINOR / MAJOR / CRITICAL |
| 值枚举 | `batch/DeviationDisposition.java` | REWORK / CONCESSION / REJECT |
| 状态枚举 | `batch/DeviationStatus.java` | OPEN→INVESTIGATING→DISPOSITIONED→RESOLVED→CLOSED; +CANCELLED |
| 状态枚举 | `batch/CapaStatus.java` | OPEN→ROOT_CAUSE→IN_PROGRESS→VERIFIED→CLOSED; +CANCELLED |
| 事件常量 | `event/types/QmsEventTypes.java` | ⭐ 18 个领域事件常量（与 PlmEventTypes/EquipEventTypes 等同包） |

### QMS 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/InspectionOrder.java` | `BaseLifecycleEntity<InspectionStatus>` | `IInspectionOrder` | ⭐ 检验指令核心：完整状态机(PENDING→IN_PROGRESS→PASSED/FAILED→CLOSED) + 5 业务便捷方法 + 领域事件发布 |
| `model/InspectionPlan.java` | `BaseEntity` | — | 检验方案：结构化字段 + InspectionItem 内部类 + 版本管理 |
| `model/InspectionRecord.java` | `BaseEntity` | `IInspectionRecord` | 检验记录：自动判定(record) + 让步接收(concession) |
| `model/Deviation.java` | `BaseLifecycleEntity<DeviationStatus>` | `IDeviation` | ⭐ 偏差核心：完整状态机 + 8 业务便捷方法 + requiresCapa() 智能判定 |
| `model/Capa.java` | `BaseLifecycleEntity<CapaStatus>` | `ICapa` | ⭐ CAPA 核心：完整状态机 + 5 业务便捷方法 + isOverdue() 逾期判定 |

### 服务接口

| 文件 | 说明 |
|------|------|
| `service/InspectionService.java` | 12 方法：方案管理(createPlan/findPlansByProduct/findPlanByProductAndProcess/getPlan) + 检验指令(createOrder/startInspection/submitResult/completeInspection/closeOrder/findOrder/findOrdersByWorkOrder) + 记录查询(getRecords) |
| `service/DeviationService.java` | 13 方法：偏差全生命周期(createDeviation/startInvestigation/completeInvestigation/dispose/linkCapa/resolve/close/cancel) + 查询(findDeviation/findDeviationsByInspection/findDeviationsByWorkOrder/getActiveDeviations) |
| `service/CapaService.java` | 10 方法：CAPA 全生命周期(createCapa/analyzeRootCause/executeActions/verify/close/cancel) + 查询(findCapa/findCapasByDeviation/getOverdueCapas/getActiveCapas) |

### 约定规则

1. **跨模块接口** — QMS 实体通过 core `batch/` 包中的接口暴露（`IInspectionOrder`（已有）、`IInspectionRecord`、`IDeviation`、`ICapa`）；其他模块通过接口引用检验/偏差/CAPA，无需直接依赖 QMS
2. **状态机** — `InspectionStatus`（已有）、`DeviationStatus`、`CapaStatus` 放 core `batch/` 包中，实现 `ILifecycle.StatusEnum`；InspectionOrder/Deviation/Capa 继承 `BaseLifecycleEntity<S>` 获得 `transition()` 校验
3. **模型继承** — 有状态实体（InspectionOrder, Deviation, Capa）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录（InspectionPlan, InspectionRecord）继承 `BaseEntity` + 实现 core 接口
4. **IExpand 约定** — `qms.inspectionType` / `qms.aql` / `qms.specLimits` 挂载在 InspectionOrder 上；`qms.deviation.source` / `qms.deviation.capaCode` 挂载在 Deviation 上；`qms.capa.problemDescription` / `qms.capa.verificationResult` 挂载在 Capa 上
5. **业务便捷方法** — InspectionOrder: `startInspection(inspector)` / `submitResult(record)` / `completeInspection()` / `close()` + 查询 `isFinished()` / `isPassed()`；Deviation: `startInvestigation()` / `completeInvestigation()` / `dispose()` / `linkCapa()` / `resolve()` / `close()` / `cancel()` + 查询 `requiresCapa()`；Capa: `analyzeRootCause()` / `executeActions()` / `verify()` / `close()` / `cancel()` + 查询 `isOverdue()`
6. **领域事件** — 事件常量在 `QmsEventTypes` 中统一管理（18 个）：检验 5 + 偏差 5 + CAPA 5 + SPC 3；遵循 `{module}.{entity}.{past_tense}` 命名约定；**模型层发布事件**（参照 Phase 1-2 既有模式），Service 实现层统一调度
7. **偏差处理流程** — MES 自动暂停 → QMS 自动创建 Deviation → **人工判定**（QA 决定 REWORK/CONCESSION/REJECT）→ CAPA（如需）→ 通知 MES 恢复（design-decisions.md §3.3）
8. **CAPA 触发规则** — CRITICAL 偏差必须 CAPA；MAJOR 偏差非让步需 CAPA；MINOR 偏差不需 CAPA（`requiresCapa()` 方法封装此逻辑）
9. **品质门禁** — 脚本 `qms.quality-gate.v1` 实现判定逻辑（放行/让步/拒收），FAIL 时通过 `qms.createDeviation()` 创建偏差并抛出异常中断工序
10. **SPC 控制** — 脚本 `qms.spc-check.v1` 实现 Western Electric 4 条规则（3σ 超限/连续9点同侧/连续6点趋势/连续14点交替），控制限支持固定值和自动计算两种模式（design-decisions.md §3.2）
11. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；89 个测试覆盖：构造、状态转换（正常+驳回+非法+终态+完整生命周期+取消路径）、业务便捷方法、事件发布/负载验证、跨模块接口实现验证、IExpand、枚举定义（值枚举+状态枚举转换规则）、事件命名约定、同状态幂等、BigDecimal 比较精度、Repository 持久化集成、IQualityAction 桥接
12. **持久化** — JPA 注解已添加（@Entity/@Table/@Column/@Enumerated/@Transient）；4 个 Repository 接口（InspectionOrder/InspectionPlan/Deviation/Capa）；3 个 Service 实现类（构造器注入 Repository + @Transactional）；DDL 脚本 V1.10__qms.sql（6 张表）
13. **IQualityAction 桥接** — QualityAction 实现 IQualityAction，提供 4 个工厂方法（ipqc/gate/fqc/iqc），支持质检动作与 MES 工序引擎集成
14. **待实现** — SPC 实时计算引擎、IoT 数据订阅消费、REST 控制器

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(QmsEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `qms.inspection.created` | **IoT** | 激活检验用仪器数据采集 |
| `qms.inspection.passed` | **MES** | 检验通过后恢复工序流转 |
| `qms.inspection.failed` | **MES** | 检验失败后暂停工序，触发偏差 |
| `qms.inspection.completed` | **LIMS, BI** | LIMS 更新批记录 / BI 质量看板 |
| `qms.deviation.created` | **Andon** | 偏差创建时触发安灯呼叫 |
| `qms.deviation.dispositioned` | **DMS** | 触发处置审批流文档 |
| `qms.deviation.resolved` | **MES** | 偏差解决后恢复生产 |
| `qms.capa.created` | **DMS** | CAPA 创建后触发审批流 |
| `qms.capa.verified` | **DMS** | CAPA 验证完成后归档 |
| `qms.capa.closed` | **BI** | 更新质量看板统计 |
| `qms.spc.warning` | **Andon** | SPC 预警通知 |
| `qms.spc.out_of_control` | **Andon, MES** | SPC 失控触发安灯 + 暂停工序 |

---

## Andon 模块约定（2026-07-25 建立）

### 设计定位

Andon（安灯系统）是**跨模块异常响应中枢**——接收来自 MES（工序中断）、IoT（设备报警）、QMS（SPC 失控/偏差创建）的多源事件，触发安灯呼叫并执行逐级上报。Andon 是独立模块（design-decisions.md §3.4），通过领域事件与其他模块松耦合。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 状态枚举 | `batch/AndonStatus.java` | OPEN→ACKNOWLEDGED→RESOLVED\|ESCALATED→CLOSED；OPEN 可直接 ESCALATED（紧急情况） |
| 事件常量 | `event/types/AndonEventTypes.java` | ⭐ 7 个领域事件常量（5 呼叫生命周期 + 2 规则管理；与 QmsEventTypes/MesEventTypes 等同包 `com.byz.factory.event.types`） |

### Andon 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/AndonCall.java` | `BaseLifecycleEntity<AndonStatus>` | — | ⭐ 安灯呼叫核心实体：完整状态机 + 5 业务便捷方法 + 领域事件发布 + IExpand 文档 |
| `model/EscalationRule.java` | `BaseEntity` | — | 上报规则：多级 EscalationLevel 上报链 + AutoStop 策略 + 启用/禁用 |
| `model/AndonDashboard.java` | — (record) | — | 看板数据值对象：按严重程度/触发类型分组的活跃呼叫统计 |

### 值枚举（模块内）

| 文件 | 说明 |
|------|------|
| `model/TriggerType.java` | EQUIPMENT_FAULT / QUALITY_ISSUE / MATERIAL_SHORTAGE / SAFETY_INCIDENT / PROCESS_DELAY / OTHER |
| `model/AndonSeverity.java` | INFO / WARNING / CRITICAL / EMERGENCY |
| `model/AndonSource.java` | MANUAL / AUTO |
| `EscalationRule.AutoStop` | NONE / PAUSE_PROCESS / STOP_LINE / STOP_FACTORY（内部枚举） |
| `EscalationRule.EscalateCondition` | TIMEOUT / NO_RESPONSE（内部枚举） |

### 服务接口/实现

| 文件 | 说明 |
|------|------|
| `service/AndonService.java` | 19 方法：呼叫管理(trigger/acknowledge/escalate/resolve/close) + 呼叫查询(findCall/getActiveCalls/findCallsByWorkOrder/findCallsByEquipment/getUrgentCalls/getDashboard) + 规则管理(createRule/addEscalationLevel/findRule/getActiveRules/disableRule/enableRule) |
| `service/impl/AndonServiceImpl.java` | ⭐ Service 实现：构造器注入 AndonCallRepository + EscalationRuleRepository；写操作 @Transactional + DomainEventPublisher.publish()；读操作 @Transactional(readOnly=true) |

### Repository

| 文件 | 说明 |
|------|------|
| `repository/AndonCallRepository.java` | extends JpaRepository<AndonCall, Long>：findByCode / findByWorkOrderNo / findByEquipmentCode / findByStatus / findByTriggerType |
| `repository/EscalationRuleRepository.java` | extends JpaRepository<EscalationRule, Long>：findByCode / findByTriggerTypeAndSeverity / findByEnabledTrue |

### DDL

| 文件 | 说明 |
|------|------|
| `ddl/V1.11__andon.sql` | andon_call 主表（16业务列+6索引）+ andon_escalation_rule 主表（5业务列+2索引） |

### 约定规则

1. **跨模块接口** — Andon 通过事件订阅其他模块（MES/IoT/QMS/APS），不暴露 core 接口供其他模块编译期引用；其他模块通过 `AndonService` 方法调用或订阅 `AndonEventTypes` 事件协作
2. **状态机** — 使用 core 的 `AndonStatus`（实现 `ILifecycle.StatusEnum`）；AndonCall 继承 `BaseLifecycleEntity<AndonStatus>` 获得 `transition()` 校验；OPEN 可直接 ESCALATED（紧急安全事件场景）；CLOSED 终态不可转换
3. **模型继承** — 有状态实体（AndonCall）继承 `BaseLifecycleEntity<S>`；无状态记录（EscalationRule）继承 `BaseEntity`；AndonDashboard 为不可变值 record
4. **IExpand 约定** — `andon.call.escalationLevel` / `andon.call.escalatedAt` / `andon.call.acknowledgedBy` / `andon.call.acknowledgedAt` / `andon.call.resolution` / `andon.call.closedAt` 挂载在 AndonCall 上；`andon.rule.autoStop` / `andon.rule.enabled` / `andon.rule.createdBy` 挂载在 EscalationRule 上
5. **业务便捷方法** — AndonCall: `acknowledge(acknowledgedBy)` / `escalate(reason)` / `resolve(resolution)` / `close()` + 查询 `isActive()` / `isUrgent()` / `isAutoTriggered()` / `getDurationSeconds()` + 关联 `linkWorkOrder()` / `linkProcess()` / `linkEquipment()`；EscalationRule: `addLevel()` / `getLevel(n)` / `getMaxLevel()` / `enable()` / `disable()` / `setAutoStopPolicy()`
6. **领域事件** — 事件常量在 `AndonEventTypes` 中统一管理（7 个：5 呼叫 + 2 规则）；遵循 `{module}.{entity}.{past_tense}` 命名约定；**模型层发布事件**（参照 Phase 1-2 既有模式）
7. **上报规则** — 静态配置（数据库表），非脚本化（design-decisions.md §3.4）；上报链：操作工(L0) → 班组长(L1,0min) → 车间主任(L2,5min) → 生产经理(L3,15min) → 厂长(L4,30min)；EscalationLevel 为 EscalationRule 静态内部类，含 level/timeoutMinutes/notifyRoles/escalateOn
8. **升级联动** — EQUIPMENT_FAULT → 通知 EAM 创建维护工单；QUALITY_ISSUE → 通知 QMS 创建偏差；EMERGENCY → 通知 MES 暂停产线；ESCALATED L4 → AutoStop 自动执行（PAUSE_PROCESS/STOP_LINE/STOP_FACTORY）
9. **多源事件订阅** — Andon 订阅 `mes.process.interrupted` / `equip.fault.reported` / `qms.spc.out_of_control` / `qms.deviation.created` / `iot.alarm.triggered` / `aps.task.delayed` 自动创建安灯呼叫
10. **逐级上报逻辑** — escalate() 从 OPEN 或 ACKNOWLEDGED 进入 ESCALATED 状态；每次调用 escalationLevel +1（ESACALATED 状态内持续递增）；CLOSED/RESOLVED 状态拒接上报
11. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；**96 个测试**：AndonModuleTest 55 个（构造+状态转换+事件+枚举+综合场景）+ AndonRepositoryTest 12 个（CRUD+状态变更持久化+索引查询+枚举条件查询）+ AndonServiceImplTest 29 个（trigger/acknowledge/escalate/resolve/close + 查询 + 规则管理 + 综合场景 + 异常路径）
12. **待实现** — REST 控制器、通知渠道适配器（企业微信/钉钉 Webhook/短信/邮件/声光报警）、定时扫描超时自动上报、产线物理 Andon 看板对接

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(AndonEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `andon.call.created` | **EAM, QMS, BI** | EAM 设备故障→维护工单 / QMS 质量问题→偏差 / BI 安灯看板 |
| `andon.call.acknowledged` | **IoT** | 确认后取消声光报警 |
| `andon.call.escalated` | **MES, EAM** | MES 暂停工单/产线 / EAM 创建维护工单 |
| `andon.call.resolved` | **MES, EAM, QMS** | MES 恢复生产 / EAM 关闭维护工单 / QMS 关闭偏差 |
| `andon.call.closed` | **BI** | 更新安灯看板统计 |
| `andon.rule.created` / `andon.rule.updated` | — | 规则变更通知（模块内部使用） |

---

## BI 模块约定（2026-07-25 建立，2026-07-25 持久化完成）

### 设计定位

BI（看板与报表系统）是**协作层只读数据消费模块**——聚合所有业务模块（MES/QMS/Equip/LIMS/WMS/MPS/APS/Andon/EAM）的数据，生成制造运营的可视化看板和 KPI 报表。BI 不产生业务数据，只读取和聚合。通过 SSE（Server-Sent Events）向前端推送实时看板数据（design-decisions.md §4.10）。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 事件常量 | `event/types/BiEventTypes.java` | ⭐ 6 个领域事件常量（2 KPI + 4 看板刷新；与 PlmEventTypes/EquipEventTypes 等同包 `com.byz.factory.event.types`） |

### BI 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/KpiSnapshot.java` | `BaseEntity` | — | KPI 快照实体：10 种标准 KPI + setKpiValue/getKpiValue 动态存取 + markComputed 计算元数据 + isFullyComputed/getComputedKpiCount 查询 |
| `model/ProductionDashboard.java` | — (record) | — | 生产看板值对象：计划完成率+在制品+当日产出+工单进度明细(WorkOrderProgress 内嵌 record) |
| `model/QualityDashboard.java` | — (record) | — | 质量看板值对象：一次合格率+偏差统计+CAPA关闭率+最近偏差列表(DeviationSummary 内嵌 record) |
| `model/OeeDashboard.java` | — (record) | — | OEE 看板值对象：设备OEE明细(EquipmentOee 内嵌 record)+平均A/P/Q/OEE+最佳/最差设备 |
| `model/InventoryDashboard.java` | — (record) | — | 仓储看板值对象：周转率+呆滞/待检/近效期+库存金额+呆滞明细(SlowMovingItem)+近效期明细(NearExpiryItem) |
| `model/KpiType.java` | — | — | KPI 类型枚举：10 种(计划完成率/一次合格率/OEE/MTTR/MTBF/批次收率/库存周转率/偏差关闭率/CAPA关闭率/安灯响应时间)，每种含显示名+数据来源+目标值 |
| `model/DashboardPeriod.java` | — | — | 统计周期枚举：7 种(REALTIME/HOURLY/DAILY/WEEKLY/MONTHLY/QUARTERLY/YEARLY)，每种含刷新间隔(秒) |

### 服务接口

| 文件 | 说明 |
|------|------|
| `service/DashboardService.java` | 17 方法：生产看板(getProductionDashboard/getWorkOrderProgressList/getWorkOrderProgress) + 质量看板(getQualityDashboard/getDeviationStats/getRecentDeviations) + OEE看板(getOeeDashboard/getEquipmentOee/getLowOeeEquipments) + 仓储看板(getInventoryDashboard/getSlowMovingItems/getNearExpiryItems) + KPI管理(computeKpi/getKpiSnapshot/getKpiHistory/getLatestKpi) + 批次追溯(getBatchReport) |

### 持久化层

| 类型 | 文件 | 说明 |
|------|------|------|
| DDL | `ddl/V1.12__bi.sql` | kpi_snapshot 表 + 4 索引（code/period/factory_code/复合） |
| Repository | `repository/KpiSnapshotRepository.java` | 6 方法：findByCode + findByFactoryCodeAndPeriod + findByFactoryCodeOrderByPeriodDesc + findLatestByFactoryCode(@Query) + findTopByFactoryCodeOrderByPeriodDesc + deleteByFactoryCodeAndPeriod |
| Service实现 | `service/impl/DashboardServiceImpl.java` | 构造器注入 KpiSnapshotRepository；computeKpi 覆盖重建策略(delete+flush+save)；KPI 快照 CRUD 完整实现；看板查询返回占位数据(Phase 5 跨模块集成时实现)；@Transactional 写操作 + DomainEventPublisher 事件发布 |

### 约定规则

1. **协作层只读消费** — BI 不定义 core 接口供其他模块编译期引用（其他模块不依赖 BI）；BI 通过领域事件订阅各模块数据变更，通过 Service 接口聚合生成看板和 KPI
2. **模型分层** — 有身份的数据快照（KpiSnapshot）继承 `BaseEntity`；瞬态看板数据用 Java `record` 值对象（不可变、由属性定义相等性）；嵌套明细也用 record（WorkOrderProgress/DeviationSummary/EquipmentOee/SlowMovingItem/NearExpiryItem）
3. **IExpand 约定** — `bi.kpi.period` / `bi.kpi.factoryCode` / `bi.kpi.computedBy` / `bi.kpi.computedAt` / `bi.kpi.remark` 挂载在 KpiSnapshot 上
4. **KPI 动态存取** — KpiSnapshot 通过 `setKpiValue(KpiType, BigDecimal)` / `getKpiValue(KpiType)` 实现 10 种标准 KPI 的统一存取，switch 表达式路由到对应字段
5. **看板刷新频率** — 遵循 design-decisions.md §4.10：操作层（生产/OEE 看板）5s，战术层（质量/仓储看板）30s，战略层（KPI 报表）1h；DashboardPeriod 枚举内置 refreshIntervalSeconds
6. **领域事件** — 事件常量在 `BiEventTypes` 中统一管理（6 个：2 KPI + 4 看板刷新），遵循 `{module}.{entity}.{past_tense}` 命名约定；**模型层不发布事件**（design-decisions.md §1.1），由 Service 实现类统一负责
7. **数据来源映射** — 每个 KpiType 枚举值标注 dataSource 字段，明确数据来源模块；BI Service 实现层订阅对应事件增量更新 KPI
8. **值对象模式** — 所有 Dashboard record 提供 `empty(factoryCode)` 工厂方法返回零值实例；提供语义化查询方法（hasInterruptedOrders/hasOpenDeviations/hasLowOeeEquipment/hasSlowMovingItems 等）
9. **批次追溯报告** — `getBatchReport(batchNo)` 聚合 MES 工序记录 + LIMS 称量记录 + QMS 检验记录 + 收率计算，为 GMP 合规提供完整的批次追溯数据
10. **报表引擎** — Phase 5 用 SQL 视图 + JSON API；不引入 Grafana/Superset（design-decisions.md §4.10）
11. **前端技术栈** — 与 web 模块统一（Vue 3），不独立部署；实时推送用 SSE；移动端出响应式 Web（PWA）
12. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；**116 个测试**覆盖：
  - **模型测试（81）**: KpiType/DashboardPeriod 枚举（metadata），KpiSnapshot（构造+setKpiValue全10种+getKpiValue/markComputed/isFullyComputed/getComputedKpiCount+IExpand），4个Dashboard record值对象（构造+empty工厂方法+语义化查询+嵌套record），BiEventTypes（PREFIX+6事件命名+前缀约定+不可实例化），DashboardService接口契约（17方法签名），综合场景（生产全流程/KPI累积计算/OEE趋势/偏差趋势/仓储双风险）
  - **持久化集成测试（35）**: computeKpi（创建/覆盖/多工厂/多周期）+ KPI查询（getKpiSnapshot/getKpiHistory升序/getLatestKpi/限制条数/空数据）+ KPI值管理（setKpiValue持久化/全部10种往返/getKpiValue一致性/isFullyComputed）+ 看板查询（4个Dashboard+7个子查询返回占位数据）+ Repository CRUD（save/findByCode/findByFactoryCodeAndPeriod精确匹配/降序排序/deleteByFactoryCodeAndPeriod/findTopByFactoryCode）+ 综合场景（月度KPI全流程/多工厂隔离/重新计算覆盖）
13. **KPI 覆盖重建** — `computeKpi()` 对同工厂同周期的已有快照执行 `delete→flush→save` 策略确保覆盖，避免 code 唯一约束冲突
14. **看板占位返回** — 看板聚合查询（生产/质量/OEE/仓储 Dashboard）当前返回空占位数据，待 Phase 5 跨模块集成时实现真实的数据聚合
15. **已实现** — KpiSnapshot JPA实体（@Entity/@Table/@Column注解+JPA无参构造）、KpiSnapshotRepository（6查询方法）、DashboardServiceImpl（17方法：KPI CRUD完整实现+看板占位返回+@Transactional+事件发布）、BiTestConfig（Spring Boot DataJpaTest配置）、V1.12 DDL
16. **待实现** — 看板跨模块数据聚合、SSE推送端点、REST控制器方法实现（已定义在web模块DashboardController中）、事件订阅与增量KPI更新引擎、SQL视图定义、Vue 3前端看板组件、响应式PWA移动端

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(BiEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `bi.kpi.updated` | **Web** | SSE 推送 KPI 更新到前端 |
| `bi.kpi.computed` | **Web** | KPI 快照计算完成通知 |
| `bi.dashboard.production_refreshed` | **Web** | SSE 推送生产看板刷新 |
| `bi.dashboard.quality_refreshed` | **Web** | SSE 推送质量看板刷新 |
| `bi.dashboard.oee_refreshed` | **Web** | SSE 推送 OEE 看板刷新 |
| `bi.dashboard.inventory_refreshed` | **Web** | SSE 推送仓储看板刷新 |

> BI 作为数据消费端，订阅以下模块的领域事件（在 Service 实现层处理）：
> - **MES** — mes.workorder.created/completed/closed, mes.process.started/completed, mes.action.completed
> - **QMS** — qms.inspection.completed/passed/failed, qms.deviation.created/resolved, qms.capa.closed, qms.spc.warning
> - **Equip** — equip.oee.calculated, equip.status.changed
> - **MPS** — mps.plan.started/completed/closed
> - **APS** — aps.schedule.created, aps.task.delayed
> - **Andon** — andon.call.created/resolved/closed
> - **LIMS** — lims.batch_record.approved, lims.weighing.completed, lims.batch_record.archived
> - **WMS** — wms.inventory.changed

---

## Web 模块约定（2026-07-25 建立）

### 设计定位

Web 模块是 **easy-factory 的统一门户**——聚合全部 16 个业务模块（核心 MOM 层 11 + 协作层 5），基于 Spring Boot 3.x 提供统一的 REST API 入口。采用模块化单体架构，后续可按需拆分为微服务。

### Web 模块结构

```
easy-factory-web/
├── pom.xml                                  — 依赖全部 16 个业务模块 + SpringDoc
├── src/main/java/com/byz/factory/
│   ├── EasyFactoryApplication.java          — @SpringBootApplication 启动类
│   └── web/
│       ├── common/
│       │   ├── Result.java                  — 统一响应体 {code, message, data, timestamp, traceId}
│       │   ├── PageResult.java              — 分页响应（继承 Result，增加 page/size/total/pages）
│       │   └── GlobalExceptionHandler.java  — @RestControllerAdvice 全局异常处理
│       ├── config/
│       │   └── WebConfig.java               — CORS 全局配置 + 拦截器预留
│       └── controller/
│           ├── mes/WorkOrderController.java
│           ├── qms/InspectionController.java, DeviationController.java, CapaController.java
│           ├── plm/BlueprintController.java
│           ├── equip/EquipmentController.java
│           ├── lims/FormulaController.java, WeighingTaskController.java, BatchRecordController.java
│           ├── iot/IotController.java
│           ├── erp/ErpController.java
│           ├── scm/ScmController.java
│           ├── dms/DmsController.java
│           ├── wms/WmsController.java
│           ├── mps/MpsController.java
│           ├── aps/ApsController.java
│           ├── eam/EamController.java
│           ├── andon/AndonController.java
│           ├── bi/DashboardController.java
│           └── crm/CrmController.java
└── src/main/resources/
    └── application.yml                       — 服务端口、Jackson、SpringDoc、日志配置
```

### Controller API 一览

| 模块 | 基础路径 | Controller | 端点数 |
|------|---------|-----------|--------|
| MES | `/api/mes/work-orders` | WorkOrderController | 11 |
| QMS | `/api/qms/inspections` | InspectionController | 12 |
| QMS | `/api/qms/deviations` | DeviationController | 12 |
| QMS | `/api/qms/capas` | CapaController | 10 |
| PLM | `/api/plm/blueprints` | BlueprintController | 21 |
| Equip | `/api/equip/equipments` | EquipmentController | 20 |
| LIMS | `/api/lims/formulas` | FormulaController | 10 |
| LIMS | `/api/lims/weighing-tasks` | WeighingTaskController | 7 |
| LIMS | `/api/lims/batch-records` | BatchRecordController | 8 |
| IoT | `/api/iot` | IotController | 13 |
| ERP | `/api/erp` | ErpController | 16 |
| SCM | `/api/scm` | ScmController | 14 |
| DMS | `/api/dms` | DmsController | 19 |
| WMS | `/api/wms` | WmsController | 16 |
| MPS | `/api/mps` | MpsController | 14 |
| APS | `/api/aps` | ApsController | 5 |
| EAM | `/api/eam` | EamController | 10 |
| Andon | `/api/andon` | AndonController | 17 |
| BI | `/api/bi` | DashboardController | 17 |
| CRM | `/api/crm` | CrmController | 7 |
| **合计** | | **20 Controller** | **259** |

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": "2026-07-25T01:00:00Z",
  "traceId": "a1b2c3d4"
}
```

### 约定规则

1. **模块集成方式** — web 模块依赖全部 16 个业务模块；Controller 通过 `@Autowired(required = false)` 注入 Service 接口；当 Service 实现类不存在时 Spring Boot 仍然启动成功（Bean 为 null）
2. **统一响应包装** — 所有 Controller 方法返回 `Result<T>` 统一封装；成功用 `Result.ok(data)`，失败用 `Result.fail(msg)`；分页查询用 `PageResult<T>`
3. **异常处理** — `GlobalExceptionHandler` 统一处理 `IllegalArgumentException`(400)、`IllegalStateException`(409)、`Exception`(500)
4. **CORS** — 开发模式允许所有来源，生产环境需收紧
5. **API 文档** — SpringDoc OpenAPI 3.0，按模块分 16 个 Group；开发环境 `/swagger-ui.html` 可查看
6. **RESTful 设计** — 资源用复数名词；生命周期操作用 `PUT /{id}/{action}` 模式（如 `/api/mes/work-orders/WO-001/release`）
7. **包结构** — Controller 按模块分子包（`controller/mes/`, `controller/qms/`），common 和 config 放 web 层公共组件
8. **测试规范** — Given-When-Then + `@DisplayName` 中文描述；覆盖 Result/PageResult 工具类构造和工厂方法、所有 Controller 类存在性验证（Class.forName 反射加载）、公共组件加载验证
9. **待实现** — Service 实现类（Phase 5）、认证授权（JWT + RBAC）、DTO 对象（请求/响应分离）、API 版本管理、前端（Vue 3）、SSE 推送端点

---

## CRM 模块约定（2026-07-25 建立，2026-07-26 实现）

### 设计定位

CRM（客户关系管理）是**协作层模块**——管理客户主数据、销售订单和客户投诉。CRM 通过领域事件与 MPS（订单→生产计划）、QMS（投诉→偏差/CAPA）、WMS（订单→发货）对接。

### Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `crm/ICustomer.java` | 客户抽象，供 MPS/SCM/QMS 引用 |
| 接口 | `crm/ISalesOrder.java` | 销售订单抽象（含 ISalesOrderItem 嵌套接口），供 MPS/WMS/BI 引用 |
| 接口 | `crm/IComplaint.java` | 客户投诉抽象，供 QMS/Andon 引用 |
| 状态枚举 | `crm/SalesOrderStatus.java` | DRAFT→CONFIRMED→IN_PRODUCTION→SHIPPED→COMPLETED；+CANCELLED；实现 ILifecycle.StatusEnum |
| 状态枚举 | `crm/ComplaintStatus.java` | OPEN→INVESTIGATING→RESOLVED→CLOSED；+CANCELLED；实现 ILifecycle.StatusEnum |
| 事件常量 | `event/types/CrmEventTypes.java` | ⭐ 10 个领域事件常量（2 客户 + 5 订单 + 3 投诉；与 EquipEventTypes/PlmEventTypes 等同包 `com.byz.factory.event.types`） |

### CRM 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Customer.java` | `BaseEntity` | `ICustomer` | 客户实体：行业+区域+GMP审计状态+3 业务便捷方法(passGmpAudit/expireGmpAudit/updateAuditStatus) + IExpand 文档 |
| `model/SalesOrder.java` | `BaseLifecycleEntity<SalesOrderStatus>` | `ISalesOrder` | 销售订单核心实体：完整状态机 + 5 业务便捷方法(confirm/startProduction/ship/complete/cancel) + 明细管理 + 总金额计算 + 紧急判定 + IExpand 文档 |
| `model/SalesOrderItem.java` | — | `ISalesOrder.ISalesOrderItem` | 订单明细行：productCode+quantity+unitPrice（参照 PurchaseOrderItem 模式） |
| `model/Complaint.java` | `BaseLifecycleEntity<ComplaintStatus>` | `IComplaint` | 投诉实体：完整状态机 + 4 业务便捷方法(startInvestigation/resolve/close/cancel) + linkCapa + isQualityRelated + isActive + IExpand 文档 |

### 服务接口

| 文件 | 说明 |
|------|------|
| `service/CrmService.java` | 25 方法：客户管理 5(registerCustomer/getCustomer/listCustomers/findCustomersByIndustry/updateAuditStatus) + 订单管理 9(createOrder/getOrder/findOrdersByCustomer/findActiveOrders/confirmOrder/linkToProductionPlan/shipOrder/completeOrder/cancelOrder) + 投诉管理 11(createComplaint/getComplaint/findComplaintsByCustomer/findComplaintsByOrder/findActiveComplaints/findQualityComplaints/startInvestigation/resolveComplaint/closeComplaint/cancelComplaint/linkCapa) |

### 约定规则

1. **协作层模块** — CRM 是协作层模块，客户主数据和订单管理本地存储，但通过领域事件与核心 MOM 层协作
2. **跨模块接口** — CRM 实体通过 core `crm/` 包中的接口暴露（`ICustomer`、`ISalesOrder`、`IComplaint`）；MPS 通过 ISalesOrder 获取订单需求，QMS 通过 IComplaint 获取投诉信息
3. **状态机** — `SalesOrderStatus`、`ComplaintStatus` 放 core `crm/` 包中，实现 `ILifecycle.StatusEnum`；SalesOrder/Complaint 继承 `BaseLifecycleEntity<S>` 获得 `transition()` 校验
4. **模型继承** — 有状态实体（SalesOrder, Complaint）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录（Customer）继承 `BaseEntity` + 实现 core 接口
5. **IExpand 约定** — `crm.customer.*` 挂载在 Customer 上（region/contacts/gmpAuditStatus/gmpAuditDate/onTimeDeliveryRate/qualityComplaintRate）；`crm.order.*` 挂载在 SalesOrder 上（orderNo/customerCode/priority/gxpRequirements/specialInstructions/planNo）；`crm.complaint.*` 挂载在 Complaint 上（complaintNo/customerCode/orderNo/batchNo/type/capaCode/resolvedAt/closedAt）
6. **业务便捷方法** — Customer: `passGmpAudit(date)` / `expireGmpAudit()` / `updateAuditStatus(status,date)`；SalesOrder: `confirm()` / `startProduction()` / `ship()` / `complete()` / `cancel()` / `addItem()` / `linkToPlan()` / `totalAmount()` / `isRush()`；Complaint: `startInvestigation()` / `resolve(resolution)` / `close()` / `cancel()` / `linkCapa(capaCode)` / `isQualityRelated()` / `isActive()`
7. **领域事件** — 事件常量在 `CrmEventTypes` 中统一管理（10 个：2 客户 + 5 订单 + 3 投诉），遵循 `{module}.{entity}.{past_tense}` 命名约定；**模型层不发布事件**（design-decisions.md §1.1），由 Service 实现类统一负责
8. **SalesOrderItem 模式** — 订单明细为独立类实现 `ISalesOrder.ISalesOrderItem`（参照 PurchaseOrderItem 模式），含 productCode/quantity/unitPrice + @Data @NoArgsConstructor @AllArgsConstructor
9. **订单→MPS 协作** — CRM 确认订单后，MPS 通过订阅 `crm.order.confirmed` 获取订单需求，注册为 `DemandSource(SALES_ORDER)`，纳入生产计划
10. **投诉→QMS 闭环** — 质量问题投诉（type=QUALITY）创建后，QMS 通过订阅 `crm.complaint.received` 创建偏差 → CAPA 处理 → 投诉通过 `linkCapa()` 关联 CAPA 编码追溯
11. **GMP 审计管理** — Customer 内建 GMP 审计状态（PASSED/EXPIRED/NEVER），通过 `passGmpAudit()`/`expireGmpAudit()` 操作；审计状态影响是否可接受医药类订单
12. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；62 个测试覆盖：构造、状态转换（正常+非法+终态+完整生命周期+取消路径）、业务便捷方法、IExpand、枚举定义（SalesOrderStatus/ComplaintStatus 转换规则+CrmEventTypes 10事件命名约定+PREFIX+不可实例化）、CrmService 接口契约（25方法签名）、综合场景（客户订单全流程/质量投诉CAPA闭环/交期延误投诉/订单取消/误投诉撤销）
13. **已完成** — ✅ CrmServiceImpl 实现类（128行，构造器注入+事务管理+事件发布） + ✅ 3 个 Repository 接口（CustomerRepository/SalesOrderRepository/ComplaintRepository） + ✅ JPA 实体映射（4 模型全注@Entity/@Table/@Column）+ ✅ DDL V1.13__crm.sql（4 表+索引）+ ✅ CrmTestConfig + ✅ CrmServiceImplTest（28 个集成测试）+ ✅ POM 加入 JPA/H2/Test 依赖
14. **待实现** — 企业微信/邮件通知适配器、客户门户（Web端自助查订单进度/提交投诉）

### 跨模块事件契约

> 其他模块通过 `DomainEventPublisher.subscribe(CrmEventTypes.XXX, handler)` 订阅。

| 事件 | 订阅方 | 用途 |
|------|--------|------|
| `crm.customer.registered` | **BI** | 客户注册统计 |
| `crm.customer.audit_updated` | **QMS** | GMP 审计状态变更→供应商审计联动 |
| `crm.order.created` | **BI** | 订单创建统计 |
| `crm.order.confirmed` | **MPS, BI** | MPS 注册需求来源→生成生产计划 / BI 订单统计 |
| `crm.order.shipped` | **WMS, BI** | WMS 成品出库发货 / BI 交付统计 |
| `crm.order.completed` | **BI, ERP** | BI 交付看板 / ERP 应收账款 |
| `crm.order.cancelled` | **MPS, BI** | MPS 移除需求来源 / BI 取消统计 |
| `crm.complaint.received` | **QMS, Andon** | QMS 创建偏差/CAPA / Andon 触发安灯呼叫 |
| `crm.complaint.resolved` | **QMS** | 同步投诉解决状态，关闭关联偏差 |
| `crm.complaint.closed` | **BI** | 投诉关闭统计 |

---

---

## Test 模块约定（2026-07-25 建立）

### 设计定位

Test（聚合测试模块）是 **Phase 5 跨模块集成测试中心**——依赖所有15个业务模块，验证跨模块事件契约、多模块协作流程、完整制造场景。Test 不产生业务代码，只消费和执行聚合测试。

### 依赖关系

```
test ← 所有模块(common, core, mes, qms, plm, equip, lims, erp, iot, eam, mps, aps, wms, andon, bi, scm, dms)
```

### Test 测试覆盖

| 测试组 | 测试数 | 覆盖内容 |
|--------|--------|---------|
| ModuleLoading | 3 | 状态枚举验证 + 值枚举验证 + 15个模块实体构造 |
| CrossModuleEvents | 5 | 事件常量前缀 + MES/QMS/Andon事件命名 + 事件订阅发布 + 前缀匹配 |
| MesQmsIntegration | 2 | 工序中断→检验→偏差→CAPA 完整链路 + 让步接收 |
| MesEquipIntegration | 3 | 设备占用释放 + 故障维修恢复 + OEE 计算 |
| MesWmsIntegration | 2 | 拣料任务生命周期 + 收货来料检 |
| WmsScmIntegration | 2 | 采购→收货进度 + 供应商双维度分离 |
| MesLimsIntegration | 3 | 配方称量验证 + 称量偏差检测 + 批记录生命周期 |
| AndonMultiSourceIntegration | 3 | 安灯完整生命周期 + 紧急事件 + 上报规则配置 |
| BiDataConsumption | 4 | KPI 10种动态存取 + 元数据 + 看板 factory 方法 + 刷新间隔 |
| PlanScheduleExecuteFlow | 3 | MPS→APS→MES 全链路 + 产能检查 + 需求注册 |
| FullManufacturingScenario | 4 | 阿莫西林端到端 + 操作分析管道 + GMP合规 + 审计追踪 |
| AdditionalModuleIntegration | 7 | DMS文档 + EAM资产 + IoT设备 + ERP事务 + PLM蓝图 + WMS库存 |
| **总计** | **42** | **12 个集成测试组** |

### 约定规则

1. **聚合测试中心** — Test 模块依赖所有业务模块，执行跨模块集成测试；不定义 core 接口供其他模块引用
2. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；每个测试组用 `@Nested` 内嵌类分组
3. **跨模块事件测试** — 通过 `DomainEventPublisher.subscribe()` 订阅事件 → 调用模型业务方法触发事件 → 验证事件发布正确性
4. **集成场景优先级** — 优先覆盖核心 MOM 链路（MES+QMS+Equip+WMS+LIMS），其次覆盖协作层（SCM+DMS+BI+Andon），最后覆盖适配层（ERP+IoT+EAM+PLM）
5. **全流程测试** — 必须包含至少一个完整制造场景（蓝图→计划→排程→工单→称量→检验→批记录→KPI），验证模块间数据流转
6. **模型构造** — 测试中直接使用业务模型的 public 构造函数创建测试数据，不依赖 Spring 容器
7. **待实现** — Spring Boot 集成测试、REST API 端到端测试、数据库集成测试、消息队列集成测试

---

## 更新记录

| 日期 | 内容 |
|------|------|
| 2026-07-25 | **WMS 持久化层完成**：JPA 注解(@Entity/@Table/@Column/@Enumerated/@OneToMany)全部4模型+2嵌套类 + 4 个 Repository(Storage/Receipt/PickingTask/InventorySnapshot) + WmsServiceImpl(构造器注入+@Transactional+DomainEventPublisher) + DDL(V1.8__wms.sql, 6表) + POM 升级(spring-boot-starter-data-jpa+h2+spring-boot-starter-test) + 33 测试全过 |
| 2026-07-25 | **Equip 持久化层完成**：JPA 注解(@Entity/@Table/@Column) + 4 个 Repository + EquipmentServiceImpl(构造器注入+@Transactional+DomainEventPublisher) + DDL(V1.3__equip.sql, 5表) + TestConfig + 6 个集成测试 + 49 测试全过 |
| 2026-07-25 | **EAM 模块约定补充**：Asset/MaintenanceOrder/CalibrationRecord 业务便捷方法 + IExpand 文档 + EamEventTypes 迁入 core event/types + EamService 扩展(12→18方法) + 29 个测试 + eam.md 更新 |
| 2026-07-25 | **Test 模块约定建立**：POM 扩展依赖全部15个业务模块 + 42 个跨模块集成测试（12 个测试组覆盖: 模块加载、事件契约、MES+QMS、MES+Equip、MES+WMS、WMS+SCM、MES+LIMS、Andon多源事件、BI数据消费、计划排程执行、阿莫西林端到端、DMS+EAM+IoT+ERP+PLM综合验证）+ 父 POM dependencyManagement 补全所有模块版本 + web 模块编译修复 |
| 2026-07-19 | **池→线固化裁定**：三层描述体系正式确定；否决能力匹配引擎；core/PLM/Equip/MES/Physical 五会话任务卡列出 |
| 2026-07-19 | **高层设计**：核心层vs协作层边界 + CRM设计 + 协作协议建立 |
| 2026-07-25 | **APS 模块服务层实现**：1 个 core 新增(IOptimizationEngine 优化引擎抽象) + ApsServiceImpl 实现(ConcurrentHashMap存储+事件发布+5方法全部实现) + easy-factory-aps pom.xml 补充 JPA 依赖 + 28 个服务实现测试（内存存储/事件发布/重排程/IOptimizationEngine）→ APS 模块合计 91 个测试 |
| 2026-07-19 | **APS 模块约定建立**：1 个 core 新增(ApsEventTypes 5事件) + 5 个模型(Schedule升级+ScheduledTask内嵌类+ResourceCalendar新建+RescheduleTrigger新建+TaskStatus枚举+ResourceType枚举) + SchedulingRule 排程规则引擎(EDD/SPT/CR) + ApsService 接口(5方法) + 63 个测试 |
| 2026-07-19 | **MES 模块 Service 实现完成**：BlueprintProvider 解耦接口 + WorkOrderServiceImpl 内存实现（工单生命周期+工序流转+动作报工+QMS中断恢复+事件发布）+ mes pom.xml 添加 spring-boot-starter-data-jpa provided 依赖 + WorkOrderServiceImplTest 28 个测试 + 完整生命周期集成测试全通过。全项目 BUILD SUCCESS（60 个 MES 测试） |
| 2026-07-19 | **MES 模块约定建立**：2 个 core 新增(ProcessStatus 枚举 + MesEventTypes 10事件) + 3 个模型(MesWorkOrder升级+ProcessRecord新建+ActionRecord新建) + WorkOrderService 接口扩展(6→11方法) + 32 个测试 |
| 2026-07-19 | **WMS 模块约定完成**：4 个 core 接口(IStorage/IReceipt/IPickingTask/IInventorySnapshot) + 4 个 core 枚举(PickingTaskStatus/StorageType/PickingType/MaterialStatus) + 4 个模型(Storage重构+Receipt重构+PickingTask新建+InventorySnapshot新建) + WmsService扩展(4→20方法) + 33 个测试 |
| 2026-07-25 | **LIMS 模块持久化完成**：接口/枚举迁至 core `com.byz.factory.lims` 包（匹配 equip 模式）+ JPA 注解全部模型 + DDL(V1.7__lims.sql, 9表) + 3 Repository + 3 ServiceImpl + 5 持久化集成测试；总计 52 测试全部通过 |
| 2026-07-19 | **LIMS 模块约定完成**：3 个 core 状态枚举(FormulaStatus/WeighingTaskStatus/BatchRecordStatus) + 3 个 core 接口(IFormula/IWeighingTask/IBatchRecord) + LimsEventTypes(9事件) + 4 个模型(Formula重写+WeighingTask+WeighingItem+BatchRecord) + 3 个服务接口(FormulaService 10方法/WeighingTaskService 7方法/BatchRecordService 8方法) + 脚本迁移(mes→lims) + 新建批记录生成脚本 + 47 个测试 |
| 2026-07-25 | **MPS 持久化层完成**：2 个模型添加 JPA 注解 + 2 个 Repository + MpsServiceImpl (14方法) + V1.9__mps.sql (3表6索引) + MpsTestConfig + 26 个集成测试 (H2 @DataJpaTest) |
| 2026-07-19 | **MPS 模块约定完成**：2 个 core 接口(IProductionPlan/IDemandSource) + MpsEventTypes(8事件) + 4 个模型(ProductionPlan增强 + DemandSource + DemandSourceType + CapacityCheck) + MpsService扩展(4→14方法) + 47 个测试 |
| 2026-07-19 | **DMS 持久化层完成**：DocumentStatus/DocumentCategory/IDocument 按约定迁入 DMS 模块 + 3 个模型添加 JPA 注解 + 2 个 Repository + DDL(V1.6, 3表) + DmsServiceImpl(构造器注入) + 34 个测试全通过 |
| 2026-07-19 | **DMS 模块约定完成**：DmsEventTypes(8事件) + 3 个模型(Document + ApprovalWorkflow + ApprovalStep) + DmsService 扩展(5→19方法) + 34 个测试 |
| 2026-07-25 | **IoT 模块持久化层完成**：接口从 core/batch 迁移至 IoT 模块自身包(com.byz.factory.iot)；4 个 Model 添加 JPA 注解 + 3 个 JSON 转换器；DDL V1.5__iot.sql；4 个 Repository + IotGatewayServiceImpl(17方法+领域事件发布)；17 个仓库集成测试；63 测试全通过 |
| 2026-07-19 | **IoT 模块约定完成**：5 个 core 接口/枚举(IDeviceConnection/ITagValue/ICommand/IAlarmEvent/CommandStatus) + IotEventTypes(9事件) + 4 个模型 + 2 个服务接口(IotGatewayService 13方法/IProtocolAdapter) + 46 个测试 |
| 2026-07-19 | **Equip → Core 回归审核通过**：4 个议题决议（IEquipmentBinding渐进迁移/BOM引用Recipe双轨/FactoryCapacityProfile双轨/EventTypes迁至event.types包）；详见 `docs/architecture/modules/equip-core-regression.md` 五-六章 |
| 2026-07-19 | **Equip → Core 回归**：5 个接口回归 core(IEquipment/IEquipmentParameter/IEquipmentRecipe/IOEMetrics) + EquipEventTypes 迁入 core；equip 模型全部实现新接口；跨模块影响分析文档 → `docs/architecture/modules/equip-core-regression.md` |
| 2026-07-19 | Equip 模块约定补充：Equipment 业务便捷方法 + EquipmentParameter + EquipmentRecipe + OEMetrics + EquipEventTypes + EquipmentService 扩展(4→16方法) + 38 个测试 |
| 2026-07-25 | PLM 模块持久化层完成：DDL + JPA注解 + 4 Repository + BlueprintServiceImpl + 77 测试（31模型 + 19仓库 + 27服务） |
| 2026-07-19 | PLM 模块约定第二次补充：BOM转化模型 + ChangeRequest + 版本管理抽象(core) + 领域事件契约 + Common规则引擎需求 |
| 2026-07-19 | PLM 模块约定第一次补充：Blueprint 业务便捷方法 + ProcessTemplate 便捷方法 + BlueprintDifferImpl + IExpand 文档 + 31 个测试 |
| 2026-07-25 | **ERP Service 实现完成**：ErpAdapterServiceImpl（首个 Service 实现）+ ErpEventTypes + 17 个 Mock 测试；erp 73 测试 / 全项目全过 |
| 2026-07-25 | **ERP 持久化层完成**：BaseEntity/BaseLifecycleEntity 升级 @MappedSuperclass+JPA 审计；3 模型加 JPA 注解；3 Repository；15 个 @DataJpaTest；全项目 20 模块全过 |
| 2026-07-25 | SCM 模块服务实现：ScmServiceImpl + InboundPlan 模型 + ScmEventTypes(16事件) + 29 个测试 |
| 2026-07-25 | SCM 模块持久化：DDL(4表) + JPA 注解 + 3 个 Repository + 23 个 @DataJpaTest |
| 2026-07-19 | SCM 模块初步约定建立：4 个 core 接口/枚举 + 3 个模型 + ScmService 扩展 + 39 个测试 || 2026-07-18 | ERP 模块初步约定建立：3 个枚举 + 3 个模型 + ErpAdapterService 扩展 + 13 个测试 |
| 2026-07-18 | PLM 模块初步约定建立：BlueprintStatus 状态枚举 + IBlueprintDiffer + BlueprintDiff + 3 个模型 + BlueprintService + 15 个测试 |
| 2026-07-18 | Equip 模块初步约定建立：升级 MachineStatus 状态机 + 10 个测试 + 扩展字段 |
| 2026-07-18 | EAM 模块初步约定建立：3 个 core 接口 + 5 个枚举 + 3 个模型 + 服务接口 + 测试 |
| 2026-07-12 | 全部 15 个业务模块骨架创建 + 编译测试全通过（20模块总计） |
| 2026-07-12 | 移除 easy-factory-db（持久化回归各模块） |
| 2026-07-12 | 8个业务模块骨架创建，编译通过 |
| 2026-07-12 | 初始创建，core 接口体系完成 |
| 2026-07-19 | **Core 设计完善 v5**：删除死代码(script/Action/IProcessRoute/ProcessCompatibilityChecker)；新建 10 个模型(IAuditable/AuditTrail/IElectronicSignature/SignatureMeaning/MachineStatus/UOM/IProcessParameter/IMethod/IEnvironment/IBillOfMaterial)；20 个状态枚举全部实现；23 个跨模块接口全部就位；8 个事件类型常量类完整；DomainEventPublisher 前缀匹配+unsubscribe 已修复；IActionModel.execute() 空指针已保护；Noting→Nothing 全局修正；SourceType 扩展至 18 值；计算器泛型化；formatDuration 公共提取；require() 默认值统一；PROJECT_STATUS.md 更新至 v1.0；domain-model.md 更新至 v5；各模块文档追加 AI 协作建议 |
| 2026-07-24 | **QMS 模块约定建立**：9 个 core 新增(IInspectionRecord/IDeviation/ICapa + InspectionType/DeviationSeverity/DeviationDisposition/DeviationStatus/CapaStatus + QmsEventTypes 18事件) + 5 个模型(InspectionOrder升级+InspectionPlan新建+InspectionRecord新建+Deviation新建+Capa新建) + 3 个服务接口(InspectionService 3→12方法 + DeviationService 13方法 + CapaService 10方法) + 67 个测试 |
| 2026-07-25 | **QMS 模块持久化实现**：POM 更新(JPA+H2+Spring Boot Test) + 5 个模型添加 JPA 注解 + 4 个 Repository + 3 个 ServiceImpl(构造器注入+@Transactional) + DDL V1.10__qms.sql(6张表) + IQualityAction 桥接(QualityAction+4工厂方法) + 89 个测试全部通过
| 2026-07-25 | **Andon 模块约定建立**：1 个 core 新增(AndonEventTypes 7事件) + 1 个 core 修改(AndonStatus OPEN→ESCALATED 紧急通道) + 3 个值枚举(TriggerType/AndonSeverity/AndonSource) + 2 个内部枚举(AutoStop/EscalateCondition) + 3 个模型(AndonCall重写+EscalationRule新建+AndonDashboard新建) + AndonService 接口扩展(4→19方法) + 55 个测试 |
| 2026-07-25 | **Andon 持久化层实现**：JPA 注解(AndonCall @Entity+@Table + EscalationRule @Entity+@Table) + AndonCallRepository(5查询方法) + EscalationRuleRepository(3查询方法) + AndonServiceImpl(19方法全部实现，@Service+构造器注入+@Transactional+DDP) + DDL V1.11__andon.sql + AndonTestConfig + 41 个新测试(AndonRepositoryTest 12 + AndonServiceImplTest 29) → 全项目 BUILD SUCCESS |
| 2026-07-25 | **BI 模块约定建立**：1 个 core 新增(BiEventTypes 6事件) + 2 个枚举(KpiType 10种标准KPI + DashboardPeriod 7种周期) + 5 个模型(KpiSnapshot重写继承BaseEntity + ProductionDashboard/QualityDashboard/OeeDashboard/InventoryDashboard 4个record值对象) + DashboardService 接口扩展(4→17方法) + 81 个测试
| 2026-07-25 | **BI 模块持久化完成**：ddl/V1.12__bi.sql(kpi_snapshot表+4索引) + KpiSnapshot JPA注解(@Entity/@Table/@Column+JPA无参构造) + KpiSnapshotRepository(6查询方法) + DashboardServiceImpl(17方法：KPI CRUD完整实现 + 看板占位返回 + @Transactional + DomainEventPublisher事件发布) + BiTestConfig + DashboardServiceImplTest(35个集成测试) + pom.xml 依赖更新(spring-boot-starter-data-jpa/h2/fastjson2)；全部 116 个测试通过
| 2026-07-26 | **CRM 模块持久化完成**：POM 新增 JPA/H2/Test 依赖 + 4 模型完整 JPA 注解(@Entity/@Table/@Column/@OneToMany) + ddl/V1.13__crm.sql(4表+11索引) + 3 个 Repository(CustomerRepository/SalesOrderRepository/ComplaintRepository，14个查询方法) + CrmServiceImpl(128行，构造器注入+事务管理+事件发布) + CrmTestConfig + CrmServiceImplTest(28个集成测试) + 全部 90 个测试通过 |
| 2026-07-25 | **CRM 模块约定建立**：6 个 core 新增(ICustomer/ISalesOrder/IComplaint/SalesOrderStatus/ComplaintStatus + CrmEventTypes 10事件) + 4 个模型(Customer重写+SalesOrder重写+SalesOrderItem新建+Complaint新建) + CrmService 接口扩展(3→25方法) + 62 个测试 + web CrmController 更新(BigDecimal参数) + overview.md Phase 5 状态更新 |
| 2026-07-25 | **Web 模块约定建立**：Spring Boot 3.x 启动类 + 2 个公共组件(Result/PageResult) + 全局异常处理器 + CORS 配置 + 20 个 Controller(覆盖全部 16 个业务模块) + 259 个 REST 端点 + application.yml(SpringDoc 16 Group) + 37 个测试