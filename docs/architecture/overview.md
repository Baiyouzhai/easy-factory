# 项目总体设计

## 1. 项目愿景

构建一个制造领域的数字化协作平台，以统一的领域模型为核心，衍生覆盖制造企业全价值链的子系统：
制造执行（MES）、质量管理（QMS）、研发工艺（PLM）、设备工艺、配方管理（LIMS）、资源计划（ERP）、设备互联（IoT）。

## 2. 领域驱动设计

### 2.1 统一语言 (Ubiquitous Language)

所有子系统共享以下核心概念，确保跨系统语义一致：

| 术语 | 英文 | 定义 |
|------|------|------|
| 工厂 | Factory | 一个可执行的生产系统，包含一组工序 |
| 工序 | Process | 生产流程中的一个步骤，由一组有序动作组成 |
| 动作 | Action | 工序中的最小操作单元，可携带 JS 脚本定义执行逻辑 |
| 资源 | Resource | 生产所需/产出的基本单元（人机料法环） |
| 资源包 | ResourcePack | 一组资源的集合，即物料清单/配方组分 |
| 蓝图 | Blueprint | 产品的工序路线定义 |
| 产品 | Product | 有蓝图的特殊资源 |

### 2.2 资源分类 (4M1E)

```
SourceGroup:
├── Personnel   (人) — 操作工、质检员、班组长
├── Machine     (机) — 设备、仪器、量检具
├── Material    (料) — 原料、中间体、成品
├── Method      (法) — 工艺参数、SOP、检验标准
└── Environment (环) — 温湿度、洁净度、压差
```

### 2.3 动作语义

```
Execute:
├── Nothing  — 空操作（占位/跳过）
├── Create   — 创建新资源
├── Add      — 增加资源数量
├── Use      — 消耗资源
├── Change   — 改变资源属性
├── Convert  — 将输入资源转化为输出资源（如原料→中间体）
├── Split    — 分装/拆分（一批分多批）
├── Combine  — 混合/合并（多批合一）
├── Transfer — 物料转移（库位→线边仓）
└── Hold     — 待检隔离（暂扣不放行，等待 QMS 判定）
```

### 2.4 流程控制

```
Control:
├── Default   — 顺序执行
├── Interrupt — 条件中断（质量异常时中断流程，如 HACCP CCP 监控触发暂停）
├── Count     — 计数控制（如每 N 件抽检一次）
├── Timing    — 时序控制（如搅拌 30 分钟后自动进入下一步）
└── Repeat    — 重复/返工（如检验不合格返回上道工序）
```

### 2.5 制造标准对标

系统设计参照以下工业标准：

| 标准 | 映射 | 体现位置 |
|------|------|---------|
| **ISA-95** | 企业→工厂→产线→工位 层次 | `IFactory` → `IProductionLine` → `IWorkstation` |
| **ISA-88** | 批控制模型（Procedure→Unit Procedure→Operation→Phase） | `IProcess` → `IAction` 执行链 + `ExecutionMode` |
| **4M1E / TQM** | 人机料法环 全要素质量管理 | `Dict.SourceGroup` (Personnel/Machine/Material/Method/Environment) |
| **GMP** | 批次追溯 + 放行控制 + 偏差处理 | `IBatch` + `ITraceable` + `BatchStatus.RELEASED` + `Control.Interrupt` |
| **21 CFR Part 11** | 电子记录 + 电子签名 合规 | `IElectronicSignature` + `AuditTrail` + `DMS` |
| **ICH Q10** | 制药质量体系（工艺性能+产品质量监控） | QMS 模块 + SPC 分析 + CAPA 闭环 |
| **ISO 9001** | 设计控制 + 文件控制 + 校准溯源 | PLM 版本管理 + DMS 审批流 + EAM 校准记录 |
| **ISO 22400** | 制造 KPI 标准（OEE, MTBF, 良率等） | `IOEMetrics` + BI 模块看板定义 |
| **ISO 14224** | 设备可靠性数据采集 | EAM 维护记录 + Equip 故障记录 |

