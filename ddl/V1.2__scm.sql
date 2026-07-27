-- ============================================================
-- V1.2__scm.sql — SCM 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: Supplier, PurchaseOrder, PurchaseOrderItem, InboundPlan
-- ============================================================

-- ── 供应商 ──

CREATE TABLE IF NOT EXISTS scm_supplier (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(50),
    qualification   VARCHAR(30) NOT NULL DEFAULT 'UNDER_REVIEW',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    lead_time_days  INT NOT NULL DEFAULT 0,
    on_time_rate    DOUBLE PRECISION NOT NULL DEFAULT 0,
    quality_rate    DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_scm_supplier_category ON scm_supplier(category);
CREATE INDEX idx_scm_supplier_status ON scm_supplier(status);
CREATE INDEX idx_scm_supplier_qualification ON scm_supplier(qualification);

-- ── 采购订单 ──

CREATE TABLE IF NOT EXISTS scm_purchase_order (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(100) NOT NULL UNIQUE,
    name              VARCHAR(255),
    po_no             VARCHAR(100) NOT NULL UNIQUE,
    supplier_code     VARCHAR(100) NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    approved_by       VARCHAR(100),
    expected_delivery DATE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_scm_po_supplier ON scm_purchase_order(supplier_code);
CREATE INDEX idx_scm_po_status ON scm_purchase_order(status);

CREATE TABLE IF NOT EXISTS scm_purchase_order_item (
    id              BIGSERIAL PRIMARY KEY,
    po_id           BIGINT NOT NULL REFERENCES scm_purchase_order(id) ON DELETE CASCADE,
    material_code   VARCHAR(100) NOT NULL,
    quantity        NUMERIC(20,4) NOT NULL,
    unit_price      NUMERIC(20,4),
    expected_date   DATE,
    received_qty    NUMERIC(20,4) NOT NULL DEFAULT 0
);
CREATE INDEX idx_scm_po_item_po ON scm_purchase_order_item(po_id);
CREATE INDEX idx_scm_po_item_mat ON scm_purchase_order_item(material_code);

-- ── 来料计划 ──

CREATE TABLE IF NOT EXISTS scm_inbound_plan (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(100) NOT NULL UNIQUE,
    name              VARCHAR(255),
    po_no             VARCHAR(100) NOT NULL,
    supplier_code     VARCHAR(100) NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    expected_date     DATE,
    notify_warehouse  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_scm_inbound_po ON scm_inbound_plan(po_no);
CREATE INDEX idx_scm_inbound_supplier ON scm_inbound_plan(supplier_code);
CREATE INDEX idx_scm_inbound_status ON scm_inbound_plan(status);
