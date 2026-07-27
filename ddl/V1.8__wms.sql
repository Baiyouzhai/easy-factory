-- ============================================================
-- V1.8__wms.sql — WMS 模块建表
-- 数据库: PostgreSQL 16
-- 基于模型: Storage, Receipt(+ReceiptItem), PickingTask(+PickingTaskItem), InventorySnapshot
-- ============================================================

-- 库位
CREATE TABLE IF NOT EXISTS wms_storage (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    warehouse       VARCHAR(100) NOT NULL,
    zone            VARCHAR(100) NOT NULL,
    rack            VARCHAR(20),
    level           VARCHAR(20),
    position        VARCHAR(20),
    storage_type    VARCHAR(20) NOT NULL DEFAULT 'AMBIENT',
    capacity        NUMERIC(20,4) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_wms_storage_wh ON wms_storage(warehouse);
CREATE INDEX idx_wms_storage_zone ON wms_storage(warehouse, zone);

-- ============================================================
-- 收货单
CREATE TABLE IF NOT EXISTS wms_receipt (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    source_type     VARCHAR(30),
    reference_no    VARCHAR(100) NOT NULL,
    supplier_code   VARCHAR(100),
    received_by     VARCHAR(100),
    received_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_wms_receipt_status ON wms_receipt(status);
CREATE INDEX idx_wms_receipt_ref ON wms_receipt(reference_no);
CREATE INDEX idx_wms_receipt_supplier ON wms_receipt(supplier_code);

-- 收货明细
CREATE TABLE IF NOT EXISTS wms_receipt_item (
    id              BIGSERIAL PRIMARY KEY,
    receipt_id      BIGINT NOT NULL REFERENCES wms_receipt(id),
    material_code   VARCHAR(100) NOT NULL,
    batch_no        VARCHAR(100),
    received_qty    NUMERIC(20,4) NOT NULL DEFAULT 0,
    ordered_qty     NUMERIC(20,4) NOT NULL DEFAULT 0,
    location_code   VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'QUARANTINE'
);
CREATE INDEX idx_wms_ri_receipt ON wms_receipt_item(receipt_id);
CREATE INDEX idx_wms_ri_material ON wms_receipt_item(material_code);

-- ============================================================
-- 拣料任务
CREATE TABLE IF NOT EXISTS wms_picking_task (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    work_order_no   VARCHAR(100) NOT NULL,
    batch_no        VARCHAR(100),
    picking_type    VARCHAR(20) NOT NULL DEFAULT 'FULL',
    picked_by       VARCHAR(100),
    delivered_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_wms_pt_status ON wms_picking_task(status);
CREATE INDEX idx_wms_pt_wo ON wms_picking_task(work_order_no);

-- 拣料明细
CREATE TABLE IF NOT EXISTS wms_picking_task_item (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL REFERENCES wms_picking_task(id),
    material_code   VARCHAR(100) NOT NULL,
    required_qty    NUMERIC(20,4) NOT NULL DEFAULT 0,
    picked_qty      NUMERIC(20,4) NOT NULL DEFAULT 0,
    batch_no        VARCHAR(100),
    location_code   VARCHAR(100)
);
CREATE INDEX idx_wms_pti_task ON wms_picking_task_item(task_id);
CREATE INDEX idx_wms_pti_material ON wms_picking_task_item(material_code);

-- ============================================================
-- 库存快照
CREATE TABLE IF NOT EXISTS wms_inventory_snapshot (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(300) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    material_code   VARCHAR(100) NOT NULL,
    batch_no        VARCHAR(100) NOT NULL,
    location_code   VARCHAR(100) NOT NULL,
    on_hand_qty     NUMERIC(20,4) NOT NULL DEFAULT 0,
    allocated_qty   NUMERIC(20,4) NOT NULL DEFAULT 0,
    quarantine_qty  NUMERIC(20,4) NOT NULL DEFAULT 0,
    rejected_qty    NUMERIC(20,4) NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'QUARANTINE',
    expiry_date     TIMESTAMPTZ,
    last_counted    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_wms_inv_material ON wms_inventory_snapshot(material_code, batch_no);
CREATE INDEX idx_wms_inv_location ON wms_inventory_snapshot(material_code, batch_no, location_code);
CREATE INDEX idx_wms_inv_status ON wms_inventory_snapshot(status);
