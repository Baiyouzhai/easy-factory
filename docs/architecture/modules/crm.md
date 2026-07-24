# easy-factory-crm — 客户关系管理

## 模块定位

CRM（客户关系管理）是**协作层模块**——管理客户主数据、销售订单和客户投诉。
CRM 通过领域事件与 MPS（订单→生产计划）、QMS（投诉→偏差/CAPA）、WMS（订单→发货）对接。

### 层级归属

```
核心 MOM 层（自制）              协作层（对接/集成）
─────────────────────         ─────────────────────
mes / qms / lims / plm          erp ← 财务/成本
equip / iot / eam               scm ← 供应商/采购
wms / mps / aps                 crm ← 客户/订单/投诉  ★ 本模块
andon                           dms ← 文档/审批
                                bi  ← 分析/看板
```

## Core 层新增（供跨模块引用）

| 类型 | 文件 | 说明 |
|------|------|------|
| 接口 | `crm/ICustomer.java` | 客户抽象，供 MPS/SCM/QMS 引用 |
| 接口 | `crm/ISalesOrder.java` | 销售订单抽象（含 ISalesOrderItem 嵌套接口），供 MPS/WMS/BI 引用 |
| 接口 | `crm/IComplaint.java` | 客户投诉抽象，供 QMS/Andon 引用 |
| 状态枚举 | `crm/SalesOrderStatus.java` | DRAFT→CONFIRMED→IN_PRODUCTION→SHIPPED→COMPLETED；+CANCELLED |
| 状态枚举 | `crm/ComplaintStatus.java` | OPEN→INVESTIGATING→RESOLVED→CLOSED；+CANCELLED |
| 事件常量 | `event/types/CrmEventTypes.java` | 10 个领域事件常量（与 EquipEventTypes/PlmEventTypes 等同包 `com.byz.factory.event.types`） |

## CRM 模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `model/Customer.java` | `BaseEntity` | `ICustomer` | 客户实体：行业+区域+GMP审计状态+业务便捷方法(passGmpAudit/expireGmpAudit/updateAuditStatus) |
| `model/SalesOrder.java` | `BaseLifecycleEntity<SalesOrderStatus>` | `ISalesOrder` | 销售订单核心实体：完整状态机+8个业务便捷方法(confirm/startProduction/ship/complete/cancel/addItem/linkToPlan/totalAmount/isRush) |
| `model/SalesOrderItem.java` | — | `ISalesOrder.ISalesOrderItem` | 订单明细行：productCode+quantity+unitPrice |
| `model/Complaint.java` | `BaseLifecycleEntity<ComplaintStatus>` | `IComplaint` | 投诉实体：完整状态机+5个业务便捷方法(startInvestigation/resolve/close/cancel/linkCapa)+isQualityRelated+isActive |

## 服务接口

| 文件 | 说明 |
|------|------|
| `service/CrmService.java` | 25 方法：客户管理(registerCustomer/getCustomer/listCustomers/findCustomersByIndustry/updateAuditStatus) + 订单管理(createOrder/getOrder/findOrdersByCustomer/findActiveOrders/confirmOrder/linkToProductionPlan/shipOrder/completeOrder/cancelOrder) + 投诉管理(createComplaint/getComplaint/findComplaintsByCustomer/findComplaintsByOrder/findActiveComplaints/findQualityComplaints/startInvestigation/resolveComplaint/closeComplaint/cancelComplaint/linkCapa) |

## 核心模型

### Customer (客户)

```
Customer:
├── code:             客户编码
├── name:             客户名称
├── industry:         行业
├── region:           区域
├── contacts:         联系人列表(JSON)
├── gmpAuditStatus:   GMP审计状态 (PASSED/EXPIRED/NEVER)
├── gmpAuditDate:     GMP审计日期
├── onTimeDeliveryRate: 按时交货率(%)
├── qualityComplaintRate: 质量投诉率(%)
```

### SalesOrder (销售订单)

```
SalesOrder:
├── orderNo:          订单号
├── customerCode:     客户编码
├── productCode:      产品编码
├── quantity:         订单数量
├── unitPrice:        单价
├── requiredDate:     客户要求交期
├── committedDate:    承诺交期
├── priority:         优先级 (NORMAL/RUSH/EXPRESS)
├── gxpRequirements:  GxP要求
├── specialInstructions: 特殊说明
├── planNo:           关联生产计划号
├── status:           状态 (DRAFT→CONFIRMED→IN_PRODUCTION→SHIPPED→COMPLETED)
├── items:            订单明细 (SalesOrderItem)
└── SalesOrderItem:
    ├── productCode:  产品编码
    ├── quantity:     订购数量
    └── unitPrice:    单价
```

### Complaint (投诉)

```
Complaint:
├── complaintNo:      投诉编号
├── customerCode:     客户编码
├── orderNo:          关联订单号
├── batchNo:          关联批次号
├── type:             投诉类型 (QUALITY/DELIVERY/PACKAGING/SERVICE/OTHER)
├── description:      投诉描述
├── status:           状态 (OPEN→INVESTIGATING→RESOLVED→CLOSED)
├── resolution:       处理结果/回复
└── capaCode:         关联CAPA编码
```

## 核心流程

### 订单→生产→交付 闭环

```
CRM.SalesOrder(CONFIRMED) → 发布 crm.order.confirmed
  │
  ▼
MPS 纳入生产计划（消费 crm.order.confirmed 事件）
  │
  ▼
APS 排程 → MES 工单
  │
  ▼
MES 完工 → 发货 → CRM.SalesOrder(SHIPPED→COMPLETED)
```

