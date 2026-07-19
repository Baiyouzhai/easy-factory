# 高层级设计裁定

> **日期**: 2026-07-19 | **版本**: v1.0
> 本文档对所有模块的"待决策"、"AI 协作建议"、"推荐实施"做出最终裁定。
> 裁定后的结论回写到各模块 .md 文档的"已决策"清单，原"待决策"清单移除或标注已裁定。

---

## 一、系统级裁定（影响多个模块）

### 1.1 事件发布策略 → 统一为 Service 层发布

**裁定**：领域事件由 Service 实现层发布，不在 Model 层发布。

**理由**：事件发布的时机、上下文（谁操作的、为什么）是 Service 层知识，不是模型层知识。当前 Blueprint/Document/Formula 等模型内嵌了 `publishEvent()`，这是早期约定的历史遗留。

**执行**：
- 现有已内嵌事件发布的模型 **保持兼容**，不强制改造
- **Phase 3 起**，所有新模块（mes/qms/aps/andon）的 Model 层不包含 `publishEvent()` 调用
- Service 实现类统一负责：`transition()` → `markUpdated()` → `repository.save()` → `publishEvent()`
- 在 CLAUDE.md 和 overview.md §6.8 写入此规则

### 1.2 跨模块引用方式 → 编码查找 + 接口返回

**裁定**：跨模块引用以 String 编码为主（松耦合），Repository 按编码查找后返回 core 接口（类型安全）。

**理由**：物理层通过 `String equipmentCode` 查找设备，不持有 Equip 编译期依赖。查找结果返回 `IEquipment` 接口，提供类型安全。这是松耦合与类型安全的平衡点。

**执行**：
- `IEquipmentBinding.getEquipmentCode()` 保持返回 String
- `ProcessRouteMatcher.EquipmentFound` 可新增可选 `IEquipment` 字段
- 不做 `String → IEquipment` 的强制迁移

### 1.3 batch/ 包拆分 → 按领域分包

**裁定**：`com.byz.factory.batch` 按领域拆分为子包。

**目标结构**：
```
com.byz.factory.batch     → 保留：IBatch, ITraceable, IWorkOrder, IInspectionOrder（批次追溯核心）
com.byz.factory.equip     → IEquipment, IEquipmentParameter, IEquipmentRecipe, IOEMetrics, MachineStatus
com.byz.factory.scm       → ISupplier, IPurchaseOrder, PurchaseOrderStatus, SupplierStatus
com.byz.factory.lims      → IFormula, IWeighingTask, IBatchRecord, FormulaStatus, ...
com.byz.factory.wms       → IStorage, IReceipt, IInventorySnapshot, IPickingTask, ...
com.byz.factory.mps       → IProductionPlan, IDemandSource, ProductionPlanStatus
com.byz.factory.dms       → IDocument, DocumentStatus, DocumentCategory
com.byz.factory.iot       → ICommand, IAlarmEvent, ITagValue, IDeviceConnection, CommandStatus
com.byz.factory.eam       → IAsset, IMaintenanceOrder, ICalibrationRecord, AssetStatus, ...
com.byz.factory.erp       → IErpTransaction, TransactionStatus, TransactionType
```

**执行**：Phase 3 开始前用 IDE 重构完成。涉及 40+ 文件的 package 声明和 import 更新。

---

## 二、Phase 3 模块裁定（MES + APS）

### 2.1 MES：工单与蓝图的关系 → 运行时解析 + 冻结快照

**问题**：工单引用 Blueprint 创建工序副本还是运行时解析？

**裁定**：运行时按版本号解析，工单发布时冻结 Blueprint 版本号。

**执行**：
- `MesWorkOrder` 增加 `blueprintVersion` 字段
- 工单发布时记录当前 Blueprint 的 `version`（不是副本）
- 运行时按 `blueprintCode + blueprintVersion` 从 PLM 获取 Blueprint
- 这保证蓝图后续变更不影响已发布工单，且节省存储（不复制工序树）

### 2.2 MES：报工粒度 → 动作级

**问题**：报工粒度是工序级还是动作级？

**裁定**：动作级报工，与 `ITraceable` 的 before/after 快照粒度一致。

**执行**：
- 创建 `ActionRecord`（MES 模型），每个动作执行后生成一条记录
- `ProcessRecord` 作为工序级聚合视图（汇总其下所有 ActionRecord）
- `ITraceable` 在动作完成后由 MES 写入

