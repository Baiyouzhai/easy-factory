# easy-factory-equip — 设备工艺系统

## 模块定位

管理生产设备的工艺参数、设备配方、运行状态和 OEE 效率。设备是 core 中 `Resource (group=Machine)` 的具体化和管理系统。

## 基于 core 的扩展

### 设备作为资源

```java
// IMachine extends IResourceItem (core 中已定义)
// IExpand 扩展字段
// "equip.code"           → 设备编号
// "equip.model"          → 设备型号
// "equip.status"         → IDLE | RUNNING | MAINTENANCE | FAULT
// "equip.location"       → 所在工位
// "equip.parameters"     → {工艺参数设置值JSON}
// "equip.lastMaintenance"→ 上次保养时间
// "equip.oee"            → 当前OEE值
```

## Equip 特有模型

### Equipment (设备台账)

```
Equipment:
├── code:             设备编码
├── name:             设备名称
├── model:            设备型号
├── category:         设备类别 (反应釜/离心机/包装机/检测仪)
├── group:             所属组 (core.SourceGroup.Machine)
├── type:             子类型
├── location:          安装位置/工位
├── status:           运行状态
├── specifications:   规格参数
├── supplier:         供应商
├── purchaseDate:     购置日期
└── assetCode:        固定资产编码
```

### EquipmentParameter (设备工艺参数)

```
EquipmentParameter:
├── equipmentCode:    关联设备
├── paramCode:        参数编码
├── paramName:        参数名称 (如: 转速、温度、压力、流量)
├── setValue:         设定值
├── actualValue:      实际值 (从IoT采集)
├── upperLimit:       上限
├── lowerLimit:       下限
├── alarmThreshold:   报警阈值
├── unit:             单位
└── controlMethod:    控制方式 (PLC自动/人工调节)
```

### EquipmentRecipe (设备配方)

```
EquipmentRecipe:
├── code:             配方编码
├── name:             配方名称
├── equipmentCode:    适用设备
├── productCode:      适用产品
├── parameters:       配方参数集
│   ├── paramCode:    参数编码
│   ├── phase:        阶段 (升温段/恒温段/降温段)
│   ├── setValue:     设定值
│   ├── duration:     持续时间
│   └── rampRate:     变化速率
└── version:          版本
```

### OEMetrics (OEE 指标)

```
OEMetrics:
├── equipmentCode:    设备编码
├── period:           统计周期
├── availability:     可用率 (实际运行时间/计划运行时间)
├── performance:      性能率 (实际产出/理论产出)
├── quality:          质量率 (合格品/总产出)
├── oee:              OEE综合效率 = A × P × Q
├── downtimeEvents:   停机事件列表
└── lossAnalysis:     损失分析 (六大损失)
```

## 设备与工序的绑定

设备作为资源被工序引用：

```
工序002(混料)
├── 动作: 投料      → 人工操作
├── 动作: 搅拌      → 设备: 反应釜RF-001, 参数: 转速=120rpm, 温度=80°C
└── 动作: 升温      → 设备: 反应釜RF-001, 参数: 升温速率=2°C/min, 目标=120°C
```

当 MES 执行到此工序时：
1. 从 Equip 获取设备当前状态（是否可用）
2. 从 Equip 获取设备配方参数（设定值）
3. 通过 IoT 下发参数到 PLC/DCS
4. 执行过程中 IoT 回传实际值
5. Equip 记录参数曲线，计算 OEE

## 设备配方下发流程

```
PLM 工艺设计 ──→ Equip 设备配方 ──→ MES 工单执行 ──→ IoT 参数下发 ──→ PLC/设备
                                        │
                                   ┌────▼────┐
                                   │ 实时监控  │
                                   │ 参数曲线  │
                                   │ 报警联动  │
                                   └────┬────┘
                                        │
                                   ┌────▼────┐
                                   │ 批记录   │
                                   │ OEE计算  │
                                   │ 追溯存储  │
                                   └─────────┘
```

## 外部接口

### 向上提供给 Web 的接口

```
GET    /api/equip/equipments                 设备台账
PUT    /api/equip/equipments/{id}/status     更新设备状态
GET    /api/equip/equipments/{id}/oee        OEE查询
POST   /api/equip/recipes                    创建设备配方
PUT    /api/equip/recipes/{id}/version       配方版本管理
POST   /api/equip/recipes/{id}/dispatch      配方下发
GET    /api/equip/equipments/{id}/parameters 设备实时参数
```

