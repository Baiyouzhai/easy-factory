# easy-factory-core — 领域内核

> **最后更新**: 2026-07-19 | **版本**: v0.1.2

## 1. 模块定位

定义制造系统的统一领域语言。所有接口、枚举、抽象类的所在地，是整个平台的契约层。业务模块只依赖 core，模块间通过 core 接口 + 领域事件松散耦合，零编译期依赖。

### 制造标准对标

core 的设计参照以下工业标准：

| 标准 | 映射到 core |
|------|-----------|
| **ISA-95** | Enterprise→Site→Area→Line→Work Cell 层次映射到 IFactory→IProductionLine→IWorkstation |
| **ISA-88** | 批控制模型（Procedure→Unit Procedure→Operation→Phase）映射到 IProcess→IAction 执行链 |
| **4M1E** | TQM 人机料法环分类 → `Dict.SourceGroup` (Personnel/Machine/Material/Method/Environment) |
| **21 CFR Part 11** | 电子记录/签名 → `IElectronicSignature` + `AuditTrail` |
| **GMP** | 批次追溯 + 放行控制 → `IBatch` + `ITraceable` + `BatchStatus.RELEASED` |

---

## 2. 包结构

```
com.byz.factory
├── shared/          — 共享基类、枚举、工具
│   ├── BaseEntity, BaseLifecycleEntity, AbstractResourceItem
│   ├── Dict (Execute/Control/Importance/SourceGroup/SourceType), UOM
│   ├── HasCode, HasName, HasStatus, HasVersion, HasTimestamps, IAuditable
│   ├── IVersionStrategy, SemanticVersionStrategy, IncrementalVersionStrategy
│   ├── SemanticVersion, BumpType, Constant, Creator
│   └── EmptyAction, EmptyResourcePack
│
├── resource/        — 资源抽象 (4M1E + Product)
│   ├── IResource, IResourceItem, IResourcePack
│   ├── ResourceItem, ResourcePack
│   ├── IMachine, IMaterial, IPersonnel, IMethod, IEnvironment, IUsedResource
│   └── AbstractResourceItem (4M1E 资源基类)
│
├── process/         — 工序/动作模型
│   ├── IProcess, IProcessParameter, Process
│   ├── IAction, IActionModel, Action, ActionGroup, ActionLibrary, TagFilter
│   └── action/      — 子系统动作扩展接口
│       ├── IEquipmentAction (Equip 桥接)
│       ├── IQualityAction (QMS 桥接)
│       ├── IMaterialAction (WMS/LIMS 桥接)
│       ├── IDataCollectionAction (IoT 桥接)
│       └── IComplianceAction (DMS/GMP 桥接)
│
├── factory/         — 工厂/物理层/产品定义
│   ├── IFactory, Factory
│   ├── IBlueprint, IProduct, IProductInfo, IProductItem
│   ├── IBillOfMaterial, BillOfMaterial, CustomerSpec, EngineeringBOM
│   ├── IEstimateResource
│   ├── IProductChecker, IProductFactoryResult, ProductChecker, ProductFactoryResult
│   ├── IBlueprintDiffer, BlueprintDiff, BlueprintStatus
│   └── physical/    — 产线/工位/设备绑定
│       ├── IProductionLine, ProductionLine, IWorkstation, Workstation
│       ├── IEquipmentBinding, EquipmentBinding
│       └── LineNode, LineConnection, StationType
│
├── batch/           — 跨模块业务接口（所有模块的核心契约集中于此）
│   ├── 批次追溯: IBatch, ITraceable, BatchStatus
│   ├── 工单/检验: IWorkOrder, WorkOrderStatus, IInspectionOrder, InspectionStatus
│   ├── 设备: IEquipment, IEquipmentParameter, IEquipmentRecipe(含 IRecipePhase), IOEMetrics, MachineStatus
│   ├── 供应链: ISupplier, IPurchaseOrder(含 IPurchaseOrderItem), SupplierStatus, PurchaseOrderStatus
│   ├── 配方/称量: IFormula(含 IFormulaPhase), IWeighingTask(含 IWeighingItem), IBatchRecord
│   │              FormulaStatus, WeighingTaskStatus, BatchRecordStatus
│   ├── 仓储: IStorage, IReceipt(含 IReceiptItem), IInventorySnapshot, IPickingTask(含 IPickingTaskItem)
│   │          ReceiptStatus, PickingTaskStatus, MaterialStatus, StorageType, PickingType
│   ├── 文档: IDocument, DocumentStatus, DocumentCategory
│   ├── 资产/维护: IAsset, IMaintenanceOrder, ICalibrationRecord
│   │              AssetStatus, MaintenanceOrderStatus, MaintenanceType, MaintenancePriority
│   │              CalibrationResult, CalibrationType
│   ├── IoT: ICommand(含 CommandType/CommandPriority), IDeviceConnection, ITagValue(含 TagQuality)
│   │         IAlarmEvent(含 AlarmSeverity), CommandStatus
│   ├── 计划: IProductionPlan(含 IPlanItem), IDemandSource, ProductionPlanStatus
│   ├── ERP: IErpTransaction, TransactionStatus, TransactionType
│   └── 跨模块: AndonStatus, ScheduleStatus, ReceiptStatus, DocumentStatus
│
├── lifecycle/       — 状态机框架
│   └── ILifecycle<S extends ILifecycle.StatusEnum>
│       └── StatusEnum: allowedTransitions(): Set<S>
│
├── event/           — 领域事件基础设施
│   ├── IDomainEvent, DomainEventPublisher, SimpleDomainEvent
│   ├── AuditTrail, IElectronicSignature, SignatureMeaning
│   └── types/       — ★ 所有模块的事件类型常量
│       ├── PlmEventTypes, EquipEventTypes, IotEventTypes
│       ├── LimsEventTypes, MpsEventTypes, DmsEventTypes
│       └── (待建: ScmEventTypes, MesEventTypes, QmsEventTypes, ...)
│
├── operation/       — 操作分析引擎
│   ├── common/      — IOperation, IReportable, OperationResult, ReportPrinter, IScalable
│   ├── time/        — ITimed, ActionDuration, GmpActionDuration, ProcessCycleTime,
│   │                  ProductionLeadTime, IIntervalTimed, IConstrainedTimed, IStatisticalTimed
│   ├── capability/  — IMatchingStrategy, DirectEquipmentStrategy, LineFirstStrategy,
│   │                  ProcessRouteMatcher
│   ├── capacity/    — FactoryCapacityProfile, BottleneckDetector
│   └── resource/    — BatchScaledCalculator, ResourceRequirementExploder
│
├── repository/      — 仓储端口
│   └── IRepository<T,ID>, IPageRequest, IPageResult, SimplePageRequest, SimplePageResult, Sort
│
├── script/          — 脚本引擎
│   ├── IScriptEngine, IScript, ScriptContext, ScriptMetadata, ScriptRegistry, ScriptEngines
│   ├── CompiledScript, GraalCompiledScript, NashornCompiledScript
│   └── GraalScriptEngine, NashornScriptEngine
│
└── exception/       — 领域异常
    └── ActionException, ResourceException
```

