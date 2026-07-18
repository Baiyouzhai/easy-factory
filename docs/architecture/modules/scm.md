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

## AI 协作建议（2026-07-18）

SCM 管理供应商和采购——轻量模块，与 ERP/WMS 协作。

### 推荐实施

1. **继承 BaseEntity** — `Supplier` 已继承 `BaseEntity`（获得 code/name/timestamps）。可进一步实现 `IAuditable` 以追踪供应商准入/降级的操作人。

2. **事件集成**:
   - 供应商资质变更 → `scm.supplier.qualified` / `scm.supplier.disqualified`
   - 采购订单创建 → `scm.po.created`（WMS 订阅以生成收货单）
   - 来料 → WMS 收货 → QMS 来料检（IQC）→ 放行/退货

## 约定实施记录（2026-07-19）

### 已实施

#### Core 层（`com.byz.factory.batch`）

| 文件 | 类型 | 说明 |
|------|------|------|
| `ISupplier.java` | 接口 | 供应商抽象（code/name/category/qualification/status/leadTime/onTimeRate/qualityRate） |
| `IPurchaseOrder.java` | 接口 | 采购订单抽象 + 内嵌 `IPurchaseOrderItem` 接口 |
| `SupplierStatus.java` | 值枚举 | ACTIVE / INACTIVE / BLACKLISTED |
| `PurchaseOrderStatus.java` | 状态枚举 | DRAFT→APPROVED→SENT→RECEIVING→COMPLETED；+CANCELLED |

#### SCM 模块模型

| 文件 | 继承 | 实现 | 说明 |
|------|------|------|------|
| `Supplier.java` | `BaseEntity` | `ISupplier` | 资质管理（qualify/disqualify）+ 状态管理（deactivate/reactivate/blacklist） |
| `PurchaseOrder.java` | `BaseLifecycleEntity<PurchaseOrderStatus>` | `IPurchaseOrder` | 完整状态机 + 采购明细管理 + 总金额计算 |
| `PurchaseOrderItem.java` | — | `IPurchaseOrder.IPurchaseOrderItem` | 收货累加（receive/receivedQty/remaining/isFullyReceived） |

#### ScmService 接口

扩展为 14 个方法，覆盖供应商管理（注册/查询/合格列表/资质审核/暂停/恢复）和
采购订单管理（创建/审批/发送/收货/取消/查询/按供应商查询）。

#### 测试

- `ScmModuleTest` — 8 个模型构造测试
- `SupplierTest` — 10 个测试（构造/资质/状态/扩展字段/枚举）
- `PurchaseOrderTest` — 21 个测试（构造/正常链/取消/终态/非法转换/明细/收货/枚举）

### 待实施

- [ ] InboundPlan 来料计划模型
- [ ] 领域事件集成（scm.supplier.qualified / scm.po.created 等）
- [ ] WMS 收货 → PO 进度自动更新
- [ ] 供应商评分模型（SupplierScoring）