### 模块间接口

```
Equip ← PLM:   接收工艺参数 → 转化为设备配方
Equip → MES:   提供设备状态 → 工单开工前检查
Equip → IoT:   下发设定值 → 设备执行
Equip ← IoT:   接收实际值 → 参数监控、报警
Equip ← MES:   接收生产工单 → 关联到设备运行记录
Equip → QMS:   设备参数数据 → SPC分析
```

## 数据库设计要点

- 设备台账表 (equip_equipment)
- 设备参数表 (equip_parameter)
- 设备配方表 (equip_recipe)
- 配方参数明细表 (equip_recipe_param)
- OEE记录表 (equip_oee_metrics)
- 设备运行日志表 (equip_runtime_log)

## 实现状态（2026-07-19 更新）

### 已完成
- [x] `Equipment` — 实现 `IEquipment`（⭐ = IMachine + ILifecycle\<MachineStatus\> + 台账字段），`AbstractResourceItem` 继承，7 个业务便捷方法
- [x] `EquipmentParameter` — 实现 `IEquipmentParameter`，`BaseEntity`，含 `updateActual()`；`isInControl()` 由接口 default 提供
- [x] `EquipmentRecipe` — 实现 `IEquipmentRecipe`，`BaseEntity`，含 `RecipePhase implements IRecipePhase` + `bumpVersion()`
- [x] `OEMetrics` — 实现 `IOEMetrics`，`BaseEntity`，工厂方法 `of()` + `toPercentString()`
- [x] `EquipmentService` — 接口已定义（16 方法：台账+状态+配方+参数+OEE）
- [x] `EquipEventTypes` — 9 个事件常量（core `event/types/`），含订阅指南
- [x] `IEquipment` / `IEquipmentParameter` / `IEquipmentRecipe`(含 `IRecipePhase`) / `IOEMetrics` — 跨模块接口（core `batch/`）
- [x] 单元测试 — `EquipModuleTest`（43 个测试）
- [ ] EquipmentService 实现类 + IEquipmentAction 桥接
- [ ] IoT 参数实时监控

### Core 接口回归引用

| 接口 | 位置 | 说明 |
|------|------|------|
| `IEquipment` | core `batch/` | = IMachine + ILifecycle\<MachineStatus\> + model/category/location/assetCode |
| `IEquipmentParameter` | core `batch/` | setValue/actualValue/upperLimit/lowerLimit + `isInControl()` default |
| `IEquipmentRecipe` | core `batch/` | 设备配方 + 内嵌 `IRecipePhase` |
| `IOEMetrics` | core `batch/` | OEE = A×P×Q |
| `MachineStatus` | core `batch/` | IDLE→RUNNING\|SETUP\|MAINTENANCE → FAULT→MAINTENANCE→IDLE |

### 制造标准背景

| 标准 | 体现 |
|------|------|
| **OEE 六大损失** | 设备故障/换型调整/空转暂停/减速运行/启动废品/过程废品 → `OEMetrics` + `downtimeEvents` |
| **TPM 八大支柱** | 自主维护/计划维护/质量维护/教育训练/初期管理/间接部门/安全环境 → `MaintenanceType`(EAM) + `Equipment` 状态机 |
| **设备层次** | Plant→Line→Machine→Unit→Component → `IFactory`→`IProductionLine`→`Equipment` |

### 已决策
1. ✅ 设备状态机升级 — 已升级为 `MachineStatus`（`ILifecycle.StatusEnum`），`Equipment implements IEquipment`（= IMachine + ILifecycle）
2. ✅ 设备配方 vs LIMS 配方 — 不同概念：Equip Recipe 管设备参数（温度/转速），LIMS Formula 管物料配比（克/千克）
3. ✅ Equip→Core 回归 — 5 个接口回归 core，EquipEventTypes 迁入 `event/types/`，审核已通过
4. ✅ EngineeringBOM 引用 Recipe — A+B 方案（可选 recipeCode + 副本 fallback）
5. ✅ FactoryCapacityProfile 双轨 — 规划用静态 OEE 因子，执行后用 IOEMetrics 修正

### 待决策
1. OEE 的六大损失如何采集？手动录入还是 IoT 自动？
2. 设备状态变更是否需要审批流程？

> **最后更新**: 2026-07-19

