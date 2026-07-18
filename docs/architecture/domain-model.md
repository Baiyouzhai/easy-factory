# 领域模型总图 v4

> 最后更新: 2026-07-18
> v4 变更: 删除 IProcessRoute/ProcessCompatibilityChecker; 新增审计追踪/电子签名/设备状态机/WIP位置/计量单位/工艺参数/4M1E补全; 计算器泛型化; 所有待建项已建成

## 一、产品定义域 (process/)

### 1.1 Action（动作）

| 属性 | 说明 |
|------|------|
| 定义 | 最小操作单元 |
| 位置 | `process/IAction.java` |
| 实现 | `process/Action.java` |
| 关键字段 | `code`, `name`, `order`, `importance`(Optional/Require), `executeType`(Nothing/Create/Add/Use/Change/Convert/Split/Combine/Transfer/Hold) |
| 关系 | 属于 Process; 声明 `requireResources`(IResourceItem[]); 通过 IActionModel 支持脚本 |

```
Action 不涉及"谁来做"——那是 EquipmentBinding 的事。
Action 用标签声明"需要什么"——那是 TagFilter 的事。
```

### 1.2 IActionModel（脚本化动作）

| 属性 | 说明 |
|------|------|
| 定义 | Action + 脚本执行 + 资源需求声明 |
| 位置 | `process/IActionModel.java` |
| 关键方法 | `getScript()`, `requireResources()`, `execute()`(默认实现：有脚本走engine，无脚本按executeType) |

### 1.3 ActionGroup（动作组）

| 属性 | 说明 |
|------|------|
| 定义 | 多个Action的组合，组内按重要性分级 |
| 位置 | `process/ActionGroup.java` |
| 关系 | 继承 Action; Optional动作失败→跳过继续，Require动作失败→中断抛出 |

### 1.4 ActionLibrary（动作库）

| 属性 | 说明 |
|------|------|
| 定义 | 共享的动作定义注册表——各模块从此选取，不重复定义 |
| 位置 | `process/ActionLibrary.java` |
| 关键方法 | `register(code,name,executeType,importance)`, `get(code)`, `getAll()`, `findByPrefix(prefix)` |

### 1.5 Process（工序）

| 属性 | 说明 |
|------|------|
| 定义 | 工艺上的一个步骤，包含一组有序动作 |
| 位置 | `process/IProcess.java`, `process/Process.java` |
| 关键字段 | `code`, `name`, `order`, `actions`(List\<IAction\>), `resourcePack`, `requireResources` |
| 关键方法 | `execute(inputResources)` — 遍历动作链，前一个输出作为后一个输入 |

### 1.6 IProcessParameter（工艺参数）

| 属性 | 说明 |
|------|------|
| 定义 | 工序的目标参数——设定值 + 控制上下限，支撑 SPC/Cpk 分析 |
| 位置 | `process/IProcessParameter.java` |
| 关键字段 | `code`, `name`, `uom`(UOM), `targetValue`, `lowerLimit`, `upperLimit` |

### 1.7 Action 子系统扩展

| 接口 | 模块 | 执行行为 | 返回结果 | 位置 |
|------|------|---------|---------|------|
| `IEquipmentAction` | equip | 参数下发→状态检查→记录运行数据 | `EquipmentActionResult` | `process/action/` |
| `IQualityAction` | qms | 取样→检测→判定→创建偏差 | `QualityActionResult` | `process/action/` |
| `IDataCollectionAction` | iot | 读取传感器→存储时序→触发报警 | `DataCollectionResult` | `process/action/` |
| `IComplianceAction` | dms/gmp | 清场→双人复核→电子签名 | `ComplianceActionResult` | `process/action/` |
| `IMaterialAction` | wms/lims | 称量→投料→消耗/产出记录 | `MaterialActionResult` | `process/action/` |

### 1.8 TagFilter（标签过滤器）

