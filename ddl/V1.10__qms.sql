-- ============================================================
-- V1.10__qms.sql — QMS 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: InspectionOrder, InspectionPlan, InspectionRecord, Deviation, Capa
-- ============================================================

-- 检验方案主表
CREATE TABLE IF NOT EXISTS qms_inspection_plan (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    product_code    VARCHAR(100) NOT NULL,
    process_code    VARCHAR(100),
    inspection_type VARCHAR(10) NOT NULL,
    aql             DOUBLE PRECISION,
    sample_size     INT,
    standard        VARCHAR(100),
    version         VARCHAR(20) NOT NULL DEFAULT '1.0',
    approved_by     VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_qplan_product ON qms_inspection_plan(product_code);
CREATE INDEX idx_qplan_process ON qms_inspection_plan(product_code, process_code);

-- 检验项目（InspectionPlan.InspectionItem @ElementCollection）
CREATE TABLE IF NOT EXISTS qms_inspection_item (
    plan_id         BIGINT NOT NULL REFERENCES qms_inspection_plan(id),
    item_code       VARCHAR(100) NOT NULL,
    item_name       VARCHAR(255) NOT NULL,
    spec_type       VARCHAR(20),
    usl             NUMERIC(20,6),
    lsl             NUMERIC(20,6),
    target          NUMERIC(20,6),
    unit            VARCHAR(20),
    method          VARCHAR(255),
    sampling        VARCHAR(255),
    sort_order      INT DEFAULT 0,
    critical        BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_qitem_plan ON qms_inspection_item(plan_id);

-- 检验指令主表
CREATE TABLE IF NOT EXISTS qms_inspection_order (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    inspection_no   VARCHAR(100) NOT NULL UNIQUE,
    work_order_no   VARCHAR(100),
    batch_no        VARCHAR(100),
    process_code    VARCHAR(100) NOT NULL,
    inspection_type VARCHAR(20),
    plan_code       VARCHAR(100),
    inspector       VARCHAR(100),
    total_items     INT DEFAULT 0,
    completed_items INT DEFAULT 0,
    passed_items    INT DEFAULT 0,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_qorder_no ON qms_inspection_order(inspection_no);
CREATE INDEX idx_qorder_wo ON qms_inspection_order(work_order_no);
CREATE INDEX idx_qorder_status ON qms_inspection_order(status);

-- 检验记录
CREATE TABLE IF NOT EXISTS qms_inspection_record (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    inspection_no   VARCHAR(100) NOT NULL,
    plan_code       VARCHAR(100),
    item_code       VARCHAR(100) NOT NULL,
    item_name       VARCHAR(255) NOT NULL,
    usl             NUMERIC(20,6),
    lsl             NUMERIC(20,6),
    target          NUMERIC(20,6),
    unit            VARCHAR(20),
    measured_value  NUMERIC(20,6),
    judgement       VARCHAR(20),
    defect_code     VARCHAR(50),
    gauge_code      VARCHAR(100),
    inspector       VARCHAR(100),
    remark          VARCHAR(500),
    inspected_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_qrec_order ON qms_inspection_record(inspection_no);

-- 偏差主表
CREATE TABLE IF NOT EXISTS qms_deviation (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    source          VARCHAR(100) NOT NULL,
    inspection_no   VARCHAR(100),
    work_order_no   VARCHAR(100),
    batch_no        VARCHAR(100),
    process_code    VARCHAR(100),
    severity        VARCHAR(20) NOT NULL,
    product_impact  VARCHAR(1000),
    disposition     VARCHAR(20),
    disposition_note VARCHAR(1000),
    capa_code       VARCHAR(100),
    investigator    VARCHAR(100),
    disposed_by     VARCHAR(100),
    root_cause      VARCHAR(2000),
    closed_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_qdev_inspection ON qms_deviation(inspection_no);
CREATE INDEX idx_qdev_wo ON qms_deviation(work_order_no);
CREATE INDEX idx_qdev_status ON qms_deviation(status);

-- CAPA 主表
CREATE TABLE IF NOT EXISTS qms_capa (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(100) NOT NULL UNIQUE,
    name                VARCHAR(255) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    deviation_code      VARCHAR(100) NOT NULL,
    problem_description VARCHAR(2000) NOT NULL,
    root_cause          VARCHAR(2000),
    corrective_action   VARCHAR(2000),
    preventive_action   VARCHAR(2000),
    verification        VARCHAR(2000),
    assign_to           VARCHAR(100) NOT NULL,
    approved_by         VARCHAR(100),
    due_date            TIMESTAMPTZ,
    closed_at           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_qcapa_dev ON qms_capa(deviation_code);
CREATE INDEX idx_qcapa_assign ON qms_capa(assign_to);
CREATE INDEX idx_qcapa_status ON qms_capa(status);
