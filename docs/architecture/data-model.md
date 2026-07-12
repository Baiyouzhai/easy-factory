# 跨模块数据模型

## 1. 设计原则

1. **Core 接口不绑定表结构** — core 只定义接口契约，各模块自行设计持久化方案
2. **模块间通过 code 关联** — 不使用外键约束，模块可独立部署
3. **追溯数据不可变** — 批记录/检验记录一旦写入，物理不可删除，逻辑标记废弃
4. **快照策略** — 执行时刻的资源状态以 JSON 快照存储，便于追溯

## 2. Core 领域概念模型 (ER)

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   Factory     │       │   Resource    │       │   Blueprint   │
│──────────────│       │──────────────│       │──────────────│
│ code          │       │ name          │       │ code          │
│ name          │       │ group         │       │ name          │
│ processes[]   │       │ type          │       │ version       │
└──────┬───────┘       │ number        │       │ status        │
       │               └──────┬───────┘       │ processes[]   │
       │ 1:N                  │               └──────┬───────┘
       ▼                      │                      │
┌──────────────┐              │                      │ 1:N
│   Process     │              │                      ▼
│──────────────│              │              ┌──────────────┐
│ code          │              │              │ProductInfo   │
│ name          │              │              │──────────────│
│ order         │              │              │ name          │
│ actions[]     │              │              │ blueprint     │
│ resourcePack  │              │              └──────────────┘
└──────┬───────┘              │
       │ 1:N                  │
       ▼                      │
┌──────────────┐              │
│   Action      │              │
│──────────────│              │
│ code          │              │
│ name          │              │
│ order         │              │
│ importance    │              │
│ executeType   │              │
│ controlType   │              │
│ script        │              │
│ requireResources[]────────────┘
└──────────────┘
```

## 3. 各模块数据表设计

### 3.1 MES 表

```sql
-- 工单
CREATE TABLE mes_work_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_no VARCHAR(50) NOT NULL UNIQUE,
    product_code VARCHAR(50) NOT NULL,
    blueprint_code VARCHAR(50) NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    batch_size DECIMAL(18,6) NOT NULL,
    factory_code VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    planned_start DATETIME,
    planned_end DATETIME,
    actual_start DATETIME,
    actual_end DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_batch (batch_no)
);

-- 工序记录
CREATE TABLE mes_process_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_id BIGINT NOT NULL,
    process_code VARCHAR(50) NOT NULL,
    process_name VARCHAR(100),
    order_no INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    actual_start DATETIME,
    actual_end DATETIME,
    operator VARCHAR(50),
    input_snapshot JSON,      -- 输入资源快照
    output_snapshot JSON,     -- 输出资源快照
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_work_order (work_order_id),
    INDEX idx_status (status)
);

-- 动作记录
CREATE TABLE mes_action_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_record_id BIGINT NOT NULL,
    action_code VARCHAR(50) NOT NULL,
    action_name VARCHAR(100),
    order_no INT NOT NULL,
    execute_type VARCHAR(20),
    control_type VARCHAR(20),
    script_executed BOOLEAN DEFAULT FALSE,
    input_snapshot JSON,
    output_snapshot JSON,
    result VARCHAR(20) DEFAULT 'PENDING',
    operator VARCHAR(50),
    start_time DATETIME,
    end_time DATETIME,
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_process_record (process_record_id)
);
```

### 3.2 QMS 表

```sql
-- 检验方案
CREATE TABLE qms_inspection_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    product_code VARCHAR(50) NOT NULL,
    process_code VARCHAR(50),
    inspection_type VARCHAR(10),     -- IQC/IPQC/FQC/OQC
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 检验项目
CREATE TABLE qms_inspection_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    spec_type VARCHAR(10),           -- 计量/计数
    usl DECIMAL(18,6),               -- 规格上限
    lsl DECIMAL(18,6),               -- 规格下限
    target DECIMAL(18,6),            -- 目标值
    unit VARCHAR(20),
    method TEXT,
    sampling_plan JSON,
    FOREIGN KEY (plan_id) REFERENCES qms_inspection_plan(id)
);

