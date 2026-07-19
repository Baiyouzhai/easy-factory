# easy-factory-erp — 资源计划集成

## 模块定位

作为 ERP 系统与本平台的适配层，负责物料主数据同步、库存交互、成本核算数据归集。不是重新实现 ERP，而是建立 core 领域模型与 ERP 之间的双向映射。

## 设计理念

ERP 是外部的"权威数据源"，easy-factory 通过适配层消费 ERP 数据，同时将制造执行结果回传 ERP（物料消耗、成品入库、工时）。

## 数据映射

### 物料主数据映射

```
ERP 物料主数据          core IResourceItem (group=Material)
┌────────────────┐     ┌────────────────────┐
│ MaterialCode   │────→│ name               │
│ Description    │     │ (IExpand扩展)       │
│ BaseUnit       │     │   erp.materialCode │
│ MaterialType   │     │   erp.description  │
│ BatchManaged   │     │   erp.unit         │
│ ShelfLife      │     │   erp.type         │
│ GHS_Class      │     │   erp.batchManaged │
└────────────────┘     └────────────────────┘
```

### BOM 映射

```
ERP BOM                core IResourcePack
┌────────────────┐     ┌────────────────────┐
│ ParentMaterial │────→│ 关联产品            │
│ ComponentItem  │────→│ IResourceItem     │
│ QtyPerAssembly │────→│ number (数量)       │
│ ScrapPercent   │────→│ IExpand: lossRate  │
│ ValidFrom/To   │     │ IExpand: validPeriod│
└────────────────┘     └────────────────────┘
```

### 库存交互

```
ERP 库存              easy-factory
┌────────────────┐    ┌────────────────────┐
│ MaterialStock  │───→│ 资源可用性检查      │
│                │←───│ 物料消耗回传        │
│                │←───│ 成品入库回传        │
│ BatchInventory │←──→│ 批次追溯            │
└────────────────┘    └────────────────────┘
```

## ERP 适配层模型

### MaterialMaster (物料主数据缓存)

```
MaterialMaster:
├── materialCode:     ERP物料编码
├── description:      物料描述
├── unit:             基本单位
├── materialType:     物料类型 (ROH原材料/HALB半成品/FERT成品)
├── batchManaged:     是否批次管理
├── shelfLife:        保质期(天)
├── ghsClass:         GHS危险等级
├── lastSyncTime:     最后同步时间
└── sourceSystem:     来源ERP系统
```

### InventorySnapshot (库存快照)

```
InventorySnapshot:
├── materialCode:     物料编码
├── plantCode:        工厂代码
├── storageLocation:  存储地点
├── batchNo:          批次号
├── unrestrictedQty:  非限制库存
├── inspectionQty:    待检库存
├── blockedQty:       冻结库存
├── snapshotTime:     快照时间
└── sourceSystem:     来源系统
```

### Transaction (事务回传)

```
Transaction:
├── transactionType:  事务类型 (GoodsIssue发料/GoodsReceipt入库/Transfer转储)
├── materialCode:     物料编码
├── quantity:         数量
├── batchNo:          批次号
├── movementType:     ERP移动类型
├── referenceDoc:     参考单据 (工单号)
├── postingDate:      过账日期
├── status:           回传状态 (Pending/Sent/Confirmed/Failed)
└── retryCount:       重试次数
```

## 同步策略

### 物料主数据：定时全量 + 增量更新

```
每天凌晨: 全量同步物料主数据到本地缓存
每30分钟: 增量同步（基于 lastModified 时间戳）
工单开工前: 实时校验物料是否存在于本地缓存
```

### 库存：按需查询 + 预留

```
工单下达时: 检查物料可用性 → 创建预留
工序投料时: 扣减本地库存计数 → 批量回传ERP
工序完工时: 成品入库 → 回传ERP
```

### 成本：批次归集

```
批记录关闭时: 汇总实际物料成本、工时成本 → 回传ERP成本模块
```

## 外部接口

### 向上提供给 Web 的接口

```
GET    /api/erp/materials                    物料主数据查询
GET    /api/erp/materials/{code}/stock       库存查询
POST   /api/erp/transactions                 事务回传 (手动)
GET    /api/erp/transactions?status=Failed   失败事务重试列表
PUT    /api/erp/transactions/{id}/retry      重试失败事务
```

### 模块间接口

```
ERP → MES:   物料主数据、库存可用性
ERP ← MES:   物料消耗、成品入库、工时
ERP → PLM:   物料编码供BOM引用
ERP ← LIMS:  批次消耗明细
```

## 容错设计

1. **本地缓存** — 物料主数据本地缓存，ERP 不可用时使用缓存数据
2. **事务重试** — 回传失败自动重试（指数退避），超过阈值人工介入
3. **幂等性** — 所有回传接口幂等，防止重复过账
4. **异步解耦** — 生产执行不因 ERP 回传失败而中断

## 数据库设计要点

- 物料主数据缓存表 (erp_material_cache)
- 库存快照表 (erp_inventory_snapshot)
- 事务回传队列表 (erp_transaction_queue)
- 同步日志表 (erp_sync_log)

## 遗留问题

### 当前实现
- [x] `MaterialCache` — 物料主数据本地缓存，含 `toResourceItem()` 映射、`mapUom()` 单位映射
- [x] `ErpAdapterService` — ERP 适配接口已扩展（库存查询、事务队列管理、同步方法）
- [x] `InventorySnapshot` — 库存快照模型，按物料+工厂+库位+批次记录
- [x] `Transaction` — 事务回传记录，带 `TransactionStatus` 状态机（PENDING→SENT→CONFIRMED / FAILED→PENDING）
- [x] `TransactionStatus` / `TransactionType` / `ErpMaterialType` — 枚举定义完整
- [x] 单元测试 — MaterialCache + InventorySnapshot + Transaction 共 13 个测试，覆盖构造/状态转换/映射
- [ ] 库存查询 — 未与任何 ERP 系统对接
- [ ] 事务回传队列持久化 — 未实现（当前仅模型定义）
- [ ] 同步策略（全量/增量）— 未定义

### 待决策
1. 对接哪个 ERP？SAP/Oracle/用友/金蝶？
2. 同步方式：定时轮询还是 ERP 主动推送？
3. 回传失败的重试策略？最大重试次数和退避算法？

## AI 协作建议（2026-07-18）

ERP 是外部系统适配层——核心关注数据同步的可靠性和一致性。

### 推荐实施

1. **使用 IResourceItem 管理物料** — core 的 `IResourceItem`（getNumber/getUom/add/use/copy）为物料主数据提供统一模型。`MaterialCache` 将 ERP 物料映射为 `IResourceItem`。

2. **使用 UOM 枚举** — core 已定义标准计量单位。ERP 同步时应做单位映射（ERP 的单位编码 → core 的 `UOM` 枚举）。

3. **事件驱动同步** — 库存变更通过 `DomainEventPublisher` 广播，而非直接调用 ERP API：
   ```java
   // WMS 收货完成 → 发布事件
   DomainEventPublisher.publish(IDomainEvent.of("wms.receipt.completed", ...));
   // ERP 适配器订阅 → 异步回传
   ```

