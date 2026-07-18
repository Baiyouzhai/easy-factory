package com.byz.factory.plm;

import com.byz.factory.factory.BlueprintStatus;
import com.byz.factory.plm.model.Blueprint;
import com.byz.factory.plm.model.ProcessParameter;
import com.byz.factory.plm.model.ProcessTemplate;
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
}
