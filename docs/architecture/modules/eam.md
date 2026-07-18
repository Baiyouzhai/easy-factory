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

### 待决策
1. EAM 与 Equip 是否需要物理拆分，还是合并为一个模块？当前 Equip 偏"工艺"，EAM 偏"资产"，逻辑上可分
2. 预防性维护的触发条件：按日历周期还是按运行小时数？
3. 备件管理是否纳入 EAM 还是独立为 WMS（仓储管理）模块？
4. 与财务系统的对接深度：折旧计算在 EAM 还是 ERP？

## AI 协作建议（2026-07-18）

EAM 管理资产的财务/管理视角——与 EQUIP（操作视角）互补。

### 推荐实施

1. **使用 AssetStatus 状态机** — core 已定义 `AssetStatus`（IDLE/IN_USE/UNDER_MAINTENANCE/SCRAPPED）实现 `ILifecycle.StatusEnum`。`Asset` 继承 `BaseLifecycleEntity<AssetStatus>` 即可获得状态转换校验。

2. **与 MachineStatus 的关系** — EAM 的 `AssetStatus` 是财务/管理状态，EQUIP 的 `MachineStatus` 是操作状态。两者独立但可关联：
   - Asset.IDLE ↔ Machine.IDLE
   - Asset.IN_USE ↔ Machine.RUNNING
   - Asset.UNDER_MAINTENANCE ↔ Machine.MAINTENANCE

3. **审计追踪** — 资产状态变更（如报废）应通过 core 的 `AuditTrail` 记录。

