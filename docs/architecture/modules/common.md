# easy-factory-common — 基础数据层

## 模块定位

为整个平台提供统一的数据抽象基础，所有模块共享的最底层设施。

## 核心接口

### IData — 数据基础能力

```java
public interface IData {
    void copyProperties(Object source, String... ignoreProperties);
    Map<String, Object> toMap();
    String toJsonString();
    <T> T jsonClone(Class<T> clazz);
}
```

提供属性拷贝、JSON 序列化、深克隆等通用能力。

### IExpand — 动态扩展属性

```java
public interface IExpand {
    Object get(String key);
    void set(String key, Object value);
    Set<String> nativeKeys();          // 声明字段名集合
    Set<String> expandKeys();          // 动态属性名集合
    Map<String, Object> getProperties();
    void setProperties(Map<String, Object> properties);
}
```

核心思想：每个领域对象既有 Java 原生字段，也可携带任意动态属性。不同子系统可以往同一对象上挂不同的扩展数据。

### IDataExpand — 合并接口

```java
public interface IDataExpand extends IData, IExpand {
    // 整合 data 能力和 expand 能力
}
```

### Data / DataExpand — 抽象基类

- `Data`：空实现，作为纯数据对象的基类
- `DataExpand`：含 `JSONObject expand` 字段，实际存储扩展属性

## 工具类

### CollectionUtil

递归引用清洗工具，用于：
- 打破循环引用
- 清理空值
- 统一集合类型转换

## 依赖

无外部模块依赖，是本项目的零级模块。

## 设计要点

1. **不依赖 Spring** — 除 `copyProperties` 使用 `BeanUtils` 外，其余均为纯 JDK
2. **序列化统一** — 全项目通过 fastjson2 统一 JSON 处理
3. **扩展属性覆盖规则** — 当扩展属性和原生属性同名时，扩展属性优先
