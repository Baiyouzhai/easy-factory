# easy-factory-dms — 文档管理系统

## 模块定位

管理制造运营中的受控文档：SOP（标准操作规程）、批记录、检验报告、偏差报告、CAPA 报告。
DMS 是 GMP 合规的底层基础设施——所有 GxP 文件需要版本控制、审批流、审计追踪。

## 核心模型

### Document (文档)

```
Document:
├── code:             文档编号
├── title:            文档标题
├── category:         文档类别
│   ├── SOP            — 标准操作规程
│   ├── BATCH_RECORD   — 批记录
│   ├── INSPECTION     — 检验报告
│   ├── DEVIATION      — 偏差报告
│   ├── CAPA           — CAPA报告
│   ├── VALIDATION     — 验证文件
│   └── CALIBRATION    — 校准证书
├── version:          版本号
├── status:           状态 (DRAFT/UNDER_REVIEW/APPROVED/EFFECTIVE/OBSOLETED)
├── author:           作者
├── effectiveDate:    生效日期
├── reviewCycle:      复审周期(月)
├── nextReviewDate:   下次复审日期
└── content:          文档内容/附件
```

### ApprovalWorkflow (审批流)

```
ApprovalWorkflow:
├── documentCode:     关联文档
├── steps:            审批步骤列表
│   ├── stepNo:       步骤序号
│   ├── approverRole: 审批角色
│   ├── approver:     审批人
│   ├── decision:     审批决定 (APPROVED/REJECTED/NEEDS_REVISION)
│   ├── comment:      审批意见
│   └── timestamp:    审批时间
├── status:           流程状态
└── completedAt:      完成时间
```

### AuditTrail (审计追踪)

```
AuditTrail:
├── entityType:       实体类型 (DOCUMENT/BATCH_RECORD/INSPECTION)
├── entityId:         实体ID
├── action:           操作 (CREATE/UPDATE/REVIEW/APPROVE/OBSOLETE)
├── operator:         操作人
├── timestamp:        时间戳
├── before:           变更前内容(JSON)
├── after:            变更后内容(JSON)
└── reason:           变更原因
```

> AuditTrail 是所有模块共用的基础设施。核心模块（MES/QMS/LIMS/EAM）中的关键数据变更都应记录审计追踪。

## 模块间接口

```
DMS ← LIMS:  批记录 → 审批流 → 归档
DMS ← QMS:   偏差报告 / CAPA报告 → 审批
DMS ← PLM:   工艺路线 / SOP → 审批 → 生效
DMS ← EAM:   校准证书 → 归档
```

## 遗留问题

### 待决策
1. DMS 自建还是对接现有 EDMS（如 Veeva/OpenText/MasterControl）？
2. 电子签名合规：21 CFR Part 11（美国）/ 欧盟 Annex 11 的电子签名要求如何满足？
3. AuditTrail 的实现方式：AOP 切面自动记录 vs 各模块手动调用？
4. 文档模板：SOP/批记录是否需要在 easy-factory 内做模板引擎？

## AI 协作建议（2026-07-18）

DMS 是 GMP 合规的关键模块。core 已提供完整的审计追踪和电子签名基础设施。

### 推荐实施顺序

1. **使用 core 的 AuditTrail** — core 已定义 `AuditTrail` record（entityType, entityId, action, operator, timestamp, before, after, reason）。DMS 不应重新定义审计模型，应直接使用此 record 并通过 `DomainEventPublisher` 发布审计事件。

2. **使用 core 的电子签名** — `IElectronicSignature` + `SignatureMeaning`（REVIEWED/APPROVED/VERIFIED）。DMS 的审批工作流（DocumentStatus: DRAFT→UNDER_REVIEW→APPROVED/REJECTED→OBSOLETE）的每个状态转换应生成一个 `IElectronicSignature` 记录。

3. **回答待决策问题2（电子签名合规）**：
   - 签名绑定：`IElectronicSignature` 的 `getSignerId()` + `getTimestamp()` + `getReason()` 满足 21 CFR Part 11 的"签名+含义+时间"三要素
   - 审计追踪：每次签名的创建/修改/删除由 `AuditTrail` 自动记录
   - 不可否认性：签名的 record 应不可变（record 类型天然不可变）

4. **回答待决策问题3（AuditTrail 实现方式）**：
   - 推荐 **AOP 切面自动记录** 用于通用实体变更（CREATE/UPDATE/DELETE）
   - **手动调用** 用于业务语义事件（APPROVE/REJECT/OBSOLETE）——此时 before/after JSON 包含业务上下文

5. **桥接 IComplianceAction** — 实现 `IComplianceAction`，在 `execute()` 中集成清场检查+双人复核+电子签名，将 `ComplianceActionResult` 写入 AuditTrail。

