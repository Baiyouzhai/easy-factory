package com.byz.factory.qms.repository;

import com.byz.factory.batch.*;
import com.byz.factory.qms.QmsTestConfig;
import com.byz.factory.qms.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = QmsTestConfig.class)
@DisplayName("QMS Repository 集成测试")
class QmsRepositoryTest {

    @Autowired private InspectionOrderRepository orderRepo;
    @Autowired private InspectionPlanRepository planRepo;
    @Autowired private DeviationRepository deviationRepo;
    @Autowired private CapaRepository capaRepo;

    @BeforeEach
    void setUp() {
        capaRepo.deleteAll();
        deviationRepo.deleteAll();
        orderRepo.deleteAll();
        planRepo.deleteAll();
    }

    // ==================== InspectionOrder ====================

    @Test
    @DisplayName("保存并查询检验指令 — findByInspectionNo")
    void order_saveAndFindByInspectionNo() {
        var order = new InspectionOrder("INSP-001", "B001", "PROC-001");
        order.setWorkOrderNo("WO-001");
        order.setInspectionType("IPQC");
        orderRepo.save(order);

        var found = orderRepo.findByInspectionNo("INSP-001");
        assertNotNull(found);
        assertEquals("B001", found.getBatchNo());
        assertEquals(InspectionStatus.PENDING, found.getStatus());
    }

