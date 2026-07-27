-- ============================================================
-- V1.7__lims.sql — LIMS 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: Formula, WeighingTask, WeighingItem, BatchRecord
-- ============================================================

-- 配方主表
CREATE TABLE IF NOT EXISTS lims_formula (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    product_code    VARCHAR(100) NOT NULL,
    version         VARCHAR(20) NOT NULL DEFAULT '0.1.0',
    batch_size      NUMERIC(20,6),
    approved_by     VARCHAR(100),
    yield           VARCHAR(50),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_formula_product ON lims_formula(product_code);
CREATE INDEX idx_formula_status ON lims_formula(status);

-- 投料阶段（FormulaPhase @ElementCollection）
CREATE TABLE IF NOT EXISTS lims_formula_phase (
    formula_id      BIGINT NOT NULL REFERENCES lims_formula(id),
    phase_no        INT NOT NULL,
    phase_name      VARCHAR(100) NOT NULL,
    conditions      VARCHAR(1000)
);
CREATE INDEX idx_fphase_fid ON lims_formula_phase(formula_id);

-- 阶段内动作（FormulaPhase.actions @ElementCollection）
CREATE TABLE IF NOT EXISTS lims_formula_phase_actions (
    formula_id      BIGINT NOT NULL REFERENCES lims_formula(id),
    action          VARCHAR(255)
);
CREATE INDEX idx_fpactions_fid ON lims_formula_phase_actions(formula_id);

-- ============================================================
-- 称量任务主表
CREATE TABLE IF NOT EXISTS lims_weighing_task (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    formula_code    VARCHAR(100) NOT NULL,
    work_order_id   VARCHAR(100),
    batch_no        VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_wtask_formula ON lims_weighing_task(formula_code);
CREATE INDEX idx_wtask_wo ON lims_weighing_task(work_order_id);

-- 称量明细
CREATE TABLE IF NOT EXISTS lims_weighing_item (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT REFERENCES lims_weighing_task(id),
    material_code   VARCHAR(100) NOT NULL,
    formula_qty     NUMERIC(20,6),
    actual_qty      NUMERIC(20,6),
    tolerance       NUMERIC(10,4),
    balance         VARCHAR(50),
    operator        VARCHAR(100),
    verifier        VARCHAR(100),
    weighed_at      TIMESTAMPTZ
);
CREATE INDEX idx_witem_task ON lims_weighing_item(task_id);

-- ============================================================
-- 批记录主表
CREATE TABLE IF NOT EXISTS lims_batch_record (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    batch_no        VARCHAR(100) NOT NULL UNIQUE,
    work_order_id   VARCHAR(100),
    formula_code    VARCHAR(100),
    formula_version VARCHAR(20),
    product_code    VARCHAR(100),
    batch_size      NUMERIC(20,6),
    yield           NUMERIC(10,4),
    reviewed_by     VARCHAR(100),
    reviewed_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_brecord_batch ON lims_batch_record(batch_no);
CREATE INDEX idx_brecord_wo ON lims_batch_record(work_order_id);
CREATE INDEX idx_brecord_status ON lims_batch_record(status);

-- 工序执行记录关联
CREATE TABLE IF NOT EXISTS lims_batch_process_record (
    batch_id        BIGINT NOT NULL REFERENCES lims_batch_record(id),
    record_ref      VARCHAR(255)
);
CREATE INDEX idx_bpr_bid ON lims_batch_process_record(batch_id);

-- 称量任务关联
CREATE TABLE IF NOT EXISTS lims_batch_weighing_task (
    batch_id        BIGINT NOT NULL REFERENCES lims_batch_record(id),
    task_code       VARCHAR(100)
);
CREATE INDEX idx_bwt_bid ON lims_batch_weighing_task(batch_id);

-- 检验记录关联
CREATE TABLE IF NOT EXISTS lims_batch_inspection (
    batch_id        BIGINT NOT NULL REFERENCES lims_batch_record(id),
    inspection_ref  VARCHAR(255)
);
CREATE INDEX idx_bi_bid ON lims_batch_inspection(batch_id);

-- 偏差记录关联
CREATE TABLE IF NOT EXISTS lims_batch_deviation (
    batch_id        BIGINT NOT NULL REFERENCES lims_batch_record(id),
    deviation_ref   VARCHAR(255)
);
CREATE INDEX idx_bd_bid ON lims_batch_deviation(batch_id);
