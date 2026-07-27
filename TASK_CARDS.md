# A+B 任务卡：持久化 + Service 实现

> 基础设施：PostgreSQL 16 + Spring Data JPA + H2(test)
> JPA 扫描已配置：`easy-factory-web/.../config/JpaConfig.java`

## 每个模块的标准产出

```
Part B（持久化）:
  1. Model 类加 JPA 注解（@Entity @Table @Column）
  2. repository/{Entity}Repository.java（extends JpaRepository）
  3. ddl/V1.{x}__{module}.sql（CREATE TABLE + INDEX）

Part A（Service 实现）:
  4. service/impl/{Module}ServiceImpl.java（实现已有 Service 接口）
  5. 注入 Repository，调用 DomainEventPublisher.publish()
  6. @Transactional 读写操作

Part C（验证）:
  7. mvn test -pl easy-factory-{module}   ← 本模块
  8. mvn test                              ← 全项目不破
  9. PROJECT_STATUS.md 更新记录
```

## 通用规范

```
JPA 注解:
  - BaseEntity 子类: code/name 已有 @Column(unique=true)
  - BaseLifecycleEntity 子类: 额外 status @Enumerated(STRING)
  - AbstractResourceItem 子类: 额外 group/type/number
  - 关联: @OneToMany(cascade=ALL, orphanRemoval=true) + @JoinColumn

ServiceImpl 规范:
  - @Service @Transactional
  - 构造器注入 Repository（不用 @Autowired 字段注入）
  - 读写方法: entity = repo.findById(id) → entity.业务方法() → repo.save(entity)
  - 事件: 状态变更后 DomainEventPublisher.publish(event)
  - 查找: repo.findByCode(code) 或 repo.findAll()

DDL 规范:
  - 文件名: ddl/V1.{序号}__{module}.sql
  - 每个模块自己建（不存在"已有人帮你建好"）
  - 列名用下划线，Java 驼峰 → SQL 下划线
  - 每个表都有 created_at / updated_at (TIMESTAMPTZ DEFAULT NOW())
```

---

## 第一批：无依赖模块（6 并行）

### erp — 物料/库存/事务

```
✅ 已完成（示例模块，不再分配）

Part B 产出:
  model 已加 @Entity @Table @Column @Enumerated
  repository/: MaterialCacheRepository, InventorySnapshotRepository, TransactionRepository
  ddl/V1.1__erp.sql (3 表, 60 行)

Part A 产出: 待补充
  ⬜ service/impl/ErpAdapterServiceImpl.java
     实现 ErpAdapterService 15 个方法:
       syncMaterials() — repo.findAll() 或调用外部 ERP API
       findByCode(code) — repo.findByCode(code)
       queryStock(...) — InventorySnapshotRepository 查询
       postGoodsIssue/Receipt/Transfer(...) — 创建 Transaction → repo.save()
       getPendingTransactions() — repo.findByStatus(PENDING)
       retryFailedTransactions() — 遍历 FAILED → 重置 PENDING → repo.save()
     事务回传无需对接真实 ERP，Mock 实现即可（模拟发送，标记 SENT → CONFIRMED）
```

### scm — 供应商/采购

```
模块: easy-factory-scm | Service: ScmService (14 方法)

Part B:
  model 加 JPA 注解: Supplier, PurchaseOrder, PurchaseOrderItem
    @OneToMany: PurchaseOrder.items → PurchaseOrderItem
  repository/: SupplierRepository, PurchaseOrderRepository
  ddl/V1.2__scm.sql: scm_supplier, scm_purchase_order, scm_purchase_order_item

Part A:
  service/impl/ScmServiceImpl.java
    核心方法:
      registerSupplier(code,name,category) → new Supplier() → repo.save()
      createPurchaseOrder(poNo,supplierCode) → new PurchaseOrder() → repo.save()
      approvePurchaseOrder(poNo,by) → find → po.approve(by) → repo.save() → publish event
      sendPurchaseOrder(poNo) → find → po.send() → repo.save()
      receivePurchaseOrder(poNo,mat,qty) → find → po.addItem()/receive → repo.save()
      addItem 后自动判断 isFullyReceived() → complete()
```

