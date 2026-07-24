package com.byz.factory.web.controller.wms;

import com.byz.factory.wms.model.InventorySnapshot;
import com.byz.factory.wms.model.PickingTask;
import com.byz.factory.wms.model.Receipt;
import com.byz.factory.wms.service.WmsService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 仓储管理 REST 控制器。
 * <p>
 * 提供收货管理、拣料管理、库存查询和盘点管理 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/wms")
@Tag(name = "WMS — 仓储管理", description = "收货、拣料、库存、盘点")
public class WmsController {

    @Autowired(required = false)
    private WmsService wmsService;

    // ==================== 收货管理 ====================

    @PostMapping("/receipts")
    @Operation(summary = "创建收货单", description = "初始状态 PENDING")
    public Result<Receipt> createReceipt(@RequestParam String referenceNo,
                                          @RequestParam String supplierCode) {
        return Result.ok(wmsService.createReceipt(referenceNo, supplierCode));
    }

    @PutMapping("/receipts/{receiptNo}/accept-putaway")
    @Operation(summary = "验收上架")
    public Result<Void> acceptAndPutaway(@PathVariable String receiptNo,
                                          @RequestParam String locationCode) {
        wmsService.acceptAndPutaway(receiptNo, locationCode);
        return Result.ok();
    }

    @PutMapping("/receipts/{receiptNo}/complete")
    @Operation(summary = "完成收货", description = "PARTIAL → COMPLETED")
    public Result<Void> completeReceipt(@PathVariable String receiptNo) {
        wmsService.completeReceipt(receiptNo);
        return Result.ok();
    }

    @PutMapping("/receipts/{receiptNo}/close")
    @Operation(summary = "关闭收货单", description = "COMPLETED → CLOSED")
    public Result<Void> closeReceipt(@PathVariable String receiptNo) {
        wmsService.closeReceipt(receiptNo);
        return Result.ok();
    }

    @GetMapping("/receipts/{receiptNo}")
    @Operation(summary = "查询收货单")
    public Result<Receipt> findReceipt(@PathVariable String receiptNo) {
        return Result.ok(wmsService.findReceipt(receiptNo));
    }

    // ==================== 拣料管理 ====================

    @PostMapping("/picking-tasks")
    @Operation(summary = "创建拣料任务", description = "初始状态 PENDING")
    public Result<PickingTask> createPickingTask(@RequestParam String workOrderNo,
                                                   @RequestParam String batchNo) {
        return Result.ok(wmsService.createPickingTask(workOrderNo, batchNo));
    }

    @PutMapping("/picking-tasks/{taskCode}/start")
    @Operation(summary = "开始拣料", description = "PENDING → IN_PROGRESS")
    public Result<Void> startPicking(@PathVariable String taskCode) {
        wmsService.startPicking(taskCode);
        return Result.ok();
    }

    @PutMapping("/picking-tasks/{taskCode}/complete")
    @Operation(summary = "拣料完成", description = "IN_PROGRESS → PICKED")
    public Result<Void> completePicking(@PathVariable String taskCode) {
        wmsService.completePicking(taskCode);
        return Result.ok();
    }

    @PutMapping("/picking-tasks/{taskCode}/deliver")
    @Operation(summary = "送达线边仓", description = "PICKED → DELIVERED")
    public Result<Void> deliverPicking(@PathVariable String taskCode) {
        wmsService.deliverPicking(taskCode);
        return Result.ok();
    }

    @PutMapping("/picking-tasks/{taskCode}/cancel")
    @Operation(summary = "取消拣料任务")
    public Result<Void> cancelPicking(@PathVariable String taskCode) {
        wmsService.cancelPicking(taskCode);
        return Result.ok();
    }

    @GetMapping("/picking-tasks/{taskCode}")
    @Operation(summary = "查询拣料任务")
    public Result<PickingTask> findPickingTask(@PathVariable String taskCode) {
        return Result.ok(wmsService.findPickingTask(taskCode));
    }

    // ==================== 库存管理 ====================

    @GetMapping("/inventory/available")
    @Operation(summary = "查询物料可用库存")
    public Result<BigDecimal> queryAvailableStock(@RequestParam String materialCode,
                                                    @RequestParam String batchNo) {
        return Result.ok(wmsService.queryAvailableStock(materialCode, batchNo));
    }

    @GetMapping("/inventory/snapshot")
    @Operation(summary = "获取指定库位的库存快照")
    public Result<InventorySnapshot> getInventorySnapshot(@RequestParam String materialCode,
                                                            @RequestParam String batchNo,
                                                            @RequestParam String locationCode) {
        return Result.ok(wmsService.getInventorySnapshot(materialCode, batchNo, locationCode));
    }

    @GetMapping("/inventory/snapshots")
    @Operation(summary = "获取物料在所有库位的库存快照列表")
    public Result<List<InventorySnapshot>> getInventorySnapshots(@RequestParam String materialCode,
                                                                   @RequestParam String batchNo) {
        return Result.ok(wmsService.getInventorySnapshots(materialCode, batchNo));
    }

    @PutMapping("/inventory/allocate")
    @Operation(summary = "分配库存（工单预留）")
    public Result<Void> allocateStock(@RequestParam String materialCode,
                                       @RequestParam String batchNo,
                                       @RequestParam String locationCode,
                                       @RequestParam BigDecimal qty) {
        wmsService.allocateStock(materialCode, batchNo, locationCode, qty);
        return Result.ok();
    }

    @PutMapping("/inventory/deallocate")
    @Operation(summary = "释放已分配库存（取消预留）")
    public Result<Void> deallocateStock(@RequestParam String materialCode,
                                         @RequestParam String batchNo,
                                         @RequestParam String locationCode,
                                         @RequestParam BigDecimal qty) {
        wmsService.deallocateStock(materialCode, batchNo, locationCode, qty);
        return Result.ok();
    }

    // ==================== 盘点管理 ====================

    @PostMapping("/count-tasks")
    @Operation(summary = "创建盘点任务")
    public Result<Void> createCountTask(@RequestParam String locationCode) {
        wmsService.createCountTask(locationCode);
        return Result.ok();
    }

}
