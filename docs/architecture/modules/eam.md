# easy-factory-eam — 企业资产管理系统

## 模块定位

管理设备/资产的全生命周期：采购 → 安装 → 运行 → 维护 → 校准 → 报废。
EAM 关注的是"资产本身"，而 Equip 关注的是"设备作为生产资源"。

### EAM vs Equip 的区别

| 维度 | EAM（资产管理） | Equip（设备工艺） |
|------|----------------|-------------------|
| 关注点 | 资产价值、维护成本、折旧 | 工艺参数、设备配方、OEE |
| 生命周期 | 采购→报废，全周期 | 运行态，生产时段 |
| 核心数据 | 维护工单、备件、校准记录 | 实时参数、运行状态、效率 |
| 谁会用到 | 设备科、维修组、财务 | 工艺工程师、操作工 |

## 基于 core 的扩展

```java
// IExpand 扩展字段
// "eam.assetCode"          → 固定资产编码
// "eam.purchaseDate"       → 购置日期
// "eam.warrantyExpiry"     → 质保到期
// "eam.depreciationYears"  → 折旧年限
// "eam.residualValue"      → 残值
// "eam.supplier"           → 供应商
// "eam.maintenancePlan"    → 维护计划（周期/上次/下次）
// "eam.calibrationDue"     → 下次校准日期
```

## EAM 特有模型

### Asset (资产)

```
Asset:
├── assetCode:        资产编码
├── equipmentCode:    关联 Equip 设备
├── name:             资产名称
├── category:         资产类别
├── purchaseDate:     购置日期
├── purchaseCost:     购置成本
├── warrantyExpiry:   质保到期日
├── depreciationYears:折旧年限(年)
├── residualValue:    残值
├── supplier:         供应商
├── location:         存放位置
├── status:           资产状态 (IN_USE/IDLE/MAINTENANCE/SCRAPPED)
└── lifecycle:        生命周期事件列表
```

### MaintenanceOrder (维护工单)

```
MaintenanceOrder:
├── code:             维护工单号
├── assetCode:        关联资产
├── type:             维护类型 (PREVENTIVE/CORRECTIVE/PREDICTIVE/CALIBRATION)
├── priority:         优先级 (LOW/MEDIUM/HIGH/EMERGENCY)
├── description:      故障/维护描述
├── status:           状态 (OPEN/IN_PROGRESS/COMPLETED/VERIFIED)
├── plannedStart:     计划开始
├── plannedEnd:       计划结束
├── actualStart:      实际开始
├── actualEnd:        实际结束
├── downtime:         停机时长(分钟)
├── cost:             维护成本(人工+备件)
├── technician:       维修人
└── spareParts:       更换备件清单
```

### CalibrationRecord (校准记录)

```
CalibrationRecord:
├── code:             校准单号
├── assetCode:        关联资产(仪器/量检具)
├── calibrationType:  校准类型 (INTERNAL/EXTERNAL)
├── standard:         校准标准
├── result:           校准结果 (PASS/FAIL/ADJUSTED)
├── calibratedBy:     校准人/机构
├── calibratedAt:     校准日期
├── nextDue:          下次校准日期
├── certificate:      校准证书编号
└── deviation:        偏差值
```

## 外部接口

```
GET    /api/eam/assets                    资产台账
POST   /api/eam/maintenance-orders        创建维护工单
PUT    /api/eam/maintenance-orders/{id}   完成维护
POST   /api/eam/calibrations             创建校准记录
GET    /api/eam/assets/{code}/lifecycle   资产生命周期
```

## 模块间接口

```
EAM ← Equip:  设备运行状态 → 维护预警
EAM → Equip:  维护/校准 → 设备状态锁定
EAM → MES:    设备不可用 → 工单排程调整
EAM → ERP:    设备折旧 → 财务成本归集
```

## 遗留问题

### 当前实现（2026-07-19 更新）
- [x] `Asset` — 实现 `IAsset`，`BaseLifecycleEntity<AssetStatus>`，含 `IDLE→IN_USE→UNDER_MAINTENANCE→SCRAPPED`
- [x] `MaintenanceOrder` — 实现 `IMaintenanceOrder`，`BaseLifecycleEntity<MaintenanceOrderStatus>`，含 `OPEN→IN_PROGRESS→COMPLETED→VERIFIED`
- [x] `CalibrationRecord` — 实现 `ICalibrationRecord`，`BaseEntity`，含校准类型/标准/结果/证书
- [x] `EamService` — 接口已定义（资产管理 + 维护工单 + 校准记录，10 方法）
- [x] `AssetStatus` / `MaintenanceOrderStatus` / `MaintenanceType` / `MaintenancePriority` / `CalibrationResult` / `CalibrationType` — 枚举（core `batch/`）
- [x] `IAsset` / `IMaintenanceOrder` / `ICalibrationRecord` — 跨模块接口（core `batch/`）
- [x] 单元测试 — `EamModuleTest`（8 个测试）
- [ ] 维护计划自动生成 + 备件管理
- [ ] EamService 实现类

### Core 接口引用

| 接口 | 位置 | 说明 |
|------|------|------|
| `IAsset` | core `batch/` | 固定资产编码/设备关联/折旧 |
| `IMaintenanceOrder` | core `batch/` | 维护类型/优先级/停机时间/成本 |
| `ICalibrationRecord` | core `batch/` | 校准类型/标准/结果/证书 |
| `AssetStatus` | core `batch/` | IDLE/IN_USE/UNDER_MAINTENANCE/SCRAPPED |
| `MaintenanceOrderStatus` | core `batch/` | OPEN→IN_PROGRESS→COMPLETED→VERIFIED (+CANCELLED) |
| `MaintenanceType` | core `batch/` | PREVENTIVE/CORRECTIVE/PREDICTIVE/CALIBRATION |

### 制造标准背景

| 标准 | 体现 |
|------|------|
| **ISO 14224** | 设备可靠性数据采集标准 → `MaintenanceOrder` 的故障记录(type/cost/downtime) |
| **ISO 10012** | 测量设备校准周期确定 → `CalibrationRecord` 的 `nextDue` 字段 |
| **TPM** | 全面生产维护（自主维护+计划维护+质量维护）→ `MaintenanceType.PREVENTIVE/CORRECTIVE/PREDICTIVE` |
| **RCM** | 以可靠性为中心的维护策略 → `MaintenancePriority` 分级(HIGH/MEDIUM/LOW/EMERGENCY) |

### 待决策
1. EAM 与 Equip 是否需要物理拆分：已决策 **保持分离**（EAM 偏资产财务，Equip 偏工艺运行）
2. 预防性维护的触发条件：按日历周期还是按运行小时数？
3. 备件管理是否纳入 EAM 还是独立为 WMS 模块？
4. 与财务系统的对接深度：折旧计算在 EAM 还是 ERP？

> **最后更新**: 2026-07-19