## 3. 模块全景：核心层 vs 协作层

系统按与制造运营的关联程度分为两层。**同一套 core 接口，同一套事件协议，但边界不同。**

### 核心 MOM 层（制造运营管理）

直接参与制造运营，系统自建的核心能力：

| 模块 | 说明 |
|------|------|
| `mes` | 制造执行：工单→工序流转→报工→追溯 |
| `qms` | 质量管理：检验→SPC→偏差→CAPA |
| `plm` | 研发工艺：工艺路线→BOM转化→版本管理 |
| `lims` | 配方系统：配方版本→称量防错→批记录 |
| `equip` | 设备工艺：台账→设备配方→OEE |
| `iot` | 设备互联：协议适配→数据采集→指令下发 |
| `eam` | 资产管理：维护工单→校准→全生命周期 |
| `wms` | 仓储管理：收货→上架→FIFO拣料→线边仓 |
| `mps` | 主生产计划：订单预测→产能检查→周计划 |
| `aps` | 高级排程：设备×时间×顺序的精细排程 |
| `andon` | 安灯系统：异常呼叫→逐级上报→联动处理 |

### 协作层（通过接口/事件对接，非 MOM 核心）

对接外部系统或用轻量方式集成，通过 core 接口和领域事件与核心层通信：

| 模块 | 说明 | 典型对接方 |
|------|------|-----------|
| `erp` | 资源计划：主数据同步→库存查询→事务回传 | SAP/Oracle/用友/金蝶 |
| `scm` | 供应链：供应商管理→采购订单→来料计划 | SRM 系统 |
| `crm` | 客户关系：客户主数据→销售订单→投诉管理 | Salesforce/用友 |
| `dms` | 文档管理：SOP→批记录审批流→审计追踪 | Veeva/OpenText |
| `bi` | 看板报表：生产/质量/OEE看板+KPI | Grafana/Superset |

### 分层图

```
┌─────────────────────────────────────────────────────────────┐
│                       easy-factory-web                       │
│                      (统一门户 / API 网关)                    │
└─────────────────────────────┬───────────────────────────────┘
                              │
   ┌────────────── 核心 MOM 层 ──────────────┐    ┌──── 协作层 ────┐
   │                                         │    │                │
   │  mes ──── qms    plm    lims           │    │  erp   scm     │
   │   │        │       │      │             │    │   │     │      │
   │   ├────────┼───────┼──────┤             │    │   │     │      │
   │   │        │       │      │             │    │   │     │      │
   │  equip   iot     eam    wms            │    │  crm   dms    │
   │   │        │       │      │             │    │   │     │      │
   │   └────────┼───────┼──────┘             │    │   │     │      │
   │            │       │                    │    │   │     │      │
   │  mps ──→ aps    andon                 │    │  bi (跨层只读)│
   │                 (异常中枢)              │    │              │
   └──────────────────┬─────────────────────┘    └──────┬───────┘
                      │                                 │
                      └──────────────┬──────────────────┘
                                     │
                          ┌──────────▼──────────┐
                          │  easy-factory-core   │
                          │  领域接口 + 事件协议  │
                          │  状态机 + 脚本引擎   │
                          └──────────┬──────────┘
                                     │
                          ┌──────────▼──────────┐
                          │  easy-factory-common │
                          │  IData / IExpand     │
                          └─────────────────────┘
```

### 层的规则

| 维度 | 核心 MOM 层 | 协作层 |
|------|-----------|--------|
| 业务逻辑 | 自建，全量实现 | 轻量适配，核心逻辑在外部 |
| 数据归属 | 系统主数据 | 外部系统主数据的本地缓存 |
| 接口方向 | 提供 API + 消费事件 | 适配外部 API + 发布事件 |
| 被谁替代 | 不做对接，本系统就是实现 | 对接时换适配器，不换内部模型 |
```

## 4. 模块依赖关系

```
common ◄── core ◄── (核心层: mes, qms, plm, lims, equip, iot, eam, wms, mps, aps, andon)
                ◄── (协作层: erp, scm, crm, dms, bi)
                ◄── web (聚合所有模块)