---

## 3. 核心领域模型

### 3.1 资源体系 (4M1E + Product)

```
IResource (core)
  ├── getName(), getGroup(), getType()
  └── IResourceItem (extends IResource)
        ├── getNumber(), add(), use(), isEmpty(), copy()
        ├── IMachine     — 设备 (group = Machine)
        ├── IMaterial    — 物料 (group = Material)
        ├── IPersonnel   — 人员 (group = Personnel)
        ├── IMethod      — 工艺方法/SOP (group = Method)
        ├── IEnvironment — 环境条件 (group = Environment)
        └── IUsedResource — 实际用量跟踪

IResourcePack
  ├── merge(), compress() — 按组+名去重汇总
  ├── copy()              — 深拷贝
  └── requireResources()  — 资源需求清单
```

所有资源继承 `IData`（JSON 序列化能力），可通过 `IExpand` 挂载动态属性。数量操作（add/use）在 `ResourceItem` 中有负余额保护。

### 3.2 工序与动作

```
IProcess
  ├── getCode(), getName(), getOrder()
  ├── getActions(): List<IActionModel>    — 有序动作列表
  ├── requireResources(): IResourceItem[] — 所需资源
  └── execute(inputs): IResourcePack      — 遍历动作链，前一个输出作为下一个输入

IAction
  ├── getCode(), getName(), getOrder()
  ├── getImportance(): Dict.Importance    — Optional(可跳过) / Require(必要)
  └── execute(process, inputs): IResourcePack

IActionModel (extends IAction)
  ├── getScript(): String                 — JavaScript 脚本
  └── execute() 默认实现: 有脚本→调脚本引擎 / 无脚本→走 executeType 路由
```