-- 检验记录
CREATE TABLE qms_inspection_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT,
    item_id BIGINT,
    work_order_id BIGINT,
    process_record_id BIGINT,
    action_record_id BIGINT,
    batch_no VARCHAR(50),
    measured_value DECIMAL(18,6),
    judgement VARCHAR(20),           -- PASS/FAIL/CONCESSION
    defect_code VARCHAR(50),
    inspector VARCHAR(50),
    inspected_at DATETIME,
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_batch (batch_no),
    INDEX idx_process (process_record_id)
);

-- 偏差
CREATE TABLE qms_deviation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    source_type VARCHAR(30),         -- INSPECTION/EQUIPMENT/HUMAN
    source_id BIGINT,                -- 关联检验记录ID或设备报警ID
    batch_no VARCHAR(50),
    severity VARCHAR(10),            -- MINOR/MAJOR/CRITICAL
    description TEXT,
    product_impact TEXT,
    disposition VARCHAR(20),         -- REWORK/CONCESSION/REJECT
    capa_id BIGINT,
    status VARCHAR(20),
    created_by VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    INDEX idx_batch (batch_no)
);

-- CAPA
CREATE TABLE qms_capa (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    deviation_id BIGINT,
    root_cause TEXT,
    corrective_action TEXT,
    preventive_action TEXT,
    verification TEXT,
    status VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    closed_at DATETIME,
    FOREIGN KEY (deviation_id) REFERENCES qms_deviation(id)
);
```

### 3.3 LIMS 表

```sql
-- 配方
CREATE TABLE lims_formula (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    version VARCHAR(20) NOT NULL,
    batch_size DECIMAL(18,6) NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    approved_by VARCHAR(50),
    effective_date DATE,
    expiry_date DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code_version (code, version)
);

-- 配方组分
CREATE TABLE lims_formula_component (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    formula_id BIGINT NOT NULL,
    phase_no INT NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100),
    formula_qty DECIMAL(18,6) NOT NULL,
    unit VARCHAR(20),
    tolerance_min DECIMAL(5,2),
    tolerance_max DECIMAL(5,2),
    addition_order INT,
    addition_method VARCHAR(50),
    hazard_class VARCHAR(30),
    FOREIGN KEY (formula_id) REFERENCES lims_formula(id)
);

-- 称量任务
CREATE TABLE lims_weighing_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    formula_code VARCHAR(50) NOT NULL,
    work_order_id BIGINT,
    batch_no VARCHAR(50) NOT NULL,
    status VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_batch (batch_no)
);

-- 称量明细
CREATE TABLE lims_weighing_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    formula_qty DECIMAL(18,6) NOT NULL,
    actual_qty DECIMAL(18,6),
    deviation_percent DECIMAL(5,2),
    tolerance DECIMAL(5,2),
    balance_code VARCHAR(50),
    operator VARCHAR(50),
    verifier VARCHAR(50),
    weighed_at DATETIME,
    status VARCHAR(20),
    FOREIGN KEY (task_id) REFERENCES lims_weighing_task(id)
);

-- 批记录
CREATE TABLE lims_batch_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    batch_no VARCHAR(50) NOT NULL UNIQUE,
    product_code VARCHAR(50) NOT NULL,
    formula_code VARCHAR(50) NOT NULL,
    formula_version VARCHAR(20),
    blueprint_code VARCHAR(50),
    batch_size DECIMAL(18,6),
    actual_yield DECIMAL(18,6),
    yield_percent DECIMAL(5,2),
    status VARCHAR(20),
    reviewed_by VARCHAR(50),
    reviewed_at DATETIME,
    approved_by VARCHAR(50),
    approved_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 3.4 ERP 表（本地缓存）

```sql
-- 物料主数据缓存
CREATE TABLE erp_material_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    unit VARCHAR(20),
    material_type VARCHAR(10),    -- ROH/HALB/FERT
    batch_managed BOOLEAN,
    shelf_life_days INT,
    ghs_class VARCHAR(30),
    last_sync_time DATETIME,
    source_system VARCHAR(50)
);

-- 事务回传队列
CREATE TABLE erp_transaction_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_type VARCHAR(20),
    material_code VARCHAR(50),
    quantity DECIMAL(18,6),
    batch_no VARCHAR(50),
    movement_type VARCHAR(10),
    reference_doc VARCHAR(50),
    posting_date DATE,
    status VARCHAR(20),            -- PENDING/SENT/CONFIRMED/FAILED
    retry_count INT DEFAULT 0,
    error_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
);
```