### equip — 设备/参数/配方/OEE

```
模块: easy-factory-equip | Service: EquipmentService (16 方法)

Part B:
  model 加 JPA 注解: Equipment, EquipmentParameter, EquipmentRecipe(+RecipePhase)
    OEMetrics 也需要注解
    Equipment 继承 AbstractResourceItem（注意 group/type/number 映射）
    @OneToMany: EquipmentRecipe.phases → RecipePhase
  repository/: EquipmentRepository, EquipmentParameterRepository,
              EquipmentRecipeRepository, OEMetricsRepository
  ddl/V1.3__equip.sql: 5 表

Part A:
  service/impl/EquipmentServiceImpl.java
    核心方法:
      registerEquipment(...) → new Equipment() → repo.save()
      startProduction(code) → find → eq.startProduction() → repo.save() → publish equip.production.started
      reportFault(code,reason) → find → eq.reportFault() → repo.save() → publish equip.fault.reported
      createRecipe(...) → new EquipmentRecipe() → repo.save()
      dispatchRecipe(code) → 发布 equip.recipe.dispatched
      calculateOee(code,start,end,a,p,q) → OEMetrics.of(...) → repo.save() → publish equip.oee.calculated
```

### plm — 蓝图/工艺模板/参数/变更

```
模块: easy-factory-plm | Service: BlueprintService (30+ 方法)

Part B:
  model 加 JPA 注解: Blueprint, ProcessTemplate, ProcessParameter, ChangeRequest
    Blueprint.processes → @Convert(JSON) 存 List<IProcess>
  repository/: BlueprintRepository, ProcessTemplateRepository,
              ProcessParameterRepository, ChangeRequestRepository
  ddl/V1.4__plm.sql: 4 表

Part A:
  service/impl/BlueprintServiceImpl.java
    核心方法:
      createBlueprint(template,productCode) → new Blueprint() → repo.save()
      submitForReview(code) → find → bp.submitForReview() → repo.save() → publish plm.blueprint.submitted
      approve(code,by) → find → bp.approve(by) → repo.save() → publish plm.blueprint.approved
      releaseVersion(code) → find → bp.release() → repo.save() → publish plm.blueprint.released
      diff(code,vA,vB) → 两个版本对比 → BlueprintDifferImpl.compare()
      注意: Blueprint 当前 Model 层已在便捷方法中内嵌事件发布（历史遗留）。
      这里 ServiceImpl 不要重复发布——要么去掉 Model 层的发布，要么 ServiceImpl 只调 Model 方法。
```

### iot — 设备连接/标签/指令/报警

```
模块: easy-factory-iot | Service: IotGatewayService (13 方法)

Part B:
  model 加 JPA 注解: DeviceConnection, TagValue, Command, AlarmEvent
    Command.parameters → @Convert(JSON) 存 Map<String,Object>
  repository/: DeviceConnectionRepository, TagValueRepository,
              CommandRepository, AlarmEventRepository
  ddl/V1.5__iot.sql: 4 表

Part A:
  service/impl/IotGatewayServiceImpl.java
    核心方法:
      register(conn) → repo.save() → publish iot.device.connected
      readTags(code,tags) → TagValueRepository 查询
      sendCommand(code,type,params) → new Command() → repo.save() → cmd.markSent() → publish iot.command.sent
      startPolling(code) → 定时任务（用 @Scheduled 模拟）
      getActiveAlarms() → AlarmEventRepository 查未恢复的
      acknowledgeAlarm(code,by) → find → alarm.acknowledge(by) → repo.save() → publish iot.alarm.acknowledged
```

### dms — 文档/审批流

