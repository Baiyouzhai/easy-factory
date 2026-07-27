package com.byz.factory.andon.service.impl;

import com.byz.factory.event.DomainEventPublisher;
import com.byz.factory.event.IDomainEvent;
import com.byz.factory.event.types.AndonEventTypes;
import com.byz.factory.andon.model.*;
import com.byz.factory.andon.repository.AndonCallRepository;
import com.byz.factory.andon.repository.EscalationRuleRepository;
import com.byz.factory.andon.service.AndonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AndonService 实现 — Andon 模块核心业务逻辑。
 * <p>
 * 提供安灯呼叫全生命周期管理和上报规则配置。
 * 写操作在 Service 层统一管理事务和领域事件发布。
 *
 * @author 苏政
 */
@Service
public class AndonServiceImpl implements AndonService {

    private final AndonCallRepository callRepo;
    private final EscalationRuleRepository ruleRepo;

    public AndonServiceImpl(AndonCallRepository callRepo, EscalationRuleRepository ruleRepo) {
        this.callRepo = callRepo;
        this.ruleRepo = ruleRepo;
    }

    // ==================== 呼叫管理 ====================

    @Override
    @Transactional
    public AndonCall trigger(TriggerType triggerType, AndonSeverity severity,
                             AndonSource source, String triggeredBy, String description) {
        String code = "ANDON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        AndonCall call = new AndonCall(code, triggerType, severity, source, triggeredBy, description);
        AndonCall saved = callRepo.save(call);
        publish(AndonEventTypes.ANDON_CALL_CREATED, Map.of(
                "callCode", saved.getCode(),
                "triggerType", triggerType.name(),
                "severity", severity.name(),
                "source", source.name(),
                "triggeredBy", triggeredBy,
                "description", description));
        return saved;
    }

    @Override
    @Transactional
    public AndonCall acknowledge(String callCode, String acknowledgedBy) {
        AndonCall call = requireCall(callCode);
        call.acknowledge(acknowledgedBy);
        return callRepo.save(call);
    }

    @Override
    @Transactional
    public AndonCall escalate(String callCode, String reason) {
        AndonCall call = requireCall(callCode);
        call.escalate(reason);
        return callRepo.save(call);
    }

    @Override
    @Transactional
    public AndonCall resolve(String callCode, String resolution) {
        AndonCall call = requireCall(callCode);
        call.resolve(resolution);
        return callRepo.save(call);
    }

    @Override
    @Transactional
    public AndonCall close(String callCode) {
        AndonCall call = requireCall(callCode);
        call.close();
        return callRepo.save(call);
    }

    // ==================== 呼叫查询 ====================

    @Override
    @Transactional(readOnly = true)
    public AndonCall findCall(String callCode) {
        return callRepo.findByCode(callCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AndonCall> getActiveCalls() {
        return callRepo.findAll().stream()
                .filter(AndonCall::isActive)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AndonCall> findCallsByWorkOrder(String workOrderNo) {
        return callRepo.findByWorkOrderNo(workOrderNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AndonCall> findCallsByEquipment(String equipmentCode) {
        return callRepo.findByEquipmentCode(equipmentCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AndonCall> getUrgentCalls() {
        return callRepo.findAll().stream()
                .filter(c -> c.isActive() && c.isUrgent())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AndonDashboard getDashboard() {
        List<AndonCall> activeCalls = getActiveCalls();

        int total = activeCalls.size();
        int urgentCount = (int) activeCalls.stream().filter(AndonCall::isUrgent).count();

        Map<String, Integer> bySeverity = new HashMap<>();
        Map<String, Integer> byTriggerType = new HashMap<>();

        for (AndonCall call : activeCalls) {
            bySeverity.merge(call.getSeverity().name(), 1, Integer::sum);
            byTriggerType.merge(call.getTriggerType().name(), 1, Integer::sum);
        }

        double avgResponse = activeCalls.stream()
                .filter(c -> c.getAcknowledgedAt() != null)
                .mapToLong(c -> c.getAcknowledgedAt().getEpochSecond() - c.getTriggeredAt().getEpochSecond())
                .average()
                .orElse(0.0) / 60.0; // 秒转分钟

        return new AndonDashboard(total, bySeverity, byTriggerType, urgentCount, avgResponse);
    }

    // ==================== 上报规则管理 ====================

    @Override
    @Transactional
    public EscalationRule createRule(TriggerType triggerType, AndonSeverity severity) {
        String code = "RULE-" + triggerType.name() + "-" + severity.name();
        EscalationRule rule = new EscalationRule(code, triggerType, severity);
        EscalationRule saved = ruleRepo.save(rule);
        publish(AndonEventTypes.RULE_CREATED, Map.of(
                "ruleCode", saved.getCode(),
                "triggerType", triggerType.name(),
                "severity", severity.name()));
        return saved;
    }

    @Override
    @Transactional
    public void addEscalationLevel(String ruleCode, EscalationRule.EscalationLevel level) {
        EscalationRule rule = requireRule(ruleCode);
        rule.addLevel(level);
        ruleRepo.save(rule);
        publish(AndonEventTypes.RULE_UPDATED, Map.of(
                "ruleCode", ruleCode,
                "levelCount", rule.getMaxLevel()));
    }

    @Override
    @Transactional(readOnly = true)
    public EscalationRule findRule(TriggerType triggerType, AndonSeverity severity) {
        return ruleRepo.findByTriggerTypeAndSeverity(triggerType, severity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscalationRule> getActiveRules() {
        return ruleRepo.findByEnabledTrue();
    }

    @Override
    @Transactional
    public void disableRule(String ruleCode) {
        EscalationRule rule = requireRule(ruleCode);
        rule.disable();
        ruleRepo.save(rule);
    }

    @Override
    @Transactional
    public void enableRule(String ruleCode) {
        EscalationRule rule = requireRule(ruleCode);
        rule.enable();
        ruleRepo.save(rule);
    }

    // ==================== 内部辅助 ====================

    private AndonCall requireCall(String callCode) {
        AndonCall call = callRepo.findByCode(callCode);
        if (call == null) {
            throw new IllegalArgumentException("安灯呼叫不存在: " + callCode);
        }
        return call;
    }

    private EscalationRule requireRule(String ruleCode) {
        EscalationRule rule = ruleRepo.findByCode(ruleCode);
        if (rule == null) {
            throw new IllegalArgumentException("上报规则不存在: " + ruleCode);
        }
        return rule;
    }

    private void publish(String eventType, Object payload) {
        DomainEventPublisher.publish(IDomainEvent.of(eventType, AndonEventTypes.PREFIX, payload));
    }

}