**动作语义** (`Dict.Execute`, 10 个值)：

| 值 | 制造业含义 | 资源操作 |
|----|---------|---------|
| `Nothing` | 占位/跳过 | 不操作资源 |
| `Create` | 产出新资源 | 创建输出 |
| `Add` | 补充资源 | 增加数量 |
| `Use` | 消耗资源 | 减少数量 |
| `Change` | 改变属性 | 更新资源属性 |
| `Convert` | 转化 | 输入→输出 |
| `Split` | 分装/拆分 | 一分多 |
| `Combine` | 混合/合并 | 多合一 |
| `Transfer` | 物料转移 | 位置变更 |
| `Hold` | 待检隔离 | 暂扣不放行 |

**流程控制** (`Dict.Control`, 5 个值)：

| 值 | 制造业含义 |
|----|---------|
| `Default` | 顺序执行 |
| `Interrupt` | 条件中断（如 HACCP CCP 监控触发暂停） |
| `Count` | 计数控制（如每 N 件抽检一次） |
| `Timing` | 时序控制（如搅拌 30 分钟后自动进入下一步） |
| `Repeat` | 重复/返工（如检验不合格返回上道工序） |

### 3.3 工厂与物理层

```
IFactory → 工序容器 + 产线集合 + 设备池
  ├── IProductionLine → IWorkstation[] → IEquipmentBinding[]
  │     └── EquipmentBinding = 设备 + 支持的动作编码 + 参数模板
  └── IEquipmentBinding: 设备-动作的匹配依据
```

**匹配引擎**：`ProcessRouteMatcher` 将蓝图的每道工序匹配到具体设备。
- `DirectEquipmentStrategy` — 直接指定设备
- `LineFirstStrategy` — 产线优先匹配

### 3.4 产品与蓝图

```
IProductInfo
  ├── getName(), getBlueprint()
  └── IBlueprint
        ├── getCode(), getVersion()
        ├── getProductionProcessList(): List<IProcess>  — 工序路线
        └── BlueprintStatus: DRAFT→UNDER_REVIEW→APPROVED→RELEASED→OBSOLETED

IBillOfMaterial — 物料清单
  ├── EBOM (设计BOM) → PBOM (工艺BOM) → MBOM (制造BOM)
  └── BOMType.next(): EBOM→PBOM→MBOM→null

CustomerSpec → EngineeringBOM → Blueprint(RELEASED) → MES 工单
```

### 3.5 批次追溯

```
IBatch
  ├── getBatchNo(), getProductCode(), getBatchSize()
  ├── getActualYield(), getStatus(): BatchStatus
  ├── getCurrentProcessCode()  — WIP 位置追踪
  ├── getParentBatchNos()      — 物料谱系（上游）
  └── getChildBatchNos()       — 物料谱系（下游）

ITraceable
  └── 谁(who) + 何时(when) + 哪批(batchNo) + 哪道工序(processCode) + 做了什么(action)

BatchStatus: CREATED→IN_PROGRESS→COMPLETED→UNDER_REVIEW→APPROVED→RELEASED→ARCHIVED
                        ↘ CANCELLED          ↘ REJECTED
```

---

## 4. 数据字典 (Dict)

### 4.1 核心枚举

| 枚举 | 值 | 用途 |
|------|----|------|
| `Dict.Execute` | Nothing, Create, Add, Use, Change, Convert, Split, Combine, Transfer, Hold | 动作语义 |
| `Dict.Control` | Default, Interrupt, Count, Timing, Repeat | 流程控制 |
| `Dict.Importance` | Optional, Require | 动作重要性 |
| `Dict.SourceGroup` | Other, Personnel, Machine, Material, Method, Environment, Product | 4M1E 资源分类 |
| `Dict.SourceType` | 20 个子类型（RawMaterial, WIP, FinishedGood, Machine, Tool, etc.） | 资源子分类 |

### 4.2 计量单位 (UOM)

