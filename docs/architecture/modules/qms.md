# easy-factory-qms — 质量管理系统

## 模块定位

基于 core 的工序/动作模型，在制造过程中内嵌质量管理，而非作为独立系统运行。核心功能：检验工序管理、SPC 统计过程控制、偏差/CAPA 处理、质量门禁。

## 设计理念

**质量长在工序里，不是贴在产品上。**

相比于传统的"产品完工后检验"，本系统将质量动作直接嵌入生产工序链。任何工序都可以包含检验动作，质量判定结果直接影响工序流转（继续/中断/返工）。

## 基于 core 的扩展

### 检验工序

QMS 不发明新的工序概念，而是复用 `IProcess`，通过扩展属性注入质量上下文：

```java
// IExpand 扩展字段
// "qms.inspectionType"   → "IQC" | "IPQC" | "FQC" | "OQC"
// "qms.inspectionLevel"  → "NORMAL" | "REDUCED" | "TIGHTENED"
// "qms.aql"              → 0.65  (允收质量水平)
// "qms.sampleSize"       → 80    (抽样数)
// "qms.standard"         → {检验标准JSON}
// "qms.specLimits"       → {USL: 10.5, LSL: 9.5, Target: 10.0}
```

### 检验动作

```java
// IExpand 扩展字段
// "qms.measuredValue"    → 实际测量值
// "qms.judgement"        → "PASS" | "FAIL" | "CONCESSION"
// "qms.defectCode"       → 不良代码
// "qms.gaugeId"          → 使用的量检具ID
// "qms.inspector"        → 检验员
```

## QMS 特有模型

### InspectionPlan (检验方案)

```
InspectionPlan:
├── code:             方案编码
├── productCode:      适用产品
├── processCode:      适用工序
├── inspectionType:   检验类型 (IQC/IPQC/FQC/OQC)
├── items:            检验项目列表
│   ├── itemCode:     项目编码
│   ├── itemName:     项目名称 (如: 粘度、pH值、外观)
│   ├── specType:     规格类型 (计量/计数)
│   ├── usl:          规格上限
│   ├── lsl:          规格下限
│   ├── target:       目标值
│   ├── unit:         单位
│   ├── method:       检验方法
│   └── sampling:     抽样方案
└── controlPlan:      控制计划 (触发条件 → 响应措施)
```

### InspectionRecord (检验记录)

```
InspectionRecord:
├── planId:           关联检验方案
├── workOrderId:      关联工单
├── processRecordId:  关联工序记录
├── actionRecordId:   关联动作记录
├── measuredValue:    测量值
├── judgement:        判定结果
├── defectCode:       不良代码
├── inspector:        检验员
├── inspectedAt:      检验时间
└── remark:           备注
```

### Deviation (偏差)

```
Deviation:
├── code:             偏差编号
├── source:           来源 (检验不合格/设备异常/人为差错)
├── severity:         严重程度 (Minor/Major/Critical)
├── productImpact:    产品影响评估
├── disposition:      处置 (Rework返工/Concession让步/Reject报废)
├── capa:             关联CAPA
├── status:           状态
└── timeline:         时间线
```

### CAPA (纠正与预防措施)

```
CAPA:
├── code:             CAPA编号
├── deviationId:      关联偏差
├── rootCause:        根因分析
├── correctiveAction: 纠正措施
├── preventiveAction: 预防措施
├── verification:     效果验证
├── status:           状态
└── closeDate:        关闭日期
```

## 质量门禁机制

```
生产流程:
  工序A ──→ 工序B ──→ [质量门] ──→ 工序C ──→ 工序D
                          │
                    ┌─────┼─────┐
                    ▼     ▼     ▼
                 放行   让步   拒收
                    │     │     │
                    ▼     ▼     ▼
                 继续   记录   发起偏差
                        审批   → CAPA
                        继续   → 返工/报废
```

质量门实现为一种特殊工序，其动作脚本包含判定逻辑：

```javascript
function execute(process, inputResources) {
    var inspectionRecord = qms.getLastInspection(process);
    if (inspectionRecord.judgement === 'PASS') {
        return process.getResourcePack();  // 放行
    } else if (inspectionRecord.judgement === 'CONCESSION') {
        qms.recordConcession(inspectionRecord);  // 让步记录
        return process.getResourcePack();
    } else {
        qms.triggerDeviation(inspectionRecord);  // 发起偏差
        throw new InterruptedException("质量门禁: 不合格");  // 中断
    }
}
```

## SPC 统计过程控制

对计量型检验值进行实时 SPC 分析：

