package com.byz.factory.dms.repository;

import com.byz.factory.dms.model.ApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 审批流 Repository。
 *
 * @author 苏政
 */
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {

    /** 按关联文档编码查找审批流 */
    Optional<ApprovalWorkflow> findByDocumentCode(String documentCode);

}
