# easy-factory-iot — 设备互联

## 模块定位

连接生产设备层与业务系统层，实现设备数据采集、工艺参数下发、设备状态监控。对接 PLC/SCADA/DCS 等工业控制系统，将设备数据转化为 core 领域模型的实时状态。

## 基于 core 的扩展

### 设备资源动态化

```java
// core: IResourceModel (group=Machine) 是静态的设备定义
// IoT 通过 IExpand 注入实时数据：

// "iot.connectionStatus"  → "ONLINE" | "OFFLINE" | "ERROR"
// "iot.protocol"          → "OPC_UA" | "MODBUS" | "MQTT" | "S7"
// "iot.endpoint"          → "opc.tcp://192.168.1.100:4840"
// "iot.lastHeartbeat"     → 最后心跳时间
// "iot.realtimeData"      → {温度: 82.3, 压力: 1.2, 转速: 1450}
// "iot.alarms"            → [{code, severity, message, time}]
```

## IoT 特有模型

### DeviceConnection (设备连接)

```
DeviceConnection:
├── equipmentCode:    关联设备 (Equip模块)
├── protocol:         通信协议 (OPC_UA/MODBUS_TCP/MQTT/S7_Comm/HTTP)
├── endpoint:         连接端点
├── authConfig:       认证配置 (证书/用户名密码/Token)
├── pollInterval:     采集间隔(ms)
├── tags:             采集标签列表
│   ├── tagName:      标签名 (如: NS=2;S=Temperature)
│   ├── dataType:     数据类型
│   ├── multiplier:   换算系数
│   ├── offset:       偏移量
│   └── deadband:     死区(变化小于此值不上报)
├── status:           连接状态
└── lastConnected:    最后连接时间
```

### TagValue (标签值)

```
TagValue:
├── equipmentCode:    设备编码
├── tagName:          标签名
├── value:            原始值
├── scaledValue:      换算后值
├── quality:          数据质量 (GOOD/BAD/UNCERTAIN)
├── timestamp:        采集时间戳 (设备端时间)
├── serverTimestamp:  服务器接收时间
└── batchId:          批次ID (用于批量存储)
```

### Command (指令下发)

```
Command:
├── equipmentCode:    目标设备
├── commandType:      指令类型 (SET_PARAM/START/STOP/READ/ACK_ALARM)
├── parameters:       参数 (如 {Temperature_Setpoint: 80.0})
├── priority:         优先级 (HIGH/NORMAL/LOW)
├── timeout:          超时时间
├── status:           执行状态 (QUEUED/SENT/ACKNOWLEDGED/COMPLETED/FAILED)
├── resultCode:       返回码
├── resultMessage:    返回消息
└── timeline:         时间线 (创建/发送/确认/完成)
```

### AlarmEvent (报警事件)

```
AlarmEvent:
├── equipmentCode:    设备编码
├── alarmCode:        报警编码
├── severity:         严重程度 (INFO/WARNING/CRITICAL/EMERGENCY)
├── message:          报警消息
├── triggerValue:     触发值
├── threshold:        阈值
├── triggeredAt:      触发时间
├── acknowledgedBy:   确认人
├── acknowledgedAt:   确认时间
├── resolvedAt:       恢复时间
└── actions:          联动动作 (如停机、通知、发起QMS偏差)
```

## 数据流

```
设备层 (PLC/传感器)
  │
  ├── OPC UA / MODBUS / MQTT ──→ IoT Protocol Adapter
  │                                  │
  │                          ┌───────▼────────┐
  │                          │  TagValue 采集   │
  │                          │  原始值 → 换算值  │
  │                          └───────┬────────┘
  │                                  │
  │                     ┌────────────┼────────────┐
  │                     ▼            ▼            ▼
  │              Equip 参数     报警检测      时序存储
  │              实时显示      (AlarmEvent)  (TSDB)
  │                     │            │
  │                     ▼            ▼
  │               MES 工单      QMS 偏差
  │               工序关联      → 自动发起
  │
  ◄── Command 指令下发 (参数设定/启停/确认报警)
```

## 协议适配器

支持多种工业协议的统一抽象：

```java
public interface IProtocolAdapter {
    boolean connect(DeviceConnection connection);
    void disconnect();
    boolean isConnected();
    List<TagValue> read(List<String> tags);
    boolean write(String tag, Object value);
    void subscribe(List<String> tags, Consumer<TagValue> callback);
}
```

目标实现：
- `OpcUaAdapter` — OPC UA 协议 (工业标准)
- `ModbusAdapter` — Modbus TCP/RTU (PLC 通用)
- `MqttAdapter` — MQTT (IoT 网关)
- `S7Adapter` — Siemens S7 (西门子PLC)
- `HttpAdapter` — REST API (简易设备)

## 外部接口

### 向上提供给 Web 的接口

```
GET    /api/iot/devices/{code}/status        设备连接状态
GET    /api/iot/devices/{code}/realtime      实时数据
GET    /api/iot/devices/{code}/history       历史数据查询
POST   /api/iot/devices/{code}/command       下发指令
GET    /api/iot/alarms                       报警列表 (含筛选)
PUT    /api/iot/alarms/{id}/acknowledge      确认报警
```

### 模块间接口

```
IoT → Equip:  设备实时参数值 → 更新参数显示
IoT → MES:    设备状态变化 → 影响工单执行
IoT → QMS:    设备参数异常 → 触发SPC预警/偏差
IoT ← Equip:  设备配方设定值 → 下发到设备
IoT ← MES:    开工/停机指令 → 下发到设备
```

## 存储策略

- **时序数据** — 标签值采用时序数据库 (InfluxDB / TimescaleDB)，支持高效的时间范围查询和降采样
- **报警事件** — 存入关系数据库，便于关联处理流程
- **指令记录** — 存入关系数据库，保证下发-确认-完成的强一致性

## 数据库设计要点

- 设备连接配置表 (iot_connection)
- 采集标签定义表 (iot_tag_definition)
- 报警事件表 (iot_alarm_event) — 关系库
- 指令队列表 (iot_command) — 关系库
- 标签时序数据 — 时序库 (TSDB)，不放在关系库
