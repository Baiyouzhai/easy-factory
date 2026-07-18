# easy-factory-aps — 高级排程系统

## 模块定位

在 MPS 确定"生产什么、生产多少、何时交付"之后，APS 负责"在哪个设备上、按什么顺序、什么时间段"来执行。

APS 的输出直接驱动 MES 的工单下发，是计划→执行的最后一环。

## 基于 core 的扩展

APS 利用 core 中的工序和设备模型，在排程时考虑真实产能约束：

```java
// IExpand 扩展字段（挂载在 Process/Action 上）
// "aps.scheduledStart"     → 排程开始时间
// "aps.scheduledEnd"       → 排程结束时间
// "aps.assignedMachine"    → 分配的设备
// "aps.assignedOperator"   → 分配的操作人
// "aps.sequenceNo"         → 在同设备上的执行顺序
// "aps.setupTime"          → 换型时间(分钟)
// "aps.processTime"        → 加工时间(分钟)
```

## APS 特有模型

### Schedule (排程方案)

```
Schedule:
├── code:             排程编号
├── planNo:           关联 MPS 计划
├── version:          版本
├── factoryCode:      工厂
├── horizonStart:     排程范围起始
├── horizonEnd:       排程范围结束
├── strategy:         排程策略 (FORWARD/BACKWARD/BOTTLENECK)
├── optimizationGoal: 优化目标 (MIN_MAKESPAN/MIN_TARDINESS/MAX_UTILIZATION)
├── status:           状态
└── tasks:            排程任务列表
```

### ScheduledTask (排程任务)

```
ScheduledTask:
├── workOrderNo:      工单号
├── processCode:      工序编码
├── machineCode:      分配设备
├── operatorCode:     分配操作人
├── scheduledStart:   排程开始时间
├── scheduledEnd:     排程结束时间
├── setupTime:        换型时间(分钟)
├── processTime:      加工时间(分钟)
├── predecessors:     前置任务列表（工序依赖）
├── status:           状态 (SCHEDULED/DISPATCHED/IN_PROGRESS/COMPLETED)
└── priority:         优先级
```

### ResourceCalendar (资源日历)

```
ResourceCalendar:
├── resourceCode:     资源编码(设备/人员)
├── resourceType:     资源类型 (MACHINE/PERSONNEL)
├── shifts:           班次列表
│   ├── shiftName:    班次名称 (白班/夜班)
│   ├── startTime:    开始时间
│   ├── endTime:      结束时间
│   └── capacity:     可用产能(小时)
├── holidays:         节假日/计划停机
│   ├── date:         日期
│   └── reason:       原因
└── maintenanceWindows: 维护窗口
```

### Constraint (约束条件)

```
Constraint:
├── type:             约束类型
│   ├── SEQUENCE_DEPENDENT  — 工序顺序依赖
│   ├── MACHINE_DEDICATED   — 设备专用约束
│   ├── CALENDAR_LIMITED  — 日历产能限制
│   ├── MATERIAL_AVAILABLE — 物料可用性
│   ├── TOOLING_LIMITED    — 工装模具限制
│   └── CLEANING_REQUIRED   — 清洁/换批要求
├── description:      约束描述
├── priority:         约束优先级 (HARD/SOFT)
└── penalty:          违反惩罚值(软约束)
```

## 排程算法（规划）

```
1. 输入: MPS 计划 + 产品工艺路线(Blueprint) + 设备日历 + 在制品状态
2. 构建约束模型: 工序依赖 + 设备产能 + 物料可用性 + 人员排班
3. 优化求解: 启发式算法 / 遗传算法 / 约束规划
4. 输出: ScheduledTask 列表 + 设备甘特图数据
5. 评估: 交期达成率 / 设备利用率 / 总完工时间
6. 发布: 排程结果 → MES 工单 → Equip 设备占用
```

## 外部接口

```
POST   /api/aps/schedules                  创建排程
GET    /api/aps/schedules/{id}             查看排程结果
GET    /api/aps/schedules/{id}/gantt       甘特图数据
PUT    /api/aps/schedules/{id}/reschedule  重排程
GET    /api/aps/calendars                  资源日历
POST   /api/aps/schedules/{id}/dispatch    下发到 MES
```

## 模块间接口

```
APS ← MPS:   主生产计划（产品/数量/交付日期）
APS ← PLM:   工艺路线 + 标准工时
APS ← Equip: 设备产能 + 设备日历
APS ← MES:   在制品状态 + 工单执行进度
APS → MES:   排程结果 → 工单下发 + 工序顺序 + 设备分配
APS → Equip: 设备占用时间段
```

## 遗留问题

### 待决策
1. 排程算法选择：先做简单的规则式排程（EDD/SPT/CR），还是直接上优化引擎？
2. 重排程触发条件：新订单插入？设备故障？物料延迟？多长时间自动重排一次？
3. 软约束 vs 硬约束：交期延误算软约束（可违反但有惩罚）还是硬约束（必须满足）？
4. 与 MES 的双向反馈：MES 上报实际工时的延迟如何反馈到 APS 以校正后续排程？

## AI 协作建议（2026-07-18）

APS 应直接使用 core 的 operation 分析引擎，而不是重复实现计算逻辑。

### 集成操作分析层

core 已提供 4 个计算器，APS 的排程逻辑应直接调用：

1. **产能检查** — `FactoryCapacityProfile` + `BottleneckDetector`
   ```java
   FactoryCapacityProfile profile = new FactoryCapacityProfile(factoryCode, dailyHours);
   profile.withMachine("PT-001", 960, 0.90);  // 16h×60min, OEE 90%
   BottleneckResult bn = new BottleneckDetector().detect(blueprint, durations, profile);
   // → 约束工序 + 最大日产能 → 用于排程的产能约束
   ```

2. **提前期计算** — `ProductionLeadTime`
   ```java
   ProductionLeadTime calc = new ProductionLeadTime(queueTime, transferTime);
   LeadTimeResult lt = calc.calculate(blueprint, durations, batchSize);
   // → 每道工序的周期/排队/转运时间 → 用于排程的时间估算
   ```

3. **资源需求** — `ResourceRequirementExploder`
   ```java
   new ResourceRequirementExploder()
       .withYield("P002", new BigDecimal("0.97"))
       .explode(blueprint, batchSize);
   ```

4. **匹配验证** — `ProcessRouteMatcher`
   - 排程结果中每道工序是否能找到可用设备？

### 排程算法建议（回答问题1）
先实现**规则式排程**（EDD/SPT/CR），直接调用上述 core 计算器获取参数。优化引擎（遗传算法/约束规划）可以后续叠加——因为计算器接口不变。