```

- `common` — 无依赖，纯基础层（IData, IExpand, Data, DataExpand）
- `core` — 仅依赖 `common`，定义全部跨模块接口、状态枚举、共享基类、事件基础设施
- 业务模块 — 仅依赖 `core`，模块间**零编译期依赖**——各自通过 core 接口 + 领域事件松散耦合
- 核心层 vs 协作层 — 编译期完全相同，差异仅在运行时职责和对接方式
- `web` — 聚合所有模块，提供统一 REST API 入口

### 运行时依赖（非编译期）

```
erp  ← (无运行时依赖，纯被调用方)
iot  ← (无运行时依赖，设备数据采集)
plm  ← (无运行时依赖，工艺设计)
equip← (无运行时依赖，设备管理)
scm  ← (无运行时依赖，供应链)
dms  ← (无运行时依赖，文档管理)

lims ← erp(物料编码) + plm(蓝图编码)
wms  ← erp(物料主数据) + scm(采购订单)          ← Phase 2
mps  ← plm(产品工艺数据)

mes  ← plm(蓝图) + equip(设备) + wms(物料) + lims(配方)  ← Phase 3
aps  ← mps(计划) + mes(在制品) + equip(设备日历)

qms  ← mes(工序触发检验)
andon← mes(工序异常) + iot(设备报警) + qms(质量异常)   ← Phase 4
eam  ← equip(设备台账)

bi   ← 所有模块(只读消费)                                ← Phase 5
web  ← 所有模块(API 聚合)
test ← 所有模块(跨模块集成场景)
```

## 5. 技术选型

| 层次 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 语言 | Java | 21 | LTS 版本，虚拟线程支持 |
| 框架 | Spring Boot | 3.x (升级) | Web 服务框架 |
| 构建 | Maven | 3.9+ | 多模块构建管理 |
| 脚本引擎 | Nashorn / GraalJS | — | 动作脚本执行 |
| JSON | fastjson2 | 2.0.53 | JSON 序列化 |
| 简化代码 | Lombok | 1.18.30 | 减少样板代码 |
| 持久层 | MyBatis-Plus / JPA | 待定 | 数据库访问 |
| 数据库 | PostgreSQL / MySQL | 待定 | 关系数据库 |
| 缓存 | Redis | 待定 | 会话与缓存 |
| 消息 | RabbitMQ / Kafka | 待定 | 模块间异步通信 |

> **注意**：当前项目为早期阶段，部分技术选型（数据库、缓存、消息）待后续确定。

## 6. 关键设计决策

### 6.1 接口-实现分离

所有领域模型均以接口形式定义在 core 中（如 `IProcess`、`IActionModel`），业务模块提供具体实现。这种设计：
- 允许同一领域概念有多种实现（内存版、数据库版、远程调用版）
- 各模块可以在编译期保持独立，运行时装配

### 6.2 扩展属性机制

`IExpand` 接口赋予领域对象动态属性能力。当不同子系统需要向同一对象附加不同字段时，无需修改 core 接口：
```
Process:
  ├── 原生属性: code, name, order
  └── 扩展属性:
        MES → { workOrderId, dueDate }
        QMS → { inspectionLevel, aql }
        PLM → { version, approvedBy }
```

### 6.3 脚本化动作

每个 Action 可通过 `getScript()` 携带 JavaScript 脚本，由 `ScriptExecutor` 在运行时解释执行。这使得：
- 工序逻辑可以先上线后调整，无需重新部署
- 不同工厂/产线可复用同一工序结构，仅脚本不同

### 6.4 质量内嵌而非外挂

质量检验不是独立模块，而是"长在工序里"的。任意工序都可以包含检验类动作（`SourceGroup: Method`），通过 `Control.Interrupt` 实现质量异常中断。

### 6.5 生命周期状态机

所有带状态的实体使用统一的泛型状态机。core 定义契约，模块实现。

```
ILifecycle<S extends StatusEnum>
  ├── canTransition(S target): boolean   — 检查转换是否合法
  ├── transition(S target): void         — 执行转换（非法则抛异常）
  └── StatusEnum:
        └── allowedTransitions(): Set<S> — 声明允许的目标状态集合

