package com.byz.factory.plm;

import com.byz.factory.factory.BlueprintDiff;
import com.byz.factory.factory.BlueprintStatus;
import com.byz.factory.plm.model.Blueprint;
import com.byz.factory.plm.model.ProcessParameter;
import com.byz.factory.plm.model.ProcessTemplate;
import com.byz.factory.plm.service.BlueprintDifferImpl;
import com.byz.factory.process.Process;
import com.byz.factory.shared.UOM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PLM 模块模型构造测试")
class PlmModuleTest {

    // ==================== ProcessTemplate ====================

    @Test
    @DisplayName("ProcessTemplate 创建 — 初始版本 0.1.0，状态 DRAFT")
    void processTemplate_creation_shouldSetDefaults() {
        // Given
        // When
        ProcessTemplate template = new ProcessTemplate("TPL-001", "阿莫西林片剂工艺", "化工");

        // Then
        assertEquals("TPL-001", template.getCode());
        assertEquals("阿莫西林片剂工艺", template.getName());
        assertEquals("化工", template.getCategory());
        assertEquals("0.1.0", template.getVersion());
        assertEquals("DRAFT", template.getStatus());
        assertNotNull(template.getProcesses());
        assertTrue(template.getProcesses().isEmpty());
        assertNotNull(template.getParameters());
        assertTrue(template.getParameters().isEmpty());
    }

    @Test
    @DisplayName("ProcessTemplate — 可设置状态和版本")
    void processTemplate_shouldUpdateStatusAndVersion() {
        // Given
        ProcessTemplate template = new ProcessTemplate("TPL-002", "注塑工艺", "机加工");

        // When
        template.setVersion("1.0.0");
        template.setStatus("ACTIVE");

        // Then
        assertEquals("1.0.0", template.getVersion());
        assertEquals("ACTIVE", template.getStatus());
    }

    @Test
    @DisplayName("ProcessTemplate — 可添加工艺参数")
    void processTemplate_shouldAddParameters() {
        // Given
        ProcessTemplate template = new ProcessTemplate("TPL-003", "热处理工艺", "机加工");
        ProcessParameter param = new ProcessParameter("PARAM-TEMP", "淬火温度",
                UOM.CELSIUS, new BigDecimal("850"), new BigDecimal("830"),
                new BigDecimal("870"));

        // When
        template.getParameters().add(param);

        // Then
        assertEquals(1, template.getParameters().size());
        assertEquals("淬火温度", template.getParameters().get(0).getName());
    }

    // ==================== ProcessTemplate 业务便捷方法 ====================

    @Test
    @DisplayName("ProcessTemplate.activate — DRAFT → ACTIVE")
    void processTemplate_activate_shouldSetActive() {
        // Given
        ProcessTemplate template = new ProcessTemplate("TPL-010", "激活测试模板", "化工");

        // When
        template.activate();

        // Then
        assertEquals("ACTIVE", template.getStatus());
    }

    @Test
    @DisplayName("ProcessTemplate.obsolete — ACTIVE → OBSOLETED")
    void processTemplate_obsolete_shouldSetObsoleted() {
        // Given
        ProcessTemplate template = new ProcessTemplate("TPL-011", "废弃测试模板", "电子");
        template.activate();

        // When
        template.obsolete();

        // Then
        assertEquals("OBSOLETED", template.getStatus());
    }

    // ==================== Blueprint ====================

    @Test
    @DisplayName("Blueprint 创建 — 初始状态为 DRAFT，版本 0.1.0")
    void blueprint_creation_shouldSetDefaultStatus() {
        // Given
        // When
        Blueprint bp = new Blueprint("BP-001", "阿莫西林片剂-工艺路线", "PROD-AMX-001");

        // Then
        assertEquals("BP-001", bp.getCode());
        assertEquals("阿莫西林片剂-工艺路线", bp.getName());
        assertEquals("PROD-AMX-001", bp.getProductCode());
        assertEquals("0.1.0", bp.getVersion());
        assertEquals(BlueprintStatus.DRAFT, bp.getStatus());
        assertNotNull(bp.getProcesses());
        assertTrue(bp.getProcesses().isEmpty());
    }

