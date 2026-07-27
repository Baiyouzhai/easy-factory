package com.byz.factory.crm.repository;

import com.byz.factory.crm.ComplaintStatus;
import com.byz.factory.crm.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户投诉 Repository。
 *
 * @author easy-factory
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    /** 按投诉编号查找 */
    Optional<Complaint> findByComplaintNo(String complaintNo);

    /** 按客户编码查找 */
    List<Complaint> findByCustomerCode(String customerCode);

    /** 按订单号查找 */
    List<Complaint> findByOrderNo(String orderNo);

    /** 按状态查找 */
    List<Complaint> findByStatus(ComplaintStatus status);

    /** 按类型查找 */
    List<Complaint> findByType(String type);

    /** 按状态列表查找（用于查找活跃投诉） */
    List<Complaint> findByStatusIn(List<ComplaintStatus> statuses);

}