| 属性 | 说明 |
|------|------|
| 定义 | 按标签从动作的 requireResources 中提取子系统关注的资源 |
| 位置 | `process/TagFilter.java` |
| 方法 | `hasTag(key)`, `hasTagPrefix(prefix)`, `group(SourceGroup)`, `list()`, `first()`, `tagValues()`, `exists()` |

---

## 二、资源域 (resource/)

### 2.1 IResource（资源标识）

| 属性 | 说明 |
|------|------|
| 定义 | 制造系统中最基础的资源抽象（身份标识） |
| 位置 | `resource/IResource.java` |
| 关键字段 | `name`, `group`(SourceGroup), `type`(SourceType) |
| 继承 | `IData` (JSON序列化) |

### 2.2 IResourceItem（资源项）

| 属性 | 说明 |
|------|------|
| 定义 | 带数量管理能力的资源 |
| 位置 | `resource/IResourceItem.java` |
| 关键方法 | `getNumber()`, `getUom()`(UOM), `add(n)`, `use(n)`(负余额抛异常), `put(n)`, `copy()`, `require()` |
| 实现 | `resource/ResourceItem.java` |
| 拓展 | `IScalable<IResourceItem>` |

### 2.3 ResourcePack（资源包）

| 属性 | 说明 |
|------|------|
| 定义 | 资源集合，即BOM/配方组分 |
| 位置 | `resource/IResourcePack.java`, `resource/ResourcePack.java` |
| 关键方法 | `merge()`, `compress()`(合并同名同组资源), `copy()`, `isEmpty()`, `requireResources()` |

### 2.4 4M1E + Product 子类型

| 接口 | 分组 | 固定 getGroup() | 位置 |
|------|------|----------------|------|
| `IMachine` | Machine | `SourceGroup.Machine` | `resource/IMachine.java` |
| `IMaterial` | Material | `SourceGroup.Material` | `resource/IMaterial.java` |
| `IPersonnel` | Personnel | `SourceGroup.Personnel` | `resource/IPersonnel.java` |
| `IMethod` | Method | `SourceGroup.Method` | `resource/IMethod.java` |
| `IEnvironment` | Environment | `SourceGroup.Environment` | `resource/IEnvironment.java` |
| `IUsedResource` | — | 不固定（记录实际使用量） | `resource/IUsedResource.java` |

### 2.5 Dict 枚举

| 枚举 | 值 | 位置 |
|------|-----|------|
| `Execute` | Nothing, Create, Add, Use, Change, Convert, Split, Combine, Transfer, Hold | `shared/Dict.java` |
| `Control` | Default, Interrupt, Count, Timing, Repeat | `shared/Dict.java` |
| `Importance` | Optional, Require | `shared/Dict.java` |
| `SourceGroup` | Other, Personnel, Machine, Material, Method, Environment, Product | `shared/Dict.java` |
| `SourceType` | Other, Machine, Equipment, Instrument, Tooling, Operator, Technician, Inspector, RawMaterial, WIP, FinishedGood, Packaging, Consumable, SOP, Specification, Drawing, Temperature, Humidity, Pressure, Cleanliness | `shared/Dict.java` |

### 2.6 资源标签约定

```
equip.required     equip.tooling      equip.parameter
personnel.skill    personnel.count
qms.method         qms.specification  qms.interval
iot.tag            iot.interval
gmp.clearance      gmp.review
```

### 2.7 UOM（计量单位）

| 类别 | 值 |
|------|-----|
| 质量 | KG, G, MG |
| 体积 | L, ML |
| 长度 | M, CM, MM |
| 计数 | PCS, TAB(片), CAP(粒), BTL(瓶), BOX(箱) |
| 特殊 | CELSIUS(℃), PERCENT(%), BATCH(批), NONE |

---

## 三、工厂物理域 (factory/ + factory/physical/)

### 3.1 Factory（工厂）

| 属性 | 说明 |
|------|------|
| 定义 | 物理生产系统，两层描述 |
| 位置 | `factory/IFactory.java`, `factory/Factory.java` |
| 工艺层 | `getProcesses()` — 能力声明 |
| 物理层 | `getEquipmentPool()` — 全厂设备池; `getWorkstationRegistry()` — 工位注册表; `getProductionLines()` — 产线列表 |
| 查询 | `findWorkstationsFor(actionCode)`, `canExecute(actionCode)`, `findLinesUsing(workstationCode)` |

