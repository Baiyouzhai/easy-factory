package com.byz.factory.wms.model;

import com.byz.factory.wms.IInventorySnapshot;
import com.byz.factory.wms.MaterialStatus;
import com.byz.factory.shared.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 库存快照 — 继承 BaseEntity 获得 code/name/audit，实现 IInventorySnapshot 供跨模块引用。
 * <p>
 * 按 物料+批次+库位 三维度记录库存状态。可用库存 = 在手 − 已分配，由 getter 动态计算。
 *
 * <h3>典型操作</h3>
 * <pre>
 * 入库: addStock(qty)
 * 出库: removeStock(qty)
 * 分配（工单预留）: allocate(qty)
 * 释放（取消预留）: deallocate(qty)
 * 来料待检: quarantine(qty)
 * 验收放行: release(qty)
 * 拒收: reject(qty)
 * 盘点: count(qty, countedAt)
 * </pre>
 *
 * <h3>IExpand 约定</h3>
 * <pre>
 *   wms.materialCode    — 物料编码
 *   wms.batchNo         — 批次号
 *   wms.locationCode    — 库位编码
 *   wms.expiryDate      — 有效期至
 *   wms.lastCounted     — 最后盘点时间
 *   wms.status          — 物料质量状态
 * </pre>
 *
 * @author 苏政
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InventorySnapshot extends BaseEntity implements IInventorySnapshot {

    /** 物料编码 */
    private String materialCode;

    /** 批次号 */
    private String batchNo;

    /** 库位编码 */
    private String locationCode;

    /** 在手库存 */
    private BigDecimal onHandQty;

    /** 已分配（被工单预留） */
    private BigDecimal allocatedQty;

    /** 待检库存 */
    private BigDecimal quarantineQty;

    /** 不合格库存 */
    private BigDecimal rejectedQty;

    /** 有效期至 */
    private Instant expiryDate;

    /** 最后盘点时间 */
    private Instant lastCounted;

    /** 物料质量状态 */
    private MaterialStatus status;

    /**
     * @param materialCode 物料编码
     * @param batchNo      批次号
     * @param locationCode 库位编码
     */
    public InventorySnapshot(String materialCode, String batchNo, String locationCode) {
        super(materialCode + "-" + batchNo + "-" + locationCode,
                "库存-" + materialCode);
        this.materialCode = materialCode;
        this.batchNo = batchNo;
        this.locationCode = locationCode;
        this.onHandQty = BigDecimal.ZERO;
        this.allocatedQty = BigDecimal.ZERO;
        this.quarantineQty = BigDecimal.ZERO;
        this.rejectedQty = BigDecimal.ZERO;
        this.status = MaterialStatus.QUARANTINE;
    }

    // ==================== IInventorySnapshot 实现 ====================

    @Override
    public BigDecimal getAvailableQty() {
        if (onHandQty == null || allocatedQty == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal available = onHandQty.subtract(allocatedQty);
        return available.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available;
    }

    // ==================== 业务方法 ====================

    /**
     * 添加入库库存。
     *
     * @param qty 入库数量
     * @throws IllegalArgumentException 如果数量为负
     */
    public void addStock(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("入库数量不能为负: " + qty);
        }
        this.onHandQty = this.onHandQty.add(qty);
        markUpdated();
    }

    /**
     * 扣除出库库存。
     *
     * @param qty 出库数量
     * @throws IllegalArgumentException 如果数量超过在手库存
     */
    public void removeStock(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("出库数量不能为负: " + qty);
        }
        if (qty.compareTo(getAvailableQty()) > 0) {
            throw new IllegalArgumentException("出库数量超过可用库存: 可用=" + getAvailableQty() + ", 请求=" + qty);
        }
        this.onHandQty = this.onHandQty.subtract(qty);
        markUpdated();
    }

    /**
     * 分配库存（工单预留）。
     *
     * @param qty 分配数量
     * @throws IllegalArgumentException 如果数量超过可用库存
     */
    public void allocate(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("分配数量不能为负: " + qty);
        }
        if (qty.compareTo(getAvailableQty()) > 0) {
            throw new IllegalArgumentException("分配数量超过可用库存: 可用=" + getAvailableQty() + ", 请求=" + qty);
        }
        this.allocatedQty = this.allocatedQty.add(qty);
        markUpdated();
    }

    /**
     * 释放已分配库存（取消预留）。
     *
     * @param qty 释放数量
     * @throws IllegalArgumentException 如果数量超过已分配数量
     */
    public void deallocate(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("释放数量不能为负: " + qty);
        }
        if (qty.compareTo(this.allocatedQty) > 0) {
            throw new IllegalArgumentException("释放数量超过已分配数量: 已分配=" + this.allocatedQty + ", 请求=" + qty);
        }
        this.allocatedQty = this.allocatedQty.subtract(qty);
        markUpdated();
    }

    /**
     * 转入待检——来料入库后在隔离区等待 QMS 检验。
     *
     * @param qty 待检数量
     */
    public void quarantine(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("待检数量不能为负: " + qty);
        }
        this.quarantineQty = this.quarantineQty.add(qty);
        this.status = MaterialStatus.QUARANTINE;
        markUpdated();
    }

    /**
     * 验收放行——QMS 来料检合格。
     *
     * @param qty 放行数量
     */
    public void release(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("放行数量不能为负: " + qty);
        }
        if (qty.compareTo(this.quarantineQty) > 0) {
            throw new IllegalArgumentException("放行数量超过待检数量: 待检=" + this.quarantineQty + ", 请求=" + qty);
        }
        this.quarantineQty = this.quarantineQty.subtract(qty);
        this.onHandQty = this.onHandQty.add(qty);
        this.status = MaterialStatus.RELEASED;
        markUpdated();
    }

    /**
     * 拒收——QMS 来料检不合格，转入不合格库存。
     *
     * @param qty 拒收数量
     */
    public void reject(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("拒收数量不能为负: " + qty);
        }
        if (qty.compareTo(this.quarantineQty) > 0) {
            throw new IllegalArgumentException("拒收数量超过待检数量: 待检=" + this.quarantineQty + ", 请求=" + qty);
        }
        this.quarantineQty = this.quarantineQty.subtract(qty);
        this.rejectedQty = this.rejectedQty.add(qty);
        this.status = MaterialStatus.REJECTED;
        markUpdated();
    }

    /**
     * 盘点——记录盘点结果并更新最后盘点时间。
     *
     * @param qty       盘点确认数量
     * @param countedAt 盘点时间
     */
    public void count(BigDecimal qty, Instant countedAt) {
        if (qty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("盘点数量不能为负: " + qty);
        }
        this.onHandQty = qty;
        this.lastCounted = countedAt;
        markUpdated();
    }

}