### 2.3 MES：并行工序 → ExecutionMode.PARALLEL

**问题**：并行工序如何建模？

**裁定**：使用 core 已有的 `ExecutionMode.PARALLEL` 标记。`ProcessCycleTime` 已支持并行时间计算。

**执行**：
- `Process.execute()` 检查 `ExecutionMode`：SEQUENTIAL 串行执行动作，PARALLEL 并发提交
- 工厂建模时不限制——并行工序的每个分支必须只能使用不同的工位/设备

### 2.4 MES：QMS 检验触发 → Control.Interrupt + 领域事件

**问题**：QMS 检验触发时 MES 工序暂停/恢复的机制？

**裁定**：使用 core 已有的 `Control.Interrupt` + 领域事件双向通信。

**流程**：
```
MES 执行到检验动作 → Control.Interrupt → 暂停工序
  → publish("mes.process.interrupted", {actionCode, workOrderNo})
  → QMS 订阅 → 创建 InspectionOrder → 执行检验
  → QMS 判定: PASS → publish("qms.inspection.passed") → MES 订阅 → 恢复工序
             FAIL → publish("qms.deviation.created") → CAPA 流程 → resolve → 恢复
```

### 2.5 APS：排程算法 → 规则式起步，优化引擎预留

**问题**：先做简单规则式还是直接上优化引擎？

**裁定**：Phase 3 实现规则式排程（EDD/SPT/CR），同时预留 `IOptimizationEngine` 接口供后续接入 OR-Tools / OptaPlanner。

**首批规则**：
- EDD (Earliest Due Date) — 交期优先
- SPT (Shortest Processing Time) — 最小工时优先
- CR (Critical Ratio) — (交期-今天)/剩余加工时间，最小者优先
- 规则可组合（先按优先级分组 → 组内按 EDD 排序）

### 2.6 APS：重排程触发 → 事件驱动

**裁定**：以下事件触发增量重排程（非全量）：
- `mes.workorder.released`（新工单插入）
- `equip.fault.reported`（设备不可用）
- `scm.receipt.delayed`（物料延迟）

全量重排程由计划员手动触发（每日/每班次），非自动。

---

## 三、Phase 4 模块裁定（QMS + Andon + EAM）

### 3.1 QMS：检验方案存储 → 结构化字段优先

**问题**：InspectionPlan 存储为结构化字段还是 JSON？

**裁定**：结构化字段为主，扩展属性（IExpand）为辅。

**执行**：
- 标准字段（检验项目/方法/规格限/AQL）→ 结构化
- 非标字段（客户特殊要求/法规条款引用）→ IExpand JSON

### 3.2 QMS：SPC 控制限 → 双模式

**问题**：SPC 控制限固定值还是自动计算？

**裁定**：两种模式并存，通过配置切换：
- **固定模式**：使用 `IProcessParameter` 的 `upperLimit/lowerLimit`（适用于已验证工艺）
- **自动模式**：从 IoT 采集的历史数据自动计算 UCL/LCL（适用于新工艺验证阶段）
- 控制限变更需要审批（DMS 审批流）

### 3.3 QMS：偏差处理流程 → 自动暂停 + 人工介入

**问题**：偏差处理是自动还是人工？

**裁定**：MES 自动暂停 → QMS 自动创建 Deviation → **人工判定**（QA 决定是拒绝/让步接收/返工）→ CAPA（如需要）→ 通知 MES 恢复。

**理由**：GMP 合规要求偏差判定由 QA 人工决策，不可完全自动化。

### 3.4 Andon：模块定位 → 独立模块

**问题**：Andon 是否需要独立模块还是作为 MES 子系统？

**裁定**：独立模块。Andon 接收多源事件（MES 异常 + IoT 报警 + QMS 偏差），是跨模块的异常响应中枢，不是 MES 的子系统。

**上报规则**：静态配置（数据库表），不需要脚本化。上报链：操作工(1min) → 班组长(3min) → 车间主任(5min) → 厂长(10min)。

**通知渠道**：先实现企业微信/钉钉 Webhook，预留短信/邮件/声光报警接口。

### 3.5 DMS：AuditTrail → AOP 自动 + 手动补充

**问题**：AuditTrail 是 AOP 切面自动记录还是各模块手动调用？

**裁定**：双轨。

