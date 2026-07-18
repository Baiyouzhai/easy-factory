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
├── Nothing — 空操作（占位/跳过）
├── Create  — 创建新资源
├── Add     — 增加资源数量
├── Use     — 消耗资源
├── Change  — 改变资源属性
└── Convert — 将输入资源转化为输出资源
```

### 2.4 流程控制

```
Control:
├── Default   — 顺序执行
├── Interrupt — 条件中断（质量异常时中断流程）
├── Count     — 计数控制
├── Timing    — 时序控制
└── Repeat    — 重复/返工
```

## 3. 模块全景

```
                        ┌──────────────────────┐
                        │   easy-factory-web    │  统一门户
                        │  (Spring Boot 聚合)    │
                        └──────────┬───────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
    ┌─────────▼────────┐ ┌────────▼─────────┐ ┌───────▼──────────┐
    │   easy-factory-   │ │   easy-factory-   │ │   easy-factory-   │
    │       mes          │ │       qms         │ │       plm         │
    │   (制造执行)       │ │   (质量管理)       │ │   (研发工艺)       │
    └────────┬─────────┘ └────────┬─────────┘ └───────┬──────────┘
              │                    │                    │
              └────────────────────┼────────────────────┘
                                   │
                        ┌──────────▼───────────┐
                        │   easy-factory-core   │
                        │     (领域内核)         │
                        │  IProcess / IAction   │
                        │  IResource / IResourcePack │
                        │  IBlueprint / ScriptExecutor │
                        └──────────┬───────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
    ┌─────────▼────────┐ ┌────────▼─────────┐ ┌───────▼──────────┐
    │   easy-factory-   │ │   easy-factory-   │ │   easy-factory-   │
    │       equip        │ │       lims        │ │       iot         │
    │   (设备工艺)       │ │   (配方系统)       │ │   (设备互联)       │
    └──────────────────┘ └──────────────────┘ └──────────────────┘
                                   │
                        ┌──────────▼───────────┐
                        │   easy-factory-common │
                        │     (基础数据层)       │
                        │  IData / IExpand     │
                        │  CollectionUtil      │
                        └──────────────────────┘
```

## 4. 模块依赖关系

```
common ◄── core ◄── (mes, qms, plm, equip, lims)
                ◄── erp (主数据同步)
                ◄── iot (设备数据采集)
                ◄── web (聚合所有模块)
```

- `common` — 无依赖，纯基础层
- `core` — 仅依赖 `common`
- 业务模块 (mes/qms/plm/equip/lims) — 依赖 `core`，模块间通过 core 接口松散耦合
- `erp` — 依赖 `core`，作为外部系统的适配层
- `iot` — 依赖 `core`，设备数据采集与指令下发
- `web` — 聚合所有模块，提供统一入口

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

## 7. 包命名规范

```
com.byz.factory.<module>.<layer>

模块: common | core | mes | qms | plm | equip | lims | erp | iot | web
层次: model | data | design | service | repository | web | config
```

## 8. 版本演进规划

| 阶段 | 版本 | 目标 |
|------|------|------|
| 0.1 | core 完善 | 解决未定义类型引用，完善核心接口，补充单元测试 |
| 0.2 | 持久化 | easy-factory-db 模块，实现数据持久化 |
| 0.3 | MES 雏形 | 工单创建、工序流转、报工 |
| 0.4 | QMS 雏形 | 检验工序、质量门禁、偏差处理 |
| 0.5 | Web 集成 | 统一门户聚合 MES+QMS |
| 0.6+ | 其他模块 | PLM、LIMS、IoT 逐步扩展 |

## 9. 设计边界（不做什么）

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

## 10. 模块间通信

### 10.1 同步调用

用于实时性要求高的场景（如 QMS 质量判定、设备状态查询）：

```
MES ──→ QMS.getInspectionPlan() ──→ 返回检验方案（同步）
MES ──→ Equip.checkStatus() ──→ 返回设备可用性（同步）
```

### 10.2 事件驱动（异步）

用于解耦的模块间通知。所有事件通过消息总线传递：

| 事件 | 发布者 | 订阅者 | 说明 |
|------|--------|--------|------|
| `ProcessStarted` | MES | QMS, Equip | 工序开始 → QMS准备检验方案、Equip标记设备占用 |
| `ActionExecuted` | MES | LIMS, QMS | 动作执行完成 → LIMS记录、QMS采集SPC数据 |
| `ProcessInterrupted` | MES | QMS | 工序异常中断 → QMS发起偏差 |
| `DeviationResolved` | QMS | MES | 偏差关闭 → MES恢复工序 |
| `BatchCompleted` | MES | LIMS, ERP | 批完成 → LIMS生成批记录, ERP成本归集 |
| `EquipmentAlarm` | IoT | Equip, QMS, MES | 设备报警 → 联动处理 |
| `BlueprintReleased` | PLM | MES, LIMS | 工艺发布 → 通知下游 |

### 10.3 事件结构

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

## 11. 脚本引擎设计

### 11.1 架构

```
IScriptEngine (接口)
├── GraalScriptEngine  — GraalJS (推荐，内置沙箱，JDK 21+)
├── NashornScriptEngine — Nashorn (过渡，需独立包)
└── J2V8ScriptEngine   — V8 via J2V8 (备选)

ScriptRegistry — 脚本注册索引（元数据 + 编译产物）
ScriptMetadata — 从 JSDoc 注释解析的结构化元数据
ScriptContext  — 执行时上下文（白名单 API + 参数）
```

### 11.2 安全沙箱

默认全部禁止，只开放显式授权的 API：

```
❌ Java 类访问 (HostClassLookup)
❌ 文件/网络 IO
❌ 线程创建
❌ 进程创建
❌ JNI 本地访问
✅ ScriptContext 中显式绑定的白名单 API 表面对象
```

### 11.3 脚本标准化

见 [AI 协作指南](../ai-collaboration/guide.md) 脚本模板章节。

每个脚本必须包含 `@id`, `@name`, `@version`, `@module` 元数据。脚本存放在 `easy-factory-core/src/main/resources/scripts/` 目录下，按模块分文件夹，通过 `registry.json` 索引。

### 11.4 脚本生命周期

```
编写 → 元数据校验 → 沙箱内测试执行 → 注册(registry.json)
                                        │
                               ┌────────┘
                               ▼
                          旧版本 → history/
                          新版本 → 替换 ACTIVE
```

