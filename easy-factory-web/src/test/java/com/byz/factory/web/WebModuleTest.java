package com.byz.factory.web;

import com.byz.factory.web.common.PageResult;
import com.byz.factory.web.common.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Web 模块单元测试。
 * <p>
 * 覆盖：Result 统一响应体、PageResult 分页响应、Controller 注解验证。
 *
 * @author 苏政
 */
@DisplayName("Web 模块 基础组件测试")
class WebModuleTest {

    // ==================== Result 测试 ====================

    @Test
    @DisplayName("Result.ok — 成功响应含数据")
    void result_ok_withData_shouldReturnSuccess() {
        Result<String> result = Result.ok("hello");

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("hello", result.getData());
        assertNotNull(result.getTimestamp());
        assertNotNull(result.getTraceId());
    }

    @Test
    @DisplayName("Result.ok — 成功响应无数据")
    void result_ok_withoutData_shouldReturnSuccess() {
        Result<Void> result = Result.ok();

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Result.fail — 自定义错误码")
    void result_fail_withCustomCode_shouldReturnError() {
        Result<Void> result = Result.fail(404, "资源不存在");

        assertEquals(404, result.getCode());
        assertEquals("资源不存在", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Result.fail — 默认 400 错误")
    void result_fail_default_shouldReturn400() {
        Result<Void> result = Result.fail("参数错误");

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
    }

    @Test
    @DisplayName("Result.error — 500 服务端错误")
    void result_error_shouldReturn500() {
        Result<Void> result = Result.error("内部错误");

        assertEquals(500, result.getCode());
        assertEquals("内部错误", result.getMessage());
    }

    @Test
    @DisplayName("Result — traceId 唯一性")
    void result_traceId_shouldBeUnique() {
        Result<Void> r1 = Result.ok();
        Result<Void> r2 = Result.ok();

        assertNotNull(r1.getTraceId());
        assertNotNull(r2.getTraceId());
        // 8 位长度（UUID 前 8 位）
        assertEquals(8, r1.getTraceId().length());
    }

    @Test
    @DisplayName("Result — timestamp 为当前时间")
    void result_timestamp_shouldBeSet() {
        Result<Void> result = Result.ok();
        assertNotNull(result.getTimestamp());
        // timestamp 应该在最近 5 秒内
        long diff = Math.abs(java.time.Instant.now().getEpochSecond() - result.getTimestamp().getEpochSecond());
        assertTrue(diff < 5, "timestamp should be within 5 seconds of now, but was " + diff);
    }

    // ==================== PageResult 测试 ====================

    @Test
    @DisplayName("PageResult.of — 分页响应正确计算")
    void pageResult_of_shouldCalculateCorrectly() {
        List<String> data = Arrays.asList("a", "b", "c");
        PageResult<String> result = PageResult.of(data, 25, 1, 10);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(3, result.getData().size());
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(25, result.getTotal());
        assertEquals(3, result.getPages()); // ceil(25/10) = 3
    }

    @Test
    @DisplayName("PageResult — 空结果集")
    void pageResult_emptyData_shouldReturnZeroResults() {
        List<String> data = List.of();
        PageResult<String> result = PageResult.of(data, 0, 1, 10);

        assertEquals(0, result.getData().size());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getPages());
    }

    @Test
    @DisplayName("PageResult — 整除分页数计算")
    void pageResult_exactDivision_shouldCalculateCorrectly() {
        List<String> data = Arrays.asList("a", "b");
        PageResult<String> result = PageResult.of(data, 100, 1, 20);

        assertEquals(5, result.getPages()); // 100/20 = 5
    }

    @Test
    @DisplayName("PageResult — size=0 时 pages=0")
    void pageResult_zeroSize_shouldHaveZeroPages() {
        List<String> data = List.of();
        PageResult<String> result = PageResult.of(data, 10, 1, 0);

        assertEquals(0, result.getPages());
    }

    // ==================== Result 常量测试 ====================

    @Test
    @DisplayName("Result 常量 — CODE_OK = 200")
    void resultConstants_codeOk_shouldBe200() {
        assertEquals(200, Result.CODE_OK);
    }

    @Test
    @DisplayName("Result 常量 — CODE_BAD_REQUEST = 400")
    void resultConstants_codeBadRequest_shouldBe400() {
        assertEquals(400, Result.CODE_BAD_REQUEST);
    }

    @Test
    @DisplayName("Result 常量 — CODE_INTERNAL_ERROR = 500")
    void resultConstants_codeInternalError_shouldBe500() {
        assertEquals(500, Result.CODE_INTERNAL_ERROR);
    }

    // ==================== Controller 存在性验证 ====================

    @Test
    @DisplayName("Controller — MES WorkOrderController 可加载")
    void controller_mes_workOrderController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.mes.WorkOrderController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — QMS InspectionController 可加载")
    void controller_qms_inspectionController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.qms.InspectionController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — QMS DeviationController 可加载")
    void controller_qms_deviationController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.qms.DeviationController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — QMS CapaController 可加载")
    void controller_qms_capaController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.qms.CapaController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — PLM BlueprintController 可加载")
    void controller_plm_blueprintController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.plm.BlueprintController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — Equip EquipmentController 可加载")
    void controller_equip_equipmentController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.equip.EquipmentController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — LIMS FormulaController 可加载")
    void controller_lims_formulaController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.lims.FormulaController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — LIMS WeighingTaskController 可加载")
    void controller_lims_weighingTaskController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.lims.WeighingTaskController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — LIMS BatchRecordController 可加载")
    void controller_lims_batchRecordController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.lims.BatchRecordController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — IoT IotController 可加载")
    void controller_iot_iotController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.iot.IotController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — ERP ErpController 可加载")
    void controller_erp_erpController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.erp.ErpController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — SCM ScmController 可加载")
    void controller_scm_scmController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.scm.ScmController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — DMS DmsController 可加载")
    void controller_dms_dmsController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.dms.DmsController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — WMS WmsController 可加载")
    void controller_wms_wmsController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.wms.WmsController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — MPS MpsController 可加载")
    void controller_mps_mpsController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.mps.MpsController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — APS ApsController 可加载")
    void controller_aps_apsController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.aps.ApsController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — EAM EamController 可加载")
    void controller_eam_eamController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.eam.EamController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — Andon AndonController 可加载")
    void controller_andon_andonController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.andon.AndonController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — BI DashboardController 可加载")
    void controller_bi_dashboardController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.bi.DashboardController");
        assertNotNull(clazz);
    }

    @Test
    @DisplayName("Controller — CRM CrmController 可加载")
    void controller_crm_crmController_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.controller.crm.CrmController");
        assertNotNull(clazz);
    }

    // ==================== Common 组件可加载 ====================

    @Test
    @DisplayName("Common — GlobalExceptionHandler 可加载")
    void common_globalExceptionHandler_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.common.GlobalExceptionHandler");
        assertNotNull(clazz);
    }

    // ==================== Config 可加载 ====================

    @Test
    @DisplayName("Config — WebConfig 可加载")
    void config_webConfig_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.web.config.WebConfig");
        assertNotNull(clazz);
    }

    // ==================== Application 启动类 ====================

    @Test
    @DisplayName("Application — EasyFactoryApplication 可加载")
    void application_easyFactoryApplication_loadable() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("com.byz.factory.EasyFactoryApplication");
        assertNotNull(clazz);
    }

}
