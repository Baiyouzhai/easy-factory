# 开发指南

## 1. 环境准备

### 必需工具

| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 21+ | Java 开发运行环境 |
| Maven | 3.9+ | 构建管理 |
| Git | 2.40+ | 版本控制 |
| IDE | IntelliJ IDEA / VS Code | 开发编辑 |

### 推荐插件 (VS Code / IDEA)

- Lombok Plugin — 注解处理
- Maven Helper — 依赖分析
- SonarLint — 代码质量检查

### 克隆与构建

```bash
git clone <repo-url>
cd easy-factory

# 编译全项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package -DskipTests

# 启动 web 模块
cd easy-factory-web
mvn spring-boot:run
```

## 2. 模块开发顺序

按依赖关系自底向上开发：

```
阶段 0 (已完成):  common → core          基础层 + 领域内核
阶段 1 (进行中):  core 问题修复           解决未定义类型、完善接口
阶段 2:           common + core 测试      单元测试覆盖
阶段 3:           db                      持久化模块
阶段 4:           mes + qms               MES 制造执行 + QMS 质量管理
阶段 5:           web                     门户聚合
阶段 6:           plm + lims + equip      研发工艺 + 配方 + 设备
阶段 7:           iot + erp               设备互联 + ERP 集成
```

## 3. 创建一个新模块

### 3.1 模块目录结构

```
easy-factory-<name>/
├── pom.xml
└── src/
    ├── main/java/com/byz/factory/<name>/
    │   ├── model/              — 领域模型（实现 core 接口）
    │   ├── service/            — 业务服务
    │   ├── repository/         — 数据访问
    │   └── config/             — Spring 配置
    └── test/java/com/byz/factory/<name>/
        ├── model/
        └── service/
```

### 3.2 POM 模板

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.byz.factory</groupId>
        <artifactId>easy-factory</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>easy-factory-<name></artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <!-- 依赖 core -->
        <dependency>
            <groupId>com.byz.factory</groupId>
            <artifactId>easy-factory-core</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 3.3 在父 POM 中注册

```xml
<modules>
    <module>easy-factory-common</module>
    <module>easy-factory-core</module>
    <module>easy-factory-<name></module>
    <!-- 新增 -->
</modules>
```

## 4. 核心开发模式

### 4.1 实现 core 接口

```java
// core 定义接口
public interface IProcess {
    String getCode();
    List<IActionModel> getActions();
    IResourcePack execute(IResourceItem... inputResources);
}

// mes 模块实现
@Data
public class MesProcess implements IProcess, IDataExpand {
    private String code;
    private String name;
    private int order;
    private List<IActionModel> actions;
    private IResourcePack resourcePack;

    @Override
    public IResourcePack execute(IResourceItem... inputResources) {
        // MES 特有的执行逻辑：关联工单、记录时间、触发 QMS
        IResourcePack result = resourcePack;
        for (IActionModel action : actions) {
            result = action.execute(this, inputResources);
        }
        return result;
    }
}
```

### 4.2 使用 IExpand 扩展属性

```java
// 通过扩展属性注入模块特有字段，而非修改 core 接口
process.set("mes.workOrderId", "WO-20260712-001");
process.set("mes.status", "IN_PROGRESS");
process.set("mes.operatorId", "OP001");

// 读取
String workOrderId = (String) process.get("mes.workOrderId");
```

### 4.3 脚本化动作

```java
// 定义带脚本的动作
Action action = new Action();
action.setCode("INSPECT_001");
action.setName("粘度检测");
action.setScript("""
    function execute(process, inputResources) {
        var qms = context.getQmsService();
        var plan = qms.getInspectionPlan(process);
        var value = qms.measure(plan.itemCode);
        var result = qms.judge(value, plan.specLimits);
        if (result == 'FAIL') {
            throw new ActionException('粘度检测不合格: ' + value);
        }
        return process.getResourcePack();
    }
    """);
```

### 4.4 模块间通信

模块间通过接口调用，不直接依赖实现类：

```java
// MES 调用 QMS（通过接口）
public class MesProcessService {
    private final IQmsService qmsService;  // QMS 模块提供的接口

    public void executeInspection(IProcess process) {
        InspectionPlan plan = qmsService.getInspectionPlan(process.getCode());
        // ...
    }
}
```

模块间接口定义在各自的 `*.service` 包中，作为模块的对外契约。

## 5. 测试策略

### 单元测试

```java
@Test
void processExecute_shouldAccumulateResources() {
    IProcess process = new Process();
    ResourceModel input = new ResourceModel("物料A", SourceGroup.Material, SourceType.Other, BigDecimal.TEN);

    IResourcePack result = process.execute(input);

    assertNotNull(result);
    assertFalse(result.isEmpty());
}
```

### 集成测试

```java
@SpringBootTest
class MesWorkOrderIntegrationTest {

    @Autowired
    private WorkOrderService workOrderService;

    @Test
    void createWorkOrder_shouldCreateFromBlueprint() {
        IProductInfo product = mockProduct();
        WorkOrder order = workOrderService.create(product, BigDecimal.ONE);
        assertEquals("CREATED", order.getStatus());
    }
}
```

### 场景测试

用脚本模拟完整生产流程：

```javascript
// test/resources/scenarios/full_production.js
var factory = context.getFactory("FACTORY-001");
var blueprint = context.getBlueprint("PRODUCT-A", "1.0.0");
var checker = context.getProductChecker();
var result = checker.check(blueprint, factory);

if (result.isPass()) {
    var resources = createInputResources(blueprint);
    for (var process of blueprint.getProcesses()) {
        resources = process.execute(resources);
    }
}
```

## 6. Git 工作流

### 分支策略

```
main          — 稳定版本
├── develop   — 开发主分支
│   ├── feature/mes-001   — 功能分支
│   ├── feature/qms-001
│   └── fix/core-001      — 修复分支
└── release/0.1.0          — 发布分支
```

### 提交规范

```
<type>(<scope>): <subject>

类型: feat | fix | docs | style | refactor | test | chore
范围: common | core | mes | qms | plm | equip | lims | erp | iot | web

示例:
feat(core): 添加 IAction 接口定义
fix(mes): 修复工单状态流转异常
docs(architecture): 添加模块间通信文档
test(qms): 补充检验方案单元测试
```

## 7. 构建与发布

### 版本号规则

```
<major>.<minor>.<patch>

0.1.0 — core 问题修复完成
0.2.0 — db 持久化模块完成
0.3.0 — MES 雏形
0.4.0 — QMS 雏形
...
1.0.0 — 首次正式发布
```

### CI/CD (规划)

```
Git Push → GitHub Actions
  ├── mvn compile
  ├── mvn test
  ├── mvn checkstyle
  ├── mvn package
  └── deploy to Nexus / Docker Registry
```
