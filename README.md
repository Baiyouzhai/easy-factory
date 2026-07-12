# easy-factory

基于 **领域驱动设计（DDD）** 的制造运营管理（MOM）平台。以统一的领域模型为内核，衍生覆盖制造企业全价值链的子系统。

```
Java 21 · Spring Boot 3.4 · Maven · 20 模块
```

---

## 核心设计

整个项目构建在两个基础模块之上：

### easy-factory-common — 数据基础层

为所有模块提供统一的数据抽象，不依赖任何业务概念：

| 接口 | 能力 |
|------|------|
| `IData` | JSON 序列化、属性拷贝、深克隆 |
| `IExpand` | 动态扩展属性——领域对象除固定字段外可携带任意键值对 |
| `IDataExpand` | 合并 IData + IExpand，所有上层模型类的父接口 |

```java
// 各模块通过 IExpand 挂载自身特有属性，无需修改 core 接口
process.set("mes.workOrderId", "WO-001");    // MES 注入工单号
process.set("qms.inspectionLevel", "NORMAL"); // QMS 注入检验水平
process.set("plm.version", "2.1.0");          // PLM 注入版本号
```

### easy-factory-core — 领域内核

定义制造系统的**统一语言**和**标准交互流程**。所有接口、枚举、抽象类的所在地，不包含任何持久化或业务实现：

```
统一语言:
  Factory(工厂) → Process(工序) → Action(动作) → Resource(资源)
                                                    ├── Personnel(人)
                                                    ├── Machine(机)
                                                    ├── Material(料)
                                                    ├── Method(法)
                                                    ├── Environment(环)
                                                    └── Product(产品)

标准交互流程:
  Factory.getProcesses()
    → Process.execute(inputs)
      → Action[0].execute(process, inputs)      ← 有脚本走脚本引擎
           → ResourcePack(输出)
      → Action[1].execute(process, 前一个输出)    ← 动作链串联
           → ...
      → 最终 ResourcePack(本工序输出)
```

core 还提供了：

- **状态机** (`ILifecycle<Status>`) — 任何带状态的实体（工单/批次/检验指令）复用同一套转换校验
- **领域事件** (`IDomainEvent`) — 模块间异步解耦通信的标准协议
- **脚本引擎** (`IScriptEngine`) — 抽象接口，实现可切换 Nashorn/GraalJS/V8，内置沙箱
- **批次追溯** (`IBatch` / `ITraceable`) — GMP 合规的批记录追溯基座
- **生命周期** (`BatchStatus`) — 从 CREATED → RELEASED → APPROVED → ARCHIVED

---

## 模块全景

```
common ──→ core ──→ 15 个业务模块 ──→ web
                     │
                     ├── 执行层: mes, qms, plm, lims
                     ├── 集成层: equip, iot, erp, eam, wms
                     ├── 计划层: mps, aps, andon
                     ├── 治理层: dms, bi
                     └── 供应层: scm
```

| 层面 | 模块 | 说明 |
|------|------|------|
| **基础** | `common` | 数据抽象：IData/IExpand/JSON序列化 |
| **基础** | `core` | 领域内核：接口/枚举/抽象类/脚本引擎/状态机/事件 |
| 执行 | `mes` | 制造执行：工单→工序流转→报工→追溯 |
| 执行 | `qms` | 质量管理：检验→SPC→偏差→CAPA→质量门禁 |
| 执行 | `plm` | 研发工艺：工艺路线→BOM转化→版本管理 |
| 执行 | `lims` | 配方系统：配方版本→称量防错→批记录 |
| 集成 | `equip` | 设备工艺：台账→设备配方→OEE |
| 集成 | `iot` | 设备互联：协议适配→数据采集→指令下发 |
| 集成 | `erp` | 资源计划：主数据同步→库存查询→事务回传 |
| 集成 | `eam` | 资产管理：维护工单→校准→备件→全生命周期 |
| 集成 | `wms` | 仓储管理：收货→上架→FIFO拣料→线边仓 |
| 计划 | `mps` | 主生产计划：订单预测→产能检查→周计划 |
| 计划 | `aps` | 高级排程：设备×时间×顺序的精细排程 |
| 计划 | `andon` | 安灯系统：异常呼叫→逐级上报→联动处理 |
| 治理 | `dms` | 文档管理：SOP→批记录审批流→审计追踪(GMP) |
| 治理 | `bi` | 看板报表：生产/质量/OEE看板+KPI聚合 |
| 供应 | `scm` | 供应链：供应商→采购订单→来料计划 |
| **聚合** | `web` | Spring Boot 统一门户 |
| **聚合** | `test` | 跨模块集成测试 |

---

## 快速开始

```bash
# 环境: JDK 21 + Maven 3.9+

git clone <repo-url>
cd easy-factory

# 编译全部 20 模块
mvn clean compile

# 运行测试
mvn test

# 启动 web 模块
cd easy-factory-web && mvn spring-boot:run
```

---

## 文档

| 文档 | 说明 |
|------|------|
| [总体设计](docs/architecture/overview.md) | 项目愿景、DDD 统一语言、模块依赖图、技术选型 |
| [贯穿示例](docs/architecture/walkthrough.md) | 阿莫西林片剂生产——全模块串联演示 |
| [数据模型](docs/architecture/data-model.md) | 跨模块 ER 图 + 15 张核心表设计 |
| [模块设计](docs/architecture/modules/) | 每个模块的详细设计文档（含遗留问题） |
| [实施路线图](docs/implementation/roadmap.md) | 五阶段实施顺序——哪些先做、哪些可并行 |
| [编码规范](docs/implementation/conventions.md) | 命名/接口设计/测试/异常处理 |
| [开发指南](docs/implementation/guide.md) | 环境搭建/构建/Git 工作流 |
| [AI 协作](docs/ai-collaboration/guide.md) | 与 AI 高效协作的 Prompt 模板 |
| [项目状态](PROJECT_STATUS.md) | 各模块当前进度 + 待办事项 |

---

## 设计原则

1. **接口驱动** — core 只定义契约（`IProcess`, `IAction`, `IResource`），不绑定实现
2. **模块松耦合** — 各子系统独立演进，通过 core 共享语义，运行时事件解耦
3. **脚本化扩展** — 动作逻辑可通过 JavaScript 动态注入，沙箱隔离
4. **4M1E 分类** — 资源按人机料法环+产品分组，对齐制造业标准
5. **质量内嵌** — 质量检验不是独立系统，而是"长在工序里"
6. **持久化归模块** — 没有独立的 db 层，每个业务模块自管存储
