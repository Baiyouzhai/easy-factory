# easy-factory-plm — 研发工艺系统

## 模块定位

管理产品研发阶段的工艺路线设计、BOM 转化、工艺参数版本管理。PLM 是"设计态"，MES 是"运行态"——产品先在 PLM 中完成工艺设计，再发布到 MES 执行。

## 基于 core 的扩展

### 蓝图版本管理

core 中 `IBlueprint` 定义了产品的工序列表。PLM 在此基础上增加版本管理：

```java
// IExpand 扩展字段
// "plm.version"          → "1.0.0" | "2.1.0"
// "plm.status"           → "DRAFT" | "REVIEW" | "APPROVED" | "RELEASED" | "OBSOLETED"
// "plm.author"           → 设计人
// "plm.approver"         → 审批人
// "plm.releaseDate"      → 发布日期
// "plm.effectiveDate"    → 生效日期
// "plm.changeReason"     → 变更原因
```

### 工艺 BOM → 制造 BOM 转化

```
设计BOM (EBOM)     工艺BOM (PBOM)      制造BOM (MBOM)
┌─────────────┐   ┌─────────────┐    ┌─────────────┐
│ 产品结构树    │──→│ 增加工艺辅料  │──→│ 增加工序信息  │
│ 零部件清单    │   │ 虚拟件拆分    │   │ 按工序分组    │
│ (CAD导出)    │   │ 损耗率计算    │   │ 关联工位/设备  │
└─────────────┘   └─────────────┘    └─────────────┘
                          │
                          ▼
                   IResourcePack + IProcess
                   (core 统一模型)
```

## PLM 特有模型

### ProcessTemplate (工艺模板)

```
ProcessTemplate:
├── code:             模板编码
├── name:             模板名称
├── category:         类别 (机加工/装配/化工/电子)
├── processes:        工序模板列表
├── version:          版本
├── status:           状态
└── parameters:       工艺参数集合
```

### ProcessParameter (工艺参数)

```
ProcessParameter:
├── code:             参数编码
├── name:             参数名称 (如: 温度、压力、转速)
├── dataType:         数据类型 (NUMERIC/BOOLEAN/ENUM/TEXT)
├── unit:             单位
├── defaultValue:     默认值
├── minValue:         最小值
├── maxValue:         最大值
├── controlMethod:    控制方式 (手动/自动/半自动)
└── importance:       重要性 (关键/重要/一般)
```

### BOMConverter (BOM转化规则)

```
BOMConverter:
├── sourceType:       源BOM类型 (EBOM/PBOM)
├── targetType:       目标BOM类型 (PBOM/MBOM)
├── rules:            转化规则列表
│   ├── materialAdd:      哪些辅料需要添加
│   ├── phantomExpand:    虚拟件如何展开
│   ├── lossRate:         损耗率计算逻辑
│   └── groupByProcess:   按工序分组的规则
└── validations:      转化后的校验规则
```

## 工艺路线设计流程

```
1. 产品设计完成 → 导入EBOM
2. 工艺工程师创建工艺路线 (IBlueprint)
3. 添加工序 (IProcess)，编排顺序
4. 每个工序设置工艺参数和资源需求
5. BOM 转化：EBOM → PBOM → MBOM
6. 工艺评审 → 审批 → 发布
7. 发布后推送到 MES (变为可执行的工单模板)
```

## 版本比较

不同版本之间对比（呼应 README"生产同一产品不同工厂的差异性"）：

```java
public interface IBlueprintDiffer {
    /**
     * 对比两个蓝图版本的差异
     * @return 差异报告：工序变更、参数变更、资源变更
     */
    BlueprintDiff compare(IBlueprint versionA, IBlueprint versionB);
}
```

差异维度：
- 新增/删除/重排工序
- 动作变更（脚本修改、资源调整）
- 工艺参数调整
- 资源定额变更（IEstimateResourceModel）

## 外部接口

### 向上提供给 Web 的接口

```
POST   /api/plm/blueprints                   创建工艺路线
PUT    /api/plm/blueprints/{id}/version      发布新版本
GET    /api/plm/blueprints/{id}/diff         版本对比
POST   /api/plm/bom/convert                   BOM转化
GET    /api/plm/process-templates            工艺模板列表
POST   /api/plm/parameters                   创建工艺参数
GET    /api/plm/blueprints/{id}/feasibility  可制造性检查
POST   /api/plm/change-requests              创建变更请求
```

### 模块间接口（领域事件驱动）

PLM 通过 core 的 `DomainEventPublisher` 发布事件，其他模块订阅：

| 事件类型 | 载荷 | 订阅方 | 用途 |
|---------|------|--------|------|
| `plm.blueprint.submitted` | blueprintCode, productCode, version | **DMS** | 触发审批流文档创建 |
| `plm.blueprint.approved` | blueprintCode, productCode, version, approvedBy | **DMS** | 审批完成，归档 |
| `plm.blueprint.rejected` | blueprintCode, version | **DMS** | 审批驳回通知 |
| `plm.blueprint.released` | blueprintCode, productCode, version, processCount | **MES, APS, WMS** | MES 获取可执行路线，APS 更新排程列表，WMS 预置物料储位 |
| `plm.blueprint.obsoleted` | blueprintCode, version | **MES, APS** | 标记蓝图不可用 |
| `plm.change.requested` | blueprintCode, changeReason | **DMS** | 触发变更审批流 |
| `plm.change.approved` | blueprintCode, newVersion | **MES** | 通知变更后的新版本 |
| `plm.bom.transformed` | productCode, sourceType→targetType | **ERP** | 同步物料需求到采购计划 |