```
SPC 规则:
├── Rule 1: 1点超出 3σ 控制限 → 报警
├── Rule 2: 连续9点在中心线同侧 → 报警
├── Rule 3: 连续6点递增或递减 → 报警
└── Rule 4: 连续14点交替上下 → 报警
```

SPC 结果通过 `Action.Control.Interrupt` 触发质量预警，暂停工序并通知相关人员。

## 外部接口

### 向上提供给 Web 的接口

```
GET    /api/qms/inspection-plans/{productCode}  获取检验方案
POST   /api/qms/inspection-records              提交检验记录
POST   /api/qms/deviations                      发起偏差
PUT    /api/qms/deviations/{id}/disposition       处置
POST   /api/qms/capas                           创建CAPA
GET    /api/qms/spc/{processCode}               获取SPC图表数据
```

### 模块间接口

```
QMS ← MES:  接收工序进入通知 → 返回检验方案
QMS → MES:  判定结果 → 放行/中断/返工指令
QMS ← Equip: 设备参数数据用于SPC分析
QMS ← LIMS:  实验室检测结果
```

## 数据库设计要点

- 检验方案表 (qms_inspection_plan)
- 检验项目表 (qms_inspection_item)
- 检验记录表 (qms_inspection_record)
- 偏差表 (qms_deviation)
- CAPA表 (qms_capa)
- SPC数据表 (qms_spc_data)

## 遗留问题

### 当前实现（2026-07-24 更新）
- [x] `InspectionOrder` — 完整状态机 + 业务便捷方法 + 领域事件发布（升级）
- [x] `InspectionService` — 接口扩展至 12 方法（方案管理 + 检验指令 + 判定逻辑）
- [x] `InspectionPlan` — 检验方案实体，InspectionItem 内部类（新建）
- [x] `InspectionRecord` — 检验记录实体，自动判定逻辑（新建）
- [x] `Deviation` — 偏差实体，完整状态机 + 8 业务便捷方法 + requiresCapa()（新建）
- [x] `Capa` — CAPA 实体，完整状态机 + 5 业务便捷方法 + isOverdue()（新建）
- [x] `DeviationService` / `CapaService` — 服务接口已定义（新建）
- [x] Core 层接口/枚举 — IInspectionRecord / IDeviation / ICapa / InspectionType / DeviationSeverity / DeviationDisposition / DeviationStatus / CapaStatus / QmsEventTypes（新建）
- [ ] SPC 实时计算引擎 — 示例脚本存在但未接入
- [ ] Service 实现类 — InspectionService / DeviationService / CapaService 实现类未创建
- [ ] IQualityAction 实现类 — 未创建
- [ ] REST 控制器 — 未创建

### 已决策（design-decisions.md 裁定）
1. **检验方案存储** → §3.1 结构化字段为主，IExpand 为辅
2. **SPC 控制限** → §3.2 双模式：固定值（IProcessParameter）+ 自动计算（IoT 历史数据）
3. **偏差处理流程** → §3.3 MES 自动暂停 + QMS 自动创建 Deviation + **人工判定**（QA 决定处置方式）
4. **模块定位** → 质量长在工序里，不是贴在产品上

## AI 协作建议（2026-07-18）

QMS 是质量内嵌设计的关键实现。core 已提供审计追踪和电子签名基础设施。

### 推荐实施顺序

1. **接入审计追踪** — 所有 QMS 实体（检验记录、偏差、CAPA）的操作应自动记录 `AuditTrail`（core 已定义 record：entityType, entityId, action, operator, timestamp, before, after, reason）。

2. **接入电子签名** — 检验报告审核、偏差批准、CAPA 关闭等操作使用 `IElectronicSignature` + `SignatureMeaning`（REVIEWED/APPROVED/VERIFIED）。

3. **桥接 IQualityAction** — 实现 `IQualityAction` 的类应覆盖 `execute()` 方法，先调用标准动作逻辑，再执行质量专用逻辑（取样→检测→判定），将 `QualityActionResult` 挂入事件 payload。

4. **订阅 MES 事件** — 在 `InspectionService` 中订阅 `mes.process.started`，自动创建检验指令：
   ```java
   DomainEventPublisher.subscribe("mes.process.started", event -> {
       if (hasInspectionPlan(event.getPayload().get("processCode"))) {
           createInspectionOrder(event);
       }
   });
   ```

5. **工艺参数集成** — QMS 的检验标准（USL/LSL/目标值）应与 core 的 `IProcessParameter` 对齐。检验时：`IProcessParameter.getTargetValue()` vs 实际测量值 → 判定。

6. **IExpand 动态属性约定**：
   ```
   process.set("qms.inspectionLevel", "NORMAL");
   process.set("qms.aql", "0.65");
   process.set("qms.method", "HPLC");
   ```

