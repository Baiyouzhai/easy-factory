# easy-factory-crm — 客户关系管理

## 模块定位

管理客户全生命周期：客户主数据、订单、投诉、满意度跟踪。
CRM 属于**协作层**（非 MOM 核心），通过 core 的领域事件和接口与制造系统对接。

### 层级归属

```
核心 MOM 层（自制）              协作层（对接/集成）
─────────────────────         ─────────────────────
mes / qms / lims / plm          erp ← 财务/成本
equip / iot / eam               scm ← 供应商/采购
wms / mps / aps                 crm ← 客户/订单/投诉  ★ 本模块
                                dms ← 文档/审批
                                bi  ← 分析/看板
                                andon ← 异常呼叫
```

## 核心模型

### Customer (客户)

```
Customer:
├── code:             客户编码
├── name:             客户名称
├── category:         类别 (PHARMA/MEDICAL_DEVICE/FOOD/CHEMICAL/OTHER)
├── industry:         行业
├── region:           区域
├── contacts:         联系人列表
│   ├── name:         姓名
│   ├── role:         角色
│   ├── phone:        电话
│   └── email:        邮箱
├── gmpAuditStatus:   GMP审计状态 (PASSED/EXPIRED/NEVER)
├── gmpAuditDate:     GMP审计日期
├── onTimeDeliveryRate: 按时交货率
├── qualityComplaintRate: 质量投诉率(每百万)
└── status:           状态 (ACTIVE/INACTIVE/BLACKLISTED)
```

### SalesOrder (销售订单)

```
SalesOrder:
├── orderNo:          订单号
├── customerCode:     客户
├── productCode:      产品
├── quantity:         数量
├── unitPrice:        单价
├── requiredDate:     需求日期
├── committedDate:    承诺交付日期
├── priority:         优先级 (NORMAL/RUSH/EXPRESS)
├── status:           状态 (RECEIVED/CONFIRMED/SCHEDULED/IN_PRODUCTION/SHIPPED/COMPLETED)
├── gxpRequirements:  GxP要求（GMP/GLP/GCP文档要求）
├── specialInstructions: 特殊说明
└── relatedComplaints: 关联投诉
```

### Complaint (投诉)

```
Complaint:
├── code:             投诉编号
├── customerCode:     客户
├── orderNo:          关联订单
├── batchNo:          关联批次
├── type:             投诉类型
│   ├── QUALITY_DEFECT  — 质量缺陷
│   ├── DELIVERY_DELAY  — 交付延迟
│   ├── PACKAGING_DAMAGE— 包装破损
│   ├── DOCUMENTATION   — 文件缺失/错误
│   ├── QUANTITY_ERROR  — 数量错误
│   └── ADVERSE_EVENT   — 不良反应(药品)
├── severity:         严重程度 (CRITICAL/MAJOR/MINOR)
├── description:      投诉描述
├── status:           处理状态 (RECEIVED/INVESTIGATING/CAPA_CREATED/RESOLVED/CLOSED)
├── capaCode:         关联CAPA（→QMS）
├── rootCause:        根因分析结果
├── resolution:       解决方案
├── customerResponse: 回复客户的内容
├── reportedAt:       投诉时间
├── resolvedAt:       解决时间
└── isReportable:     是否需要向监管机构报告
```

## 核心流程

### 订单→生产→交付 闭环

```
CRM.SalesOrder(CONFIRMED)
  │
  ▼
MPS 纳入生产计划（消费 crm.order.confirmed 事件）
  │
  ▼
APS 排程 → MES 工单
  │
  ▼
MES 完工 → ERP 出货
  │
  ▼
CRM.SalesOrder(SHIPPED) → 客户
```

### 投诉→质量 闭环

```
CRM.Complaint(RECEIVED)
  │
  ▼
QMS 偏差调查（消费 crm.complaint.received 事件）
  │
  ├── CAPA 制定与执行
  │     │
  │     ▼
  │   CAPA 关闭 → CRM.Complaint(RESOLVED)
  │
  └── 回复客户
```

## 模块间接口

```
CRM → MPS:   销售订单 → 需求来源（crm.order.confirmed）
CRM → QMS:   客户投诉 → 偏差/CAPA（crm.complaint.received）
CRM → BI:    客户满意度指标
CRM ← MES:   工单完成 → 订单进度更新
CRM ← ERP:   发货确认 → 订单状态更新
CRM ← QMS:   CAPA关闭 → 投诉关闭
```

## 依赖关系

```
easy-factory-crm
├── depends on: easy-factory-core（仅接口依赖）
├── events published:
│   ├── crm.order.confirmed
│   ├── crm.complaint.received
│   └── crm.customer.status.changed
└── events consumed:
    ├── mes.workorder.completed
    ├── erp.shipment.confirmed
    └── qms.capa.closed
```

## 遗留问题

### 待决策
1. CRM 自建还是对接外部 CRM（Salesforce/用友/金蝶）？多数成熟企业已有 CRM
2. 客户审计管理（GMP 供应商审计）放在 CRM 还是 QMS？
3. 客户门户：是否需要客户自助查订单进度/提交投诉的 Web 端？
4. 不良反应报告（药品 GVP）的监管提交流程是否纳入？