```
模块: easy-factory-dms | Service: DmsService (19 方法)

Part B:
  model 加 JPA 注解: Document, ApprovalWorkflow, ApprovalStep
    @OneToMany: ApprovalWorkflow.steps → ApprovalStep
  repository/: DocumentRepository, ApprovalWorkflowRepository
  ddl/V1.6__dms.sql: 3 表

Part A:
  service/impl/DmsServiceImpl.java
    核心方法:
      createDraft(code,title,category,author) → new Document() → repo.save()
      submitForReview(code) → find → doc.submitForReview() → repo.save() → publish dms.document.submitted
      approve(code,by,comment) → find → doc.approve(by) → repo.save() → publish dms.document.approved
      makeEffective(code,date) → find → doc.makeEffective(date) → repo.save() → publish dms.document.effective
      obsolete(code,reason) → find → doc.obsolete(reason) → repo.save() → publish dms.document.obsoleted
      createApprovalWorkflow(code,initiator) → new ApprovalWorkflow() → repo.save()
      executeApprovalStep(wfId,approver,decision,comment) → wf.executeCurrentStep() → repo.save()
      Document 当前 Model 层内嵌事件发布（历史遗留），ServiceImpl 可选择不重复发布。
```

---

## 第二批：依赖第一批（3 并行）

### lims — 配方/称量/批记录

```
模块: easy-factory-lims | Service: FormulaService(10) + WeighingTaskService(7) + BatchRecordService(8)

Part B:
  model 加 JPA 注解: Formula(+FormulaPhase), WeighingTask(+WeighingItem), BatchRecord
    @OneToMany: Formula.phases → FormulaPhase, WeighingTask.items → WeighingItem
  repository/: FormulaRepository, WeighingTaskRepository, BatchRecordRepository
  ddl/V1.7__lims.sql: 5 表

Part A:
  service/impl/FormulaServiceImpl, WeighingTaskServiceImpl, BatchRecordServiceImpl
    FormulaServiceImpl:
      create(code,name,productCode,components) → new Formula() → repo.save()
      activate(code) → find → f.activate() → repo.save() → publish lims.formula.activated
      retire(code,reason) → find → f.retire(reason) → repo.save() → publish lims.formula.retired
    WeighingTaskServiceImpl:
      create(...) → new WeighingTask() → repo.save()
      recordWeighingItem(...) → find task → item.recordWeighing(...) → 检测超差 → getDeviatedItems()
      complete(code) → find → task.completeWeighing() → repo.save() → publish lims.weighing.completed
    BatchRecordServiceImpl:
      create(...) → new BatchRecord() → repo.save()
      approve(batchNo,reviewer) → find → br.approve(reviewer) → repo.save() → publish lims.batch_record.approved
```

### wms — 库位/收货/库存/拣料

```
模块: easy-factory-wms | Service: WmsService (20 方法)

Part B:
  model 加 JPA 注解: Storage, Receipt(+ReceiptItem), InventorySnapshot, PickingTask(+PickingTaskItem)
    @OneToMany: Receipt.items → ReceiptItem, PickingTask.items → PickingTaskItem
  repository/: StorageRepository, ReceiptRepository, InventorySnapshotRepository, PickingTaskRepository
  ddl/V1.8__wms.sql: 6 表

Part A:
  service/impl/WmsServiceImpl.java
    核心方法:
      createReceipt(refNo,supplier) → new Receipt() → repo.save()
      acceptAndPutaway(receiptNo,location) → find → acceptItem() → repo.save()
      createPickingTask(workOrderNo,batchNo) → new PickingTask() → repo.save()
      allocateStock(mat,batch,loc,qty) → find snapshot → snapshot.allocate(qty) → repo.save()
      queryAvailableStock(mat,batch) → 汇总所有库位 onHandQty - allocatedQty
```

### mps — 生产计划/需求

```
模块: easy-factory-mps | Service: MpsService (14 方法)

Part B:
  model 加 JPA 注解: ProductionPlan(+PlanItem), DemandSource
    @OneToMany: ProductionPlan.items → PlanItem
  repository/: ProductionPlanRepository, DemandSourceRepository
  ddl/V1.9__mps.sql: 3 表

Part A:
  service/impl/MpsServiceImpl.java
    核心方法:
      create(periodType,start,end) → new ProductionPlan() → repo.save()
      approve(planNo,by) → find → plan.approve(by) → repo.save() → publish mps.plan.approved
      release(planNo) → find → plan.release() → repo.save() → publish mps.plan.released
      registerDemand(type,refNo,productCode,qty,dueDate) → new DemandSource() → repo.save()
      checkCapacity(planNo,factoryCode) → 调用 core FactoryCapacityProfile/BottleneckDetector → CapacityCheck
```

