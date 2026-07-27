-- ============================================================
-- V1.4__plm.sql — PLM 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: ProcessTemplate, Blueprint, ProcessParameter, ChangeRequest
-- ============================================================

-- ── 工艺模板 ──

CREATE TABLE IF NOT EXISTS plm_process_template (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(50),
    version         VARCHAR(30) NOT NULL DEFAULT '0.1.0',
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_plm_template_status ON plm_process_template(status);
CREATE INDEX idx_plm_template_category ON plm_process_template(category);

-- ── 蓝图 ──

CREATE TABLE IF NOT EXISTS plm_blueprint (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    product_code    VARCHAR(100) NOT NULL,
    version         VARCHAR(30) NOT NULL DEFAULT '0.1.0',
    description     TEXT,
    author          VARCHAR(100),
    approved_by     VARCHAR(100),
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_plm_blueprint_product ON plm_blueprint(product_code);
CREATE INDEX idx_plm_blueprint_status ON plm_blueprint(status);
CREATE INDEX idx_plm_blueprint_version ON plm_blueprint(product_code, version);

-- ── 工艺参数 ──

CREATE TABLE IF NOT EXISTS plm_process_parameter (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    uom             VARCHAR(20),
    target_value    NUMERIC(20,6),
    lower_limit     NUMERIC(20,6),
    upper_limit     NUMERIC(20,6),
    data_type       VARCHAR(20) NOT NULL DEFAULT 'NUMERIC',
    control_method  VARCHAR(20) NOT NULL DEFAULT 'AUTO',
    importance      VARCHAR(20) NOT NULL DEFAULT 'IMPORTANT',
    default_value   VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_plm_param_importance ON plm_process_parameter(importance);

-- ── 变更请求 ──

CREATE TABLE IF NOT EXISTS plm_change_request (
    id                    BIGSERIAL PRIMARY KEY,
    code                  VARCHAR(100) NOT NULL UNIQUE,
    name                  VARCHAR(255),
    blueprint_code        VARCHAR(100) NOT NULL,
    from_version          VARCHAR(30) NOT NULL,
    to_version            VARCHAR(30),
    change_reason         VARCHAR(255) NOT NULL,
    description           TEXT,
    affected_dimensions   VARCHAR(100),
    requested_by          VARCHAR(100),
    approved_by           VARCHAR(100),
    status                VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_plm_cr_blueprint ON plm_change_request(blueprint_code);
CREATE INDEX idx_plm_cr_status ON plm_change_request(status);