### 3.2 Workstation（工位）

| 属性 | 说明 |
|------|------|
| 定义 | 工厂地板上的一个物理位置，安装了一组设备 |
| 位置 | `factory/physical/IWorkstation.java`, `factory/physical/Workstation.java` |
| 关键字段 | `code`, `name`, `stationType`(StationType), `equipmentBindings` |
| 特征 | **工厂级共享资源**，可被多条产线引用 |

### 3.3 EquipmentBinding（设备-动作绑定）

| 属性 | 说明 |
|------|------|
| 定义 | 一台设备的能力声明——能执行哪些动作 |
| 位置 | `factory/physical/IEquipmentBinding.java`, `factory/physical/EquipmentBinding.java` |
| 关键字段 | `equipmentCode`, `supportedActionCodes`(Set\<String\>), `isPrimary`, `parameters` |
| 特征 | **动作-设备匹配的唯一依据** |

### 3.4 StationType（工位编组类型）

| 类型 | 含义 | 匹配行为 |
|------|------|---------|
| `SINGLE` | 单设备 | 这台能执行即可 |
| `PARALLEL` | 多台并行 | 任一台能执行即可，产能叠加 |
| `SERIES` | 多台串联 | 每个动作需对应设备 |
| `PRIMARY_BACKUP` | 主备 | 主设备优先，不可用时切备设备 |

### 3.5 ProductionLine（产线）

| 属性 | 说明 |
|------|------|
| 定义 | **预设配置**——工序序列 + 设备分配 + 衔接参数，不是物理约束 |
| 位置 | `factory/physical/IProductionLine.java`, `factory/physical/ProductionLine.java` |
| 关键 | `getNodes()`(LineNode序列); `getConnections()`(LineConnection衔接); `getEquipmentAssignments()`(预设分配); `getSupportedProcessCodes()` |

### 3.6 LineNode（产线节点）

| 属性 | 说明 |
|------|------|
| 定义 | 工位在产线中的位置——引用工位编码+顺序号 |
| 位置 | `factory/physical/LineNode.java` |
| 关键字段 | `workstationCode`, `sequence` |

### 3.7 LineConnection（产线衔接）

| 属性 | 说明 |
|------|------|
| 定义 | 两道工序之间的衔接参数——转运时间+缓冲容量 |
| 位置 | `factory/physical/LineConnection.java` |
| 关键字段 | `fromProcessCode`, `toProcessCode`, `transferTime`(Duration), `bufferCapacity` |

---

## 四、产品-蓝图域 (factory/)

### 4.1 IProduct / IProductInfo / IProductItem（产品层级）

| 接口 | 定义 | 位置 |
|------|------|------|
| `IProduct` | 产品标识，持有 IProductInfo | `factory/IProduct.java` |
| `IProductInfo` | 产品详情+蓝图引用，继承 IResourceItem(Product组) | `factory/IProductInfo.java` |
| `IProductItem` | 携带产品信息的物理实体 | `factory/IProductItem.java` |
| `IEstimateResource` | 设计阶段资源定额估算(值+偏差+范围) | `factory/IEstimateResource.java` |

### 4.2 IBlueprint（蓝图）

| 属性 | 说明 |
|------|------|
| 定义 | 产品的工艺路线定义——静态骨架，不携带设备/阶段信息 |
| 位置 | `factory/IBlueprint.java` |
| 关键 | `code`, `version`, `getProductionProcessList()` |
| 关系 | 被 CustomerSpec → EngineeringBOM → WorkOrder 逐层引用 |

### 4.3 蓝图三阶段