BaseLifecycleEntity<S>
  ├── 继承 BaseEntity（获得 code/name/createdAt/updatedAt）
  ├── 内置 status 字段 + ILifecycle 实现
  └── 调用方通过 transition() 改变状态，不直接 setStatus()
```

**规则**：
- 每个模块只定义自己的状态枚举（如 `BlueprintStatus`、`MachineStatus`），枚举实现 `ILifecycle.StatusEnum`
- 状态枚举放 core 层，供跨模块引用
- 终态（如 `OBSOLETED`、`CANCELLED`、`CLOSED`）的 `allowedTransitions()` 返回空集合
- 同状态转换幂等（`DRAFT → DRAFT` 不抛异常）

### 6.6 模型继承体系

```
BaseEntity (common)
  ├── code, name, createdAt, updatedAt
  ├── 适用：无状态记录、纯数据对象
  │
  ├── BaseLifecycleEntity<S> (core)
  │     ├── + status, transition(), canTransition()
  │     ├── 适用：有生命周期的实体（工单、蓝图、配方、采购订单...）
  │     └── 构造器: super(code, name, initialStatus)
  │
  └── AbstractResourceItem (core)
        ├── + group, type, number, add(), use(), copy()
        ├── 适用：4M1E 资源（特别是 Equipment）
        └── 构造器: super(code, name, group, type, number)
```

**决策树**：
```
实体是 4M1E 资源？ → 是 → AbstractResourceItem
              → 否 → 有状态机？ → 是 → BaseLifecycleEntity<S>
                                → 否 → BaseEntity
```

### 6.7 跨模块接口契约

模块间编译期引用只依赖 core 接口，不依赖对方模块的实现类。

```
core 层                           业务模块
─────────                         ────────
IEquipment (batch/)          ←── SCM 的 PurchaseOrder 引用 equipmentCode
IPurchaseOrder (batch/)      ←── WMS 的 Receipt 引用 referenceNo
IFormula (batch/)            ←── MES 的 WorkOrder 引用 formulaCode
```

**规则**：
- 跨模块接口定义在 core 的 `com.byz.factory.{domain}` 包下
- 接口只暴露 getter，不暴露 setter 和业务方法
- 跨模块引用以编码为主（`String equipmentCode`），运行时通过 Repository 按编码查找实体
- 内部枚举（如 `DemandSourceType`、`ErpMaterialType`）留在模块内，不放入 core

### 6.8 领域事件体系

模块间异步通信通过 `DomainEventPublisher` 完成。事件类型常量集中管理。

```
事件发布: DomainEventPublisher.publish(event)
事件订阅: DomainEventPublisher.subscribe(eventType, handler)

事件类型常量: com.byz.factory.event.types.{Module}EventTypes
命名格式:     {module}.{entity}.{past_tense}
示例:         plm.blueprint.released / equip.fault.reported / iot.alarm.triggered
```

**规则**：
- 每个模块的 EventTypes 类放在 `com.byz.factory.event.types`，与 `IDomainEvent`、`DomainEventPublisher` 同属 event 族
- 事件命名严格遵循 `{module}.{entity}.{past_tense}` 三段式
- 事件发布由 Service 实现层负责（不在 Model 层直接发布——Model 只负责状态转换）
- EventTypes 类用 `public final class` + 私有构造器，不可实例化

### 6.9 嵌套项模式

当一个实体包含明细列表，且明细需要跨模块引用时，使用"内部接口 + 独立类"模式：

```
core 层:
  IPurchaseOrder {
      List<? extends IPurchaseOrderItem> getItems();
      interface IPurchaseOrderItem { ... }   ← 内部接口
  }

