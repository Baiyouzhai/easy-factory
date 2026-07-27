package com.byz.factory.plm.service;

import com.byz.factory.factory.BlueprintStatus;
import com.byz.factory.plm.model.Blueprint;
import com.byz.factory.plm.model.ChangeRequest;
import com.byz.factory.plm.model.ProcessParameter;
import com.byz.factory.plm.model.ProcessTemplate;
import com.byz.factory.plm.repository.*;
import com.byz.factory.plm.service.impl.BlueprintServiceImpl;
import com.byz.factory.shared.BumpType;
import com.byz.factory.shared.UOM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({BlueprintServiceImpl.class, BlueprintDifferImpl.class})
@DisplayName("BlueprintServiceImpl 服务层集成测试")
class PlmServiceImplTest {

    @Autowired private BlueprintRepository blueprintRepo;
    @Autowired private ProcessTemplateRepository templateRepo;
    @Autowired private ChangeRequestRepository crRepo;

    @Autowired
    private BlueprintServiceImpl service;

    private ProcessTemplate savedTemplate;

    @BeforeEach
    void setUp() {
        savedTemplate = templateRepo.save(
                new ProcessTemplate("TPL-001", "阿莫西林片剂工艺", "化工"));
        savedTemplate.activate();
        templateRepo.save(savedTemplate);
    }

    // ==================== 蓝图生命周期 ====================

    @Test
    @DisplayName("createBlueprint — 从模板创建，自动生成编码和默认值")
    void createBlueprint_shouldCreateWithDefaults() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-AMX-001");