---

## 第三批：依赖第二批（串行：先 mes 后 aps）

### mes — 工单/工序/动作（集成中枢）★

```
模块: easy-factory-mes | Service: WorkOrderService (11 方法)

Part B:
  model 加 JPA 注解: MesWorkOrder, ProcessRecord, ActionRecord
    ActionRecord.inputResources/outputResources → @Convert(JSON)
  repository/: WorkOrderRepository, ProcessRecordRepository, ActionRecordRepository
  ddl/V1.10__mes.sql: 3 表

Part A:
  service/impl/WorkOrderServiceImpl.java
    核心方法:
      create(blueprintCode,productCode,quantity) → new MesWorkOrder() → repo.save()
      release(woNo) → find → wo.release() 冻结 blueprintVersion → repo.save() → publish mes.workorder.released
      start(woNo) → find → wo.start() → repo.save() → publish mes.workorder.started
      executeProcess(woNo,processCode) → 创建 ProcessRecord → 遍历 Action 链
        → 每步创建 ActionRecord（ITraceable before/after 快照）
        → 遇到 Control.Interrupt → 暂停 → publish mes.process.interrupted
      reportAction(...) → 生成 ActionRecord → repo.save() → publish mes.action.completed
      complete(woNo) → find → wo.complete() → repo.save() → publish mes.workorder.completed
```

### aps — 排程/资源日历

```
模块: easy-factory-aps | Service: ApsService (5 方法)

Part B:
  model 加 JPA 注解: Schedule(+ScheduledTask), ResourceCalendar, RescheduleTrigger
    @OneToMany: Schedule.tasks → ScheduledTask
    ScheduledTask.predecessors → @Convert(JSON)
  repository/: ScheduleRepository, ResourceCalendarRepository, RescheduleTriggerRepository
  ddl/V1.11__aps.sql: 4 表

Part A:
  service/impl/ApsServiceImpl.java
    核心方法:
      createSchedule(planNo,factoryCode) → new Schedule() → repo.save()
      optimize(code,strategy) → find → SchedulingRule.of(strategy).sort(tasks) → repo.save()
      dispatch(code) → find → schedule.dispatch() → repo.save() → publish aps.schedule.released
      reschedule(code,triggerEvent) → 记录 RescheduleTrigger → 重新 optimize() → repo.save()
      getSchedule(code) → repo.findByCode(code)
```

---

## 第四批：依赖第三批（3 并行）

### qms — 检验/偏差/CAPA

```
模块: easy-factory-qms | Service: InspectionService(12) + DeviationService(13) + CapaService(10)

Part B:
  model 加 JPA 注解: InspectionOrder, InspectionPlan, InspectionRecord, Deviation, Capa
  repository/: InspectionOrderRepository, InspectionPlanRepository,
              InspectionRecordRepository, DeviationRepository, CapaRepository
  ddl/V1.12__qms.sql: 5 表

Part A:
  service/impl/InspectionServiceImpl, DeviationServiceImpl, CapaServiceImpl
    InspectionServiceImpl:
      createOrder(...) → new InspectionOrder() → repo.save()
      recordResult(orderCode,result) → find → record result → isInControl() 检查 → repo.save()
    DeviationServiceImpl:
      createDeviation(...) → new Deviation() → repo.save() → publish qms.deviation.created
      investigate(code,findings) → find → dev.investigate() → repo.save()
      resolve(code,disposition) → find → dev.resolve() → repo.save() → publish qms.deviation.resolved
    CapaServiceImpl:
      createCapa(deviationCode,...) → new Capa() → repo.save()
      verifyEffectiveness(code,result) → find → capa.verify() → repo.save()
```

### andon — 安灯/上报（依赖 mes, iot, qms）

