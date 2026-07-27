package com.byz.factory.plm.repository;

import com.byz.factory.factory.BlueprintStatus;
import com.byz.factory.plm.model.Blueprint;
import com.byz.factory.plm.model.ChangeRequest;
import com.byz.factory.plm.model.ProcessParameter;
import com.byz.factory.plm.model.ProcessTemplate;
import com.byz.factory.shared.UOM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("PLM Repository 集成测试")
class PlmRepositoryTest {

    @Autowired private BlueprintRepository blueprintRepo;
    @Autowired private ProcessTemplateRepository templateRepo;
    @Autowired private ProcessParameterRepository paramRepo;
    @Autowired private ChangeRequestRepository crRepo;

    // ==================== BlueprintRepository ====================

    @Test
    @DisplayName("Blueprint — 保存并查回，初始状态 DRAFT")
    void blueprint_saveAndFind() {
        Blueprint bp = new Blueprint("BP-001", "阿莫西林片剂-工艺路线", "PROD-AMX-001");
        Blueprint saved = blueprintRepo.save(bp);

        assertNotNull(saved.getId(), "save 后应自动生成 id");
        assertEquals(BlueprintStatus.DRAFT, saved.getStatus());
        assertEquals("0.1.0", saved.getVersion());
    }

    @Test
    @DisplayName("Blueprint — findByCode 按编码查询")
    void blueprint_findByCode() {
        blueprintRepo.save(new Blueprint("BP-002", "布洛芬-工艺路线", "PROD-IBP-001"));

        Optional<Blueprint> found = blueprintRepo.findByCode("BP-002");
        assertTrue(found.isPresent());
        assertEquals("布洛芬-工艺路线", found.get().getName());
    }

    @Test
    @DisplayName("Blueprint — findByProductCode 按产品查所有版本")
    void blueprint_findByProductCode() {
        Blueprint bp1 = new Blueprint("BP-003", "v1.0", "PROD-VC-001");
        bp1.setVersion("1.0.0");
        Blueprint bp2 = new Blueprint("BP-004", "v2.0", "PROD-VC-001");
        bp2.setVersion("2.0.0");
        blueprintRepo.save(bp1);
        blueprintRepo.save(bp2);

        List<Blueprint> versions = blueprintRepo.findByProductCodeOrderByVersionDesc("PROD-VC-001");
        assertEquals(2, versions.size());
        assertEquals("2.0.0", versions.get(0).getVersion());
    }

    @Test
    @DisplayName("Blueprint — findByStatus 按状态查询")
    void blueprint_findByStatus() {
        Blueprint bp1 = new Blueprint("BP-005", "草稿", "PROD-D01");
        bp1.submitForReview();
        bp1.approve("张三");
        bp1.release();
        Blueprint bp2 = new Blueprint("BP-006", "草稿", "PROD-D02");
        blueprintRepo.save(bp1);
        blueprintRepo.save(bp2);

        List<Blueprint> released = blueprintRepo.findByStatus(BlueprintStatus.RELEASED);
        assertEquals(1, released.size());
        assertEquals("BP-005", released.get(0).getCode());

        List<Blueprint> drafts = blueprintRepo.findByStatus(BlueprintStatus.DRAFT);
        assertEquals(1, drafts.size());
        assertEquals("BP-006", drafts.get(0).getCode());
    }

