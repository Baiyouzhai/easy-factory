# easy-factory 协作文档

## 文档导航

### 🏗️ 总体设计
- [项目总体设计](architecture/overview.md) — 项目愿景、DDD 统一语言、模块全景、技术选型
- [**设计裁定**](architecture/design-decisions.md) — ★ 所有待决策项的最终裁定（各模块会话必读）
- [领域模型总图](architecture/domain-model.md) — 完整领域模型定义（143 个定义）
- [贯穿示例](architecture/walkthrough.md) — 阿莫西林片剂全流程穿通所有模块
- [数据模型](architecture/data-model.md) — 跨模块 ER 图 + 核心表设计

### 📦 模块设计
- [easy-factory-common](architecture/modules/common.md) — 基础数据层
- [easy-factory-core](architecture/modules/core.md) — 领域内核
- [easy-factory-mes](architecture/modules/mes.md) — 制造执行系统
- [easy-factory-qms](architecture/modules/qms.md) — 质量管理系统
- [easy-factory-plm](architecture/modules/plm.md) — 研发工艺系统
- [easy-factory-equip](architecture/modules/equip.md) — 设备工艺系统
- [easy-factory-lims](architecture/modules/lims.md) — 配方系统
- [easy-factory-erp](architecture/modules/erp.md) — 资源计划集成
- [easy-factory-iot](architecture/modules/iot.md) — 设备互联
- [easy-factory-web](architecture/modules/web.md) — Web 门户
- [easy-factory-eam](architecture/modules/eam.md) — 企业资产管理
- [easy-factory-mps](architecture/modules/mps.md) — 主生产计划
- [easy-factory-aps](architecture/modules/aps.md) — 高级排程系统
- [easy-factory-wms](architecture/modules/wms.md) — 仓储管理系统
- [easy-factory-andon](architecture/modules/andon.md) — 安灯异常呼叫
- [easy-factory-bi](architecture/modules/bi.md) — 看板与报表
- [easy-factory-scm](architecture/modules/scm.md) — 供应链管理
- [easy-factory-dms](architecture/modules/dms.md) — 文档管理（GMP合规）
- [easy-factory-crm](architecture/modules/crm.md) — 客户关系管理

### 🔧 实现
- [实施路线图](implementation/roadmap.md) — 五阶段实施顺序（★ 必读）
- [开发指南](implementation/guide.md) — 环境搭建、模块开发顺序、构建部署
- [编码规范](implementation/conventions.md) — 命名、接口设计、异常处理、测试规范

### 🤖 AI 协作
- [AI 协作指南](ai-collaboration/guide.md) — 与 AI 高效协作完成本项目开发

### 📋 问题反馈
- [问题反馈模板](feedback/template.md) — Bug 报告、功能需求、设计讨论
- [协作协议](feedback/collaboration-protocol.md) — 会话间协作反馈流程

### 📊 项目进度
- [PROJECT_STATUS.md](../PROJECT_STATUS.md) — 各模块工作状态、待办事项、技术债务

---

## 项目简介

**easy-factory** 是一个基于领域驱动设计（DDD）的制造系统平台。它以 `easy-factory-core` 为领域内核，定义工序、动作、资源、蓝图等统一语言，衍生出 MES、QMS、PLM、LIMS 等子系统，构建制造企业全价值链的数字化协作平台。

### 核心设计原则

1. **面向接口编程** — core 只定义契约，不绑定实现
2. **模块化松耦合** — 各子系统独立演进，通过 core 共享语义
3. **脚本化扩展** — 动作逻辑可通过 JavaScript 动态注入
4. **人机料法环 (4M1E)** — 资源分类对齐制造业质量管理标准