`KG, G, MG, L, ML, M, CM, MM, PCS, TAB, CAP, BTL, BOX, CELSIUS, PERCENT, BATCH, NONE`

---

## 5. 跨模块接口（batch/ 包）

> ⚠️ `com.byz.factory.batch` 当前承载了所有跨模块接口。后续将按领域拆分为 `equip/`, `scm/`, `lims/`, `wms/`, `mps/`, `dms/`, `iot/` 等子包。

### 5.1 批次/工单/检验

| 接口 | 所属模块 | 说明 |
|------|---------|------|
| `IWorkOrder` | MES | 工单号/产品/蓝图/批次/状态 |
| `IBatch` | MES | 批号/收率/状态/追溯记录 |
| `ITraceable` | MES | 追溯接口（谁/何时/在哪批/做了什么） |
| `IInspectionOrder` | QMS | 检验类型/方案/进度 |

### 5.2 设备

| 接口 | 说明 |
|------|------|
| `IEquipment` | = IMachine + ILifecycle<MachineStatus> + 台账字段（型号/位置/资产编码） |
| `IEquipmentParameter` | 设定值/实际值/控制限 + `isInControl()` default 方法 |
| `IEquipmentRecipe` | 设备配方 + 内嵌 `IRecipePhase`（阶段化参数集） |
| `IOEMetrics` | OEE = 可用率×性能率×质量率 |

### 5.3 供应链

| 接口 | 说明 |
|------|------|
| `ISupplier` | 供应商编码/类别/资质/绩效 |
| `IPurchaseOrder` | 采购订单 + 内嵌 `IPurchaseOrderItem` |

### 5.4 配方/称量/批记录

| 接口 | 说明 |
|------|------|
| `IFormula` | 配方编码/产品/版本/批量 + 内嵌 `IFormulaPhase` |
| `IWeighingTask` | 称量任务 + 内嵌 `IWeighingItem` |
| `IBatchRecord` | 批记录（工序记录+称量+检验+偏差） |

### 5.5 仓储

| 接口 | 说明 |
|------|------|
| `IStorage` | 库位（仓库/区域/货架/层/位） |
| `IReceipt` | 收货单 + 内嵌 `IReceiptItem` |
| `IInventorySnapshot` | 库存快照（在手/已分配/待检/不合格） |
| `IPickingTask` | 拣料任务 + 内嵌 `IPickingTaskItem` |

### 5.6 文档

| 接口 | 说明 |
|------|------|
| `IDocument` | 文档编号/类别/版本/复审周期（GMP 受控文档） |

### 5.7 资产/维护

| 接口 | 说明 |
|------|------|
| `IAsset` | 固定资产编码/设备关联/折旧 |
| `IMaintenanceOrder` | 维护类型/优先级/停机时间/成本 |
| `ICalibrationRecord` | 校准类型/标准/结果/证书 |

### 5.8 IoT

| 接口 | 说明 |
|------|------|
| `ICommand` | 设备指令 + 内嵌 CommandType(5种) + CommandPriority(3级) |
| `IDeviceConnection` | 设备连接（协议/端点/心跳） |
| `ITagValue` | 标签值 + 内嵌 TagQuality(GOOD/BAD/UNCERTAIN) |
| `IAlarmEvent` | 报警事件 + 内嵌 AlarmSeverity(INFO/WARNING/CRITICAL/EMERGENCY) |

### 5.9 计划

| 接口 | 说明 |
|------|------|
| `IProductionPlan` | 生产计划 + 内嵌 `IPlanItem` |
| `IDemandSource` | 需求来源（销售订单/预测/安全库存/手工） |

### 5.10 ERP

| 接口 | 说明 |
|------|------|
| `IErpTransaction` | ERP 事务回传记录（物料消耗/成品入库/转储） |

---

## 6. 状态枚举（全部实现 ILifecycle.StatusEnum）