- **AOP 自动记录**：Service 层方法调用（谁、何时、调了什么方法、参数摘要）
- **手动补充**：关键业务决策（审批、驳回、报废）由业务代码显式创建 `AuditTrail` 记录 why（原因）
- AOP 记录"发生了什么"，手动记录"为什么这么做"——两者互补

### 3.6 DMS：电子签名合规 → 自建

**问题**：21 CFR Part 11 / EU Annex 11 电子签名如何满足？

**裁定**：自建，不依赖外部 EDMS。core 已有 `IElectronicSignature` + `AuditTrail` 基础设施。

**合规要点**：
- 签名=用户名+密码（或生物识别）二次验证，不是简单的 checkbox
- 签名与文档内容绑定（签名时计算文档 hash）
- 签名不可篡改（存储签名记录 + 签名时的文档快照 hash）
- 审计追踪包含：谁、何时、签了什么、签名含义(REVIEWED/APPROVED/VERIFIED)

---

## 四、Phase 1-2 模块裁定（已有约定的补充）

### 4.1 LIMS：配方版本变更 → 关联旧版本（不自动升级）

**问题**：配方版本变更后，已创建的称量任务关联旧版本还是自动升级？

**裁定**：已创建的称量任务关联旧版本（不自动升级）。

**理由**：GMP 合规要求——批记录必须记录实际使用的配方版本。如果自动升级，追溯链断裂。新批次自动使用最新 ACTIVE 版本。

### 4.2 LIMS：称量数据来源 → IoT 网关（天平→IoT→LIMS）

**问题**：天平串口直连还是 IoT 网关？

**裁定**：天平 → IoT 协议适配器（串口/RS232/TCP） → IoT 网关 → LIMS。

**理由**：统一数据采集通道。天平视为一种"设备"，数据流走标准 IoT→Equip→LIMS 路径。避免 LIMS 直接对接硬件。

### 4.3 LIMS：批记录生成 → 边做边记

**问题**：批记录实时生成还是批完成后统一生成？

**裁定**：边做边记（实时记录，批次完成后提交审核）。

**执行**：
- MES 每个动作完成后 → 写入 `ProcessRecord`
- LIMS 每次称量完成后 → 写入 `WeighingItem`
- QMS 每次检验完成后 → 写入检验记录
- 批次完成时 → `BatchRecord.submitForReview()`，聚合所有记录

### 4.4 IoT：协议适配器 → 集成现有库

**裁定**：
- OPC UA → Eclipse Milo（Java 生态最成熟）
- Modbus → Modbus4J 或 j2mod
- MQTT → Eclipse Paho
- Siemens S7 → 暂缓（Phase 4 根据实际需求决定）
- HTTP → Spring RestTemplate（内置）

**时序数据库**：Phase 3 暂用 PostgreSQL（利用已有基础设施），Phase 5 根据数据量评估是否需要 TDengine/TimescaleDB。

**边缘计算**：数据过滤放在 IoT 适配器层（服务器侧，非设备侧）。Deadband 已在 `TagValue.deadband` 中定义。

### 4.5 Equip：OEE 六大损失 → IoT 自动 + 人工补充

**裁定**：
- 自动采集（IoT）：设备故障、空转暂停、减速运行
- 人工录入：换型调整时间、启动废品原因、过程废品数量
- 审批流程：设备状态变更（IDLE↔RUNNING）不需要审批，但故障→维修需要通知 EAM

### 4.6 WMS：批次策略 → 物料主数据驱动

**问题**：FIFO/FEFO/LEFO？

**裁定**：由 `MaterialCache` 的 `erp.shelfLife` 字段驱动：
- 有保质期 + `erp.batchManaged=true` → FEFO（先到期先出）
- 无保质期 → FIFO（先进先出）
- 特殊物料（如需要陈化的化学品）→ LEFO，由工艺参数显式标记

### 4.7 WMS：库存事务回传 → 定时批量

**问题**：库存事务实时回传 ERP 还是按批次汇总？

**裁定**：定时批量（每 30 分钟或每满 100 条），通过 `ErpAdapterService.postTransfer()` 回传。实时性要求高的场景（如投料消耗）走 `Transaction` 队列，独立发送。

### 4.8 ERP：对接策略 → 适配器模式，默认提供 REST 接口

**裁定**：不绑定具体 ERP 产品。`ErpAdapterService` 定义契约，具体 ERP 对接通过实现类完成。

