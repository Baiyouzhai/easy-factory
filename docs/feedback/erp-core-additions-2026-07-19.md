# Core 层变更知会 — ERP 模块约定建立

> **来源会话**: ERP 模块约定建立（2026-07-19）
> **目标**: core 模块会话管理者
> **性质**: 新增文件，无破坏性变更

## 变更原因

建立 ERP 模块约定时，最初将 `TransactionStatus` 和 `TransactionType` 放在 erp 模块内部。但分析跨模块协作后发现：**MES/WMS/LIMS 都需要创建 ERP 事务回传记录**，如果这些枚举留在 erp 模块，其他模块就必须依赖 `easy-factory-erp` 才能编译——违反 core 居中的架构原则。

参照 EAM 模块模式（`IAsset`/`IMaintenanceOrder`/`ICalibrationRecord` + 枚举都在 core 的 `batch/` 包），将事务相关的接口和枚举迁移到 core。

## 新增文件（3 个）

| 文件 | 路径 | 说明 |
|------|------|------|
| `IErpTransaction.java` | `easy-factory-core/.../batch/IErpTransaction.java` | 事务回传记录接口，getCode/getTransactionType/getMaterialCode/getQuantity/getStatus… |
| `TransactionStatus.java` | `easy-factory-core/.../batch/TransactionStatus.java` | 状态枚举，实现 `ILifecycle.StatusEnum`：PENDING→SENT→CONFIRMED / SENT→FAILED→PENDING / PENDING→CANCELLED |
| `TransactionType.java` | `easy-factory-core/.../batch/TransactionType.java` | 简单值枚举：GOODS_ISSUE / GOODS_RECEIPT / TRANSFER |

均在 `com.byz.factory.batch` 包下，与 `IWorkOrder`、`IInspectionOrder`、`IAsset` 等同级。

## 跨模块引用关系

```
IErpTransaction (core/batch)  ←── MES  创建发料/入库事务
                              ←── WMS  创建收货/转储事务
                              ←── LIMS 创建批次消耗事务
                              ──→ ERP  Transaction 实现 → 异步发送到外部 ERP
```

## 对齐 EAM 模式

```
EAM:  core/batch/IAsset.java               ←→ eam/model/Asset
      core/batch/MaintenanceOrderStatus.java  ←→ eam/model/MaintenanceOrder

ERP:  core/batch/IErpTransaction.java       ←→ erp/model/Transaction
      core/batch/TransactionStatus.java        ←→ erp/model/Transaction
      core/batch/TransactionType.java          ←→ erp/model/Transaction
```

## 编译与测试验证

```
mvn install -pl easy-factory-core -DskipTests  → BUILD SUCCESS
mvn -pl easy-factory-erp test                  → 41 Tests, BUILD SUCCESS
```

## 备注

- `ErpMaterialType`（ROH/HALB/FERT）留在 erp 模块，因为它是 SAP 特有概念，映射到 core `Dict.SourceType` 而非直接引用
- `PROJECT_STATUS.md` 已同步更新