> **订阅方式：** `DomainEventPublisher.subscribe(PlmEventTypes.BLUEPRINT_RELEASED, handler)`
>
> 事件类型常量定义在 `core: com.byz.factory.factory.PlmEventTypes`。

## 数据库设计要点

- 工艺路线版本表 (plm_blueprint_version)
- 工序模板表 (plm_process_template)
- 工艺参数表 (plm_process_parameter)
- BOM转化规则表 (plm_bom_converter)
- 版本差异记录表 (plm_version_diff)

## 新增模型（2026-07-19 补充）

### BOM 转化模型（`plm/bom/` 包）

```
BOMType (枚举):         EBOM → PBOM → MBOM (转化链)
BOMConversionRequest:   转化请求（源类型+目标类型+蓝图+工厂）
BOMConversionResult:    转化结果（产物BOM+应用规则+告警）
IBOMConversionRule:     转化规则接口（⭐ 由 Common 模块规则引擎实现）
```

> **BOM 转化委托给 Common 模块的规则引擎执行。** 详见 `docs/architecture/modules/common-rule-engine-requirements.md`。

### ChangeRequest (变更请求)

```
ChangeRequest (BaseLifecycleEntity):
├── code:                变更请求编码 (如 CR-2026-001)
├── blueprintCode:       关联蓝图编码
├── fromVersion:         变更前版本
├── toVersion:           变更后版本（实施后填写）
├── changeReason:        变更原因（客户要求/工艺优化/法规更新/质量改进）
├── description:         变更描述
├── affectedDimensions:  影响维度 (PROCESS/PARAMETER/RESOURCE)
├── requestedBy:         申请人
├── approvedBy:          批准人
└── Status: DRAFT → SUBMITTED → APPROVED → IMPLEMENTED → CLOSED
                                ↓              ↓
                            REJECTED       REJECTED
```

## 遗留问题

### 当前实现
- [x] `ProcessTemplate` — 工艺模板（含 status/parameters 字段 + activate/obsolete 业务方法）
- [x] `BlueprintService` — 接口已充实（蓝图生命周期 + 工艺参数 + 版本 + BOM + 可制造性检查 + 变更管理）
- [x] `Blueprint` — 蓝图核心模型（BaseLifecycleEntity + IBlueprint + 业务方法 + 版本策略 + 领域事件发布）
- [x] `ProcessParameter` — 工艺参数模型（BaseEntity + IProcessParameter）
- [x] `BlueprintStatus` — 蓝图生命周期状态枚举（core factory/ 包）
- [x] `IBlueprintDiffer` + `BlueprintDiff` — 蓝图版本差异比较接口（core factory/ 包）
- [x] `BlueprintDifferImpl` — 差异比较器默认实现（PLM service 层）
- [x] `ChangeRequest` — 变更请求模型（ECR/ECO 生命周期 + 6 个业务方法）
- [x] `BOMType` + `BOMConversionRequest/Result` + `IBOMConversionRule` — BOM 转化契约模型（plm/bom/ 包）
- [x] `IVersionStrategy` + `SemanticVersion` + `BumpType` — 版本管理策略体系（⭐ core shared/ 包，DMS/PLM/LIMS 共用）
- [x] `PlmEventTypes` — PLM 领域事件类型常量（⭐ core factory/ 包，供 MES/DMS/APS/ERP/WMS 订阅）
- [ ] BOM 转化规则引擎实现 — 契约已定义（`IBOMConversionRule`），委托 Common 模块实现（见 `common-rule-engine-requirements.md`）
- [ ] BlueprintService 实现类 — 接口已完善（30+ 方法），待下一阶段实现
- [ ] 工艺参数标准库 — 模型已创建，标准库待建立

### 已决策
1. ✅ **版本号策略** — 采用语义版本（MAJOR.MINOR.PATCH），由 core `SemanticVersion` + `SemanticVersionStrategy` 统一管理；DMS 文档可用 `IncrementalVersionStrategy`（简单递增）
2. ✅ **BOM 转化规则** — 采用规则引擎，由 Common 模块提供 `IRuleEngine`；PLM 提供 `IBOMConversionRule` 契约
3. ✅ **PLM 与 MES 交互** — 推送模式：蓝图 RELEASED 时通过 `DomainEventPublisher` 发布 `plm.blueprint.released` 事件，MES 订阅处理

## AI 协作建议（2026-07-18）

PLM 的核心职责是产品定义——将客户需求转化为可执行的蓝图。

### 推荐实施

1. **实现蓝图三阶段管道** — core 已定义完整链条：
   ```
   CustomerSpec → EngineeringBOM → WorkOrder(冻结核准版)
   ```
   PLM 负责前两步，MES 使用最终输出。

2. **使用 IProcessParameter** — core 的 `IProcessParameter`（code/name/uom/targetValue/lowerLimit/upperLimit）是工艺参数的标准定义。`ProcessTemplate` 中的参数应使用此接口。

3. **版本管理** — 使用 `IBlueprint.getVersion()` + `HasVersion` 接口。PLM 的 `BlueprintService` 管理版本发布。

4. **用 IMethod 标记 SOP/规范** — `IMethod`（固定 SourceGroup.Method）表示工艺方法、SOP、检验标准等。蓝图引用 IMethod 资源，而非内嵌文档。

