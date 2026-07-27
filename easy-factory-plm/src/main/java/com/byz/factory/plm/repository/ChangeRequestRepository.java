package com.byz.factory.plm.repository;

import com.byz.factory.plm.model.ChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 变更请求 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {

    /** 按编码查找 */
    Optional<ChangeRequest> findByCode(String code);

    /** 按关联蓝图查找 */
    List<ChangeRequest> findByBlueprintCode(String blueprintCode);

    /** 按状态查找 */
    List<ChangeRequest> findByStatus(ChangeRequest.ChangeRequestStatus status);

    /** 按蓝图查找非终端状态的变更请求 */
    List<ChangeRequest> findByBlueprintCodeAndStatusNotIn(String blueprintCode,
            List<ChangeRequest.ChangeRequestStatus> statuses);

    /** 按审批人查找 */
    List<ChangeRequest> findByApprovedBy(String approvedBy);

    /** 按申请人查找 */
    List<ChangeRequest> findByRequestedBy(String requestedBy);
}
