package com.byz.factory.iot.repository;

import com.byz.factory.iot.CommandStatus;
import com.byz.factory.iot.model.Command;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 指令 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {

    /** 按指令编码查找 */
    Command findByCode(String code);

    /** 按设备编码查找 */
    List<Command> findByEquipmentCode(String equipmentCode);

    /** 按指令状态查找 */
    List<Command> findByStatus(CommandStatus status);

    /** 按设备+状态查找 */
    List<Command> findByEquipmentCodeAndStatus(String equipmentCode, CommandStatus status);

}