    @Test
    @DisplayName("Blueprint — 完整生命周期状态持久化")
    void blueprint_fullLifecyclePersistence() {
        Blueprint bp = new Blueprint("BP-007", "全生命周期", "PROD-LC");

        bp.submitForReview();
        blueprintRepo.saveAndFlush(bp);
        assertEquals(BlueprintStatus.UNDER_REVIEW, blueprintRepo.findByCode("BP-007").orElseThrow().getStatus());

        bp.approve("赵审批");
        blueprintRepo.saveAndFlush(bp);
        assertEquals(BlueprintStatus.APPROVED, blueprintRepo.findByCode("BP-007").orElseThrow().getStatus());

        bp.release();
        blueprintRepo.saveAndFlush(bp);
        assertEquals(BlueprintStatus.RELEASED, blueprintRepo.findByCode("BP-007").orElseThrow().getStatus());

        bp.obsolete();
        blueprintRepo.saveAndFlush(bp);
        assertEquals(BlueprintStatus.OBSOLETED, blueprintRepo.findByCode("BP-007").orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Blueprint — findByAuthor 按作者查询")
    void blueprint_findByAuthor() {
        Blueprint bp = new Blueprint("BP-008", "作者测试", "PROD-A01");
        bp.setAuthor("张三");
        blueprintRepo.save(bp);

        List<Blueprint> byAuthor = blueprintRepo.findByAuthor("张三");
        assertEquals(1, byAuthor.size());
    }

    @Test
    @DisplayName("Blueprint — countByStatus 按状态计数")
    void blueprint_countByStatus() {
        blueprintRepo.save(new Blueprint("BP-009", "d1", "PROD-C01"));
        Blueprint bp = new Blueprint("BP-010", "d2", "PROD-C02");
        bp.submitForReview();
        bp.approve("审批人");
        bp.release();
        blueprintRepo.save(bp);

        assertEquals(1, blueprintRepo.countByStatus(BlueprintStatus.DRAFT));
        assertEquals(1, blueprintRepo.countByStatus(BlueprintStatus.RELEASED));
    }

    @Test
    @DisplayName("Blueprint — createdAt/updatedAt 自动填充")
    void blueprint_auditTimestamps() {
        Blueprint bp = new Blueprint("BP-011", "审计测试", "PROD-AT01");
        Blueprint saved = blueprintRepo.save(bp);

        assertNotNull(saved.getCreatedAt(), "createdAt 应自动填充");
        assertNotNull(saved.getUpdatedAt(), "updatedAt 应自动填充");
    }

    // ==================== ProcessTemplateRepository ====================

    @Test
    @DisplayName("ProcessTemplate — 保存并查回")
    void template_saveAndFind() {
        ProcessTemplate tmpl = new ProcessTemplate("TPL-001", "阿莫西林片剂工艺", "化工");
        ProcessTemplate saved = templateRepo.save(tmpl);

        assertNotNull(saved.getId());
        assertEquals("DRAFT", saved.getStatus());
        assertEquals("0.1.0", saved.getVersion());
    }

    @Test
    @DisplayName("ProcessTemplate — findByCategory 按类别查询")
    void template_findByCategory() {
        templateRepo.save(new ProcessTemplate("TPL-002", "化工工艺A", "化工"));
        templateRepo.save(new ProcessTemplate("TPL-003", "化工工艺B", "化工"));
        templateRepo.save(new ProcessTemplate("TPL-004", "机加工工艺", "机加工"));

        assertEquals(2, templateRepo.findByCategory("化工").size());
        assertEquals(1, templateRepo.findByCategory("机加工").size());
    }

    @Test
    @DisplayName("ProcessTemplate — activate/obsolete 状态变更持久化")
    void template_statusTransitionPersistence() {
        ProcessTemplate tmpl = new ProcessTemplate("TPL-005", "状态测试", "电子");
        templateRepo.save(tmpl);

        tmpl.activate();
        templateRepo.saveAndFlush(tmpl);
        assertEquals("ACTIVE", templateRepo.findByCode("TPL-005").orElseThrow().getStatus());

        tmpl.obsolete();
        templateRepo.saveAndFlush(tmpl);
        assertEquals("OBSOLETED", templateRepo.findByCode("TPL-005").orElseThrow().getStatus());
    }

    // ==================== ProcessParameterRepository ====================

    @Test
    @DisplayName("ProcessParameter — 保存并查回，BigDecimal 精度正确")
    void param_saveAndFind() {
        ProcessParameter param = new ProcessParameter("PARAM-001", "干燥温度",
                UOM.CELSIUS, new BigDecimal("60.5"), new BigDecimal("55.0"),
                new BigDecimal("65.0"));
        ProcessParameter saved = paramRepo.save(param);

        assertNotNull(saved.getId());
        assertEquals(0, new BigDecimal("60.5").compareTo(saved.getTargetValue()));
    }

    @Test
    @DisplayName("ProcessParameter — 枚举持久化为字符串")
    void param_enumPersistence() {
        ProcessParameter param = new ProcessParameter("PARAM-002", "pH 值",
                UOM.NONE, BigDecimal.ONE, BigDecimal.ZERO, new BigDecimal("14"));
        param.setDataType(ProcessParameter.DataType.ENUM);
        param.setControlMethod(ProcessParameter.ControlMethod.MANUAL);
        param.setImportance(ProcessParameter.Importance.CRITICAL);
        paramRepo.saveAndFlush(param);

        ProcessParameter found = paramRepo.findByCode("PARAM-002").orElseThrow();
        assertEquals(ProcessParameter.DataType.ENUM, found.getDataType());
        assertEquals(ProcessParameter.ControlMethod.MANUAL, found.getControlMethod());
        assertEquals(ProcessParameter.Importance.CRITICAL, found.getImportance());
    }

    @Test
    @DisplayName("ProcessParameter — findByImportance 按重要性查询")
    void param_findByImportance() {
        ProcessParameter p1 = new ProcessParameter("PARAM-003", "关键参数",
                UOM.CELSIUS, BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("20"));
        p1.setImportance(ProcessParameter.Importance.CRITICAL);
        ProcessParameter p2 = new ProcessParameter("PARAM-004", "普通参数",
                UOM.CELSIUS, BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("20"));
        paramRepo.save(p1);
        paramRepo.save(p2);

        assertEquals(1, paramRepo.findByImportance(ProcessParameter.Importance.CRITICAL).size());
        assertEquals(1, paramRepo.findByImportance(ProcessParameter.Importance.IMPORTANT).size());
    }

    // ==================== ChangeRequestRepository ====================

    @Test
    @DisplayName("ChangeRequest — 保存并查回，初始状态 DRAFT")
    void cr_saveAndFind() {
        ChangeRequest cr = new ChangeRequest("CR-001", "BP-001", "1.0.0", "客户要求调整参数");
        cr.setRequestedBy("工程师甲");
        ChangeRequest saved = crRepo.save(cr);

        assertNotNull(saved.getId());
        assertEquals(ChangeRequest.ChangeRequestStatus.DRAFT, saved.getStatus());
        assertEquals("BP-001", saved.getBlueprintCode());
    }

    @Test
    @DisplayName("ChangeRequest — 完整生命周期状态持久化")
    void cr_fullLifecyclePersistence() {
        ChangeRequest cr = new ChangeRequest("CR-002", "BP-001", "1.0.0", "工艺优化");

        cr.submit();
        crRepo.saveAndFlush(cr);
        assertEquals(ChangeRequest.ChangeRequestStatus.SUBMITTED,
                crRepo.findByCode("CR-002").orElseThrow().getStatus());

        cr.approve("李审批");
        crRepo.saveAndFlush(cr);
        assertEquals(ChangeRequest.ChangeRequestStatus.APPROVED,
                crRepo.findByCode("CR-002").orElseThrow().getStatus());

        cr.implement("1.1.0");
        crRepo.saveAndFlush(cr);
        assertEquals(ChangeRequest.ChangeRequestStatus.IMPLEMENTED,
                crRepo.findByCode("CR-002").orElseThrow().getStatus());

        cr.close();
        crRepo.saveAndFlush(cr);
        assertEquals(ChangeRequest.ChangeRequestStatus.CLOSED,
                crRepo.findByCode("CR-002").orElseThrow().getStatus());
    }

    @Test
    @DisplayName("ChangeRequest — findByBlueprintCode 按蓝图查询")
    void cr_findByBlueprintCode() {
        crRepo.save(new ChangeRequest("CR-003", "BP-001", "1.0.0", "变更1"));
        crRepo.save(new ChangeRequest("CR-004", "BP-001", "1.0.0", "变更2"));
        crRepo.save(new ChangeRequest("CR-005", "BP-002", "1.0.0", "变更3"));

        assertEquals(2, crRepo.findByBlueprintCode("BP-001").size());
        assertEquals(1, crRepo.findByBlueprintCode("BP-002").size());
    }

    @Test
    @DisplayName("ChangeRequest — findByStatus 按状态查询")
    void cr_findByStatus() {
        ChangeRequest cr = new ChangeRequest("CR-006", "BP-001", "1.0.0", "测试");
        cr.submit();
        crRepo.save(cr);

        assertEquals(1, crRepo.findByStatus(ChangeRequest.ChangeRequestStatus.SUBMITTED).size());
        assertEquals(0, crRepo.findByStatus(ChangeRequest.ChangeRequestStatus.CLOSED).size());
    }

    @Test
    @DisplayName("ChangeRequest — 驳回后可重新提交")
    void cr_rejectAndResubmit() {
        ChangeRequest cr = new ChangeRequest("CR-007", "BP-001", "1.0.0", "先驳后提");
        cr.submit();
        cr.reject();
        crRepo.saveAndFlush(cr);
        assertEquals(ChangeRequest.ChangeRequestStatus.REJECTED,
                crRepo.findByCode("CR-007").orElseThrow().getStatus());

        cr.resubmit();
        crRepo.saveAndFlush(cr);
        assertEquals(ChangeRequest.ChangeRequestStatus.SUBMITTED,
                crRepo.findByCode("CR-007").orElseThrow().getStatus());
    }
}
