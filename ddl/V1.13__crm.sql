-- ============================================================
-- V1.13__crm.sql — CRM 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: Customer, SalesOrder, SalesOrderItem, Complaint
-- ============================================================

-- ── 客户 ──

CREATE TABLE IF NOT EXISTS crm_customer (
    id                      BIGSERIAL PRIMARY KEY,
    code                    VARCHAR(100) NOT NULL UNIQUE,
    name                    VARCHAR(255) NOT NULL,
    industry                VARCHAR(50),
    region                  VARCHAR(50),
    contacts                TEXT,
    gmp_audit_status        VARCHAR(20) NOT NULL DEFAULT 'NEVER',
    gmp_audit_date          VARCHAR(20),
    on_time_delivery_rate   DOUBLE PRECISION NOT NULL DEFAULT 0,
    quality_complaint_rate  DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_crm_customer_industry ON crm_customer(industry);
CREATE INDEX idx_crm_customer_region ON crm_customer(region);
CREATE INDEX idx_crm_customer_gmp_audit ON crm_customer(gmp_audit_status);

-- ── 销售订单 ──

CREATE TABLE IF NOT EXISTS crm_sales_order (
    id                    BIGSERIAL PRIMARY KEY,
    code                  VARCHAR(100) NOT NULL UNIQUE,
    name                  VARCHAR(255),
    order_no              VARCHAR(100) NOT NULL UNIQUE,
    customer_code         VARCHAR(100) NOT NULL,
    product_code          VARCHAR(100),
    quantity              NUMERIC(20,4) NOT NULL DEFAULT 0,
    unit_price            NUMERIC(20,4),
    required_date         TIMESTAMPTZ,
    committed_date        TIMESTAMPTZ,
    priority              VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    gxp_requirements      TEXT,
    special_instructions  TEXT,
    plan_no               VARCHAR(100),
    status                VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_crm_so_customer ON crm_sales_order(customer_code);
CREATE INDEX idx_crm_so_status ON crm_sales_order(status);
CREATE INDEX idx_crm_so_priority ON crm_sales_order(priority);

CREATE TABLE IF NOT EXISTS crm_sales_order_item (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES crm_sales_order(id) ON DELETE CASCADE,
    product_code    VARCHAR(100) NOT NULL,
    quantity        NUMERIC(20,4) NOT NULL,
    unit_price      NUMERIC(20,4)
);
CREATE INDEX idx_crm_so_item_order ON crm_sales_order_item(order_id);
CREATE INDEX idx_crm_so_item_product ON crm_sales_order_item(product_code);

-- ── 客户投诉 ──

CREATE TABLE IF NOT EXISTS crm_complaint (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255),
    complaint_no    VARCHAR(100) NOT NULL UNIQUE,
    customer_code   VARCHAR(100) NOT NULL,
    order_no        VARCHAR(100),
    batch_no        VARCHAR(100),
    type            VARCHAR(30) NOT NULL,
    description     TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    resolution      TEXT,
    capa_code       VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_crm_complaint_customer ON crm_complaint(customer_code);
CREATE INDEX idx_crm_complaint_order ON crm_complaint(order_no);
CREATE INDEX idx_crm_complaint_status ON crm_complaint(status);
CREATE INDEX idx_crm_complaint_type ON crm_complaint(type);
