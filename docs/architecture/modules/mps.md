# easy-factory-mps — 主生产计划

## 模块定位

根据销售订单、预测需求、库存水平，生成中长期的主生产计划（MPS）。
回答"何时生产多少什么产品"。

MPS 是 MES 的上游——MPS 告诉 MES"需要生产什么"，MES 负责"如何生产"。

### MPS vs APS 的区别

| 维度 | MPS（主生产计划） | APS（高级排程） |
|------|-----------------|-----------------|
| 时间粒度 | 天/周 | 分钟/小时 |
| 产能考虑 | 粗产能检查 (RCCP) | 有限产能排程 (FCS) |
| 输出 | 产品、数量、交付周 | 具体设备、时间段、顺序 |
| 输入 | 订单+预测 | MPS + 工序工时 + 设备日历 |

## 核心模型

### ProductionPlan (生产计划)

```
ProductionPlan:
├── planNo:           计划编号
├── version:          版本
├── periodType:       周期类型 (WEEKLY/MONTHLY/QUARTERLY)
├── periodStart:      周期起始
├── periodEnd:        周期结束
├── status:           状态 (DRAFT/APPROVED/RELEASED)
├── items:            计划明细列表
│   ├── productCode:  产品编码
│   ├── productName:  产品名称
│   ├── quantity:     计划数量
│   ├── dueDate:      交付日期
│   ├── priority:     优先级
│   └── factoryCode:  执行工厂
├── createdAt:        创建时间
└── approvedBy:       批准人
```

### DemandSource (需求来源)

```
DemandSource:
├── sourceType:       来源类型 (SALES_ORDER/FORECAST/SAFETY_STOCK/MANUAL)
├── referenceNo:      来源单号
├── productCode:      产品编码
├── quantity:         需求数量
├── dueDate:          需求日期
├── priority:         优先级
└── customer:         客户 (销售订单时)
```

### CapacityCheck (粗产能检查)

```
CapacityCheck:
├── planNo:           关联计划
├── factoryCode:      工厂
├── resourceType:     瓶颈资源 (MACHINE/PERSONNEL/MATERIAL)
├── requiredCapacity: 需求产能(小时)
├── availableCapacity:可用产能(小时)
├── utilizationRate:  利用率(%)
├── status:           结果 (PASS/WARNING/FAIL)
└── bottleneck:       瓶颈工序
```

## 外部接口

```
POST   /api/mps/plans                      创建生产计划
PUT    /api/mps/plans/{id}/approve         审批
POST   /api/mps/plans/{id}/release         发布到 MES
GET    /api/mps/capacity-check/{planId}    粗产能检查
GET    /api/mps/demands                    需求列表
```

## 模块间接口

```
MPS ← ERP:   销售订单、成品库存
MPS ← MES:   在制品(WIP)状态、已完成工单
MPS → MES:   发布生产计划 → 生成工单
MPS ← PLM:   产品工艺路线（用于产能计算）
MPS → APS:   MPS 作为 APS 的输入进行精细排程
```

## 实现状态（2026-07-19 更新）

### 已完成
- [x] `ProductionPlan` — 实现 `IProductionPlan`，`BaseLifecycleEntity<ProductionPlanStatus>`，含 `PlanItem implements IPlanItem` + 6 个业务便捷方法（approve/reject/release/start/complete/close）+ 领域事件
- [x] `DemandSource` — 实现 `IDemandSource`，`BaseEntity`，含 SALES_ORDER/FORECAST/SAFETY_STOCK/MANUAL 四种来源
- [x] `DemandSourceType` — MPS 内部枚举（SALES_ORDER/FORECAST/SAFETY_STOCK/MANUAL）
- [x] `CapacityCheck` — 粗产能检查值对象（PASS/WARNING/FAIL），`isAcceptable()` 判定
- [x] `MpsService` — 接口已定义（计划 CRUD + 生命周期 + 需求管理 + RCCP）
- [x] `ProductionPlanStatus` — 状态枚举（core `batch/`，DRAFT→APPROVED→RELEASED→IN_PROGRESS→COMPLETED→CLOSED）
- [x] `IProductionPlan`(含 `IPlanItem`) / `IDemandSource` — 跨模块接口（core `batch/`）
- [x] `MpsEventTypes` — 6 个事件常量（core `event/types/`）
- [x] 单元测试 — `MpsModuleTest`（47 个测试）
- [ ] MpsService 实现类
- [ ] `checkCapacity()` 与 core `FactoryCapacityProfile`/`BottleneckDetector` 集成

### Core 接口引用

| 接口 | 位置 | 说明 |
|------|------|------|
| `IProductionPlan` | core `batch/` | 生产计划 + 内嵌 `IPlanItem` |
| `IDemandSource` | core `batch/` | 需求来源（sourceType/referenceNo/productCode/quantity/dueDate） |
| `ProductionPlanStatus` | core `batch/` | DRAFT→APPROVED→RELEASED→IN_PROGRESS→COMPLETED→CLOSED |

### 制造标准背景

MPS 在制造计划体系中的位置（S&OP 产销协调流程）：

```
长期(年)   → S&OP 产销协调 → 需求计划 + 供应计划
中期(月/周) → MPS 主生产计划 → RCCP 粗产能检查（★ 本模块职责）
短期(日)   → APS 高级排程   → CRP 细产能需求计划
实时       → MES 制造执行   → 工单工序流转
```

| 概念 | 体现 |
|------|------|
| **RCCP 粗产能检查** | `CapacityCheck` — 需求产能 vs 可用产能（小时），利用率和瓶颈识别 |
| **MTO / MTS / MTA** | 制造策略：Make-to-Order(按单) / Make-to-Stock(备库) / Make-to-Availability(可用量承诺) |
| **ATP 可用量承诺** | 计划量 − 已承诺量 = 可承诺量 → `PlanItem.quantity` 与销售订单对冲 |
| **MPS 时界** | 冻结期(不可改) / 半冻结期(可调整) / 自由期(可新增) → `ProductionPlanStatus` 控制 |
| **DemandSourceType** | 独立需求(销售订单/预测) vs 相关需求(安全库存补货) |

### 待决策
1. 需求优先级如何排序？交期紧急度还是客户重要性？
2. 粗产能检查的模型：仅看瓶颈设备还是所有资源？
3. MPS 滚动周期多长？每周滚动还是每月冻结？
4. 与 APS 的边界：MPS 出周计划，APS 排到小时——是否需要明确的数据契约？

> **最后更新**: 2026-07-19

