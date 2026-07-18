# Common 模块 — BOM 转化规则引擎需求规格

> **来源模块：** easy-factory-plm
> **目标模块：** easy-factory-common
> **日期：** 2026-07-19
> **状态：** 待 Common 模块实现

---

## 1. 背景

PLM 模块需要将产品 BOM 从设计阶段逐步转化为制造阶段：

```
EBOM (设计BOM) → PBOM (工艺BOM) → MBOM (制造BOM)
```

每次转化涉及多条规则（物料添加、虚拟件展开、损耗率计算、工序分组等），规则的配置和执行需要一个通用的规则引擎。Common 模块作为基础设施层，应提供此能力。

## 2. PLM 已定义的契约

PLM 模块已创建以下契约接口和模型，Common 模块可直接引用（依赖 `easy-factory-plm` 或我们提取到 core 的共享接口）：

| 文件 | 位置 | 说明 |
|------|------|------|
| `BOMType.java` | `plm/bom/` | EBOM / PBOM / MBOM 枚举 |
| `BOMConversionRequest.java` | `plm/bom/` | 转化请求（源类型→目标类型+蓝图+工厂） |
| `BOMConversionResult.java` | `plm/bom/` | 转化结果（产物 BOM + 应用的规则 + 告警） |
| `IBOMConversionRule.java` | `plm/bom/` | 单条转化规则接口（Common 实现） |

## 3. 需求规格

### 3.1 IRuleEngine 接口

Common 模块需提供 `IRuleEngine` 接口：

```java
package com.byz.util.rule;

/**
 * 规则引擎 — 按优先级执行一组规则，产出结果。
 * 供 PLM/BOM 转化、QMS/检验方案生成等场景复用。
 */
public interface IRuleEngine<C, R> {

    /** 注册规则 */
    void registerRule(IBOMConversionRule rule);

    /** 批量注册 */
    void registerRules(Collection<IBOMConversionRule> rules);

    /** 按优先级排序执行所有匹配的规则 */
    R execute(C context, Predicate<IBOMConversionRule> filter);
}
```

### 3.2 内置规则实现

Common 模块需提供以下内置规则（实现 `IBOMConversionRule`）：

| 规则编码 | 名称 | 适用转化 | 优先级 | 行为 |
|---------|------|---------|--------|------|
| `bom.material.add` | 工艺辅料添加 | EBOM→PBOM | 10 | 根据产品类型注入工艺辅料（如片剂添加润滑剂、包衣粉） |
| `bom.phantom.expand` | 虚拟件展开 | EBOM→PBOM | 20 | 将"混合料"等虚拟物料展开为实际组分 |
| `bom.loss.apply` | 损耗率应用 | EBOM→PBOM | 30 | 对物料用量乘以损耗系数（如 1.02 表示 2% 损耗） |
| `bom.process.group` | 工序分组 | PBOM→MBOM | 10 | 按蓝图的工序顺序将物料分配到各工序 |
| `bom.workstation.bind` | 工位绑定 | PBOM→MBOM | 20 | 将设备/工位绑定到 MBOM 行项 |
| `bom.qty.recalc` | 批量重算 | PBOM→MBOM | 30 | 按批次量重新计算每个工序的物料需求量 |

### 3.3 规则配置方式

规则通过 **脚本** 配置（与项目现有的 Nashorn/GraalJS 脚本引擎一致）：

```
easy-factory-core/src/main/resources/scripts/plm/
├── bom_material_add.js       # 工艺辅料添加规则
├── bom_phantom_expand.js     # 虚拟件展开规则
├── bom_loss_apply.js         # 损耗率规则
├── bom_process_group.js      # 工序分组规则
└── bom_workstation_bind.js   # 工位绑定规则
```

脚本格式遵循项目规范（`@id @name @version @module @description` 元数据头 + `function execute(context)` 入口）。

### 3.4 使用示例（PLM 调用 Common）

```java
// PLM 侧：构造请求
BOMConversionRequest request = BOMConversionRequest.ebomToPbom(
    "REQ-001", "PROD-AMX-001", ebom, blueprint);

// Common 侧：规则引擎执行
IRuleEngine<BOMConversionRequest, BOMConversionResult> engine = ...;
BOMConversionResult result = engine.execute(request,
    rule -> rule.getSourceType() == request.sourceType()
         && rule.getTargetType() == request.targetType());

// PLM 侧：消费结果
if (result.isSuccess()) {
    IBillOfMaterial pbom = result.getResultBOM();
    // 发布 BOM 转化完成事件 → ERP 同步物料需求
}
```

## 4. 边界与职责

| 关注点 | PLM 负责 | Common 负责 |
|--------|---------|------------|
| BOM 类型定义 | ✅ `BOMType` 枚举 | — |
| 转化请求/结果模型 | ✅ `BOMConversionRequest/Result` | — |
| 规则接口定义 | ✅ `IBOMConversionRule` | — |
| 规则引擎实现 | — | ✅ `IRuleEngine` |
| 内置规则实现 | — | ✅ 6 条 BOM 规则 |
| 规则执行调度 | 调用方 | ✅ 引擎负责 |
| 转化后事件发布 | ✅ `PlmEventTypes.BOM_TRANSFORMED` | — |

## 5. 扩展性

Common 模块的 `IRuleEngine<C, R>` 设计为泛型，不仅服务 PLM：
- **QMS** — 检验方案生成规则（产品→检验项→抽样计划）
- **APS** — 排程规则（订单→工序→设备优先级）
- **LIMS** — 配方转化规则

---

*此文档由 PLM 模块会话编写，供 Common 模块实施参考。*
