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
```

### 模块间接口

```
PLM → MES:   发布工艺路线（Blueprint）供工单引用
PLM → core:  提供 IEstimateResourceModel 估算数据
PLM ← ERP:   获取物料主数据供 BOM 引用
PLM → PLM:   版本管理、审批流
```

## 数据库设计要点

- 工艺路线版本表 (plm_blueprint_version)
- 工序模板表 (plm_process_template)
- 工艺参数表 (plm_process_parameter)
- BOM转化规则表 (plm_bom_converter)
- 版本差异记录表 (plm_version_diff)