| 状态枚举 | 所属模块 | 状态流转 |
|---------|---------|---------|
| `WorkOrderStatus` | MES | CREATED→RELEASED→IN_PROGRESS→COMPLETED→CLOSED |
| `BatchStatus` | MES | CREATED→IN_PROGRESS→COMPLETED→UNDER_REVIEW→APPROVED→RELEASED→ARCHIVED |
| `InspectionStatus` | QMS | PENDING→IN_PROGRESS→PASSED/FAILED→CLOSED |
| `MachineStatus` | Equip | IDLE→RUNNING\|SETUP\|MAINTENANCE / FAULT→MAINTENANCE→IDLE |
| `BlueprintStatus` | PLM | DRAFT→UNDER_REVIEW→APPROVED→RELEASED→OBSOLETED |
| `FormulaStatus` | LIMS | DRAFT→APPROVED→ACTIVE→RETIRED |
| `WeighingTaskStatus` | LIMS | PENDING→WEIGHING→VERIFIED→COMPLETE |
| `BatchRecordStatus` | LIMS | IN_PROGRESS→REVIEW→APPROVED→ARCHIVED |
| `PurchaseOrderStatus` | SCM | DRAFT→APPROVED→SENT→RECEIVING→COMPLETED; +CANCELLED |
| `ReceiptStatus` | WMS | PENDING→PARTIAL→COMPLETED→CLOSED |
| `PickingTaskStatus` | WMS | PENDING→IN_PROGRESS→PICKED→DELIVERED; +CANCELLED |
| `DocumentStatus` | DMS | DRAFT→UNDER_REVIEW→APPROVED\|REJECTED→EFFECTIVE→OBSOLETED |
| `AssetStatus` | EAM | IDLE→IN_USE→UNDER_MAINTENANCE→SCRAPPED |
| `MaintenanceOrderStatus` | EAM | OPEN→IN_PROGRESS→COMPLETED→VERIFIED; +CANCELLED |
| `ProductionPlanStatus` | MPS | DRAFT→APPROVED→RELEASED→IN_PROGRESS→COMPLETED→CLOSED |
| `CommandStatus` | IoT | QUEUED→SENT→ACKNOWLEDGED→COMPLETED/FAILED; +CANCELLED |
| `TransactionStatus` | ERP | PENDING→SENT→CONFIRMED/FAILED; +CANCELLED |
| `AndonStatus` | Andon | OPEN→ACKNOWLEDGED→RESOLVED/ESCALATED→CLOSED |
| `ScheduleStatus` | APS | DRAFT→OPTIMIZED→DISPATCHED→IN_PROGRESS→COMPLETED; +CANCELLED |
| `DocumentStatus` | DMS | (见上) |

---

## 7. 状态机与模型继承

### 7.1 ILifecycle 框架

```java
public interface ILifecycle<S extends ILifecycle.StatusEnum> {
    S getStatus();
    void transition(S target);       // 非法转换抛 IllegalStateException
    boolean canTransition(S target);  // 检查合法性

    interface StatusEnum {
        Set<? extends StatusEnum> allowedTransitions();
    }
}
```

### 7.2 模型继承决策树

```
实体是 4M1E 资源？ → 是 → AbstractResourceItem（获得 group/type/number + 数量操作）
              → 否 → 有状态机？ → 是 → BaseLifecycleEntity<S>（获得 status + transition() + canTransition()）
                                → 否 → BaseEntity（获得 code/name/createdAt/updatedAt）

值对象（不可变、无标识） → Java record（如 CapacityCheck, BlueprintDiff, BOMConversionRequest）
```

---

## 8. 领域事件体系

### 8.1 基础设施

```java
IDomainEvent.of(eventType, source, payload)   // 创建事件
DomainEventPublisher.publish(event)            // 发布
DomainEventPublisher.subscribe(eventType, handler)  // 订阅
DomainEventPublisher.subscribePrefix(prefix, handler) // 前缀订阅
```

### 8.2 事件类型常量

所有模块的事件类型常量统一在 `com.byz.factory.event.types`：

```
PlmEventTypes    — plm.blueprint.{released|submitted|approved|rejected|obsoleted} + plm.bom.transformed + plm.change.*
EquipEventTypes  — equip.{status.changed|fault.reported|production.*|maintenance.*|recipe.dispatched|oee.calculated}
IotEventTypes    — iot.{device.*|tag.collected|command.*|alarm.*}
LimsEventTypes   — lims.{formula.*|weighing.*|batch_record.*}
MpsEventTypes    — mps.plan.{approved|released|started|completed|closed}
DmsEventTypes    — dms.document.{submitted|approved|rejected|effective|obsoleted|expired|versioned}
```

