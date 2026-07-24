package com.byz.factory.web.controller.erp;

import com.byz.factory.erp.TransactionType;
import com.byz.factory.erp.model.InventorySnapshot;
import com.byz.factory.erp.model.MaterialCache;
import com.byz.factory.erp.model.Transaction;
import com.byz.factory.erp.service.ErpAdapterService;
import com.byz.factory.web.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * ERP 适配 REST 控制器。
 * <p>
 * 提供物料主数据同步、库存查询、事务回传和事务队列管理 API。
 *
 * @author 苏政
 */
@RestController
@RequestMapping("/api/erp")
@Tag(name = "ERP — 资源计划集成", description = "物料同步、库存查询、事务回传")
public class ErpController {

    @Autowired(required = false)
    private ErpAdapterService erpAdapterService;

    // ==================== 物料主数据 ====================

    @PostMapping("/materials/sync")
    @Operation(summary = "全量同步物料主数据")
    public Result<Void> syncMaterials() {
        erpAdapterService.syncMaterials();
        return Result.ok();
    }

    @PostMapping("/materials/sync-incremental")
    @Operation(summary = "增量同步物料主数据")
    public Result<Void> syncMaterialsIncremental() {
        erpAdapterService.syncMaterialsIncremental();
        return Result.ok();
    }

    @GetMapping("/materials/{materialCode}")
    @Operation(summary = "根据物料编码查找本地缓存")
    public Result<MaterialCache> findByCode(@PathVariable String materialCode) {
        return Result.ok(erpAdapterService.findByCode(materialCode));
    }

    @GetMapping("/materials/codes")
    @Operation(summary = "获取所有缓存的物料编码")
    public Result<List<String>> getAllMaterialCodes() {
        return Result.ok(erpAdapterService.getAllMaterialCodes());
    }

    // ==================== 库存查询 ====================

    @GetMapping("/inventory/query")
    @Operation(summary = "按工厂+库位查询库存快照")
    public Result<InventorySnapshot> queryStock(@RequestParam String materialCode,
                                                  @RequestParam String plantCode,
                                                  @RequestParam String storageLocation) {
        return Result.ok(erpAdapterService.queryStock(materialCode, plantCode, storageLocation));
    }

    @GetMapping("/inventory/query-summary")
    @Operation(summary = "按工厂汇总查询库存")
    public Result<BigDecimal> queryStockSummary(@RequestParam String materialCode,
                                                  @RequestParam String plantCode) {
        return Result.ok(erpAdapterService.queryStock(materialCode, plantCode));
    }

    @PostMapping("/inventory/query-batch")
    @Operation(summary = "批量查询库存快照")
    public Result<List<InventorySnapshot>> queryStockBatch(@RequestBody List<String> materialCodes,
                                                             @RequestParam String plantCode) {
        return Result.ok(erpAdapterService.queryStockBatch(materialCodes, plantCode));
    }

    // ==================== 事务回传 ====================

    @PostMapping("/transactions/goods-issue")
    @Operation(summary = "物料消耗回传 ERP")
    public Result<String> postGoodsIssue(@RequestParam String materialCode,
                                          @RequestParam BigDecimal quantity,
                                          @RequestParam String batchNo,
                                          @RequestParam String referenceDoc) {
        return Result.ok(erpAdapterService.postGoodsIssue(materialCode, quantity, batchNo, referenceDoc));
    }

    @PostMapping("/transactions/goods-receipt")
    @Operation(summary = "成品入库回传 ERP")
    public Result<String> postGoodsReceipt(@RequestParam String materialCode,
                                            @RequestParam BigDecimal quantity,
                                            @RequestParam String batchNo,
                                            @RequestParam String referenceDoc) {
        return Result.ok(erpAdapterService.postGoodsReceipt(materialCode, quantity, batchNo, referenceDoc));
    }

    @PostMapping("/transactions/transfer")
    @Operation(summary = "库存转储回传 ERP")
    public Result<String> postTransfer(@RequestParam String materialCode,
                                        @RequestParam BigDecimal quantity,
                                        @RequestParam String batchNo,
                                        @RequestParam String fromLocation,
                                        @RequestParam String toLocation,
                                        @RequestParam String referenceDoc) {
        return Result.ok(erpAdapterService.postTransfer(materialCode, quantity, batchNo, fromLocation, toLocation, referenceDoc));
    }

    // ==================== 事务队列管理 ====================

    @PostMapping("/transactions")
    @Operation(summary = "创建事务回传记录")
    public Result<Transaction> createTransaction(@RequestParam TransactionType type,
                                                   @RequestParam String materialCode,
                                                   @RequestParam BigDecimal quantity,
                                                   @RequestParam String batchNo,
                                                   @RequestParam String referenceDoc) {
        return Result.ok(erpAdapterService.createTransaction(type, materialCode, quantity, batchNo, referenceDoc));
    }

    @GetMapping("/transactions/pending")
    @Operation(summary = "获取所有待发送事务")
    public Result<List<Transaction>> getPendingTransactions() {
        return Result.ok(erpAdapterService.getPendingTransactions());
    }

    @PostMapping("/transactions/retry-all")
    @Operation(summary = "重试所有失败事务")
    public Result<List<Transaction>> retryFailedTransactions() {
        return Result.ok(erpAdapterService.retryFailedTransactions());
    }

    @PutMapping("/transactions/{transactionCode}/retry")
    @Operation(summary = "重试指定事务")
    public Result<Transaction> retryTransaction(@PathVariable String transactionCode) {
        return Result.ok(erpAdapterService.retryTransaction(transactionCode));
    }

    @PutMapping("/transactions/{transactionCode}/cancel")
    @Operation(summary = "取消指定事务")
    public Result<Transaction> cancelTransaction(@PathVariable String transactionCode) {
        return Result.ok(erpAdapterService.cancelTransaction(transactionCode));
    }

}
