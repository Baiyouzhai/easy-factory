# easy-factory-equip — 设备工艺系统

## 模块定位

管理生产设备的工艺参数、设备配方、运行状态和 OEE 效率。设备是 core 中 `Resource (group=Machine)` 的具体化和管理系统。

## 基于 core 的扩展

### 设备作为资源

```java
// IMachineModel extends IResourceModel (core 中已定义)
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

## 遗留问题

### 当前实现
- [x] `Equipment` — 实现 IMachineModel
- [x] `EquipmentService` — 接口已定义
- [ ] 设备配方 (EquipmentRecipe) — 未创建
- [ ] OEE 计算逻辑 — 未实现
- [ ] 设备参数实时监控 — 未接入 IoT

### 待决策
1. 设备配方与 LIMS 配方是什么关系？同一概念还是不同？
2. OEE 的六大损失如何采集？手动录入还是 IoT 自动？
3. 设备状态变更是否需要审批流程？

## AI 协作建议（2026-07-18）

EQUIP 是物理层设备管理的核心实现。core 已新增 `MachineStatus` 状态机和 `IEquipmentAction` 桥接路径。

### 推荐实施顺序

1. **升级设备状态机** — `Equipment.Status`（IDLE/RUNNING/MAINTENANCE/FAULT/OFFLINE）是普通枚举，无状态校验。应升级为使用 core 的 `MachineStatus`（实现 `ILifecycle.StatusEnum`），获得完整的状态转换校验：
   ```
   IDLE→RUNNING|SETUP|MAINTENANCE / RUNNING→IDLE|FAULT / FAULT→MAINTENANCE→IDLE
   ```
   可通过 `BaseLifecycleEntity<MachineStatus>` 获得 `transition()` 校验。

2. **桥接 IEquipmentAction** — 实现类应覆盖 `execute()` 桥接：
   ```java
   @Override
   public IResourcePack execute(IProcess process, IResourceItem... resources) {
       IResourcePack result = IActionModel.super.execute(process, resources);
       EquipmentActionResult eqResult = executeEquipment(result);
       process.set("equip.lastResult", eqResult);  // 挂入动态属性
       return result;
   }
   ```

3. **对接匹配引擎** — `EquipmentBinding.supportedActionCodes` 是匹配的唯一依据。确保设备注册时正确声明支持的动作编码集合。`DirectEquipmentStrategy` 和 `LineFirstStrategy` 都依赖此声明。

4. **OEE 计算接入** — core 的 `FactoryCapacityProfile` 已定义 OEE 模型（日可用时间 × OEE 系数）。EQUIP 的 `OEMetrics` 应向 `FactoryCapacityProfile` 提供实际数据。

5. **IExpand 动态属性约定**：
   ```
   resource.set("equip.status", "RUNNING");
   resource.set("equip.oee", "0.85");
   resource.set("equip.nextMaintenanceDate", "2026-08-01");
   ```

