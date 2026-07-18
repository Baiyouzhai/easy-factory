# easy-factory-wms — 仓储管理系统

## 模块定位

管理物料/成品的仓储运作：收货、上架、拣料、发运、线边仓、库存盘点。
WMS 是 MES 和 LIMS 的物料来源——没有仓储，投料和称量在真空中操作。

## 基于 core 的扩展

```java
// IExpand 扩展字段（挂载在 Resource 上）
// "wms.location"          → 存储位置（库位编码）
// "wms.batchNo"           → 物料批次号
// "wms.status"            → 物料状态 (QUARANTINE/RELEASED/REJECTED)
// "wms.expiryDate"        → 有效期至
// "wms.receiptNo"         → 收货单号
// "wms.fifoDate"          → FIFO 日期（用于先进先出）
```

## WMS 特有模型

### Storage (库位)

```
Storage:
├── locationCode:     库位编码
├── warehouse:        仓库
├── zone:             区域（原料区/包材区/成品区/待检区/不合格区）
├── rack:             货架
├── level:            层
├── position:         位
├── storageType:      存储类型 (AMBIENT/COLD/FROZEN/HAZARDOUS)
└── capacity:         容量
```

### Receipt (收货单)

```
Receipt:
├── receiptNo:        收货单号
├── sourceType:       来源 (PURCHASE_ORDER/RETURN/TRANSFER)
├── referenceNo:      来源单号
├── supplierCode:     供应商
├── items:            收货明细
│   ├── materialCode: 物料编码
│   ├── batchNo:      供应商批号
│   ├── receivedQty:  实收数量
│   ├── orderedQty:   订单数量
│   ├── status:       状态 (RECEIVED/QUARANTINE/ACCEPTED/REJECTED)
│   └── locationCode: 上架库位
├── receivedBy:       收货人
└── receivedAt:       收货时间
```

### PickingTask (拣料任务)

```
PickingTask:
├── code:             拣料单号
├── workOrderNo:      关联工单（MES）
├── batchNo:          关联批号
├── pickingType:      拣料类型 (FULL/STAGED/JIT)
├── items:            拣料明细
│   ├── materialCode: 物料编码
│   ├── requiredQty:  需求数量
│   ├── pickedQty:    实拣数量
│   ├── batchNo:      物料批次号（FIFO选择）
│   └── locationCode: 拣料库位
├── status:           状态 (PENDING/IN_PROGRESS/PICKED/DELIVERED)
├── pickedBy:         拣料人
└── deliveredAt:      送达时间（线边仓）
```

### InventorySnapshot (库存快照)

```
InventorySnapshot:
├── materialCode:     物料编码
├── batchNo:          批次号
├── locationCode:     库位
├── onHandQty:        在手库存
├── allocatedQty:     已分配（被工单预留）
├── availableQty:     可用库存 = 在手 - 已分配
├── quarantineQty:    待检库存
├── rejectedQty:      不合格库存
├── expiryDate:       有效期至
└── lastCounted:      最后盘点时间
```

## 核心流程

### 收货→上架
```
供应商送货 → 收货(Receipt) → 贴标签 → 待检区 → QMS来料检(IQC)
  → 合格 → 上架到原料区 → 库存更新
  → 不合格 → 不合格区 → 退货
```

### 拣料→投料（MES触发）
```
MES 工单下达 → WMS 创建拣料任务 → FIFO 选择批次
  → 拣料 → 复核 → 送到线边仓 → MES 接收 → LIMS 称量
```

## 外部接口

```
POST   /api/wms/receipts                   创建收货单
PUT    /api/wms/receipts/{id}/accept       验收上架
POST   /api/wms/picking-tasks              创建拣料任务
PUT    /api/wms/picking-tasks/{id}/complete 拣料完成
GET    /api/wms/inventory/{materialCode}   库存查询
POST   /api/wms/counts                     创建盘点任务
```

## 模块间接口

```
WMS ← SCM/ERP: 采购订单 → 收货单
WMS ← MES:    工单 → 拣料任务
WMS → MES:    拣料完成 → 投料确认
WMS → LIMS:   物料批次 → 称量
WMS → QMS:    来料 → 待检通知
WMS ← QMS:    来料检结果 → 放行/退货
WMS → ERP:    库存变更 → 财务过账
```

## 遗留问题

### 待决策
1. 批次策略：FIFO/FEFO/LEFO？不同物料可能需要不同策略
2. 线边仓管理：线边仓库存是否独立于主仓库？盘点周期？
3. 条码/RFID：出入库扫码实现？与 IoT 的关系？
4. 库存事务是否实时回传 ERP？还是按批次汇总回传？

## AI 协作建议（2026-07-18）

WMS 管理仓储——与物料追溯和批次管理紧密集成。

### 推荐实施

1. **实现 IMaterial 接口** — core 已定义 `IMaterial`（固定 SourceGroup.Material）。仓储管理的物料应实现此接口，获得 `IResourceItem` 的数量管理能力。

2. **批次谱系集成** — 出入库操作关联 `IBatch`。使用 `IBatch.getParentBatchNos()`/`getChildBatchNos()` 追踪批次拆分/合并。

3. **使用 UOM 枚举** — core 已定义 `UOM`（KG/G/L/PCS/TAB 等）。所有物料数量应附带 `getUom()` 声明单位。

4. **使用 ReceiptStatus 状态机** — core 已定义（PENDING→PARTIAL→COMPLETED→CLOSED）。