```
CustomerSpec (客户规格书)
  客户/PLM定义 → 标注物料需求+质量标准+法规要求
  位置: factory/CustomerSpec.java

        ↓ 工程转换

EngineeringBOM (工程标注书)
  工厂工程部标注 → 动作→设备映射 + 工艺参数
  位置: factory/EngineeringBOM.java
  内部记录: EquipmentAssignment(equipmentCode, parameters)

        ↓ 生产执行

WorkOrder (工单)
  MES创建 → 引用最终 Blueprint + EngineeringBOM + Batch
  位置: batch/IWorkOrder.java
```

### 4.4 IBillOfMaterial（物料清单）

| 属性 | 说明 |
|------|------|
| 定义 | 产品所需全部物料的清单（不分工序） |
| 位置 | `factory/IBillOfMaterial.java`, `factory/BillOfMaterial.java` |
| 关键字段 | `productCode`, `materials`(List\<MaterialLineItem\>) |

---

## 五、执行追溯域 (batch/)

### 5.1 IWorkOrder（工单）

| 属性 | 说明 |
|------|------|
| 定义 | 一个生产任务 |
| 位置 | `batch/IWorkOrder.java` |
| 关键字段 | `workOrderNo`, `productCode`, `blueprint`, `quantity`, `batchNo`, `factoryCode`, `status`(WorkOrderStatus), 计划/实际时间 |
| 实现 | `easy-factory-mes: MesWorkOrder extends BaseLifecycleEntity<WorkOrderStatus> implements IWorkOrder` |

### 5.2 IBatch（批次）

| 属性 | 说明 |
|------|------|
| 定义 | 制造追溯基本单元——GMP批记录核心 |
| 位置 | `batch/IBatch.java` |
| 关键字段 | `batchNo`, `productCode`, `blueprint`(冻结快照), `batchSize`, `actualYield`, `yieldPercent`, `status`(BatchStatus), `traceRecords` |
| WIP追踪 | `getCurrentProcessCode()`, `getCurrentWorkstationCode()` |
| 物料谱系 | `getParentBatchNos()`(合并来源), `getChildBatchNos()`(拆分去向) |

### 5.3 ITraceable（追溯记录）

| 属性 | 说明 |
|------|------|
| 定义 | 一次资源操作的完整记录——"谁、何时、在哪批、做了什么" |
| 位置 | `batch/ITraceable.java` |
| 关键字段 | `batchNo`, `processCode`, `actionCode`, `operator`, `timestamp`, `operationType`, `beforeSnapshot`(JSON), `afterSnapshot`(JSON), `remark` |

### 5.4 IInspectionOrder（检验指令）

| 属性 | 说明 |
|------|------|
| 定义 | 一次检验任务 |
| 位置 | `batch/IInspectionOrder.java` |
| 关键字段 | `inspectionNo`, `type`(IQC/IPQC/FQC/OQC), `planCode`, `status`(InspectionStatus), `passedCount`, `failedCount` |
| 实现 | `easy-factory-qms: InspectionOrder extends BaseLifecycleEntity<InspectionStatus> implements IInspectionOrder` |

### 5.5 状态枚举（9个，均实现 ILifecycle.StatusEnum）

| 枚举 | 状态流转 | 使用者 |
|------|---------|--------|
| `WorkOrderStatus` | CREATED→RELEASED→IN_PROGRESS→COMPLETED→CLOSED (CANCELLED) | MES |
| `BatchStatus` | CREATED→IN_PROGRESS→COMPLETED→UNDER_REVIEW→APPROVED→RELEASED→ARCHIVED (REJECTED→返工, CANCELLED) | MES |
| `MachineStatus` | IDLE→RUNNING\|SETUP\|MAINTENANCE; RUNNING→IDLE\|FAULT; FAULT→MAINTENANCE→IDLE | EQUIP |
| `InspectionStatus` | PENDING→IN_PROGRESS→PASSED\|FAILED→CLOSED | QMS |
| `DocumentStatus` | DRAFT→UNDER_REVIEW→APPROVED\|REJECTED→OBSOLETE | DMS |
| `AndonStatus` | OPEN→ACKNOWLEDGED→RESOLVED\|ESCALATED→CLOSED | ANDON |
| `AssetStatus` | IDLE→IN_USE\|UNDER_MAINTENANCE→SCRAPPED | EAM |
| `ProductionPlanStatus` | DRAFT→APPROVED→RELEASED→IN_PROGRESS→COMPLETED→CLOSED | MPS |
| `ScheduleStatus` | DRAFT→OPTIMIZED→DISPATCHED→IN_PROGRESS→COMPLETED→CANCELLED | APS |
| `ReceiptStatus` | PENDING→PARTIAL→COMPLETED→CLOSED | WMS |

