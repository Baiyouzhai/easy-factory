package com.byz.factory.dms.repository;

import com.byz.factory.dms.DocumentCategory;
import com.byz.factory.dms.DocumentStatus;
import com.byz.factory.dms.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 文档 Repository。
 *
 * @author 苏政
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /** 按文档编号查找 */
    Optional<Document> findByDocumentCode(String documentCode);

    /** 按类别查找 */
    List<Document> findByCategory(DocumentCategory category);

    /** 按状态查找 */
    List<Document> findByStatus(DocumentStatus status);

    /** 按标题模糊搜索 */
    List<Document> findByTitleContainingIgnoreCase(String keyword);

}
