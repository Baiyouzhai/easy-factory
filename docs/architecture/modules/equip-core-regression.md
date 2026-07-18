# Equip 模块 → Core 回归记录

> **日期**: 2026-07-19
> **状态**: 接口已创建，equip 模块已实现；**等待高层级会话审核**
> **决策**: 本次仅创建接口 + equip 实现，不做破坏性修改。现有 `Map<String,Object>` 保持兼容，逐步迁移。

---

## 一、变更清单

### 新增 core 接口（5 个文件）

| # | 文件 | 位置 | 用途 |
|---|------|------|------|
| 1 | `IEquipment.java` | `com.byz.factory.batch` | 设备台账抽象 = IMachine + ILifecycle<MachineStatus> + 台账字段 |
| 2 | `IEquipmentParameter.java` | `com.byz.factory.batch` | 工艺参数抽象：设定值/实际值/控制限 + `isInControl()` default 方法 |
| 3 | `IEquipmentRecipe.java` | `com.byz.factory.batch` | 设备配方抽象 + 内嵌 `IRecipePhase` 接口 |
| 4 | `IOEMetrics.java` | `com.byz.factory.batch` | OEE 指标抽象：A×P×Q |
| 5 | `EquipEventTypes.java` | `com.byz.factory.factory` | 9 个领域事件常量（与 PlmEventTypes 同级） |

### 更新 equip 模块（5 个文件）

| # | 文件 | 变更 |
|---|------|------|
| 1 | `Equipment.java` | `implements IEquipment`（替代原来的 `IMachine, ILifecycle<MachineStatus>`） |
| 2 | `EquipmentParameter.java` | `implements IEquipmentParameter`，删除重复的 `isInControl()` |
| 3 | `EquipmentRecipe.java` | `implements IEquipmentRecipe`，`RecipePhase implements IRecipePhase` |
| 4 | `OEMetrics.java` | `implements IOEMetrics` |
| 5 | `EquipEventTypes.java` | **删除**（已迁移到 core） |

---

## 二、跨模块影响矩阵

### 已有 core 文件 → 未来迁移方向

以下 core 文件当前使用 `String equipmentCode` 或 `Map<String,Object>`，新接口为其提供类型安全的替代：

| 文件 | 当前引用方式 | 建议迁移为 | 优先级 | 风险 |
|------|------------|-----------|--------|------|
| `IEquipmentBinding` | `String getEquipmentCode()` + `Map<String,Object> getParameters()` | `IEquipment` + `List<IEquipmentParameter>` | 中 | 物理层广泛引用，需全量回归 |
| `EquipmentBinding` | 同上（实现类） | 同上 | 中 | 同上 |
| `IEquipmentAction` | `Map<String,Object> getEquipmentParameters()` | `List<IEquipmentParameter>` | 低 | 运行时接口，与 IoT 耦合 |
| `EngineeringBOM.EquipmentAssignment` | `String equipmentCode` + `Map<String,Object> parameters` | `IEquipment` + recipeCode/phaseName 引用 `IEquipmentRecipe` | 高 | PLM ↔ Equip 核心链路 |
| `FactoryCapacityProfile` | `Map<String, BigDecimal> machineOeeFactors` + `machineDailyHours` | 接收 `IOEMetrics` 做精确计算 | 低 | 保持静态因子兼容 |
| `BottleneckDetector.findEquipmentCode()` | 搜索 `SourceGroup.Machine` 资源，返回 String | 返回 `IEquipment`，强类型引用 | 低 | 内部实现，影响可控 |
| `ProcessRouteMatcher.EquipmentFound` | `String equipmentCode` 字段 | `IEquipment` 引用（或保持 String 作为 key） | 低 | 匹配引擎核心，需谨慎 |
| `IAsset.getEquipmentCode()` | 返回 String，通过 Equip 查台账 | 直接引用 `IEquipment` | 中 | EAM ↔ Equip 跨模块 |
| `IDataCollectionAction.DataTag` | `String equipmentCode` 字段 | `IEquipment` 引用 | 低 | IoT 数据采集 |

### 其他业务模块 → 应订阅的事件

