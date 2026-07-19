# 编码规范

## 1. 命名规范

### 包命名

```
com.byz.factory.<module>.<layer>

layer: model | service | repository | config | web | dto | exception | util
```

### 类命名

| 类型 | 前缀/后缀 | 示例 |
|------|----------|------|
| 接口 | `I` 前缀 | `IProcess`, `IActionModel`, `IResourcePack` |
| 抽象类 | `Abstract` 前缀 或 无前缀 | `DataExpand`, `AbstractFactory` |
| 实现类 | 接口名去 I | `Process`, `Action`, `ResourcePack` |
| 枚举 | 名词 | `Dict`, `SourceGroup`, `Execute` |
| 异常 | `Exception` 后缀 | `ActionException`, `ResourceException` |
| 服务 | `Service` 后缀 | `WorkOrderService`, `InspectionService` |
| 控制器 | `Controller` 后缀 | `WorkOrderController` |
| DTO | `DTO` / `Request` / `Response` 后缀 | `CreateWorkOrderRequest` |

### 方法命名

```java
// 查询: get / find / query / is / has
IProcess getProcess(String code);
List<IProcess> findProcesses(String factoryCode);
boolean isCompleted();
boolean hasResources();

// 命令: create / add / update / delete / remove / execute
void createWorkOrder(WorkOrder order);
void addAction(IProcess process, IActionModel action);
void updateStatus(Status status);
IResourcePack execute(IResourceItem... input);

// 转换: to / from / convert / map
Map<String, Object> toMap();
String toJsonString();
WorkOrder fromBlueprint(IBlueprint blueprint);
```

## 2. 接口设计规范

### 接口粒度

```java
// ✅ 好：职责单一
public interface IProcess {
    String getCode();
    String getName();
    List<IActionModel> getActions();
    IResourcePack execute(IResourceItem... input);
}

// ❌ 差：接口臃肿
public interface IEverything {
    void doWork();
    void doQuality();
    void doEquipment();
    void doEverything();
}
```

### 接口中避免的实现细节

```java
// ✅ 好：接口只定义契约
public interface IResourcePack {
    List<IResourceItem> getResources();
    void compress();
}

// ❌ 差：接口中暴露 collection 实现
public interface IResourcePack {
    ArrayList<IResourceItem> getResources();  // 绑定了 ArrayList
    HashMap<String, IResourceItem> getResourceMap();  // 暴露内部结构
}
```

### 默认方法

```java
// Java 8+ 可以在接口中提供默认实现
public interface IResourceItem {
    String getName();
    BigDecimal getNumber();

    default boolean isEmpty() {
        return getNumber() == null || getNumber().compareTo(BigDecimal.ZERO) <= 0;
    }

    default boolean isSufficient(BigDecimal required) {
        return getNumber() != null && getNumber().compareTo(required) >= 0;
    }
}
```

## 3. IExpand 使用规范

### 扩展属性的 Key 命名

```
<module>.<property>

mes.workOrderId       — MES 模块的工单ID
qms.inspectionLevel   — QMS 模块的检验水平
plm.version           — PLM 模块的版本号
equip.parameters      — Equip 模块的设备参数
lims.formulaVersion   — LIMS 模块的配方版本
```

### 读写规范

```java
// ✅ 好：通过 set/get 读写
process.set("mes.workOrderId", "WO-001");
String id = (String) process.get("mes.workOrderId");

// ✅ 好：用常量管理 key，避免拼写错误
public class MesKeys {
    public static final String WORK_ORDER_ID = "mes.workOrderId";
    public static final String STATUS = "mes.status";
    public static final String OPERATOR = "mes.operatorId";
}
process.set(MesKeys.WORK_ORDER_ID, "WO-001");

// ❌ 差：硬编码字符串，容易拼错
process.set("workOrderId", "WO-001");         // 缺少模块前缀
process.set("mes.workorderid", "WO-001");     // 大小写不一致
```

### 类型安全

```java
// 考虑封装类型安全的访问器
public class MesProcessAccessor {
    private final IExpand target;

    public MesProcessAccessor(IExpand target) {
        this.target = target;
    }

    public void setWorkOrderId(String id) {
        target.set(MesKeys.WORK_ORDER_ID, id);
    }

    public String getWorkOrderId() {
        return (String) target.get(MesKeys.WORK_ORDER_ID);
    }
}
```

## 4. 异常处理规范