### 3.5 Equip 表

```sql
-- 设备台账
CREATE TABLE equip_equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    model VARCHAR(100),
    category VARCHAR(50),
    location VARCHAR(100),
    status VARCHAR(20),
    specifications JSON,
    supplier VARCHAR(100),
    asset_code VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 设备配方
CREATE TABLE equip_recipe (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100),
    equipment_code VARCHAR(50) NOT NULL,
    product_code VARCHAR(50),
    version VARCHAR(20),
    status VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code_version (code, version)
);

-- OEE指标
CREATE TABLE equip_oee_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_code VARCHAR(50) NOT NULL,
    period_start DATETIME NOT NULL,
    period_end DATETIME NOT NULL,
    availability DECIMAL(5,2),
    performance DECIMAL(5,2),
    quality DECIMAL(5,2),
    oee DECIMAL(5,2),
    downtime_minutes INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_equip_period (equipment_code, period_start)
);
```

### 3.6 IoT 表

```sql
-- 设备连接配置
CREATE TABLE iot_connection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_code VARCHAR(50) NOT NULL UNIQUE,
    protocol VARCHAR(30),
    endpoint VARCHAR(200),
    auth_config JSON,
    poll_interval_ms INT DEFAULT 1000,
    status VARCHAR(20),
    last_connected DATETIME
);

-- 报警事件
CREATE TABLE iot_alarm_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_code VARCHAR(50) NOT NULL,
    alarm_code VARCHAR(50),
    severity VARCHAR(10),
    message VARCHAR(500),
    trigger_value DECIMAL(18,6),
    threshold DECIMAL(18,6),
    triggered_at DATETIME,
    acknowledged_by VARCHAR(50),
    acknowledged_at DATETIME,
    resolved_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_equip_time (equipment_code, triggered_at)
);
```

> 标签时序数据建议用时序数据库（InfluxDB/TimescaleDB），不在此 ER 范围内。

## 4. 跨模块共享字段约定

所有涉及追溯的表，必须包含以下字段：

```
batch_no        VARCHAR(50)    -- 批号（核心追溯键）
process_code    VARCHAR(50)    -- 工序编码
action_code     VARCHAR(50)    -- 动作编码
operator        VARCHAR(50)    -- 操作人
created_at      DATETIME       -- 创建时间
```

通过这些字段可以在跨模块的表中进行全链路追溯查询：

```sql
-- 从批号查出全链路追溯
SELECT * FROM mes_action_record WHERE batch_no = 'B20260712-001'
UNION ALL
SELECT ... FROM qms_inspection_record WHERE batch_no = 'B20260712-001'
UNION ALL
SELECT ... FROM lims_weighing_item WHERE batch_no = 'B20260712-001'
ORDER BY created_at;
```

## 5. 模块间数据依赖关系

```
ERP(物料主数据) ──→ PLM: 物料编码供BOM引用
                ──→ LIMS: 物料编码供配方引用

PLM(工艺路线)   ──→ MES: Blueprint → 工单工序模板
                ──→ LIMS: 配方关联的工艺版本

LIMS(配方)      ──→ MES: 称量任务 → 工单投料

MES(工单/工序)  ──→ QMS: 触发检验
                ──→ Equip: 申请设备资源
                ──→ IoT: 设备启停指令
                ──→ ERP: 物料消耗/成品入库回传

QMS(检验/偏差)  ──→ MES: 放行/中断/返工指令
                ──→ LIMS: 批记录中的质量部分

IoT(设备数据)   ──→ Equip: 实时参数更新
                ──→ QMS: SPC数据输入

Equip(设备)     ──→ IoT: 设定值下发
                ──→ MES: 设备可用性
```

## 6. 快照策略

执行时刻的资源状态以 JSON 格式存储，确保即使后续资源定义变更，历史记录仍然可追溯：

```json
// mes_action_record.input_snapshot
[
  {"name": "阿莫西林API", "group": "Material", "type": "Other", "number": 500.0},
  {"name": "淀粉", "group": "Material", "type": "Other", "number": 4500.0}
]

// mes_action_record.output_snapshot
[
  {"name": "湿颗粒", "group": "Material", "type": "Other", "number": 5000.0}
]
```
