-- ============================================================
-- V1.6__dms.sql — DMS 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: Document, ApprovalWorkflow, ApprovalStep
-- ============================================================

-- ── 文档 ──

CREATE TABLE IF NOT EXISTS dms_document (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(100) NOT NULL UNIQUE,
    name              VARCHAR(255) NOT NULL,
    document_code     VARCHAR(100) NOT NULL UNIQUE,
    title             VARCHAR(500) NOT NULL,
    category          VARCHAR(20) NOT NULL,
    version           VARCHAR(20) NOT NULL DEFAULT '1',
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    author            VARCHAR(100),
    approved_by       VARCHAR(100),
    effective_date    DATE,
    review_cycle_months INT NOT NULL DEFAULT 0,
    next_review_date  DATE,
    content           TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_dms_doc_code ON dms_document(document_code);
CREATE INDEX idx_dms_doc_category ON dms_document(category);
CREATE INDEX idx_dms_doc_status ON dms_document(status);
CREATE INDEX idx_dms_doc_next_review ON dms_document(next_review_date);

-- ── 审批流 ──

CREATE TABLE IF NOT EXISTS dms_approval_workflow (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(200) NOT NULL UNIQUE,
    name            VARCHAR(300),
    document_code   VARCHAR(100) NOT NULL,
    initiator       VARCHAR(100),
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_dms_wf_doc ON dms_approval_workflow(document_code);

-- ── 审批步骤 ──

CREATE TABLE IF NOT EXISTS dms_approval_step (
    id              BIGSERIAL PRIMARY KEY,
    workflow_id     BIGINT NOT NULL REFERENCES dms_approval_workflow(id) ON DELETE CASCADE,
    step_no         INT NOT NULL,
    approver_role   VARCHAR(100) NOT NULL,
    approver        VARCHAR(100),
    decision        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    comment         VARCHAR(500),
    timestamp       TIMESTAMPTZ
);
CREATE INDEX idx_dms_step_wf ON dms_approval_step(workflow_id);
CREATE INDEX idx_dms_step_wf_no ON dms_approval_step(workflow_id, step_no);