模块层:
  PurchaseOrder implements IPurchaseOrder {      ← 实体
      List<PurchaseOrderItem> items;
      static class PurchaseOrderItem implements IPurchaseOrderItem { ... }  ← 独立内部类
  }
```

**已应用**：`IPurchaseOrder.IPurchaseOrderItem`、`IEquipmentRecipe.IRecipePhase`、`IFormula.IFormulaPhase`、`IWeighingTask.IWeighingItem`、`IReceipt.IReceiptItem`、`IPickingTask.IPickingTaskItem`、`IProductionPlan.IPlanItem`、`ICommand.CommandType`（内嵌枚举）

### 6.10 业务便捷方法

所有生命周期实体对外暴露语义化方法，封装底层 `transition()` 调用。调用方不直接操作 `setStatus()`。

```java
// ✅ 正确：语义化方法
blueprint.submitForReview();      // 内部: transition(UNDER_REVIEW) + markUpdated() + publishEvent(...)
equipment.reportFault();          // 内部: transition(FAULT) + markUpdated()
purchaseOrder.approve("张三");    // 内部: transition(APPROVED) + 记录审批人 + markUpdated()

// ❌ 禁止：直接操作状态
blueprint.setStatus(UNDER_REVIEW);
equipment.setStatus(FAULT);
```

每个便捷方法内部固定调用顺序：`transition() → markUpdated()`。事件发布由 Service 层负责。

### 6.11 版本管理策略

版本号采用策略模式，不同模块按需选择。

```
IVersionStrategy (core shared/)
  ├── SemanticVersionStrategy(BumpType)  → "1.0.0" → "1.1.0"（PLM 蓝图、LIMS 配方）
  │     └── BumpType: MAJOR / MINOR / PATCH
  └── IncrementalVersionStrategy()       → "1" → "2" → "3"（DMS 文档）

SemanticVersion: 解析和表示 MAJOR.MINOR.PATCH
```

**选用规则**：蓝图和配方需要表达变更规模 → 语义版本；文档只需知道"比上一版新" → 整数递增。

### 6.12 IExpand 动态属性约定

模块通过 `IExpand` 给 core 实体附加本模块字段，无需修改 core 接口。

**命名格式**：`{module}.{property}` 或 `{module}.{entity}.{property}`

**已注册的扩展属性**（部分）：

| 模块 | 属性键 | 挂载实体 | 用途 |
|------|--------|---------|------|
| equip | `equip.status`, `equip.oee`, `equip.lastMaintenanceDate` | Equipment | 设备运行状态 |
| plm | `plm.version`, `plm.author`, `plm.changeReason` | Blueprint | 蓝图版本信息 |
| erp | `erp.materialType`, `erp.batchManaged`, `erp.shelfLife` | MaterialCache | 物料主数据扩展 |
| scm | `scm.category`, `scm.leadTime`, `scm.onTimeRate` | Supplier | 供应商扩展信息 |
| lims | `lims.effectiveDate`, `lims.expiryDate`, `lims.retireReason` | Formula | 配方时效管理 |
| dms | `dms.rejectionReason`, `dms.obsoleteReason` | Document | 审批/作废原因 |
| iot | `iot.connectionStatus`, `iot.lastHeartbeat` | DeviceConnection | 连接状态 |

### 6.13 模型 vs 值对象

| 类型 | 特征 | 继承 | 示例 |
|------|------|------|------|
| **领域实体** | 有唯一标识(code)、有生命周期、可变 | BaseEntity / BaseLifecycleEntity | Equipment, Blueprint, PurchaseOrder |
| **值对象** | 无标识、不可变、由属性定义相等性 | Java `record` | CapacityCheck, BlueprintDiff, BOMConversionRequest |
| **纯数据对象** | 有标识但无生命周期、不可变或有控制的业务方法 | BaseEntity（无状态机） | TagValue, WeighingItem, OEMetrics |

---

## 7. 业务模块设计约定

> 本节提炼自 erp/scm/plm/equip/eam/iot/dms/lims/wms/mps 十个模块的约定建立过程。
> 所有新模块遵循以下约定建立，已建立模块在此基础上迭代。

### 7.1 约定建立三步

每个模块从骨架到约定的标准产出：

```
1. 创建 core 接口/枚举   → 跨模块契约（按需，非全部实体都需要）
2. 实现 model 实体        → 继承正确的基类 + 业务便捷方法 + @author
3. 编写 test 测试         → Given-When-Then + @DisplayName 中文描述
```

### 7.2 Core 接口创建决策树

```
该实体是否被其他模块引用？
  ├── 是 → 在 core 创建接口（如 ISupplier 被 WMS/QMS/ERP 引用）
  │        放 com.byz.factory.{domain} 包下
  │        接口只暴露 getter
  └── 否 → 接口留在模块内或不需要接口（如 ApprovalWorkflow）
