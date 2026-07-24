# easy-factory-bi — 看板与报表系统

## 模块定位

BI 是**协作层只读数据消费模块**——聚合所有业务模块（MES/QMS/Equip/LIMS/WMS/MPS/APS/Andon/EAM）的数据，生成制造运营的可视化看板和 KPI 报表。BI 不产生业务数据，只读取和聚合。

## 当前实现（2026-07-25 约定建立）

### 已实现模型

| 模型 | 类型 | 说明 |
|------|------|------|
| `KpiSnapshot` | BaseEntity 实体 | KPI 快照：10 种标准 KPI + 动态存取 + 计算元数据 |
| `ProductionDashboard` | record 值对象 | 生产看板：计划完成率 + 在制品 + 工单进度明细 |
| `QualityDashboard` | record 值对象 | 质量看板：一次合格率 + 偏差统计 + CAPA 关闭率 |
| `OeeDashboard` | record 值对象 | OEE 看板：设备级 A×P×Q + 平均/最佳/最差 |
| `InventoryDashboard` | record 值对象 | 仓储看板：周转率 + 呆滞/待检/近效期预警 |
| `KpiType` | enum | 10 种标准 KPI 指标（含显示名 + 数据来源 + 目标值） |
| `DashboardPeriod` | enum | 7 种统计周期（含 SSE 刷新间隔） |

### 已实现接口

- `DashboardService` — 17 方法：4 看板 + KPI CRUD + 批次追溯报告

### Core 层新增

- `BiEventTypes` — 6 个领域事件（2 KPI + 4 看板刷新）

## 核心看板

### 1. 生产看板（5s 刷新）

```
┌──────────────────┬──────────────────┬──────────────────┐
│  计划完成率       │  在制品(WIP)      │  当日产出         │
│  85.3% ▲         │  12 批次         │  9,500 片        │
├──────────────────┼──────────────────┼──────────────────┤
│  总工单: 15  |  已完成: 8  |  进行中: 5  |  中断: 2      │
│                                                   │
│  工单进度:                                          │
│  工单WO-001 产品A ████████████░░ 80% (压片中)       │
│  工单WO-002 产品B ██████░░░░░░░░ 40% (制粒中)       │
│  工单WO-003 产品C ░░░░░░░░░░░░░░  0% (待开始)       │
└───────────────────────────────────────────────────┘
```

### 2. 质量看板（30s 刷新）

```
┌──────────────────┬──────────────────┬──────────────────┐
│  一次合格率       │  未关闭偏差       │  CAPA关闭率       │
│  98.7%           │  3 OPEN          │  87.5%           │
├──────────────────┴──────────────────┴──────────────────┤
│  总检验: 150  |  通过: 148  |  失败: 2                  │
│  最近偏差:                                              │
│  DEV-002 压片片重偏移  MAJOR  OPEN  2026-07-12         │
│  DEV-001 水分超标      MINOR  CLOSED 2026-07-11        │
└────────────────────────────────────────────────────────┘
```

### 3. 设备 OEE 看板（5s 刷新）

```
┌──────────┬──────┬──────┬──────┬──────┬──────┬──────────┐
│ 设备      │ 可用率 │ 性能率 │ 质量率 │ OEE  │ 状态  │ 趋势     │
├──────────┼──────┼──────┼──────┼──────┼──────┼──────────┤
│ WG-001   │ 95%  │ 88%  │ 99%  │ 83%  │ RUN  │ → 稳定   │
│ MX-001   │ 92%  │ 91%  │ 98%  │ 82%  │ RUN  │ ↗ 提升   │
│ PT-001   │ 78%  │ 85%  │ 97%  │ 64%  │ IDLE │ ↘ 需关注  │
├──────────┴──────┴──────┴──────┴──────┴──────┴──────────┤
│  平均 OEE: 73.5%  |  最佳: WG-001  |  最差: PT-001     │
└────────────────────────────────────────────────────────┘
```

### 4. 仓储看板（30s 刷新）

```
┌──────────────────────────────────────────────┐
│  库存周转率:  12.5 次/月                      │
│  呆滞物料:    3 项 (> 90天未动)              │
│  待检库存:    5 批                            │
│  近效期物料:   2 项 (< 30天内过效期)           │
│  在手总计: 10,000 | 可用: 6,000 | 库存金额: 50万│
└──────────────────────────────────────────────┘
```

## KPI 指标定义

