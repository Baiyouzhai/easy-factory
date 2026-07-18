# easy-factory-mes — 制造执行系统

## 模块定位

基于 core 的工序/动作模型，实现制造执行系统的核心功能：工单管理、工序流转、操作报工、生产追溯。

## 核心业务场景

### 工单生命周期

```
创建 ──→ 下达 ──→ 开工 ──→ 工序流转 ──→ 完工 ──→ 关闭
                       │
                       ├── 正常流转
                       ├── 质量异常中断 (QMS介入)
                       └── 设备故障暂停 (Equip介入)
```

### 工序流转

```
工序001(投料) ──→ 工序002(混料) ──→ 工序003(检验) ──→ 工序004(包装)
     │                │                │                │
     ▼                ▼                ▼                ▼
  动作:称量        动作:搅拌         动作:取样         动作:装箱
  动作:复核        动作:升温         动作:检测         动作:贴标
                                   动作:判定
```

## 基于 core 的扩展

### 工序扩展

```java
// core 接口
public interface IProcess {
    String getCode();
    String getName();
    int getOrder();
    List<IActionModel> getActions();
    // ...
}

// MES 扩展字段
// IExpand.set("workOrderId", "WO-20260712-001")
// IExpand.set("status", "IN_PROGRESS")
// IExpand.set("startTime", Instant.now())
// IExpand.set("operatorId", "OP001")
```

MES 不重新定义工序概念，而是通过 `IExpand` 机制在 core 工序上挂载工单状态、时间戳、操作人等运行时信息。

### 动作扩展

```java
// MES 扩展字段
// IExpand.set("actualStartTime", ...)
// IExpand.set("actualEndTime", ...)
// IExpand.set("operator", ...)
// IExpand.set("equipmentId", ...)
// IExpand.set("result", "PASS" | "FAIL")
// IExpand.set("remark", ...)
```

## MES 特有模型

### WorkOrder (工单)

```
WorkOrder:
├── workOrderNo:     工单号
├── productInfo:     IProductInfoModel (来自 core)
├── quantity:        计划数量
├── processes:       工序列表 (来自产品蓝图)
├── status:          状态 (Created/Released/Started/Completed/Closed)
├── plannedStart:    计划开始
├── plannedEnd:      计划结束
└── factory:         执行工厂
```

### ProcessRecord (工序记录)

```
ProcessRecord:
├── workOrderId:     所属工单
├── processCode:     工序编码
├── status:          工序状态
├── actualStart:     实际开始时间
├── actualEnd:       实际结束时间
├── operator:        操作人
├── actions:         动作执行记录列表
└── defectCount:     不良数
```

### ActionRecord (动作记录)

```
ActionRecord:
├── processRecordId: 所属工序记录
├── actionCode:      动作编码
├── scriptExecuted:  是否执行了脚本
├── inputResources:  输入资源快照
├── outputResources: 输出资源快照
├── startTime:       开始时间
├── endTime:         结束时间
├── result:          执行结果
└── remark:          备注
```

## 与 QMS 的穿插点

在工序执行过程中，如果某个动作关联了质量检验：

```
工序003(检验)
├── Action: 取样   → MES 报工
├── Action: 检测   → QMS 获取检验标准 → 记录检验值
└── Action: 判定   → QMS 判定逻辑 → 合格则继续 / 不合格则触发偏差
                    → Control.Interrupt 中断流程
```

穿插机制：
1. MES 执行到检验工序时，调用 QMS 接口获取检验方案
2. 检测动作执行后，将数据传给 QMS 记录
3. QMS 判定不合格时，通过 `Control.Interrupt` 挂起工序
4. QMS 发起偏差处理流程（偏差→CAPA→关闭或让步接收）
5. 处理完成后通知 MES 恢复工序流转

## 外部接口

### 向上提供给 Web 的接口

```
POST   /api/mes/work-orders          创建工单
PUT    /api/mes/work-orders/{id}/release  下达
PUT    /api/mes/work-orders/{id}/start    开工
POST   /api/mes/processes/{id}/actions    报工
GET    /api/mes/work-orders/{id}/trace    追溯
```

### 模块间接口

```
MES → QMS:  获取检验方案、提交检验数据、接收判定结果
MES → Equip: 获取设备状态、请求设备资源
MES → ERP:   消耗物料、成品入库
MES → LIMS:  请求配方、提交批记录
```

## 数据库设计要点

- 工单表 (mes_work_order)
- 工序记录表 (mes_process_record)
- 动作记录表 (mes_action_record)
- 资源快照表 (mes_resource_snapshot) — 记录执行时的资源状态
- 与 core 实体通过 code 关联，而非外键强耦合

## 遗留问题

### 当前实现
- [x] `MesWorkOrder` — 实现 IWorkOrder
- [x] `WorkOrderService` — 接口已定义，方法未实现
- [ ] ProcessRecord / ActionRecord — 未创建
- [ ] 工单状态机 (ILifecycle) — 未接入
- [ ] 工序流转脚本 — 串行动作链不支持并行工序

### 待决策
1. 工单引用 Blueprint 创建工序副本还是运行时解析？
2. 报工粒度：工序级还是动作级？
3. 并行工序如何建模（同时进行的称量和配料）？
4. QMS 检验触发时 MES 工序暂停/恢复的机制？

## AI 协作建议（2026-07-18）

MES 是执行追溯域的核心实现模块。以下为建议优先处理的事项：

### 推荐实施顺序

1. **实现 IBatch + ITraceable** — core 已定义完整接口。Batch 应继承 `BaseLifecycleEntity<BatchStatus>`，覆盖 `getCurrentProcessCode()`/`getCurrentWorkstationCode()`（WIP 位置追踪）和 `getParentBatchNos()`/`getChildBatchNos()`（物料谱系）。TraceRecord 应实现 `ITraceable`，记录 before/after 资源快照（JSON）。

2. **完善 MesWorkOrder** — 当前已继承 `BaseLifecycleEntity<WorkOrderStatus>` 并实现 `IWorkOrder`，但缺少：
   - `formulaCode` 字段（记录使用的配方版本——追溯合规关键）
   - 工单发布时冻结 Blueprint 快照（防止蓝图后续变更影响已发布工单）

3. **集成操作分析层** — 在 `WorkOrderService` 中调用 core 的分析引擎：
   ```java
   // 工单发布前验证可制造性
   ProcessRouteMatcher matcher = new ProcessRouteMatcher();
   RouteMatchResult result = matcher.match(blueprint, factory);
   if (!result.feasible()) throw new ActionException("工厂不可生产");
   ```

4. **接入领域事件** — MES 是事件的主要生产者：
   - 工单发布 → `mes.workorder.released`
   - 工序开始 → `mes.process.started`（QMS 订阅以自动创建检验）
   - 工序完成 → `mes.process.completed`
   - 批次完成 → `mes.batch.completed`

5. **IExpand 动态属性约定**：
   ```
   process.set("mes.workOrderId", woNo);
   process.set("mes.batchNo", batchNo);
   process.set("mes.formulaCode", formulaCode);
   ```

### 待决策问题的新建议
- 问题1（副本 vs 解析）：推荐**运行时解析 + 冻结快照**——工单发布时把当前 Blueprint 版本号写入工单，运行时按版本号查找
- 问题2（报工粒度）：推荐**动作级**——与 ITraceable 的 before/after 快照粒度一致
- 问题3（并行工序）：使用 `ExecutionMode.PARALLEL` 标记，ProcessCycleTime 已支持