### 领域异常

```java
// core 层定义领域异常
public class ActionException extends RuntimeException {
    private final String actionCode;
    private final String processCode;

    public ActionException(String actionCode, String processCode, String message) {
        super(message);
        this.actionCode = actionCode;
        this.processCode = processCode;
    }
}

public class ResourceException extends RuntimeException {
    private final String resourceName;
    private final BigDecimal required;
    private final BigDecimal available;

    public ResourceException(String resourceName, BigDecimal required, BigDecimal available) {
        super(String.format("资源不足: %s, 需要 %s, 可用 %s", resourceName, required, available));
        this.resourceName = resourceName;
        this.required = required;
        this.available = available;
    }
}
```

### 异常传播

```java
// ✅ 好：抛出有意义的领域异常
public void use(BigDecimal number) {
    if (getNumber().compareTo(number) < 0) {
        throw new ResourceException(getName(), number, getNumber());
    }
    this.number = this.number.subtract(number);
}

// ❌ 差：吞掉异常或抛出不明确的异常
public void use(BigDecimal number) {
    try {
        this.number = this.number.subtract(number);
    } catch (Exception e) {
        // 什么都不做
    }
}
```

## 5. 空值处理

```java
// ✅ 好：使用 Optional 表示可能为空
public interface IActionModel {
    Optional<String> getScript();  // 不是所有动作都有脚本
}

// ✅ 好：提供空对象哨兵
public static final EmptyResourcePack EMPTY = new EmptyResourcePack();
public static final EmptyAction EMPTY_ACTION = new EmptyAction();

// ✅ 好：显式检查
public boolean isEmpty() {
    return resources == null || resources.isEmpty();
}

// ❌ 差：返回 null 而不说明
public String getScript() {
    return null;  // 调用方不知道可能为 null
}
```

## 6. BigDecimal 使用规范

```java
// ✅ 好：使用构造函数或 valueOf
BigDecimal ten = BigDecimal.TEN;
BigDecimal precise = new BigDecimal("10.50");
BigDecimal fromInt = BigDecimal.valueOf(10);

// ❌ 差：使用 double 构造函数（精度丢失）
BigDecimal bad = new BigDecimal(10.50);  // 10.5 的 double 表示可能不精确

// ✅ 好：使用 compareTo 比较
if (number.compareTo(BigDecimal.ZERO) > 0) { }

// ❌ 差：使用 equals（比较 scale 也参与）
if (number.equals(BigDecimal.ZERO)) { }  // new BigDecimal("0.00").equals(BigDecimal.ZERO) == false
```

## 7. Lombok 使用规范

```java
// 数据类
@Data
public class Action extends DataExpand implements IActionModel {
    private String code;
    private String name;
    private int order;
    private Dict.Importance importance;
    private String script;
}

// 不可变对象
@Value
public class ImmutablePoint {
    String x;
    String y;
}

// 构建器
@Builder
public class WorkOrder {
    private String workOrderNo;
    private String productCode;
    private BigDecimal quantity;
}
```

## 8. 测试规范

### 测试方法命名

```java
// 模式: methodName_condition_expectedResult
@Test
void execute_withScript_shouldReturnScriptResult() { }

@Test
void use_insufficientResource_shouldThrowException() { }

@Test
void compress_duplicateResources_shouldAggregateQuantity() { }
```

### Given-When-Then

```java
@Test
void processExecute_shouldAccumulateResources() {
    // Given
    IProcess process = TestFactory.createProcess("P001");
    ResourceModel material = TestFactory.createMaterial("物料A", 10);
    ResourceModel machine = TestFactory.createMachine("设备A", 1);

    // When
    IResourcePack result = process.execute(material, machine);

    // Then
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(2, result.getResources().size());
}
```

## 9. JavaDoc 规范

```java
/**
 * 执行工序中的所有动作，按顺序处理输入资源并累积输出。
 *
 * <p>如果动作包含脚本（{@link IActionModel#getScript()}），
 * 则通过 {@link ScriptExecutor} 执行脚本逻辑，
 * 否则返回工序的默认资源包。
 *
 * @param inputResources 输入资源列表（物料、设备、人员等）
 * @return 执行完成后的输出资源包
 * @throws ActionException 如果某个动作执行失败
 * @throws ResourceException 如果输入资源不满足动作要求
 */
IResourcePack execute(IResourceItem... inputResources);
```
