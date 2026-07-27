package com.byz.factory.andon.repository;

import com.byz.factory.andon.model.AndonSeverity;
import com.byz.factory.andon.model.EscalationRule;
import com.byz.factory.andon.model.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 上报规则 Repository。
 *
 * @author 苏政
 */
@Repository
public interface EscalationRuleRepository extends JpaRepository<EscalationRule, Long> {

    /** 按规则编码（code）查询 */
    EscalationRule findByCode(String code);

    /** 按触发类型和严重程度查询唯一规则 */
    EscalationRule findByTriggerTypeAndSeverity(TriggerType triggerType, AndonSeverity severity);

    /** 查询所有启用的规则 */
    List<EscalationRule> findByEnabledTrue();

}
