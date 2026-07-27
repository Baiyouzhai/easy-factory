package com.byz.factory.iot.repository;

import com.byz.factory.iot.model.DeviceConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备连接 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface DeviceConnectionRepository extends JpaRepository<DeviceConnection, Long> {

    /** 按设备编码查找 */
    DeviceConnection findByCode(String code);

    /** 按协议筛选 */
    List<DeviceConnection> findByProtocol(String protocol);

}