    @Test
    @DisplayName("Blueprint 状态转换 — DRAFT → UNDER_REVIEW → APPROVED → RELEASED → OBSOLETED")
    void blueprint_lifecycle_shouldTransitionCorrectly() {
        // Given
        Blueprint bp = new Blueprint("BP-002", "布洛芬片剂-工艺路线", "PROD-IBP-001");

        // When & Then: DRAFT → UNDER_REVIEW
        bp.transition(BlueprintStatus.UNDER_REVIEW);
        assertEquals(BlueprintStatus.UNDER_REVIEW, bp.getStatus());

        // When & Then: UNDER_REVIEW → APPROVED
        bp.transition(BlueprintStatus.APPROVED);
        assertEquals(BlueprintStatus.APPROVED, bp.getStatus());

        // When & Then: APPROVED → RELEASED
        bp.transition(BlueprintStatus.RELEASED);
        assertEquals(BlueprintStatus.RELEASED, bp.getStatus());

        // When & Then: RELEASED → OBSOLETED
        bp.transition(BlueprintStatus.OBSOLETED);
        assertEquals(BlueprintStatus.OBSOLETED, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint 评审驳回 — UNDER_REVIEW → DRAFT")
    void blueprint_reviewRejection_shouldReturnToDraft() {
        // Given
        Blueprint bp = new Blueprint("BP-003", "维生素C片-工艺路线", "PROD-VC-001");
        bp.transition(BlueprintStatus.UNDER_REVIEW);

        // When
        bp.transition(BlueprintStatus.DRAFT);

        // Then
        assertEquals(BlueprintStatus.DRAFT, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint 非法状态转换 — 应抛出 IllegalStateException")
    void blueprint_illegalTransition_shouldThrow() {
        // Given
        Blueprint bp = new Blueprint("BP-004", "阿司匹林-工艺路线", "PROD-ASP-001");

        // When & Then: DRAFT 不能直接到 APPROVED（跳过了 REVIEW）
        assertThrows(IllegalStateException.class,
                () -> bp.transition(BlueprintStatus.APPROVED));

        // DRAFT 不能直接到 RELEASED
        assertThrows(IllegalStateException.class,
                () -> bp.transition(BlueprintStatus.RELEASED));

        // 转到 RELEASED 后...
        bp.transition(BlueprintStatus.UNDER_REVIEW);
        bp.transition(BlueprintStatus.APPROVED);
        bp.transition(BlueprintStatus.RELEASED);

        // RELEASED 不能回到 APPROVED
        assertThrows(IllegalStateException.class,
                () -> bp.transition(BlueprintStatus.APPROVED));
    }

    @Test
    @DisplayName("Blueprint OBSOLETED — 终端状态，任何转换都抛异常")
    void blueprint_obsoleted_shouldBeTerminal() {
        // Given
        Blueprint bp = new Blueprint("BP-005", "过期工艺", "PROD-OLD-001");
        bp.transition(BlueprintStatus.UNDER_REVIEW);
        bp.transition(BlueprintStatus.APPROVED);
        bp.transition(BlueprintStatus.RELEASED);
        bp.transition(BlueprintStatus.OBSOLETED);

        // When & Then: OBSOLETED 是终端状态
        assertThrows(IllegalStateException.class,
                () -> bp.transition(BlueprintStatus.DRAFT));
        assertThrows(IllegalStateException.class,
                () -> bp.transition(BlueprintStatus.APPROVED));
        assertThrows(IllegalStateException.class,
                () -> bp.transition(BlueprintStatus.RELEASED));
    }

    @Test
    @DisplayName("Blueprint 同状态转换 — 幂等，不抛异常")
    void blueprint_sameStateTransition_shouldBeIdempotent() {
        // Given
        Blueprint bp = new Blueprint("BP-006", "对乙酰氨基酚-工艺路线", "PROD-PCM-001");

        // When & Then
        assertDoesNotThrow(() -> bp.transition(BlueprintStatus.DRAFT));
        assertEquals(BlueprintStatus.DRAFT, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint canTransition — 正确判断合法/非法转换")
    void blueprint_canTransition_shouldCheckCorrectly() {
        // Given
        Blueprint bp = new Blueprint("BP-007", "头孢-工艺路线", "PROD-CEF-001");

        // Then: DRAFT 状态
        assertTrue(bp.canTransition(BlueprintStatus.UNDER_REVIEW));
        assertTrue(bp.canTransition(BlueprintStatus.OBSOLETED));
        assertFalse(bp.canTransition(BlueprintStatus.APPROVED));
        assertFalse(bp.canTransition(BlueprintStatus.RELEASED));

        // When: 转到 APPROVED
        bp.transition(BlueprintStatus.UNDER_REVIEW);
        bp.transition(BlueprintStatus.APPROVED);

        // Then: APPROVED 状态
        assertTrue(bp.canTransition(BlueprintStatus.RELEASED));
        assertTrue(bp.canTransition(BlueprintStatus.DRAFT));
        assertFalse(bp.canTransition(BlueprintStatus.OBSOLETED));
    }

    @Test
    @DisplayName("Blueprint — 可设置 author 和 approvedBy")
    void blueprint_shouldSetAuthorAndApprover() {
        // Given
        Blueprint bp = new Blueprint("BP-008", "氯霉素-工艺路线", "PROD-CMP-001");

        // When
        bp.setAuthor("张三");
        bp.setDescription("氯霉素片剂生产工艺路线，共 8 道工序");
        bp.setApprovedBy("李四");

        // Then
        assertEquals("张三", bp.getAuthor());
        assertEquals("氯霉素片剂生产工艺路线，共 8 道工序", bp.getDescription());
        assertEquals("李四", bp.getApprovedBy());
    }

    // ==================== Blueprint 业务便捷方法 ====================

    @Test
    @DisplayName("Blueprint.submitForReview — DRAFT → UNDER_REVIEW")
    void blueprint_submitForReview_shouldTransitionToUnderReview() {
        // Given
        Blueprint bp = new Blueprint("BP-010", "测试蓝图", "PROD-TEST");

        // When
        bp.submitForReview();

        // Then
        assertEquals(BlueprintStatus.UNDER_REVIEW, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint.approve — UNDER_REVIEW → APPROVED，记录审批人")
    void blueprint_approve_shouldTransitionAndRecordApprover() {
        // Given
        Blueprint bp = new Blueprint("BP-011", "测试蓝图", "PROD-TEST");
        bp.submitForReview();

        // When
        bp.approve("王审批");

        // Then
        assertEquals(BlueprintStatus.APPROVED, bp.getStatus());
        assertEquals("王审批", bp.getApprovedBy());
    }

    @Test
    @DisplayName("Blueprint.reject — UNDER_REVIEW → DRAFT")
    void blueprint_reject_shouldReturnToDraft() {
        // Given
        Blueprint bp = new Blueprint("BP-012", "测试蓝图", "PROD-TEST");
        bp.submitForReview();

        // When
        bp.reject();

        // Then
        assertEquals(BlueprintStatus.DRAFT, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint.reject — APPROVED → DRAFT（批准后发现问题回退）")
    void blueprint_reject_shouldReturnFromApprovedToDraft() {
        // Given
        Blueprint bp = new Blueprint("BP-013", "测试蓝图", "PROD-TEST");
        bp.submitForReview();
        bp.approve("王审批");

        // When
        bp.reject();

        // Then
        assertEquals(BlueprintStatus.DRAFT, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint.release — APPROVED → RELEASED")
    void blueprint_release_shouldTransitionToReleased() {
        // Given
        Blueprint bp = new Blueprint("BP-014", "测试蓝图", "PROD-TEST");
        bp.submitForReview();
        bp.approve("王审批");

        // When
        bp.release();

        // Then
        assertEquals(BlueprintStatus.RELEASED, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint.obsolete — 从 DRAFT 直接废弃")
    void blueprint_obsolete_shouldTransitionFromDraft() {
        // Given
        Blueprint bp = new Blueprint("BP-015", "测试蓝图", "PROD-TEST");

        // When
        bp.obsolete();

        // Then
        assertEquals(BlueprintStatus.OBSOLETED, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint.obsolete — 从 RELEASED 废弃")
    void blueprint_obsolete_shouldTransitionFromReleased() {
        // Given
        Blueprint bp = new Blueprint("BP-016", "测试蓝图", "PROD-TEST");
        bp.submitForReview();
        bp.approve("赵审批");
        bp.release();

        // When
        bp.obsolete();

        // Then
        assertEquals(BlueprintStatus.OBSOLETED, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint 完整生命周期 — 通过便捷方法走完")
    void blueprint_convenienceMethods_shouldCompleteFullLifecycle() {
        // Given
        Blueprint bp = new Blueprint("BP-017", "完整生命周期蓝图", "PROD-LC");

        // When & Then: DRAFT → UNDER_REVIEW
        bp.submitForReview();
        assertEquals(BlueprintStatus.UNDER_REVIEW, bp.getStatus());

        // When & Then: UNDER_REVIEW → APPROVED
        bp.approve("钱审批");
        assertEquals(BlueprintStatus.APPROVED, bp.getStatus());
        assertEquals("钱审批", bp.getApprovedBy());

        // When & Then: APPROVED → RELEASED
        bp.release();
        assertEquals(BlueprintStatus.RELEASED, bp.getStatus());

        // When & Then: RELEASED → OBSOLETED
        bp.obsolete();
        assertEquals(BlueprintStatus.OBSOLETED, bp.getStatus());
    }

    @Test
    @DisplayName("Blueprint 便捷方法 — 非法操作抛出异常")
    void blueprint_convenienceMethod_illegalCall_shouldThrow() {
        // Given: DRAFT 状态不能直接 release
        Blueprint bp = new Blueprint("BP-018", "非法操作蓝图", "PROD-ILL");

        // When & Then
        assertThrows(IllegalStateException.class, () -> bp.release());
        assertThrows(IllegalStateException.class, () -> bp.approve("某人"));
    }

    // ==================== ProcessParameter ====================

    @Test
    @DisplayName("ProcessParameter 创建 — 默认 NUMERIC/AUTO/IMPORTANT")
    void processParameter_creation_shouldSetDefaults() {
        // Given
        // When
        ProcessParameter param = new ProcessParameter("PARAM-001", "干燥温度",
                UOM.CELSIUS, new BigDecimal("60"), new BigDecimal("55"),
                new BigDecimal("65"));

        // Then
        assertEquals("PARAM-001", param.getCode());
        assertEquals("干燥温度", param.getName());
        assertEquals(UOM.CELSIUS, param.getUom());
        assertEquals(new BigDecimal("60"), param.getTargetValue());
        assertEquals(new BigDecimal("55"), param.getLowerLimit());
        assertEquals(new BigDecimal("65"), param.getUpperLimit());
        assertEquals(ProcessParameter.DataType.NUMERIC, param.getDataType());
        assertEquals(ProcessParameter.ControlMethod.AUTO, param.getControlMethod());
        assertEquals(ProcessParameter.Importance.IMPORTANT, param.getImportance());
    }

    @Test
    @DisplayName("ProcessParameter — 可自定义数据类型、控制方式和重要性")
    void processParameter_shouldCustomizeAttributes() {
        // Given
        ProcessParameter param = new ProcessParameter("PARAM-002", "pH 值",
                UOM.NONE, new BigDecimal("7.0"), new BigDecimal("6.5"),
                new BigDecimal("7.5"));

        // When
        param.setDataType(ProcessParameter.DataType.ENUM);
        param.setControlMethod(ProcessParameter.ControlMethod.MANUAL);
        param.setImportance(ProcessParameter.Importance.CRITICAL);
        param.setDefaultValue("7.0");

        // Then
        assertEquals(ProcessParameter.DataType.ENUM, param.getDataType());
        assertEquals(ProcessParameter.ControlMethod.MANUAL, param.getControlMethod());
        assertEquals(ProcessParameter.Importance.CRITICAL, param.getImportance());
        assertEquals("7.0", param.getDefaultValue());
    }

    @Test
    @DisplayName("ProcessParameter — 布尔类型参数")
    void processParameter_booleanType() {
        // Given
        ProcessParameter param = new ProcessParameter("PARAM-003", "密封检测",
                UOM.NONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE);

        // When
        param.setDataType(ProcessParameter.DataType.BOOLEAN);
        param.setDefaultValue("true");

        // Then
        assertEquals(ProcessParameter.DataType.BOOLEAN, param.getDataType());
        assertEquals("true", param.getDefaultValue());
    }

    // ==================== BlueprintStatus 枚举测试 ====================

    @Test
    @DisplayName("BlueprintStatus — 各状态允许的转换正确")
    void blueprintStatus_allowedTransitions_shouldBeCorrect() {
        // DRAFT
        assertTrue(BlueprintStatus.DRAFT.allowedTransitions().contains(BlueprintStatus.UNDER_REVIEW));
        assertTrue(BlueprintStatus.DRAFT.allowedTransitions().contains(BlueprintStatus.OBSOLETED));
        assertEquals(2, BlueprintStatus.DRAFT.allowedTransitions().size());

        // UNDER_REVIEW
        assertTrue(BlueprintStatus.UNDER_REVIEW.allowedTransitions().contains(BlueprintStatus.APPROVED));
        assertTrue(BlueprintStatus.UNDER_REVIEW.allowedTransitions().contains(BlueprintStatus.DRAFT));
        assertEquals(2, BlueprintStatus.UNDER_REVIEW.allowedTransitions().size());

        // APPROVED
        assertTrue(BlueprintStatus.APPROVED.allowedTransitions().contains(BlueprintStatus.RELEASED));
        assertTrue(BlueprintStatus.APPROVED.allowedTransitions().contains(BlueprintStatus.DRAFT));
        assertEquals(2, BlueprintStatus.APPROVED.allowedTransitions().size());

        // RELEASED
        assertTrue(BlueprintStatus.RELEASED.allowedTransitions().contains(BlueprintStatus.OBSOLETED));
        assertEquals(1, BlueprintStatus.RELEASED.allowedTransitions().size());

        // OBSOLETED — 终端
        assertTrue(BlueprintStatus.OBSOLETED.allowedTransitions().isEmpty());
    }

    // ==================== BlueprintDifferImpl 测试 ====================

    @Test
    @DisplayName("BlueprintDiffer — 相同蓝图无差异")
    void blueprintDiffer_identicalBlueprints_shouldHaveNoDiff() {
        // Given
        Blueprint bp1 = new Blueprint("BP-100", "相同蓝图", "PROD-SAME");
        bp1.setVersion("1.0.0");
        Blueprint bp2 = new Blueprint("BP-100", "相同蓝图", "PROD-SAME");
        bp2.setVersion("2.0.0");

        BlueprintDifferImpl differ = new BlueprintDifferImpl();

        // When
        BlueprintDiff diff = differ.compare(bp1, bp2);

        // Then
        assertFalse(diff.hasDiff());
        assertEquals("1.0.0", diff.versionA());
        assertEquals("2.0.0", diff.versionB());
    }

    @Test
    @DisplayName("BlueprintDiffer — 检测新增工序")
    void blueprintDiffer_addedProcess_shouldDetect() {
        // Given
        Blueprint bp1 = new Blueprint("BP-101", "旧版", "PROD-DIFF");
        bp1.setVersion("1.0.0");

        Blueprint bp2 = new Blueprint("BP-101", "新版", "PROD-DIFF");
        bp2.setVersion("2.0.0");
        bp2.getProcesses().add(new Process("P-001", "混合"));

        BlueprintDifferImpl differ = new BlueprintDifferImpl();

        // When
        BlueprintDiff diff = differ.compare(bp1, bp2);

        // Then
        assertTrue(diff.hasDiff());
        assertEquals(1, diff.diffItems().size());
        assertEquals(BlueprintDiff.DiffType.ADDED, diff.diffItems().get(0).diffType());
        assertEquals("P-001", diff.diffItems().get(0).targetCode());
    }

    @Test
    @DisplayName("BlueprintDiffer — 检测删除工序")
    void blueprintDiffer_removedProcess_shouldDetect() {
        // Given
        Blueprint bp1 = new Blueprint("BP-102", "旧版", "PROD-DIFF");
        bp1.setVersion("1.0.0");
        bp1.getProcesses().add(new Process("P-001", "混合"));

        Blueprint bp2 = new Blueprint("BP-102", "新版", "PROD-DIFF");
        bp2.setVersion("2.0.0");

        BlueprintDifferImpl differ = new BlueprintDifferImpl();

        // When
        BlueprintDiff diff = differ.compare(bp1, bp2);

        // Then
        assertTrue(diff.hasDiff());
        assertEquals(BlueprintDiff.DiffType.REMOVED, diff.diffItems().get(0).diffType());
    }

    @Test
    @DisplayName("BlueprintDiffer — 检测工序重排序")
    void blueprintDiffer_reorderedProcess_shouldDetect() {
        // Given
        Blueprint bp1 = new Blueprint("BP-103", "旧版", "PROD-DIFF");
        bp1.setVersion("1.0.0");
        bp1.getProcesses().add(new Process("P-001", "混合"));
        bp1.getProcesses().add(new Process("P-002", "压片"));

        Blueprint bp2 = new Blueprint("BP-103", "新版", "PROD-DIFF");
        bp2.setVersion("2.0.0");
        bp2.getProcesses().add(new Process("P-002", "压片"));
        bp2.getProcesses().add(new Process("P-001", "混合"));

        BlueprintDifferImpl differ = new BlueprintDifferImpl();

        // When
        BlueprintDiff diff = differ.compare(bp1, bp2);

        // Then
        assertTrue(diff.hasDiff());
        assertTrue(diff.diffItems().stream()
                .anyMatch(item -> item.diffType() == BlueprintDiff.DiffType.REORDERED));
    }

    @Test
    @DisplayName("BlueprintDiffer — 多维度差异（新增+删除+重排）")
    void blueprintDiffer_multiDimensionDiff_shouldDetectAll() {
        // Given
        Blueprint bp1 = new Blueprint("BP-104", "旧版", "PROD-MULTI");
        bp1.setVersion("1.0.0");
        bp1.getProcesses().add(new Process("P-001", "混合"));
        bp1.getProcesses().add(new Process("P-002", "制粒"));
        bp1.getProcesses().add(new Process("P-003", "压片"));

        Blueprint bp2 = new Blueprint("BP-104", "新版", "PROD-MULTI");
        bp2.setVersion("2.0.0");
        bp2.getProcesses().add(new Process("P-001", "混合"));
        bp2.getProcesses().add(new Process("P-004", "包衣"));  // 新增
        bp2.getProcesses().add(new Process("P-003", "压片"));
        // P-002 制粒 被删除

        BlueprintDifferImpl differ = new BlueprintDifferImpl();

        // When
        BlueprintDiff diff = differ.compare(bp1, bp2);

        // Then
        assertTrue(diff.hasDiff());
        assertTrue(diff.diffItems().stream()
                .anyMatch(item -> item.diffType() == BlueprintDiff.DiffType.ADDED
                        && "P-004".equals(item.targetCode())));
        assertTrue(diff.diffItems().stream()
                .anyMatch(item -> item.diffType() == BlueprintDiff.DiffType.REMOVED
                        && "P-002".equals(item.targetCode())));
    }
}