```

### 7.3 枚举放置规则

| 枚举类型 | 位置 | 示例 |
|---------|------|------|
| 状态机枚举 | core `batch/` 包 | FormulaStatus, PurchaseOrderStatus, WeighingTaskStatus |
| 跨模块值枚举 | core `batch/` 包 | SupplierStatus, PickingType, StorageType |
| 单个接口专用值枚举 | core 接口内部 | ICommand.CommandType, ITagValue.TagQuality |
| 模块内部值枚举 | 模块 `model/` 包 | DemandSourceType, ErpMaterialType |

### 7.4 测试规范

```java
@DisplayName("模块名 模型构造测试")           // 类级中文描述
class ModuleTest {

    @Test
    @DisplayName("实体名 创建 — 初始状态为 XXX")  // 每个测试中文描述
    void entity_creation_shouldSetDefaults() {
        // Given — 准备数据
        // When — 执行操作
        // Then — 验证结果（含状态、字段、事件）
    }
}
```

**测试覆盖清单**：
- [ ] 构造器默认值（状态、版本、空集合）
- [ ] 正常状态转换路径（全生命周期）
- [ ] 驳回/回退路径
- [ ] 非法状态转换（每个不允许的转换方向至少一个断言）
- [ ] 终态不可转换
- [ ] 同状态幂等
- [ ] canTransition 正确性
- [ ] 业务便捷方法正确性
- [ ] IExpand 动态属性
- [ ] 事件发布验证（事件类型 + 负载内容）
- [ ] 事件命名约定验证（{module}.{entity}.{past_tense}）
- [ ] 明细/子项管理（添加、列表不可修改）
- [ ] 版本管理（如适用）

### 7.5 模块文档清单

每个模块约定建立后，需在以下位置留下记录：

| 文档 | 内容 |
|------|------|
| `PROJECT_STATUS.md` 模块约定章节 | Core 层新增列表 + 模型表格 + 约定规则 + 待实现清单 |
| `PROJECT_STATUS.md` 更新记录 | 日期 + 内容摘要 |
| `docs/architecture/modules/{module}.md` | 更新"当前实现"和"已决策"清单 |
| 必要时 | `docs/architecture/modules/{module}-core-regression.md`（核心层回归分析） |

### 7.6 跨模块事件订阅指南

EventTypes 类的 Javadoc 中列出所有订阅方及其订阅的事件和用途：

```java
/**
 * <b>其他模块订阅指南：</b>
 * <ul>
 *   <li><b>MES</b> — 订阅 {@link #BLUEPRINT_RELEASED}，获取可执行的工艺路线</li>
 *   <li><b>DMS</b> — 订阅 {@link #BLUEPRINT_SUBMITTED} / {@link #BLUEPRINT_APPROVED}，触发审批流文档</li>
 *   ...
 * </ul>
 */
```

---

## 8. 包命名规范

```
com.byz.factory.<module>.<layer>

模块: common | core | mes | qms | plm | equip | lims | erp | iot | eam | mps | aps | wms | andon | bi | scm | dms | web
层次: model | data | design | service | repository | web | config | event

