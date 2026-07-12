# easy-factory-lims — 配方系统

## 模块定位

管理产品的配方（物料组成）、配料称量、批记录。LIMS 的配方本质上就是 core 中的 `IResourcePack`，在配方系统中获得版本管理、称量追溯、合规记录等能力。

## 基于 core 的扩展

### 配方 = ResourcePack + 版本管理

```java
// IResourcePack (core 定义)
// ├── resources: 物料列表 (原料/辅料/中间体)
// └── compress(): 按组+名去重汇总

// LIMS IExpand 扩展字段
// "lims.formulaCode"      → 配方编码
// "lims.formulaVersion"   → "1.0.0"
// "lims.batchSize"        → 批量规模 (如 1000kg)
// "lims.status"           → "DRAFT" | "APPROVED" | "ACTIVE" | "RETIRED"
// "lims.effectiveDate"    → 生效日期
// "lims.expiryDate"       → 失效日期
```

### 配方中每个资源的扩展

```java
// IResourceModel IExpand 扩展字段
// "lims.phase"            → 投料阶段 (Phase1/Phase2/...)
// "lims.additionOrder"    → 投料顺序
// "lims.additionMethod"   → 投料方式 (一次性/滴加/分批)
// "lims.toleranceMin"     → 允差下限 (%)
// "lims.toleranceMax"     → 允差上限 (%)
// "lims.hazardClass"      → 危险等级
// "lims.storageCondition" → 存储条件
```

## LIMS 特有模型

### Formula (配方)

```
Formula:
├── code:             配方编码
├── name:             配方名称
├── productCode:      对应产品
├── version:          版本号
├── batchSize:        标准批量
├── resourcePack:     IResourcePack (core 中的资源包即配方组分)
├── phases:           投料阶段划分
│   ├── phaseNo:      阶段号
│   ├── phaseName:    阶段名称
│   ├── actions:      阶段内的投料动作
│   └── conditions:   阶段条件 (温度/压力/搅拌)
├── yield:            收率范围
├── status:           状态
└── approvedBy:       批准人
```

### WeighingTask (称量任务)

```
WeighingTask:
├── code:             称量任务号
├── formulaCode:      配方编码
├── workOrderId:      关联工单
├── batchNo:          批号
├── items:            称量项目列表
│   ├── materialCode: 物料编码
│   ├── formulaQty:   配方量
│   ├── actualQty:    实际称量量
│   ├── tolerance:    允差
│   ├── balance:      使用天平
│   ├── operator:     称量人
│   ├── verifier:     复核人
│   └── weighedAt:    称量时间
└── status:           状态 (Pending/Weighing/Verified/Complete)
```

### BatchRecord (批记录)

```
BatchRecord:
├── batchNo:          批号
├── workOrderId:      关联工单
├── formulaCode:      配方编码
├── formulaVersion:   配方版本
├── productCode:      产品
├── batchSize:        实际批量
├── yield:            实际收率
├── processRecords:   工序执行记录
├── weighingTasks:    称量记录
├── inspectionRecords:检验记录
├── deviations:       偏差记录
├── reviewedBy:       审核人
├── reviewedAt:       审核时间
└── status:           状态 (InProgress/Review/Approved/Archived)
```

## 配方版本管理

```
配方 v1.0 ──→ 配方 v1.1 (辅料用量微调) ──→ 配方 v2.0 (原料变更)
   │                │                            │
   ▼                ▼                            ▼
批号 B001      批号 B002-B010                批号 B011+
(关联v1.0)     (关联v1.1)                    (关联v2.0)
```

每次配方变更产生新版本，历史批记录保留对旧版本的引用，确保可追溯。

## 称量防错

```javascript
// 称量动作的脚本示例
function execute(process, inputResources) {
    var task = lims.getWeighingTask(process);
    for (var item of task.items) {
        var actual = scale.read();  // 从天平读取实际值
        var deviation = Math.abs(actual - item.formulaQty) / item.formulaQty * 100;

        if (deviation > item.tolerance) {
            lims.recordDeviation(task, item, actual);
            throw new Error("称量偏差超限: " + item.materialCode
                + ", 配方量: " + item.formulaQty
                + ", 实际: " + actual
                + ", 偏差: " + deviation.toFixed(2) + "%");
        }
        item.actualQty = actual;
        item.weighedAt = new Date();
    }
    return process.getResourcePack();
}
```

## 外部接口

### 向上提供给 Web 的接口

```
GET    /api/lims/formulas                    配方列表
POST   /api/lims/formulas                    创建配方
PUT    /api/lims/formulas/{id}/version       发布新版本
POST   /api/lims/weighing-tasks              创建称量任务
PUT    /api/lims/weighing-tasks/{id}/items   称量记录
POST   /api/lims/batch-records               创建批记录
GET    /api/lims/batch-records/{id}/review   批记录审核
```

### 模块间接口

```
LIMS → MES:   配方下发给工单
LIMS ← MES:   称量完成的物料信息
LIMS ← Equip: 设备配方参数
LIMS → QMS:   称量偏差 → 偏差处理
LIMS ← ERP:   物料批次信息
```

## 数据库设计要点

- 配方表 (lims_formula)
- 配方组分表 (lims_formula_component)
- 称量任务表 (lims_weighing_task)
- 称量明细表 (lims_weighing_item)
- 批记录表 (lims_batch_record)
- 批记录关联表 (lims_batch_process_record, lims_batch_inspection, etc.)
