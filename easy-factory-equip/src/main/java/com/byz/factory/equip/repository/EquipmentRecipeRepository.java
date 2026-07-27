package com.byz.factory.equip.repository;

import com.byz.factory.equip.model.EquipmentRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备配方 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface EquipmentRecipeRepository extends JpaRepository<EquipmentRecipe, Long> {

    /** 按配方编码查找 */
    EquipmentRecipe findByCode(String code);

    /** 按设备编码查所有配方 */
    List<EquipmentRecipe> findByEquipmentCode(String equipmentCode);

    /** 按产品编码查配方 */
    List<EquipmentRecipe> findByProductCode(String productCode);

}