    @Test
    @DisplayName("按工单号查找检验指令")
    void order_findByWorkOrderNo() {
        var o1 = new InspectionOrder("INSP-001", "B001", "PROC-001");
        o1.setWorkOrderNo("WO-001");
        var o2 = new InspectionOrder("INSP-002", "B002", "PROC-002");
        o2.setWorkOrderNo("WO-001");
        var o3 = new InspectionOrder("INSP-003", "B003", "PROC-003");
        o3.setWorkOrderNo("WO-002");
        orderRepo.saveAll(List.of(o1, o2, o3));

        var results = orderRepo.findByWorkOrderNo("WO-001");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("按状态查找检验指令")
    void order_findByStatus() {
        var o1 = new InspectionOrder("INSP-001", "B001", "PROC-001");
        o1.setTotalItems(1); o1.setCompletedItems(1); o1.setPassedItems(1);
        o1.startInspection("检验员A");
        o1.completeInspection();
        orderRepo.save(o1);
        var o2 = new InspectionOrder("INSP-002", "B002", "PROC-002");
        orderRepo.save(o2);

        assertEquals(1, orderRepo.findByStatus(InspectionStatus.PASSED).size());
        assertEquals(1, orderRepo.findByStatus(InspectionStatus.PENDING).size());
    }

    // ==================== InspectionPlan ====================

    @Test
    @DisplayName("保存并查询检验方案")
    void plan_saveAndFind() {
        var plan = new InspectionPlan("PLAN-001", "检验方案", "P-001", "PROC-001", InspectionType.IPQC);
        planRepo.save(plan);

        var found = planRepo.findByCode("PLAN-001");
        assertNotNull(found);
        assertEquals(InspectionType.IPQC, found.getInspectionType());
    }

    @Test
    @DisplayName("按产品编码查找检验方案")
    void plan_findByProductCode() {
        planRepo.save(new InspectionPlan("P1", "方案1", "PROD-A", "PROC-001", InspectionType.IPQC));
        planRepo.save(new InspectionPlan("P2", "方案2", "PROD-A", "PROC-002", InspectionType.FQC));
        planRepo.save(new InspectionPlan("P3", "方案3", "PROD-B", "PROC-001", InspectionType.IPQC));

        assertEquals(2, planRepo.findByProductCode("PROD-A").size());
        assertEquals(1, planRepo.findByProductCode("PROD-B").size());
    }

    @Test
    @DisplayName("按产品和工序查找检验方案")
    void plan_findByProductCodeAndProcessCode() {
        planRepo.save(new InspectionPlan("P1", "方案1", "PROD-A", "PROC-001", InspectionType.IPQC));

        var found = planRepo.findByProductCodeAndProcessCode("PROD-A", "PROC-001");
        assertNotNull(found);
        assertEquals("P1", found.getCode());
    }

    // ==================== Deviation ====================

    @Test
    @DisplayName("保存并查询偏差")
    void deviation_saveAndFind() {
        var dev = new Deviation("DEV-001", "测试偏差", "INSPECTION", "INSP-001", DeviationSeverity.MAJOR);
        dev.setWorkOrderNo("WO-001");
        deviationRepo.save(dev);

        var found = deviationRepo.findByCode("DEV-001");
        assertNotNull(found);
        assertEquals(DeviationSeverity.MAJOR, found.getSeverity());
        assertEquals(DeviationStatus.OPEN, found.getStatus());
    }

    @Test
    @DisplayName("按检验单号查找偏差")
    void deviation_findByInspectionNo() {
        deviationRepo.save(new Deviation("D1", "偏差1", "INSPECTION", "INSP-001", DeviationSeverity.MINOR));
        deviationRepo.save(new Deviation("D2", "偏差2", "EQUIPMENT", "INSP-001", DeviationSeverity.MAJOR));
        deviationRepo.save(new Deviation("D3", "偏差3", "HUMAN", "INSP-002", DeviationSeverity.CRITICAL));

        assertEquals(2, deviationRepo.findByInspectionNo("INSP-001").size());
    }

    @Test
    @DisplayName("获取活跃偏差（排除已关闭和已取消）")
    void deviation_getActiveDeviations() {
        var d1 = new Deviation("D1", "活跃", "INSPECTION", "INSP-001", DeviationSeverity.MINOR);
        deviationRepo.save(d1);
        var d2 = new Deviation("D2", "已关闭", "INSPECTION", "INSP-002", DeviationSeverity.MAJOR);
        d2.startInvestigation("调查员");
        d2.completeInvestigation("根因", "影响");
        d2.dispose(DeviationDisposition.REWORK, "返工", "QA");
        d2.resolve();
        d2.close();
        deviationRepo.save(d2);

        var active = deviationRepo.findByStatusNotIn(
                List.of(DeviationStatus.CLOSED, DeviationStatus.CANCELLED));
        assertEquals(1, active.size());
        assertEquals("D1", active.get(0).getCode());
    }

    // ==================== Capa ====================

    @Test
    @DisplayName("保存并查询 CAPA")
    void capa_saveAndFind() {
        var capa = new Capa("CAPA-001", "测试CAPA", "DEV-001", "问题描述", "张三");
        capa.setDueDate(Instant.now());
        capaRepo.save(capa);

        var found = capaRepo.findByCode("CAPA-001");
        assertNotNull(found);
        assertEquals("张三", found.getAssignTo());
        assertEquals(CapaStatus.OPEN, found.getStatus());
    }

    @Test
    @DisplayName("按偏差编号查找 CAPA")
    void capa_findByDeviationCode() {
        capaRepo.save(new Capa("C1", "CAPA1", "DEV-001", "问题1", "张三"));
        capaRepo.save(new Capa("C2", "CAPA2", "DEV-001", "问题2", "李四"));
        capaRepo.save(new Capa("C3", "CAPA3", "DEV-002", "问题3", "王五"));

        assertEquals(2, capaRepo.findByDeviationCode("DEV-001").size());
    }

    @Test
    @DisplayName("获取活跃 CAPA（排除已关闭和已取消）")
    void capa_getActiveCapas() {
        var c1 = new Capa("C1", "活跃CAPA", "DEV-001", "问题", "张三");
        capaRepo.save(c1);
        var c2 = new Capa("C2", "已关闭CAPA", "DEV-002", "问题", "李四");
        c2.analyzeRootCause("根因");
        c2.executeActions("纠正", "预防");
        c2.verify("有效", "QA经理");
        c2.close();
        capaRepo.save(c2);

        var active = capaRepo.findByStatusNotIn(
                List.of(CapaStatus.CLOSED, CapaStatus.CANCELLED));
        assertEquals(1, active.size());
    }

    @Test
    @DisplayName("查询不存在的检验指令返回 null")
    void findNonExistent() {
        assertNull(orderRepo.findByInspectionNo("NONEXISTENT"));
        assertNull(planRepo.findByCode("NONEXISTENT"));
        assertNull(deviationRepo.findByCode("NONEXISTENT"));
        assertNull(capaRepo.findByCode("NONEXISTENT"));
    }
}