- **Phase 3**：提供 `MockErpAdapter`（内存实现，供 MES/WMS 集成测试）
- **Phase 5**：根据实际部署环境实现具体适配器（SAP RFC / 用友 REST / 金蝶 WebService）

**同步方式**：定时全量（每日）+ 增量（每 15 分钟）。ERP 推送不被假设可用。

**重试策略**：最大 3 次，指数退避（1min → 5min → 15min），3 次失败后标记 FAILED 并通知人工处理。

### 4.9 SCM：模块定位 → 自建轻量模块

**裁定**：自建。大多数中小企业 ERP 的采购模块功能有限或不符合 GMP 供应商管理要求。

- **供应商评分模型**：5 维度（Quality 质量/Delivery 交期/Cost 成本/Service 服务/Flexibility 灵活性），各维度权重可配置
- **采购审批流**：对接 DMS 审批流，不单独实现

### 4.10 BI：技术选型 → 与 web 模块统一

**裁定**：
- **前端**：与 web 模块统一（Vue 3），不独立部署
- **实时推送**：SSE（Server-Sent Events），比 WebSocket 更简单，适合 KPI 看板的单向推送场景。刷新频率：操作层 5s，战术层 30s，战略层 1h
- **报表引擎**：Phase 5 用 SQL 视图 + JSON API；不引入 Grafana/Superset（增加运维复杂度）
- **移动端**：Phase 5 出一版响应式 Web 即可（PWA），不单独开发 App

### 4.11 APS：软硬约束 + MES 反馈

**裁定**：
- 交期延误 → 软约束（可违反，违反时有惩罚分）
- 设备容量超限 → 硬约束（不可违反）
- 人员资质不匹配 → 硬约束
- MES 反馈（实际工时 vs 计划工时）→ APS 用来修正剩余产能估算，每日更新一次

---

## 五、描述体系裁定：池→线固化（2026-07-19）

> 相关讨论详见 CLAUDE.md 会话历史。这是对"汽车线 vs PCB 线可配置切换"和"能力模型要不要做"的最终结论。

### 5.1 核心原则：配线是配置，不是计算

制造现场的"哪个动作由哪个设备执行"**在设线时由工艺工程师人工配置**，不在执行时由系统动态匹配。固化到线的过程是配置数据，不是运行时计算。

### 5.2 三层描述体系

```
═══════════════════════════════════════════════════════════════
第 1 层：设计态（Blueprint — 池）
  在哪:   PLM 模块
  谁做:   工艺工程师
  内容:   Action 只描述"需要什么能力"，不指定具体设备
  例子:   Action(钻孔) → requireResources: PCB基板
          "需要: 钻孔能力, 孔径≥0.2mm" — 这是人读的注释，设备字段留空

═══════════════════════════════════════════════════════════════
第 2 层：设线态（IEquipmentBinding — 固化）
  在哪:   Physical 包（core） + Equip 模块
  谁做:   产线工程师
  内容:   Action 绑定到具体设备 + 优先级 + 参数
  例子:   Action₁(钻孔) → DX-001(Primary), DX-002(Backup)
          Action₂(沉铜) → DP-003(Primary)
          这是配置数据，不是代码——存在数据库里，可以改

═══════════════════════════════════════════════════════════════
第 3 层：执行态（MES — 走固化线）
  在哪:   MES 模块
  内容:   读 IEquipmentBinding → 查设备状态 → 选可用的一台 → 执行
  逻辑:   Primary 空闲 → 用 Primary
          Primary 故障 → 用 Backup
          全故障 → 报错/暂停/通知 Andon
═══════════════════════════════════════════════════════════════
```

### 5.3 配置化切换：同一产品，不同工厂

同一张 Blueprint 在不同工厂落地时，唯一的差异是 **IEquipmentBinding 的配置不同**：

```
产品: 4层控制板
Blueprint: "4层控制板 v1.0" (池——所有工厂共享同一份)

深圳工厂(FACTORY-SZ):
  Action₁(钻孔) → [DX-001(Primary), DX-002(Backup)]  ← 深圳的钻机
  Action₂(沉铜) → [DP-003(Primary)]

昆山工厂(FACTORY-KS):
  Action₁(钻孔) → [KS-DR-007(Primary), KS-DR-008(Primary)]  ← 昆山的钻机
  Action₂(沉铜) → [KS-DP-012(Primary)]

切换方式:
  MES 加载 Blueprint → 按 factoryCode 查绑定配置 → 走该工厂的固化线
  不需要重新设计工艺，不需要改写 Blueprint
```