| KPI | KpiType 枚举 | 计算公式 | 数据来源 | 目标 |
|-----|-------------|---------|---------|------|
| 计划完成率 | PLAN_COMPLETION_RATE | 实际产出/计划产出 × 100% | MES + MPS | ≥ 95% |
| 一次合格率 | FIRST_PASS_RATE | 一次合格数/总产出 × 100% | QMS | ≥ 98% |
| OEE | OEE | 可用率 × 性能率 × 质量率 | Equip + IoT | ≥ 85% |
| 平均维修时间 | MTTR | 总维修时间/维修次数 | EAM | < 2h |
| 平均故障间隔 | MTBF | 总运行时间/故障次数 | IoT + EAM | > 500h |
| 批次收率 | BATCH_YIELD | 实际产量/批量 × 100% | LIMS | ≥ 97% |
| 库存周转率 | INVENTORY_TURNOVER | 月出库量/平均库存 | WMS | ≥ 10 |
| 偏差关闭率 | DEVIATION_CLOSURE_RATE | 已关闭偏差/总偏差 × 100% | QMS | ≥ 90% |
| CAPA关闭率 | CAPA_CLOSURE_RATE | 已验证CAPA/总CAPA × 100% | QMS | ≥ 95% |
| 安灯响应时间 | ANDON_RESPONSE_TIME | 总响应时间/呼叫次数 | Andon | < 5min |

## 数据架构

```
                   ┌──────────────────┐
                   │   Web (Vue 3)     │
                   │   SSE 推送看板    │
                   └────────┬─────────┘
                            │
                   ┌────────▼─────────┐
                   │  DashboardService │
                   │  数据聚合层(只读)  │
                   └────────┬─────────┘
                            │
          ┌─────────────────┼─────────────────┐
          │        │        │        │         │
        MES      QMS     Equip    LIMS      WMS
       MPS/APS  Andon    EAM      ERP       SCM
```

## 外部接口（REST API，Phase 5 Web 模块统一实现）

```
GET    /api/bi/dashboard/production?factoryCode=    生产看板数据 (5s)
GET    /api/bi/dashboard/quality?factoryCode=       质量看板数据 (30s)
GET    /api/bi/dashboard/oee?factoryCode=           OEE 看板数据 (5s)
GET    /api/bi/dashboard/inventory?factoryCode=     仓储看板数据 (30s)
GET    /api/bi/kpi?factoryCode=&period=             月度KPI
GET    /api/bi/kpi/history?factoryCode=&kpiType=    KPI历史趋势
GET    /api/bi/reports/batch/{batchNo}              批次追溯报告

SSE    /api/bi/stream/production?factoryCode=       生产看板实时推送
SSE    /api/bi/stream/oee?factoryCode=              OEE 看板实时推送
```

## 已决策

1. **前端技术栈** — 与 web 模块统一（Vue 3），不独立部署
2. **实时推送** — SSE（Server-Sent Events）；刷新频率：操作层 5s，战术层 30s，战略层 1h
3. **报表引擎** — Phase 5 用 SQL 视图 + JSON API；不引入 Grafana/Superset
4. **移动端** — Phase 5 出一版响应式 Web（PWA），不单独开发 App
5. **KPI 数据源映射** — 使用 KpiType 枚举的 dataSource 字段标注数据来源，Service 实现层订阅对应领域事件增量更新

## 待实现

- DashboardService 实现类
- REST 控制器 + SSE 推送端点
- 事件订阅与增量 KPI 更新引擎（消费 MES/QMS/Equip/LIMS/WMS/MPS/APS/Andon 事件）
- SQL 视图定义（KPI 聚合查询）
- Vue 3 前端看板组件
- 响应式 PWA 移动端
- 批次追溯报告生成（聚合 MES+LIMS+QMS 数据）

## 跨模块事件订阅

BI Service 实现层订阅以下事件进行增量 KPI 更新：

| 事件 | 来源模块 | 更新目标 |
|------|---------|---------|
| `mes.workorder.created/completed/closed` | MES | 生产看板工单统计 |
| `mes.process.started/completed` | MES | 工序进度 |
| `qms.inspection.completed/passed/failed` | QMS | 质量看板合格率 |
| `qms.deviation.created/resolved` | QMS | 偏差统计 |
| `qms.capa.closed` | QMS | CAPA 关闭率 |
| `equip.oee.calculated` | Equip | OEE 看板 |
| `mps.plan.started/completed/closed` | MPS | 计划执行统计 |
| `aps.task.delayed` | APS | 排程延误统计 |
| `andon.call.created/resolved/closed` | Andon | 安灯看板、响应时间 |
| `lims.batch_record.approved` | LIMS | 批次收率 |
| `wms.inventory.changed` | WMS | 仓储看板 |