---

## 六、运营分析域 (operation/)

### 6.1 能力匹配 (capability/)

| 类/接口 | 作用 |
|---------|------|
| `IMatchingStrategy` | 匹配策略接口: `match(IBlueprint, IFactory) → RouteMatchResult` |
| `DirectEquipmentStrategy` | 默认策略：动作直接在全厂设备池匹配，忽略产线 |
| `LineFirstStrategy` | 线优先策略：先在产线内匹配，不匹配再回退设备池 |
| `ProcessRouteMatcher` | 匹配引擎（实现 IOperation\<MatchInput, RouteMatchResult\>），输出每个动作的 EQUIPPED/DEGRADED/SKIPPED/MISSING 状态 |

### 6.2 时间模型 (time/)

| 类/接口 | 作用 |
|---------|------|
| `ITimed` | 基础: setupTime, processingTime, teardownTime, TimeNature(FIXED/LINEAR/FUZZY), `estimateTotal(batchSize)` |
| `IIntervalTimed` | PERT三点估计: min/max/typical, getPertEstimate(), getUncertainty() |
| `IStatisticalTimed` | 统计: σ, CV, ±Nσ界限, isStable() |
| `IConstrainedTimed` | GMP约束: maxAllowed, minRequired, shelfLife, checkConstraints() |
| `ICategorizedTimed` | 精益: TimeCategory(VALUE_ADDED/SETUP/INSPECTION/TRANSFER/WAIT/WASTE), setup比率, SMED判断 |
| `ActionDuration` | 标准实现 (implements ITimed) |
| `GmpActionDuration` | GMP合规实现 (extends ActionDuration implements IConstrainedTimed) |
| `PreciseActionDuration` | 精确实现 (extends ActionDuration implements IIntervalTimed, IStatisticalTimed) |
| `ProcessCycleTime` | 工序周期时间计算（支持 SEQUENTIAL/PARALLEL/GROUPED） |
| `ProductionLeadTime` | 总提前期 = Σ(周期时间 + 排队时间 + 转运时间) |

> **注**: 计算器入参已泛型化为 `Map<String, ? extends ITimed>`，GmpActionDuration/PreciseActionDuration 可直接传入。

### 6.3 资源/产能 (resource/ + capacity/)

| 类 | 作用 |
|----|------|
| `BatchScaledCalculator` | 批量缩放——从标准批量缩放到目标批量 |
| `ResourceRequirementExploder` | 资源需求展开——含良率调整和批量缩放 |
| `FactoryCapacityProfile` | 工厂产能画像——设备OEE、人员日工时、年工作天数 |
| `BottleneckDetector` | 瓶颈识别——每工序 日可用时间÷单件周期=日产能，最小者即瓶颈 |

### 6.4 公共抽象 (common/)

| 类/接口 | 作用 |
|---------|------|
| `IReportable` | 可生成格式化报告: `toReport()`, `toJson()`, `printReport()` |
| `IOperation<I,O>` | 通用操作模式: `getName()`, `execute(I)`→`O extends OperationResult` |
| `OperationResult` | 操作结果基类: 耗时/消息/成功标志/格式化输出 |
| `ReportPrinter` | 报告打印工具: 表格/树形/缩进/颜色 + `formatDuration()` |
| `IScalable<T>` | 缩放契约: `scaleBy(factor)`, `scaleTo(target, standard)` |
| `ExecutionMode` | 执行模式: SEQUENTIAL / PARALLEL / GROUPED |

