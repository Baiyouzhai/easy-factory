-- ============================================================
-- V1.5__iot.sql — IoT 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: DeviceConnection, TagValue, Command, AlarmEvent
-- ============================================================

CREATE TABLE IF NOT EXISTS iot_device_connection (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    protocol        VARCHAR(50) NOT NULL,
    endpoint        VARCHAR(500) NOT NULL,
    poll_interval_ms INT NOT NULL DEFAULT 1000,
    last_connected  TIMESTAMPTZ,
    auth_config     VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_iotconn_code ON iot_device_connection(code);
CREATE INDEX idx_iotconn_protocol ON iot_device_connection(protocol);

-- ============================================================
CREATE TABLE IF NOT EXISTS iot_tag_value (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    equipment_code  VARCHAR(100) NOT NULL,
    tag_name        VARCHAR(200) NOT NULL,
    raw_value       TEXT,
    scaled_value    NUMERIC(20,6),
    quality         VARCHAR(20) NOT NULL DEFAULT 'GOOD',
    ts_timestamp    TIMESTAMPTZ,
    server_timestamp TIMESTAMPTZ,
    batch_id        VARCHAR(100),
    multiplier      NUMERIC(20,6) DEFAULT 1,
    tag_offset      NUMERIC(20,6) DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_itv_eq ON iot_tag_value(equipment_code);
CREATE INDEX idx_itv_eq_tag ON iot_tag_value(equipment_code, tag_name);
CREATE INDEX idx_itv_batch ON iot_tag_value(batch_id);
CREATE INDEX idx_itv_ts ON iot_tag_value(ts_timestamp);

-- ============================================================
CREATE TABLE IF NOT EXISTS iot_command (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    equipment_code  VARCHAR(100) NOT NULL,
    command_type    VARCHAR(20) NOT NULL,
    parameters      TEXT,
    priority        VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    timeout_seconds BIGINT,
    result_code     VARCHAR(50),
    result_message  VARCHAR(500),
    sent_at         TIMESTAMPTZ,
    acknowledged_at TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_icmd_eq ON iot_command(equipment_code);
CREATE INDEX idx_icmd_status ON iot_command(status);

-- ============================================================
CREATE TABLE IF NOT EXISTS iot_alarm_event (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    equipment_code  VARCHAR(100) NOT NULL,
    alarm_code      VARCHAR(100) NOT NULL,
    severity        VARCHAR(20) NOT NULL,
    message         VARCHAR(500),
    trigger_value   NUMERIC(20,6),
    threshold       NUMERIC(20,6),
    triggered_at    TIMESTAMPTZ,
    acknowledged_by VARCHAR(100),
    acknowledged_at TIMESTAMPTZ,
    resolved_at     TIMESTAMPTZ,
    actions         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ialarm_eq ON iot_alarm_event(equipment_code);
CREATE INDEX idx_ialarm_severity ON iot_alarm_event(severity);
CREATE INDEX idx_ialarm_triggered ON iot_alarm_event(triggered_at);
CREATE INDEX idx_ialarm_active ON iot_alarm_event(equipment_code, resolved_at)
    WHERE resolved_at IS NULL;