命名格式：`{module}.{entity}.{past_tense}`，三段式。

### 8.3 事件发布职责

事件发布由 Service 实现层负责（非 Model 层）。Model 层只负责状态转换 + `markUpdated()`。

---

## 9. 脚本引擎

### 9.1 架构

```
IScriptEngine                     — 引擎抽象
  ├── GraalScriptEngine           — GraalJS（推荐，JDK 21+ 内置沙箱）
  └── NashornScriptEngine         — Nashorn（过渡期，需独立包）

ScriptRegistry                    — 脚本索引（从 registry.json 加载元数据）
ScriptMetadata                    — 从 JSDoc 解析的结构化元数据
ScriptContext                     — 执行上下文（白名单 API 表面对象 + 参数）
CompiledScript / GraalCompiledScript / NashornCompiledScript — 编译产物
```

### 9.2 安全沙箱

默认全部禁止，只开放显式授权的 API：
- ❌ Java 类访问、IO、线程、进程、JNI
- ✅ ScriptContext 中显式绑定的白名单 API

### 9.3 脚本标准

每个脚本含 JSDoc 元数据头：
```javascript
/**
 * @id          mes.weighing-check.v1
 * @name        称量防错
 * @version     1.0.0
 * @module      mes
 */
```

脚本存放：`easy-factory-core/src/main/resources/scripts/<module>/`
索引文件：`registry.json`

---

## 10. 操作分析引擎

| 组件 | 功能 |
|------|------|
| `ProcessRouteMatcher` | 蓝图工序→工厂设备匹配（DirectEquipment / LineFirst 策略） |
| `FactoryCapacityProfile` | 工厂产能画像（日可用时间 × OEE 因子） |
| `BottleneckDetector` | 瓶颈工序识别 |
| `ActionDuration` / `GmpActionDuration` | 动作耗时（支持计划/实际/偏差） |
| `ProcessCycleTime` | 工序周期时间（支持顺序/并行模式） |
| `ProductionLeadTime` | 生产提前期（排队+准备+加工+等待+运输） |
| `BatchScaledCalculator` | 批次规模缩放（配方量→实际批量的线性/非线性换算） |
| `ResourceRequirementExploder` | 资源需求展开（BOM→工序×资源矩阵） |

---

## 11. 当前实现状态

### 已完成 (v0.1.2)

- [x] 完整接口体系：资源/工序/动作/工厂/蓝图/BOM
- [x] 4M1E+Product 资源分类 + 10 种动作语义
- [x] ILifecycle 通用状态机 + 20 个状态枚举
- [x] IDomainEvent + DomainEventPublisher 事件基础设施
- [x] GraalJS + Nashorn 双引擎 + 4 个示例脚本 + registry.json
- [x] 跨模块接口体系：batch/ 包下 40+ 接口覆盖全部业务模块
- [x] 事件类型常量统一管理：event/types/ 下 6 个 EventTypes 类
- [x] 操作分析引擎：时间/产能/能力匹配/资源展开
- [x] 版本管理策略：IVersionStrategy + SemanticVersion + IncrementalVersion
- [x] 共享基类：BaseEntity / BaseLifecycleEntity / AbstractResourceItem
- [x] 182 个单元测试全部通过

### 待进行

- [ ] `com.byz.factory.batch` 按领域拆分子包（equip/scm/lims/wms/mps/dms/iot）
- [ ] ProcessRouteMatcher 与 ProductChecker 的实现完善
- [ ] GraalJS JVMCI 编译优化（当前为解释模式，性能受限）
- [ ] IEquipmentBinding / EngineeringBOM 的类型化迁移（渐进，已有接口预留）

---

## 12. AI 协作建议

1. **新增跨模块接口规则**：被其他模块引用的实体在 core 创建接口（如 `IFormula`），内部实体不需要（如 `ApprovalWorkflow`）。接口只暴露 getter。

2. **新增状态枚举**：实现 `ILifecycle.StatusEnum`，终态的 `allowedTransitions()` 返回空集合。

3. **事件常量**：新建 `event/types/{Module}EventTypes.java`，遵循 `{module}.{entity}.{past_tense}` 命名。

4. **模型继承**：参照 §7.2 决策树选择正确基类。