---

## 七、横切域

### 7.1 生命周期 (lifecycle/)

| 类 | 说明 |
|----|------|
| `ILifecycle<S extends StatusEnum>` | 泛型状态机: `transition(target)` 校验转换合法性 |
| `ILifecycle.StatusEnum` | 嵌套接口: `name()`, `allowedTransitions()` |

### 7.2 领域事件 (event/)

| 类 | 说明 |
|----|------|
| `IDomainEvent` | 事件契约: eventId, eventType, timestamp, source, payload |
| `DomainEventPublisher` | 同步发布/订阅 |
| `AuditTrail` | 审计追踪记录(record): entityType, entityId, action, operator, timestamp, before(JSON), after(JSON), reason |
| `IElectronicSignature` | 电子签名接口: signerId, meaning, timestamp, reason |
| `SignatureMeaning` | 签名含义枚举: REVIEWED \| APPROVED \| VERIFIED |

### 7.3 审计人追踪 (shared/)

| 类 | 说明 |
|----|------|
| `IAuditable` | 可审计接口: getCreatedBy(), getUpdatedBy(), setCreatedBy(), setUpdatedBy() |

### 7.4 Trait 接口 (shared/)

| 接口 | 契约 |
|------|------|
| `HasCode` | `getCode(): String` |
| `HasName` | `getName(): String` |
| `HasStatus<S>` | `getStatus(): S` |
| `HasVersion` | `getVersion(): String` |
| `HasTimestamps` | `getCreatedAt()`, `getUpdatedAt()`, `markUpdated()` |

### 7.5 抽象基类 (shared/)

| 类 | 组合 | 模块继承 |
|----|------|---------|
| `BaseEntity` | HasCode+HasName+HasTimestamps+DataExpand | PLM, LIMS, ERP, IoT, SCM (5个) |
| `BaseLifecycleEntity<S>` | BaseEntity+ILifecycle+HasStatus | MES, QMS, EAM, MPS, APS, WMS, ANDON, DMS (8个) |
| `AbstractResourceItem` | BaseEntity+IResourceItem(add/use/copy默认实现) | EQUIP (1个) |

### 7.6 哨兵对象 (shared/)

| 类 | 说明 |
|----|------|
| `EmptyAction.Instance` | 空动作——不执行任何操作 |
| `EmptyResourcePack.Instance` | 空资源包——永不为null的占位符 |
| `ResourceItem.EMPTY_ARRAY` | 空资源数组常量 |

### 7.7 仓储 (repository/)

| 类 | 说明 |
|----|------|
| `IRepository<T,ID>` | 泛型CRUD: findById, findAll, save, saveAll, delete, deleteById, existsById, count |
| `IPageRequest` | 分页参数: page, size, sort |
| `IPageResult<T>` | 分页结果: content, totalElements, totalPages |
| `Sort` | 排序: Direction(ASC/DESC) + Order(property, direction) |

### 7.8 脚本引擎 (script/)

| 类 | 状态 |
|----|------|
| `IScriptEngine` | 脚本引擎接口 |
| `IScript` | 脚本接口 |
| `ScriptContext` | 执行上下文（process/services/inputResources） |
| `ScriptMetadata` | 脚本元数据（@id @name @version @module） |
| `ScriptRegistry` | 脚本注册表（从 registry.json 加载） |
| `ScriptEngines` | 引擎注册中心 |
| `ScriptExecutor` | @Deprecated — Nashorn 实现 (JDK15+ 已移除) |
| `GraalScriptEngine` | 骨架 — 待 GraalJS 依赖就绪后启用 |

---

## 八、模块集成状态

### 8.1 当前实现矩阵