Core 内部包结构:
  com.byz.factory.shared        — 共享基类、枚举、工具（BaseEntity, Dict, UOM, IVersionStrategy...）
  com.byz.factory.resource      — 资源抽象（IResource, IMachine, IMaterial, ResourceItem...）
  com.byz.factory.process       — 工序/动作抽象（IProcess, IAction, Action, Process...）
  com.byz.factory.factory       — 工厂/物理层抽象（IFactory, IBlueprint, IProductionLine...）
  com.byz.factory.batch         — 跨模块业务接口（IBatch, IEquipment, ISupplier, IFormula...）⚠️ 待按领域拆分
  com.byz.factory.lifecycle     — 状态机框架（ILifecycle, ILifecycle.StatusEnum）
  com.byz.factory.event         — 事件基础设施（IDomainEvent, DomainEventPublisher）
  com.byz.factory.event.types   — ★ 所有模块的事件类型常量（{Module}EventTypes）
  com.byz.factory.script        — 脚本引擎（IScriptEngine, ScriptRegistry...）
  com.byz.factory.repository    — 仓储端口（IRepository...）
  com.byz.factory.operation.*   — 操作分析（时间、产能、能力匹配）
  com.byz.factory.exception     — 领域异常
```

```
com.byz.factory.<module>.<layer>

