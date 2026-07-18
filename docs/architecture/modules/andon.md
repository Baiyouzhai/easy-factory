# easy-factory-andon — 安灯系统

## 模块定位

产线异常实时呼叫与逐级上报系统。当设备故障、质量异常、物料短缺发生时，操作工触发 Andon，
系统自动通知相关人员，超时未响应则逐级上报。

## 核心模型

### AndonCall (安灯呼叫)

```
AndonCall:
├── code:             呼叫编号
├── source:           来源 (MANUAL/AUTO) — 人工触发还是系统自动
├── triggerType:      触发类型
│   ├── EQUIPMENT_FAULT  — 设备故障
│   ├── QUALITY_ISSUE    — 质量问题（连续不良）
│   ├── MATERIAL_SHORTAGE— 物料短缺
│   ├── SAFETY_INCIDENT  — 安全事件
│   ├── PROCESS_DELAY    — 工序超时
│   └── OTHER            — 其它
├── workOrderNo:      关联工单
├── processCode:      关联工序
├── equipmentCode:    关联设备
├── severity:         严重程度 (INFO/WARNING/CRITICAL/EMERGENCY)
├── description:      呼叫描述
├── status:           状态 (OPEN/ACKNOWLEDGED/IN_PROGRESS/RESOLVED/CLOSED)
├── triggeredBy:      触发人
├── triggeredAt:      触发时间
└── resolvedAt:       解决时间
```

### EscalationRule (上报规则)

```
EscalationRule:
├── triggerType:      触发类型
├── severity:         严重程度
├── levels:           逐级上报链
│   ├── level:        级别 (1/2/3)
│   ├── timeoutMinutes:超时时间(分钟)
│   ├── notifyRoles:  通知角色列表
│   └── escalateOn:   上报条件 (TIMEOUT/NO_RESPONSE)
└── autoStop:         自动停止 (NONE/PAUSE_PROCESS/STOP_LINE/STOP_FACTORY)
```

示例上报链：

```
Level 1 (0min):  操作工 → 班组长
Level 2 (5min):  班组长未响应 → 车间主任 + 设备工程师
Level 3 (15min): 车间主任未响应 → 生产经理 + 值班厂长
Level 4 (30min): 生产线自动暂停
```

## 与现有模块的联动

```
触发                             响应                    结果
─────────────────────────────────────────────────────────────────────
IoT 设备报警 ──────────→ Andon 创建呼叫 → 通知班组
                         │
QMS 质量异常(SPC) ───────┤
                         │
MES 工序超时 ────────────┤
                         │
操作工手动触发 ──────────┘
                         │
                    ┌─────▼─────┐
                    │ 逐级上报   │
                    │ 超时→升级  │
                    └─────┬─────┘
                          │
              ┌───────────┼───────────┐
              ▼           ▼           ▼
         EAM 维护工单  QMS 偏差    MES 暂停工序
```

## 外部接口

```
POST   /api/andon/calls                    创建安灯呼叫
PUT    /api/andon/calls/{id}/acknowledge    确认
PUT    /api/andon/calls/{id}/resolve       解决
GET    /api/andon/calls?status=OPEN        活跃呼叫列表
GET    /api/andon/dashboard                安灯看板数据
POST   /api/andon/rules                    配置上报规则
```

## 模块间接口

```
Andon ← IoT:    设备报警 → 自动创建呼叫
Andon ← QMS:    连续不良/SPC超限 → 自动创建呼叫
Andon ← MES:    工序超时 → 自动创建呼叫
Andon → EAM:    设备故障 → 创建维护工单
Andon → MES:    EMERGENCY → 暂停工单/产线
Andon → QMS:    质量问题 → 创建偏差
```

## 遗留问题

### 待决策
1. 通知渠道：短信/企业微信/钉钉/邮件/声光报警？
2. 上报规则存储为静态配置还是动态脚本？（类似 Action 脚本化）
3. Andon 是否需要独立模块，还是作为 MES 的子系统？
4. 产线物理 Andon 看板（LED屏幕）如何对接？

## AI 协作建议（2026-07-18）

Andon 是异常响应中枢——接收多源事件并触发升级流程。

### 推荐实施

1. **使用 AndonStatus 状态机** — core 已定义完整流转（OPEN→ACKNOWLEDGED→RESOLVED/ESCALATED→CLOSED）。

2. **多源事件订阅** — Andon 应订阅来自多模块的领域事件：
   ```java
   DomainEventPublisher.subscribe("mes.process.timeout", this::createCall);
   DomainEventPublisher.subscribe("qms.spc.outOfControl", this::createCall);
   DomainEventPublisher.subscribe("equip.fault", this::createCall);
   DomainEventPublisher.subscribe("iot.alarm.triggered", this::createCall);
   ```

3. **升级至其它模块** — Andon 处理后通过事件通知下游：
   - ESCALATED → `eam.maintenance.requested`（创建维护工单）
   - EMERGENCY → `mes.process.pause`（暂停工单）
   - 质量问题 → `qms.deviation.created`（创建偏差）