| 模块 | 继承 Core 基类 | 实现 Core 接口 | 使用 Core 枚举 | 文件数 |
|------|---------------|---------------|---------------|--------|
| **MES** | `BaseLifecycleEntity<WorkOrderStatus>` | `IWorkOrder` | `WorkOrderStatus` | 2 |
| **QMS** | `BaseLifecycleEntity<InspectionStatus>` | `IInspectionOrder` | `InspectionStatus` | 2 |
| **EQUIP** | `AbstractResourceItem` | `IMachine` | `SourceGroup`, `SourceType` | 2 |
| **PLM** | `BaseEntity` | `HasVersion` | — | 2 |
| **LIMS** | `BaseEntity` | `HasVersion` | — | 2 |
| **ERP** | `BaseEntity` | — | — | 2 |
| **IoT** | `BaseEntity` | — | — | 2 |
| **EAM** | `BaseLifecycleEntity<AssetStatus>` | — | `AssetStatus` | 2 |
| **MPS** | `BaseLifecycleEntity<ProductionPlanStatus>` | — | `ProductionPlanStatus` | 2 |
| **APS** | `BaseLifecycleEntity<ScheduleStatus>` | — | `ScheduleStatus` | 2 |
| **WMS** | `BaseLifecycleEntity<ReceiptStatus>`+`BaseEntity` | — | `ReceiptStatus` | 3 |
| **ANDON** | `BaseLifecycleEntity<AndonStatus>` | — | `AndonStatus` | 2 |
| **BI** | `DataExpand`(from common) | — | — | 2 |
| **SCM** | `BaseEntity` | — | — | 2 |
| **DMS** | `BaseLifecycleEntity<DocumentStatus>` | — | `DocumentStatus` | 2 |

> 所有模块目前均为 model + service interface 存根，service 实现层待开发。

### 8.2 三条集成总线

```
① 接口实现 (编译期契约)
   module implements ICoreInterface
   → 编译期类型安全，其它模块依赖接口而非实现

② IExpand 动态属性 (运行时挂载)
   process.set("mes.workOrderId", "WO-001")  // MES写入
   process.get("qms.inspectionLevel")         // QMS读取
   TagFilter.of(resources).hasTagPrefix("iot.")  // IoT提取
   → 模块间不产生编译期依赖

③ 领域事件 (异步解耦)
   MES发布 "mes.process.started"
   → QMS订阅 → 自动创建检验指令
   → EQUIP订阅 → 下发设备参数
```

---

## 九、名词关系总图

```
产品定义层                 工厂物理层                  执行追溯层
─────────                 ─────────                  ─────────

Blueprint                 Factory                    WorkOrder
  └── Process               ├── Workstation(注册表)      ├── Blueprint引用
        └── Action          │     └── EquipmentBinding   │── Batch引用
              │             │           ├── code         │── 时间安排
              │ 标签        │           └── actions      └── 执行记录
              ▼             │                                  │
        requireResources    └── ProductionLine(预设)           ▼
        (IResourceItem[])       ├── LineNode(位置)        TraceRecord
              │                 └── LineConnection(衔接)    ├── batchNo
              ▼                                            ├── actionCode
          TagFilter                                        └── snapshot
         各子系统提取
              │
              ▼
    4M1E: Personnel | Machine | Material | Method | Environment
              │
              ▼
         UOM: KG | G | L | PCS | CELSIUS | PERCENT | ...

匹配:  Action.code ←→ EquipmentBinding.supportedActionCodes
策略:  DirectEquipmentStrategy (全厂设备池直接匹配)
       LineFirstStrategy (产线优先 → 回退设备池)
状态:  ILifecycle<S>.transition(target)  ← 9个Status枚举
事件:  IDomainEvent → DomainEventPublisher → 各模块订阅
审计:  AuditTrail(entityType, entityId, action, operator, before, after, reason)
签名:  IElectronicSignature(signerId, meaning, timestamp, reason)
```

---

## 十、统计

```
接口:     43 个
类:       25 个
枚举:     18 个  (Execute|Control|Importance|SourceGroup|SourceType|UOM|SignatureMeaning
                  |9个Status枚举|StationType|ExecutionMode|TimeCategory)
记录:      3 个  (AuditTrail|LineConnection|LineNode)
异常:      2 个  (ActionException|ResourceException)
─────────────────────
合计:     91 个定义
```