```
模块: easy-factory-andon | Service: AndonService

Part B:
  model 加 JPA 注解: AndonCall, EscalationRule, AndonDashboard
    (AndonSeverity, AndonSource, TriggerType 是值枚举，不建表)
  repository/: AndonCallRepository, EscalationRuleRepository, AndonDashboardRepository
  ddl/V1.13__andon.sql: 3 表

Part A:
  service/impl/AndonServiceImpl.java
    核心方法:
      createCall(source,severity,message) → new AndonCall() → repo.save() → publish andon.call.created
      acknowledge(callCode,by) → find → call.acknowledge(by) → repo.save() → publish andon.call.acknowledged
      escalate(callCode) → find → call.escalate() → repo.save() → publish andon.call.escalated
      resolve(callCode) → find → call.resolve() → repo.save() → publish andon.call.resolved
    订阅外部事件（在 @PostConstruct 中注册）:
      DomainEventPublisher.subscribe("mes.process.interrupted", this::onProcessInterrupted)
      DomainEventPublisher.subscribe("iot.alarm.triggered", this::onAlarmTriggered)
```

### eam — 资产/维护/校准

```
模块: easy-factory-eam | Service: EamService (10 方法)

Part B:
  model 加 JPA 注解: Asset, MaintenanceOrder, CalibrationRecord
    MaintenanceOrder.spareParts → @Convert(JSON)
  repository/: AssetRepository, MaintenanceOrderRepository, CalibrationRecordRepository
  ddl/V1.14__eam.sql: 3 表

Part A:
  service/impl/EamServiceImpl.java
    核心方法:
      registerAsset(...) → new Asset() → repo.save()
      createMaintenanceOrder(...) → new MaintenanceOrder() → repo.save()
      completeMaintenance(orderCode) → find → mo.complete() → repo.save() → publish eam.maintenance.completed
      recordCalibration(...) → new CalibrationRecord() → repo.save()
      getDueCalibrations() → 查 nextDue < now() 的记录
```

---

## 第五批：独立/只读（2 并行）

### bi — 看板/KPI

```
模块: easy-factory-bi | Service: DashboardService

Part B:
  model 加 JPA 注解: KpiSnapshot, ProductionDashboard, QualityDashboard, OeeDashboard
  repository/: KpiSnapshotRepository, ProductionDashboardRepository,
              QualityDashboardRepository, OeeDashboardRepository
  ddl/V1.15__bi.sql: 4 表

Part A:
  service/impl/DashboardServiceImpl.java
    核心方法:
      computeKpiSnapshot(period) → 聚合 MES/QMS/Equip 数据 → new KpiSnapshot() → repo.save()
      getProductionDashboard() → 从 ProductionDashboardRepository 查最新
      refreshAllDashboards() → 订阅事件触发刷新
    BI 是只读消费者，数据从其他模块的事件中聚合，不直接修改其他模块数据。
```

### crm — 客户/订单/投诉

```
模块: easy-factory-crm | Service: CrmService

Part B:
  model 加 JPA 注解: Customer, SalesOrder(+SalesOrderItem), Complaint
    @OneToMany: SalesOrder.items → SalesOrderItem
  repository/: CustomerRepository, SalesOrderRepository, ComplaintRepository
  ddl/V1.16__crm.sql: 4 表

Part A:
  service/impl/CrmServiceImpl.java
    核心方法:
      registerCustomer(...) → new Customer() → repo.save()
      createSalesOrder(...) → new SalesOrder() → repo.save()
      confirmOrder(orderCode) → find → so.confirm() → repo.save() → publish crm.order.confirmed
      createComplaint(...) → new Complaint() → repo.save() → publish crm.complaint.received
```

---

## 提交前硬性检查

```
Part B:
  mvn test -pl easy-factory-{module}   ← 模块测试全绿
  ddl/V1.{x}__{module}.sql 存在
  {module}/repository/ 包下有 JPA Repository

Part A:
  {module}/service/impl/ 包下有 ServiceImpl 类
  ServiceImpl 实现了 Service 接口的全部方法
  事务方法标注了 @Transactional

全局:
  mvn test                               ← 全项目不破
  PROJECT_STATUS.md 有更新记录
```

## 执行顺序

```
第一批(6并行):
  erp(✅) | scm | equip | plm | iot | dms

第二批(3并行):
  lims | wms | mps

第三批(串行):
  mes → aps

第四批(3并行):
  qms | andon | eam

第五批(2并行):
  bi | crm
```