| 模块 | 应订阅的事件（使用 `com.byz.factory.factory.EquipEventTypes`） | 用途 |
|------|--------------------------------------------------------------|------|
| **MES** | `STATUS_CHANGED`, `FAULT_REPORTED`, `PRODUCTION_STARTED`, `PRODUCTION_STOPPED` | 工单中断/开工/完工跟踪 |
| **Andon** | `FAULT_REPORTED` | 触发安灯报警 |
| **EAM** | `MAINTENANCE_STARTED`, `MAINTENANCE_COMPLETED` | 维护工单状态同步 |
| **IoT** | `RECIPE_DISPATCHED`, `PARAMETER_UPDATED` | 参数下发/变更 |
| **BI** | `OEE_CALCULATED` | OEE 看板 |
| **APS** | `STATUS_CHANGED` | 设备可用性感知 |

---

## 三、待高层级讨论的议题

### 议题 1：`IEquipmentBinding` 何时迁移？

现状：`getEquipmentCode()` 返回 String，`getParameters()` 返回 `Map<String,Object>`。
影响范围：`IWorkstation`、`IFactory`、`IProductionLine`、`DirectEquipmentStrategy`、`LineFirstStrategy`、`ProcessRouteMatcher`、`ProductChecker` —— 几乎整个物理层和匹配引擎。

选项：
- **A）立即迁移**：破坏性修改，影响面大，但一劳永逸
- **B）渐进迁移**：保留现有方法为 `@Deprecated`，新增类型化方法
- **C）暂不迁移**：接口可用，实现层自行选择用 String 还是 IEquipment

**建议**：B——新方法 `IEquipment getEquipment()` + `List<IEquipmentParameter> getTypedParameters()`，旧方法标记弃用。

### 议题 2：`EngineeringBOM.EquipmentAssignment` 是否引用 Recipe？

现状：`EquipmentAssignment(String equipmentCode, Map<String,Object> parameters)` 存储参数副本。
引入 `IEquipmentRecipe` 后，可改为引用 `recipeCode + phaseName`，不再持有参数副本。

选项：
- **A）引用 Recipe**：`EquipmentAssignment(String equipmentCode, String recipeCode, String phaseName, Map<String,Object> overrides)` —— 权威数据在 Equip，BOM 只记录引用
- **B）保持副本**：BOM 持有完整参数，不依赖 Equip 运行时可用性

**建议**：A 为主体，B 为 fallback——recipeCode 为空时走 parameters 副本。

### 议题 3：`FactoryCapacityProfile` 是否消费 `IOEMetrics`？

现状：静态 OEE 因子 `Map<String, BigDecimal> machineOeeFactors` 用于产能规划。
引入 `IOEMetrics` 后，`getEffectiveMachineTime()` 可接收实际 OEE 做精确计算。

选项：
- **A）替换因子**：移除 `machineOeeFactors`，改用 `IOEMetrics` 提供者接口
- **B）双轨并存**：规划用静态因子，实际用 IOEMetrics，两者互补

**建议**：B——规划阶段没有实际数据，只能用因子；执行后有 IOEMetrics 可修正。

### 议题 4：`EquipEventTypes` 在 core 的同级位置

现状：放在 `com.byz.factory.factory`，与 `PlmEventTypes` 同级。
问题：其他模块的 EventTypes 也该放这里吗（如 `ScmEventTypes`、`EamEventTypes`）？

**建议**：统一规范——所有模块的事件类型常量统一放在 `com.byz.factory.factory.{Module}EventTypes`。

---

## 四、本次不做（但也留档）

以下改进已识别但本次不做——需要独立的迭代会话：

1. **IEquipmentAction 桥接实现** — equip 模块实现 `IEquipmentAction`，覆盖 `executeEquipment()`
2. **EquipmentService 实现类** — `EquipmentServiceImpl` 接入 `DomainEventPublisher`
3. **IEquipmentBinding 类型化迁移** — 议题 1 的 B 方案
4. **EngineeringBOM 引用 Recipe** — 议题 2 的 A 方案
5. **IoT 参数实时监控** — 架构文档中定义的 `equip.parameter.actual` 回传链路
6. **OEE 六大损失采集** — 架构文档中定义的 `downtimeEvents` + `lossAnalysis`

---

## 五、验证

```bash
# core 测试（含新增接口编译）
mvn -pl easy-factory-core test
# → 182 tests, 0 failures

# equip 测试（含实现新接口 + EquipEventTypes 迁移）
mvn -pl easy-factory-equip test
# → 43 tests, 0 failures

# 全项目编译
mvn compile
# → 20/20 modules BUILD SUCCESS
```
