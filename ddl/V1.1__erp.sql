-- ============================================================
-- V1.1__erp.sql — ERP 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: MaterialCache, InventorySnapshot, Transaction
-- ============================================================

CREATE TABLE IF NOT EXISTS erp_material_cache (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    description     VARCHAR(500),
    unit            VARCHAR(20),
    material_type   VARCHAR(10) NOT NULL DEFAULT 'ROH',
    batch_managed   BOOLEAN NOT NULL DEFAULT FALSE,
    shelf_life_days INT NOT NULL DEFAULT 0,
    ghs_class       VARCHAR(50),
    last_sync_time  TIMESTAMPTZ,
    source_system   VARCHAR(50),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS erp_inventory_snapshot (
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(200) NOT NULL UNIQUE,
    name             VARCHAR(300),
    material_code    VARCHAR(100) NOT NULL,
    plant_code       VARCHAR(50) NOT NULL,
    storage_location VARCHAR(50) NOT NULL,
    batch_no         VARCHAR(100),
    unrestricted_qty NUMERIC(20,4) NOT NULL DEFAULT 0,
    inspection_qty   NUMERIC(20,4) NOT NULL DEFAULT 0,
    blocked_qty      NUMERIC(20,4) NOT NULL DEFAULT 0,
    snapshot_time    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source_system    VARCHAR(50),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_erp_inv_mat ON erp_inventory_snapshot(material_code);

CREATE TABLE IF NOT EXISTS erp_transaction (
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(100) NOT NULL UNIQUE,
    name             VARCHAR(200),
    transaction_type VARCHAR(20) NOT NULL,
    material_code    VARCHAR(100) NOT NULL,
    quantity         NUMERIC(20,4) NOT NULL,
    unit             VARCHAR(20),
    batch_no         VARCHAR(100),
    movement_type    VARCHAR(10),
    reference_doc    VARCHAR(100),
    posting_date     DATE,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count      INT NOT NULL DEFAULT 0,
    error_message    TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_erp_tx_mat ON erp_transaction(material_code);
CREATE INDEX idx_erp_tx_status ON erp_transaction(status);