模块: common | core | mes | qms | plm | equip | lims | erp | iot | web
层次: model | data | design | service | repository | web | config
```

## 9. 版本演进规划

| 阶段 | 模块 | 状态 |
|------|------|------|
| Phase 1 | erp, iot, plm, equip, scm, dms | ✅ 已完成（6/6） |
| Phase 2 | lims, wms, mps | ✅ 已完成（3/3） |
| Phase 3 | **mes**, aps | 🔜 待开始（集成枢纽） |
| Phase 4 | qms, andon, eam | 🔄 进行中（qms ✅, andon ✅, eam ✅） |
| Phase 5 | bi, web, test | 🔜 待开始 |

> 当前版本 v0.1.2：core 接口体系完成，Phase 1 + Phase 2 共 9 个模块约定建立完毕，编译测试全通过。
> 详见 [PROJECT_STATUS.md](../../PROJECT_STATUS.md)。

## 10. 设计边界（不做什么）

明确各模块的边界，防止范围蔓延：

| 模块 | 在范围内 | 不在范围内 |
|------|---------|-----------|
| **core** | 领域接口、枚举、抽象类 | 持久化实现、业务逻辑、API |
| **MES** | 工单、工序流转、报工、追溯 | 财务核算、高级排程(APS) |
| **QMS** | 检验工序、SPC监控、偏差/CAPA | FDA 电子提交、DMS文档管理 |
| **PLM** | 工艺路线、BOM转化、版本管理 | 3D CAD集成、PLM工作流引擎 |
| **Equip** | 设备台账、设备配方、OEE | CMMS设备维护管理 |
| **LIMS** | 配方版本、称量追溯、批记录 | ELN电子实验记录、SDMS科学数据管理 |
| **ERP** | 主数据缓存、库存查询、事务回传 | 财务总账、应收应付 |
| **IoT** | 协议适配、数据采集、指令下发 | 边缘计算、数据清洗规则引擎、PLC编程 |
| **Web** | REST API聚合、统一认证 | BI报表、大屏可视化 |

## 11. 模块间通信

### 11.1 同步调用

用于实时性要求高的场景（如 QMS 质量判定、设备状态查询），通过 core 接口完成：

```
MES ──→ IEquipment（查询设备状态）
MES ──→ IFormula（获取配方版本）
WMS ──→ ISupplier（验证供应商资质）
```

### 11.2 领域事件（异步）

模块间解耦通知通过 `DomainEventPublisher` 完成。事件类型常量在 `com.byz.factory.event.types`。

**已定义的跨模块事件契约**：

| 事件类型 | 发布模块 | 订阅方 | 用途 |
|---------|---------|--------|------|
| `plm.blueprint.released` | PLM | **MES, APS, WMS** | MES 获取可执行蓝图、APS 更新排程列表、WMS 预置物料储位 |
| `plm.blueprint.obsoleted` | PLM | **MES, APS** | 标记蓝图不可用 |
| `plm.blueprint.submitted` | PLM | **DMS** | 触发审批流文档创建 |
| `plm.blueprint.approved` | PLM | **DMS** | 审批完成归档 |
| `plm.bom.transformed` | PLM | **ERP** | 同步物料需求到采购计划 |
| `plm.change.requested` | PLM | **DMS** | 触发变更审批流 |
| `equip.status.changed` | Equip | **MES, APS** | 设备状态感知 |
| `equip.fault.reported` | Equip | **MES, Andon** | 工单中断 + 安灯报警 |
| `equip.maintenance.started` | Equip | **EAM** | 同步维护工单状态 |
| `equip.maintenance.completed` | Equip | **EAM** | 维护完成通知 |
| `equip.recipe.dispatched` | Equip | **IoT** | 接收参数下发指令 |
| `equip.oee.calculated` | Equip | **BI** | OEE 看板汇总 |
| `iot.device.connected` | IoT | **Equip** | 同步设备在线状态 |
| `iot.alarm.triggered` | IoT | **QMS, Andon, MES, EAM** | 偏差/安灯/工单中断/紧急维护 |
| `iot.tag.collected` | IoT | **Equip, QMS** | 更新参数实际值 + SPC 分析 |
| `dms.document.effective` | DMS | **LIMS, QMS, PLM, EAM** | 各模块同步受控文档生效 |
| `dms.document.obsoleted` | DMS | **PLM** | 工艺文件作废同步 |
| `lims.formula.activated` | LIMS | **MES** | 配方可用于生产 |
| `lims.weighing.completed` | LIMS | **MES** | 称量完成通知 |
| `lims.batch_record.approved` | LIMS | **DMS** | 批记录审批归档 |
| `mps.plan.released` | MPS | **MES** | 生成工单 |
| `mps.plan.completed` | MPS | **BI** | 计划执行看板 |

### 11.3 事件结构

```json
{
  "eventId": "uuid",
  "eventType": "ProcessStarted",
  "timestamp": "2026-07-12T08:10:00Z",
  "source": { "module": "mes", "instance": "mes-01" },
  "payload": {
    "workOrderNo": "WO-20260712-001",
    "batchNo": "B20260712-001",
    "processCode": "PROCESS-001",
    "factoryCode": "FACTORY-01"
  }
}
```

## 12. 脚本引擎设计

### 12.1 架构

```
IScriptEngine (接口)
├── GraalScriptEngine  — GraalJS (推荐，内置沙箱，JDK 21+)
├── NashornScriptEngine — Nashorn (过渡，需独立包)
└── J2V8ScriptEngine   — V8 via J2V8 (备选)

ScriptRegistry — 脚本注册索引（元数据 + 编译产物）
ScriptMetadata — 从 JSDoc 注释解析的结构化元数据
ScriptContext  — 执行时上下文（白名单 API + 参数）
```

### 12.2 安全沙箱

默认全部禁止，只开放显式授权的 API：

```
❌ Java 类访问 (HostClassLookup)
❌ 文件/网络 IO
❌ 线程创建
❌ 进程创建
❌ JNI 本地访问
✅ ScriptContext 中显式绑定的白名单 API 表面对象
```

### 12.3 脚本标准化

见 [AI 协作指南](../ai-collaboration/guide.md) 脚本模板章节。

每个脚本必须包含 `@id`, `@name`, `@version`, `@module` 元数据。脚本存放在 `easy-factory-core/src/main/resources/scripts/` 目录下，按模块分文件夹，通过 `registry.json` 索引。

### 12.4 脚本生命周期

```
编写 → 元数据校验 → 沙箱内测试执行 → 注册(registry.json)
                                        │
                               ┌────────┘
                               ▼
                          旧版本 → history/
                          新版本 → 替换 ACTIVE
```

