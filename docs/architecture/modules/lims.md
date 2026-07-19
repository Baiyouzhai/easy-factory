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
// IResourceItem IExpand 扩展字段
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

## 遗留问题

### 当前实现（2026-07-19 更新）
- [x] `Formula` — 实现 `IFormula` + `HasVersion`，`BaseLifecycleEntity<FormulaStatus>`，含 `submitForApproval()`/`approve()`/`activate()`/`retire()`/`reject()` 业务方法 + 领域事件发布 + `FormulaPhase` 内嵌类
- [x] `WeighingTask` — 实现 `IWeighingTask`，`BaseLifecycleEntity<WeighingTaskStatus>`，含 `startWeighing()`/`verify()`/`completeWeighing()` + 偏差检测 + 事件发布
- [x] `WeighingItem` — 实现 `IWeighingTask.IWeighingItem`，含 `recordWeighing()`/`getDeviationPercent()`/`isOutOfTolerance()`
- [x] `BatchRecord` — 实现 `IBatchRecord`，`BaseLifecycleEntity<BatchRecordStatus>`，含 `submitForReview()`/`approve()`/`reject()`/`archive()` + 记录添加方法
- [x] `FormulaService` / `WeighingTaskService` / `BatchRecordService` — 接口已定义
- [x] `FormulaStatus` / `WeighingTaskStatus` / `BatchRecordStatus` — 状态枚举（core `batch/`）
- [x] `IFormula`(含 `IFormulaPhase`) / `IWeighingTask`(含 `IWeighingItem`) / `IBatchRecord` — 跨模块接口（core `batch/`）
- [x] `LimsEventTypes` — 9 个事件常量（core `event/types/`）
- [x] 单元测试 — `LimsModuleTest`（47 个测试，覆盖全部模型+事件）
- [ ] 称量防错脚本 — 示例脚本存在（`weighing_check.js`）但未与 FormulaService 集成
- [ ] FormulaService / WeighingTaskService / BatchRecordService 实现类

### Core 接口引用

| 接口 | 位置 | 说明 |
|------|------|------|
| `IFormula` | core `batch/` | 配方抽象 + 内嵌 `IFormulaPhase` |
| `IWeighingTask` | core `batch/` | 称量任务抽象 + 内嵌 `IWeighingItem` |
| `IBatchRecord` | core `batch/` | 批记录抽象 |
| `FormulaStatus` | core `batch/` | DRAFT/APPROVED/ACTIVE/RETIRED |
| `WeighingTaskStatus` | core `batch/` | PENDING/WEIGHING/VERIFIED/COMPLETE |
| `BatchRecordStatus` | core `batch/` | IN_PROGRESS/REVIEW/APPROVED/ARCHIVED |

### 制造标准背景

LIMS 在制药行业的核心合规依据：

| 标准 | 体现 |
|------|------|
| **USP<41>/<1251>** | 称量天平精度选择（最小称量值=重复性×安全因子），`WeighingItem.balance` + `tolerance` 字段 |
| **ICH Q1A** | 稳定性考察 → 有效期管理，`Formula` 的 `lims.effectiveDate`/`lims.expiryDate` |
| **ISO 17025** | 校准溯源 → 天平定期校准，EAM 的 `ICalibrationRecord` 联动 |
| **GMP §211.188** | 批记录完整性 → `BatchRecord` 含工序记录+称量+检验+偏差 |

### 待决策
1. 配方版本变更后，已创建的称量任务如何处理？（关联旧版本还是自动升级？）
2. 称量数据来源：天平串口直连还是 IoT 网关？
3. 批记录是实时生成（边做边记）还是批完成后统一生成？

> **最后更新**: 2026-07-19