### 投诉→质量 闭环

```
CRM.Complaint(OPEN) → 发布 crm.complaint.received
  │
  ▼
QMS 偏差调查（消费 crm.complaint.received 事件）
  │
  ├── CAPA 制定与执行
  │     │
  │     ▼
  │   CAPA 关闭 → CRM.Complaint(RESOLVED→CLOSED)
  │
  └── 回复客户
```

## 约定规则

1. **协作层模块** — CRM 是协作层模块，客户主数据和订单管理本地存储，但通过领域事件与核心 MOM 层协作
2. **跨模块接口** — CRM 实体通过 core `crm/` 包中的接口暴露（`ICustomer`、`ISalesOrder`、`IComplaint`）；MPS 通过 ISalesOrder 获取订单需求，QMS 通过 IComplaint 获取投诉信息
3. **状态机** — `SalesOrderStatus`、`ComplaintStatus` 放 core `crm/` 包中，实现 `ILifecycle.StatusEnum`；SalesOrder/Complaint 继承 `BaseLifecycleEntity<S>` 获得 `transition()` 校验
4. **模型继承** — 有状态实体（SalesOrder, Complaint）继承 `BaseLifecycleEntity<S>` + 实现 core 接口；无状态记录（Customer）继承 `BaseEntity` + 实现 core 接口
5. **IExpand 约定** — `crm.customer.*` 挂载在 Customer 上（region/contacts/gmpAuditStatus/gmpAuditDate/onTimeDeliveryRate/qualityComplaintRate）；`crm.order.*` 挂载在 SalesOrder 上（orderNo/customerCode/priority/gxpRequirements/specialInstructions/planNo）；`crm.complaint.*` 挂载在 Complaint 上（complaintNo/customerCode/orderNo/batchNo/type/capaCode/resolvedAt/closedAt）
6. **业务便捷方法** — Customer: `passGmpAudit(date)` / `expireGmpAudit()` / `updateAuditStatus(status,date)`；SalesOrder: `confirm()` / `startProduction()` / `ship()` / `complete()` / `cancel()` / `addItem()` / `linkToPlan()` / `totalAmount()` / `isRush()`；Complaint: `startInvestigation()` / `resolve(resolution)` / `close()` / `cancel()` / `linkCapa(capaCode)` / `isQualityRelated()` / `isActive()`
7. **领域事件** — 事件常量在 `CrmEventTypes` 中统一管理（10 个：2 客户 + 5 订单 + 3 投诉），遵循 `{module}.{entity}.{past_tense}` 命名约定；**模型层不发布事件**（design-decisions.md §1.1），由 Service 实现类统一负责
8. **订单→MPS 协作** — CRM 确认订单后，MPS 通过订阅 `crm.order.confirmed` 获取订单需求，注册为 `DemandSource(SALES_ORDER)`，纳入生产计划
9. **投诉→QMS 闭环** — 质量问题投诉（type=QUALITY）创建后，QMS 通过订阅 `crm.complaint.received` 创建偏差 → CAPA 处理 → 投诉关联 CAPA 编码追溯
10. **GMP 审计管理** — Customer 内建 GMP 审计状态（PASSED/EXPIRED/NEVER），通过 `passGmpAudit()`/`expireGmpAudit()` 操作；审计状态影响是否可接受医药类订单
11. **测试规范** — Given-When-Then + `methodName_condition_expectedResult` + `@DisplayName` 中文描述；62 个测试覆盖：构造、状态转换（正常+非法+终态+完整生命周期+取消路径）、业务便捷方法、IExpand、枚举定义（SalesOrderStatus/ComplaintStatus 转换规则+CrmEventTypes 10事件命名约定+PREFIX+不可实例化）、CrmService 接口契约（25方法签名）、综合场景（客户订单全流程/质量投诉CAPA闭环/交期延误投诉/订单取消/误投诉撤销）
12. **待实现** — CrmService 实现类、REST 控制器、企业微信/邮件通知适配器、客户门户（Web端自助查订单进度/提交投诉）

## 跨模块事件契约

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

## 依赖关系

```
easy-factory-crm
├── depends on: easy-factory-core（仅接口依赖）
├── events published（10个）:
│   ├── crm.customer.registered / crm.customer.audit_updated
│   ├── crm.order.created / crm.order.confirmed / crm.order.shipped / crm.order.completed / crm.order.cancelled
│   └── crm.complaint.received / crm.complaint.resolved / crm.complaint.closed
└── events consumed（在 Service 实现层订阅）:
    ├── mes.workorder.completed → 订单进度更新
    ├── mps.plan.released → 订单关联生产计划
    └── qms.capa.closed → 投诉关闭
```

## 已决策

1. ✅ CRM 自建轻量模块（同 SCM 策略），核心逻辑在本地，外部 CRM 通过事件对接
2. ✅ GMP 客户审计状态内置在 Customer 中（gmpAuditStatus + gmpAuditDate），审计管理由 CRM 负责
3. ✅ 销售订单使用独立状态机 SalesOrderStatus（非复用 WorkOrderStatus）
4. ✅ 投诉通过 crm.complaint.received 事件通知 QMS 创建偏差/CAPA

## 待决策

1. 客户门户：是否需要客户自助查订单进度/提交投诉的 Web 端？
2. 不良反应报告（药品 GVP）的监管提交流程是否纳入？
