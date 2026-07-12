# AI 协作指南

## 1. 核心理念

本项目设计时充分考虑了与 AI 的协作。通过**接口契约 + 脚本化 + 扩展属性**的架构，AI 可以在不修改核心代码的情况下，帮助完成大量开发工作。

## 2. AI 能做什么

### 2.1 生成模块实现

AI 可以根据 core 接口生成业务模块的完整实现：

```
# 示例：生成 MES 工单管理的实现
"根据 IProcess 和 IActionModel 接口，
在 easy-factory-mes 中实现工单创建、工序流转、报工功能。
参考 docs/architecture/modules/mes.md 中的设计。"
```

### 2.2 编写动作脚本

AI 非常适合编写 `Action.script` 中的 JavaScript 逻辑：

```javascript
// AI 生成的称量防错脚本
function execute(process, inputResources) {
    var lims = context.getLimsService();
    var task = lims.getWeighingTask(process);
    var scale = context.getScale(task.balanceCode);

    for (var item of task.items) {
        var actual = scale.read();
        var deviation = Math.abs(actual - item.formulaQty) / item.formulaQty * 100;

        var record = lims.createWeighingRecord({
            taskCode: task.code,
            materialCode: item.materialCode,
            formulaQty: item.formulaQty,
            actualQty: actual,
            deviation: deviation,
            balance: task.balanceCode,
            timestamp: new Date()
        });

        if (deviation > item.tolerance) {
            record.status = 'DEVIATION';
            lims.save(record);
            throw new Error('称量超差: ' + item.materialCode + ' 偏差: ' + deviation.toFixed(2) + '%');
        }

        record.status = 'OK';
        lims.save(record);
    }

    return process.getResourcePack();
}
```

### 2.3 生成测试用例

```java
// AI 生成的测试
@Test
void weighingScript_withOverTolerance_shouldThrowException() {
    // Given: 配方要求 100g，允差 ±1%
    // 天平读数 102.5g，偏差 2.5% > 1%
    // Then: 抛出 ActionException
}
```

### 2.4 生成 API 文档与 DTO

```
"根据 WorkOrderService 的方法签名，
生成对应的 REST Controller 和 Request/Response DTO。"
```

### 2.5 编写数据库脚本

```sql
-- AI 生成的 MES 工单表
CREATE TABLE mes_work_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_no VARCHAR(50) NOT NULL UNIQUE,
    product_code VARCHAR(50) NOT NULL,
    quantity DECIMAL(18,6) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    factory_code VARCHAR(50),
    planned_start DATETIME,
    planned_end DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_product (product_code)
);
```

## 3. 高效协作模式

### 3.1 上下文加载

在要求 AI 干活之前，先让它了解上下文：

```
"先阅读 docs/architecture/overview.md 和 docs/architecture/modules/mes.md，
了解项目架构和 MES 模块设计后，开始实现..."
```

### 3.2 分步确认

避免一次提太大需求，分步推进：

```
步骤1: "在 easy-factory-mes 中创建 WorkOrder 模型类，实现 IDataExpand"
步骤2: "创建 WorkOrderService 接口"
步骤3: "实现 WorkOrderService，包含 create/release/start/complete 方法"
步骤4: "为 WorkOrderService 编写单元测试"
```

### 3.3 参照已有代码

```
"参照 easy-factory-core 中 Process 的实现风格，
在 easy-factory-mes 中实现 MesProcess。
命名风格、注释密度、异常处理方式保持一致。"
```

### 3.4 用文档驱动开发

文档先行是一种高效模式：

```
"在 docs/architecture/modules/ 下创建 mes.md，
定义 MES 模块的核心模型、接口、数据库设计，然后按照文档实现代码。"
```

## 4. 推荐 Prompt 模板

### 模板 A：新建模块

```
任务: 创建 easy-factory-<模块名> 模块

背景:
- 项目是基于 DDD 的制造系统平台，核心领域模型在 easy-factory-core
- 架构文档: docs/architecture/overview.md
- 模块设计: docs/architecture/modules/<模块名>.md
- 编码规范: docs/implementation/conventions.md

要求:
1. 创建模块 POM 并在父 POM 中注册
2. 实现核心领域模型（实现 core 接口，使用 IExpand 扩展属性）
3. 核心 Service 接口和实现
4. 单元测试覆盖率 > 80%
5. 遵循项目已有的编码规范和命名约定
```

### 模板 B：实现接口

```
任务: 实现 {接口名} 的具体类

上下文:
- 接口定义在 easy-factory-core: {完整类路径}
- 参考实现: {类似实现类的路径}

要求:
1. 类名: {接口名去掉I前缀}
2. 继承 DataExpand（如需扩展属性）或 Data（如不需要）
3. 关键字段使用 Lombok @Data
4. 方法实现要有负余额保护、空值检查等防御性代码
5. 编写 Given-When-Then 风格的单元测试
```

### 模板 C：编写脚本

```
任务: 为 {工序/动作} 编写 JavaScript 执行脚本

业务逻辑:
1. {逻辑描述}
2. {异常条件}

可用上下文对象:
- context.getMesService(): MES 服务
- context.getQmsService(): QMS 服务
- context.getLimsService(): LIMS 服务
- process: 当前工序对象
- inputResources: 输入资源列表

返回值: process.getResourcePack() 或其修改版本
```

### 模板 D：代码审查

```
任务: 审查 easy-factory-{模块名} 的变更

审查维度:
1. 是否正确实现了 core 接口
2. 异常处理是否完整（ResourceException, ActionException）
3. 空值处理是否有遗漏
4. BigDecimal 使用是否正确（compareTo 代替 equals）
5. IExpand key 命名是否规范（module.property）
6. 测试覆盖率是否足够
7. 是否符合编码规范 docs/implementation/conventions.md
```

## 5. 常见协作场景

### 场景 1：新增业务模块

```
对话 1: "先阅读 docs/architecture/modules/mes.md，了解 MES 设计"
对话 2: "创建 easy-factory-mes 模块，包含 POM 和基础包结构"
对话 3: "实现 MesProcess 和 MesAction，继承 core 接口"
对话 4: "实现 WorkOrderService，包含创建/下达/开工/完工"
对话 5: "实现 WorkOrderController，暴露 REST API"
对话 6: "编写单元测试"
```

### 场景 2：修复 core 问题

根据 [core.md](../architecture/modules/core.md) 中记录的已知问题：

```
对话 1: "修复 easy-factory-core 中 IAction 接口未定义的问题，
        分析所有引用 IAction 的地方，确定是补充定义还是统一为 IActionModel"
对话 2: "修复 IResource 接口未定义的问题"
对话 3: "修复 script/Action 中 HuTool StrUtil 依赖缺失的问题"
```

### 场景 3：编写工序脚本

```
对话 1: "为 QMS 质量门禁工序编写判定脚本，
        逻辑：合格放行，让步需要审批记录，不合格触发偏差处理"
对话 2: "质量门禁脚本中增加 SPC 规则检查"
对话 3: "编写质量门禁脚本的模拟测试用例"
```

## 6. AI 协作注意事项

### 6.1 始终保持上下文

每个新对话或任务开始前，让 AI 先读取相关文档和代码。不要假设 AI 记住了之前的对话。

### 6.2 代码风格一致性

始终引用 `docs/implementation/conventions.md` 和已有的实现类作为风格参考。

### 6.3 小步快跑

每次只做一个小的、明确的任务。完成后验证，再继续下一步。

### 6.4 文档与代码同步

代码变更后，同步更新对应的文档。文档是后续 AI 协作的基础。