        assertNotNull(bp.getId(), "创建后应有自增 id");
        assertTrue(bp.getCode().startsWith("BP-PROD-AMX-001-"));
        assertEquals("0.1.0", bp.getVersion());
        assertEquals(BlueprintStatus.DRAFT, bp.getStatus());
    }

    @Test
    @DisplayName("createBlueprint — 模板为 null 应抛异常")
    void createBlueprint_nullTemplate_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createBlueprint(null, "PROD-001"));
    }

    @Test
    @DisplayName("submitForReview — DRAFT → UNDER_REVIEW")
    void submitForReview_shouldTransition() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-SUB");
        Blueprint result = service.submitForReview(bp.getCode());

        assertEquals(BlueprintStatus.UNDER_REVIEW, result.getStatus());
    }

    @Test
    @DisplayName("approve — UNDER_REVIEW → APPROVED，记录审批人")
    void approve_shouldTransitionAndRecordApprover() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-APP");
        service.submitForReview(bp.getCode());

        Blueprint result = service.approve(bp.getCode(), "王审批");

        assertEquals(BlueprintStatus.APPROVED, result.getStatus());
        assertEquals("王审批", result.getApprovedBy());
    }

    @Test
    @DisplayName("reject — UNDER_REVIEW → DRAFT")
    void reject_shouldReturnToDraft() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-REJ");
        service.submitForReview(bp.getCode());

        Blueprint result = service.reject(bp.getCode());

        assertEquals(BlueprintStatus.DRAFT, result.getStatus());
    }

    @Test
    @DisplayName("releaseVersion — APPROVED → RELEASED")
    void releaseVersion_shouldTransitionToReleased() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-REL");
        service.submitForReview(bp.getCode());
        service.approve(bp.getCode(), "赵审批");

        Blueprint result = service.releaseVersion(bp.getCode());

        assertEquals(BlueprintStatus.RELEASED, result.getStatus());
    }

    @Test
    @DisplayName("obsolete — RELEASED → OBSOLETED")
    void obsolete_shouldTransitionToObsoleted() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-OBS");
        service.submitForReview(bp.getCode());
        service.approve(bp.getCode(), "赵审批");
        service.releaseVersion(bp.getCode());

        Blueprint result = service.obsolete(bp.getCode());

        assertEquals(BlueprintStatus.OBSOLETED, result.getStatus());
    }

    @Test
    @DisplayName("完整生命周期 — DRAFT→REVIEW→APPROVED→RELEASED→OBSOLETED")
    void fullLifecycle_shouldCompleteAllStages() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-FULL");

        service.submitForReview(bp.getCode());
        assertEquals(BlueprintStatus.UNDER_REVIEW, service.findBlueprint(bp.getCode()).getStatus());

        service.approve(bp.getCode(), "钱审批");
        assertEquals(BlueprintStatus.APPROVED, service.findBlueprint(bp.getCode()).getStatus());

        service.releaseVersion(bp.getCode());
        assertEquals(BlueprintStatus.RELEASED, service.findBlueprint(bp.getCode()).getStatus());

        service.obsolete(bp.getCode());
        assertEquals(BlueprintStatus.OBSOLETED, service.findBlueprint(bp.getCode()).getStatus());
    }

    @Test
    @DisplayName("非法状态转换 — 抛异常")
    void illegalTransition_shouldThrow() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-ILL");

        // DRAFT 不能直接 release
        assertThrows(IllegalStateException.class,
                () -> service.releaseVersion(bp.getCode()));

        // DRAFT 不能直接 approve
        assertThrows(IllegalStateException.class,
                () -> service.approve(bp.getCode(), "某人"));
    }

    @Test
    @DisplayName("查询不存在的蓝图 — 抛异常")
    void findNonExistent_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findBlueprint("NON-EXISTENT"));
    }

    // ==================== 版本管理 ====================

    @Test
    @DisplayName("bumpVersion — MINOR 递增: 0.1.0 → 0.2.0")
    void bumpVersion_minorBump() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-VER");

        Blueprint result = service.bumpVersion(bp.getCode(), BumpType.MINOR);

        assertEquals("0.2.0", result.getVersion());
    }

    @Test
    @DisplayName("bumpVersion — MAJOR 递增: 0.1.0 → 1.0.0")
    void bumpVersion_majorBump() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-MAJ");
        bp.setVersion("0.1.0");
        blueprintRepo.save(bp);

        Blueprint result = service.bumpVersion(bp.getCode(), BumpType.MAJOR);

        assertEquals("1.0.0", result.getVersion());
    }

    @Test
    @DisplayName("bumpVersion — PATCH 递增: 0.1.0 → 0.1.1")
    void bumpVersion_patchBump() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-PAT");
        bp.setVersion("0.1.0");
        blueprintRepo.save(bp);

        Blueprint result = service.bumpVersion(bp.getCode(), BumpType.PATCH);

        assertEquals("0.1.1", result.getVersion());
    }

    @Test
    @DisplayName("createNewVersion — 基于当前版本创建新版本草稿")
    void createNewVersion_shouldCopyFromCurrent() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-NV");
        bp.setAuthor("张三");
        bp.setDescription("初始版本");
        bp.setVersion("1.0.0");
        blueprintRepo.save(bp);

        Blueprint next = service.createNewVersion(bp.getCode(), "2.0.0", "客户要求工艺变更");

        assertEquals("2.0.0", next.getVersion());
        assertEquals(BlueprintStatus.DRAFT, next.getStatus());
        assertEquals("PROD-NV", next.getProductCode());
        assertEquals("张三", next.getAuthor());
    }

    // ==================== 工艺参数 ====================

    @Test
    @DisplayName("addProcessParameter — 添加并持久化工艺参数")
    void addProcessParameter_shouldSave() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-PARAM");
        ProcessParameter param = new ProcessParameter("PARAM-TEMP", "干燥温度",
                UOM.CELSIUS, new BigDecimal("60"), new BigDecimal("55"),
                new BigDecimal("65"));

        ProcessParameter saved = service.addProcessParameter(
                bp.getCode(), "P-001", param);

        assertNotNull(saved.getId());
        assertEquals("干燥温度", saved.getName());
    }

    @Test
    @DisplayName("updateProcessParameter — 更新工艺参数值")
    void updateProcessParameter_shouldUpdateValues() {
        Blueprint bp = service.createBlueprint(savedTemplate, "PROD-UP");
        ProcessParameter param = new ProcessParameter("PARAM-UP", "温度",
                UOM.CELSIUS, new BigDecimal("60"), new BigDecimal("55"),
                new BigDecimal("65"));
        service.addProcessParameter(bp.getCode(), "P-001", param);

        ProcessParameter updated = service.updateProcessParameter(
                bp.getCode(), "P-001", "PARAM-UP",
                new ProcessParameter("PARAM-UP", "温度",
                        UOM.CELSIUS, new BigDecimal("70"), new BigDecimal("65"),
                        new BigDecimal("75")));

        assertEquals(0, new BigDecimal("70").compareTo(updated.getTargetValue()));
    }

    // ==================== 变更管理 ====================

    @Test
    @DisplayName("createChangeRequest — 创建变更请求")
    void createChangeRequest_shouldCreate() {
        // 先创建蓝图
        Blueprint bp = new Blueprint("BP-TEST-CR", "测试蓝图", "PROD-CR-TEST");
        blueprintRepo.save(bp);

        ChangeRequest cr = service.createChangeRequest(
                "CR-001-CREATE", "BP-TEST-CR", "1.0.0", "客户要求", "工程师甲");

        assertNotNull(cr.getId());
        assertEquals(ChangeRequest.ChangeRequestStatus.DRAFT, cr.getStatus());
        assertEquals("BP-TEST-CR", cr.getBlueprintCode());
        assertEquals("工程师甲", cr.getRequestedBy());
    }

    @Test
    @DisplayName("createChangeRequest — 重复编码抛异常")
    void createChangeRequest_duplicateCode_shouldThrow() {
        // 先创建蓝图
        Blueprint bp = new Blueprint("BP-TEST", "测试蓝图", "PROD-CR");
        blueprintRepo.save(bp);

        service.createChangeRequest("CR-DUP", "BP-TEST", "1.0.0", "变更1", "甲");

        assertThrows(IllegalArgumentException.class,
                () -> service.createChangeRequest("CR-DUP", "BP-TEST", "1.0.0", "变更2", "乙"));
    }

    @Test
    @DisplayName("createChangeRequest — 已有进行中的变更请求时抛异常")
    void createChangeRequest_activeExists_shouldThrow() {
        Blueprint bp = new Blueprint("BP-ACT", "活动蓝图", "PROD-ACT");
        blueprintRepo.save(bp);

        service.createChangeRequest("CR-ACT-1", "BP-ACT", "1.0.0", "变更1", "甲");

        assertThrows(IllegalStateException.class,
                () -> service.createChangeRequest("CR-ACT-2", "BP-ACT", "1.0.0", "变更2", "乙"));
    }

    @Test
    @DisplayName("变更请求完整流程 — 提交→批准→实施→关闭")
    void changeRequest_fullFlow() {
        Blueprint bp = new Blueprint("BP-CR-FLOW-FULL", "变更流程蓝图", "PROD-CR-FLOW-FULL");
        bp.setVersion("1.0.0");
        blueprintRepo.save(bp);

        // 创建
        ChangeRequest cr = service.createChangeRequest(
                "CR-FLOW-FULL", "BP-CR-FLOW-FULL", "1.0.0", "工艺优化", "工程师甲");
        assertEquals(ChangeRequest.ChangeRequestStatus.DRAFT, cr.getStatus());

        // 提交
        service.submitChangeRequest("CR-FLOW-FULL");
        assertEquals(ChangeRequest.ChangeRequestStatus.SUBMITTED,
                service.findChangeRequest("CR-FLOW-FULL").getStatus());

        // 批准
        service.approveChangeRequest("CR-FLOW-FULL", "李审批");
        assertEquals(ChangeRequest.ChangeRequestStatus.APPROVED,
                service.findChangeRequest("CR-FLOW-FULL").getStatus());

        // 实施
        Blueprint next = service.implementChange("CR-FLOW-FULL", "2.0.0");
        assertEquals("2.0.0", next.getVersion());
        assertEquals(BlueprintStatus.DRAFT, next.getStatus());

        // 变更请求应关闭
        assertEquals(ChangeRequest.ChangeRequestStatus.CLOSED,
                service.findChangeRequest("CR-FLOW-FULL").getStatus());
    }

    @Test
    @DisplayName("驳回变更 — SUBMITTED → REJECTED → resubmit → SUBMITTED")
    void changeRequest_rejectAndResubmit() {
        Blueprint bp = new Blueprint("BP-REJ", "驳回蓝图", "PROD-REJ");
        blueprintRepo.save(bp);

        ChangeRequest cr = service.createChangeRequest(
                "CR-REJ", "BP-REJ", "1.0.0", "测试驳回", "甲");
        service.submitChangeRequest("CR-REJ");

        // 驳回
        ChangeRequest rejected = service.findChangeRequest("CR-REJ");
        rejected.reject();
        crRepo.save(rejected);
        assertEquals(ChangeRequest.ChangeRequestStatus.REJECTED,
                service.findChangeRequest("CR-REJ").getStatus());

        // 重新提交
        ChangeRequest resubmitted = service.findChangeRequest("CR-REJ");
        resubmitted.resubmit();
        crRepo.save(resubmitted);
        assertEquals(ChangeRequest.ChangeRequestStatus.SUBMITTED,
                service.findChangeRequest("CR-REJ").getStatus());
    }

    // ==================== 查询辅助方法 ====================

    @Test
    @DisplayName("getReleasedBlueprints — 按产品查询已发布蓝图")
    void getReleasedBlueprints_shouldFilterCorrectly() {
        Blueprint bp1 = service.createBlueprint(savedTemplate, "PROD-FILTER");
        service.submitForReview(bp1.getCode());
        service.approve(bp1.getCode(), "审批人");
        service.releaseVersion(bp1.getCode());

        Blueprint bp2 = service.createBlueprint(savedTemplate, "PROD-FILTER");
        // bp2 保持 DRAFT

        List<Blueprint> released = service.getReleasedBlueprints("PROD-FILTER");
        assertEquals(1, released.size());
    }

    @Test
    @DisplayName("getPendingChangeRequests — 查询进行中的变更请求")
    void getPendingChangeRequests_shouldExcludeTerminal() {
        Blueprint bp = new Blueprint("BP-PEND", "待处理蓝图", "PROD-PEND");
        blueprintRepo.save(bp);

        service.createChangeRequest("CR-PEND", "BP-PEND", "1.0.0", "变更1", "甲");

        List<ChangeRequest> pending = service.getPendingChangeRequests("BP-PEND");
        assertEquals(1, pending.size());

        // 关闭后不应出现在 pending 中
        ChangeRequest cr = service.findChangeRequest("CR-PEND");
        cr.submit();
        cr.approve("审批");
        cr.implement("1.1.0");
        cr.close();
        crRepo.save(cr);

        List<ChangeRequest> afterClose = service.getPendingChangeRequests("BP-PEND");
        assertEquals(0, afterClose.size());
    }

    @Test
    @DisplayName("searchBlueprints — 按名称/描述模糊搜索")
    void searchBlueprints_shouldFindByKeyword() {
        Blueprint bp = new Blueprint("BP-SEARCH", "阿莫西林片剂工艺", "PROD-SRCH");
        bp.setDescription("包含干燥、压片、包衣工序");
        blueprintRepo.save(bp);

        List<Blueprint> byName = service.searchBlueprints("阿莫西林");
        assertEquals(1, byName.size());

        List<Blueprint> byDesc = service.searchBlueprints("压片");
        assertEquals(1, byDesc.size());
    }

    // ==================== BOM 转化存根 ====================

    @Test
    @DisplayName("convertBOM — 抛出 UnsupportedOperationException（等待 Common 模块）")
    void convertBOM_shouldThrowUnsupported() {
        assertThrows(UnsupportedOperationException.class,
                () -> service.convertBOM(null));
    }

    @Test
    @DisplayName("convertEBOMtoPBOM — 抛出 UnsupportedOperationException")
    void convertEBOMtoPBOM_shouldThrow() {
        assertThrows(UnsupportedOperationException.class,
                () -> service.convertEBOMtoPBOM("PROD-001"));
    }

    // ==================== 可制造性检查存根 ====================

    @Test
    @DisplayName("checkFeasibility — 抛出 UnsupportedOperationException（等待工厂数据）")
    void checkFeasibility_shouldThrowUnsupported() {
        assertThrows(UnsupportedOperationException.class,
                () -> service.checkFeasibility("BP-001", "FACTORY-001"));
    }
}