### 5.4 为什么不做能力匹配引擎

| 需要能力匹配引擎的情况 | 实际发生频率 | 是否需要系统支持 |
|----------------------|------------|---------------|
| 新工厂设线 | 一次（设线时配好就不变了） | 人工配置，不需要自动化 |
| 新增设备 | 偶尔（加到绑定列表即可） | 一条配置记录 |
| 设备故障切换到备用 | 偶尔（Primary→Backup） | 状态查询 + 简单 fallback |
| 换产品重新设线 | 按产品频次（几个月一次） | 人工重新配置 |
| 运行时动态匹配 | **极少发生** — 这不是正常制造流程 | 不需要 |

**运行时的全部逻辑就是查状态 + 选可用的。不需要 Cpk 评估，不需要能力池查询，不需要匹配引擎。**

### 5.5 否决记录：能力模型

以下从早期讨论中产生的概念**明确否决**，不作为开发任务：

| 概念 | 决定 | 理由 |
|------|------|------|
| `ICapability` / `ICapabilityProvider` | [×] 不做 | 设备能做什么 = IEquipmentBinding 配置，不需要额外抽象 |
| `CapabilityPool` 运行时聚合 | [×] 不做 | 执行时只需要"配置的那几台谁空着" |
| `IFrozenRoute` | [×] 不做 | IEquipmentBinding 本身就是固化产物，另起名字是重复 |
| `IExecutionResolver` (PoolResolver) | [×] 不做 | 设备选择 = 一个状态查询，不需要策略模式 |
| 运行时 Cpk 评估做路由 | [×] 不做 | Cpk 是 SPC 的周期性监控，不是每个动作执行前都要算 |

### 5.6 保留为远期扩展点

以下内容**不做但也不反对未来做**，当满足以下条件时可重新考虑：

- easy-factory 已在 > 5 个工厂部署
- 单个工厂管理的设备数 > 200 台
- 出现真实的"人工配置跟不上了"的业务需求
- 有实际的"高混合低批量"生产需要跨工厂自动调度

此时可参考业界实践（Siemens BOPEX、用友多工厂调度）引入优化层。当前阶段，这些属于过度设计。

---

## 六、AI 协作建议整合

以下为各模块实现时的统一指导原则，替代分散在各模块 .md 中的"AI 协作建议"：

### 5.1 通用实现顺序

每个模块的标准实现路径：

```
1. Model 层：继承正确基类 + 实现 core 接口 + 业务便捷方法
2. Service 接口：定义方法签名，标注跨模块协作方向
3. Service 实现类：实现业务逻辑 + 领域事件发布 + 调用 Repository
4. Repository 接口：继承 IRepository<T,ID>
5. Test：Given-When-Then + 13 项覆盖清单（见 overview.md §7.4）
6. Controller：REST API（Phase 5 web 聚合时统一实现）
```

### 5.2 模块实现优先级

| 批次 | 模块 | 关键集成点 |
|------|------|-----------|
| **当前** | mes | 蓝图解析 + 工序流转 + 报工 + 与 QMS 的 Interrupt 机制 |
| | aps | 规则式排程（EDD/SPT/CR）+ 约束校验 + Gantt 输出 |
| **下一批** | qms | InspectionPlan + SPC 分析 + 偏差/CAPA 闭环 |
| | andon | 异常事件订阅 + 逐级上报 + 联动动作 |
| | eam | 维护计划 + 校准到期提醒 + 与 Equip 状态联动 |
| **最后** | bi | 看板聚合（只读消费） |
| | web | REST API 聚合 + 统一认证 |
| | test | 阿莫西林全流程集成测试 |

### 5.3 实现原则

1. **先跑通主链路，再做异常分支**：正常流程先实现（工单创建→工序流转→完工），异常流程后补（中断、返工、报废）
2. **先内存实现，再持久化**：Service 实现类先用 `ConcurrentHashMap` 做存储，Phase 5 统一换数据库
3. **先规则引擎，再机器学习**：SPC 控制限、APS 排程先用固定规则，不引入 ML 依赖
4. **领域事件是集成胶水**：模块间不直接调用对方的 Service，通过事件异步通信
5. **每个模块独立可测**：不依赖其他模块的 Service 实现即可运行单元测试（Mock 外部依赖）
