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
