package com.byz.factory.equip.repository;

import com.byz.factory.equip.EquipTestConfig;
import com.byz.factory.equip.MachineStatus;
import com.byz.factory.equip.model.Equipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = EquipTestConfig.class)
@DisplayName("EquipmentRepository 集成测试")
class EquipmentRepositoryTest {

    @Autowired
    private EquipmentRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    @DisplayName("保存并查询设备 — findByCode")
    void saveAndFindByCode() {
        // Given
        Equipment eq = new Equipment("EQ-001", "反应釜", "RF-2000");

        // When
        repo.save(eq);
        Equipment found = repo.findByCode("EQ-001");

        // Then
        assertNotNull(found);
        assertEquals("反应釜", found.getName());
        assertEquals(MachineStatus.IDLE, found.getStatus());
    }

    @Test
    @DisplayName("按状态查找设备 — findByStatus")
    void findByStatus() {
        // Given
        Equipment eq1 = new Equipment("EQ-001", "反应釜", "RF-2000");
        Equipment eq2 = new Equipment("EQ-002", "离心机", "CF-500");
        eq2.startProduction();
        repo.saveAll(List.of(eq1, eq2));

        // When
        List<Equipment> idles = repo.findByStatus(MachineStatus.IDLE);
        List<Equipment> running = repo.findByStatus(MachineStatus.RUNNING);

        // Then
        assertEquals(1, idles.size());
        assertEquals(1, running.size());
    }

    @Test
    @DisplayName("按类别查找 — findByCategory")
    void findByCategory() {
        // Given
        Equipment eq = new Equipment("EQ-001", "反应釜", "RF-2000");
        eq.setCategory("反应设备");
        repo.save(eq);

        // When
        List<Equipment> found = repo.findByCategory("反应设备");

        // Then
        assertEquals(1, found.size());
    }

    @Test
    @DisplayName("按位置查找 — findByLocation")
    void findByLocation() {
        // Given
        Equipment eq = new Equipment("EQ-001", "反应釜", "RF-2000");
        eq.setLocation("车间A-3号工位");
        repo.save(eq);

        // When
        List<Equipment> found = repo.findByLocation("车间A-3号工位");

        // Then
        assertEquals(1, found.size());
    }

}
