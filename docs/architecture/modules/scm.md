# easy-factory-scm — 供应链管理

## 模块定位

管理上游供应链：供应商主数据、采购订单、来料计划、供应商绩效。
SCM 是 WMS 的上游——采购订单驱动收货，来料计划驱动入库准备。

## 核心模型

### Supplier (供应商)

```
Supplier:
├── code:             供应商编码
├── name:             供应商名称
├── category:         类别 (RAW_MATERIAL/PACKAGING/EQUIPMENT/SERVICE)
├── qualification:    资质状态 (QUALIFIED/UNDER_REVIEW/DISQUALIFIED)
├── contacts:         联系人列表
├── leadTime:         平均交货周期(天)
├── onTimeRate:       准时交货率(%)
├── qualityRate:      一次合格率(%)
└── status:           状态 (ACTIVE/INACTIVE/BLACKLISTED)
```

### PurchaseOrder (采购订单)

```
PurchaseOrder:
├── poNo:             采购单号
├── supplierCode:     供应商
├── status:           状态 (DRAFT/APPROVED/SENT/RECEIVING/COMPLETED)
├── items:            采购明细
│   ├── materialCode: 物料编码
│   ├── quantity:     采购数量
│   ├── unitPrice:    单价
│   ├── expectedDate: 预计到货日期
│   └── receivedQty:  已收数量
├── approvedBy:       审批人
├── createdAt:        创建时间
└── expectedDelivery: 预计交付日期
```

### InboundPlan (来料计划)

```
InboundPlan:
├── code:             到货计划编号
├── poNo:             关联采购订单
├── supplierCode:     供应商
├── expectedDate:     预计到货日期
├── items:            到货明细
├── status:           状态
└── notifyWarehouse:  是否已通知仓库
```

## 模块间接口

```
SCM → WMS:    来料计划 → 收货准备
SCM ← WMS:    收货结果 → 采购订单进度更新
SCM → ERP:    采购成本 → 财务应付
SCM → QMS:    供应商批次 → 来料检验关联
```

## 遗留问题

### 待决策
1. SCM 自建还是对接外部 ERP 的采购模块？大多数场景下 ERP 已包含 SCM
2. 供应商评分模型：仅看质量和交期，还是加成本和响应速度？
3. 采购审批流：在 easy-factory 内实现还是对接 OA 系统？
