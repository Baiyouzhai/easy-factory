# 会话间协作协议

## 会话角色

| 角色 | 会话 | 职责 |
|------|------|------|
| **协调者** | (当前) | 高层设计、架构裁定、跨模块约束、验收 |
| **core 会话** | 独立的 | easy-factory-core 的接口定义、枚举、基类、事件类型 |
| **模块会话** | 每个模块一个 | 业务模块的 model + service + repository + test 实现 |

## 协作流程

```
协调者（设计/裁定）
  │
  │  产出：模块设计文档、设计裁定文档
  │        任务卡（必读文档 + 产出清单 + 约束）
  │
  ▼
模块会话（实现）
  │
  │  执行中发现问题：
  │  ├── 模块边界内可自决的 → 直接实现，无需反馈
  │  ├── 涉及 core 接口的 → 写入模块文档"遗留问题" + [?] 标记
  │  └── 涉及架构/模式冲突的 → 写入模块文档"遗留问题" + [!] 标记
  │
  ▼
协调者检查
  │
  │  ┌─[?] 待审核 → 协调者裁定后改为 [√] 或给出方案
  │  └─[!] 阻塞   → 协调者介入，调整设计或修改约束
  │
  ▼
协调者更新设计文档/裁定文档 → 模块会话继续
```

## 任务卡模板

协调者发给模块会话的启动指令：

```markdown
处理模块: easy-factory-{module}
阶段: Phase {n}

必读文档:
  - docs/architecture/overview.md §3（核心层 vs 协作层）
  - docs/architecture/modules/{module}.md（模块设计）
  - docs/architecture/design-decisions.md（已裁定事项）
  - docs/implementation/roadmap.md（本模块在实施路线图中的位置）
  - PROJECT_STATUS.md 中该模块的当前状态

产出:
  1. model 实体 — 继承正确基类 + 实现 core 接口 + 业务便捷方法
  2. service 接口 — 方法签名完整，Javadoc 写明前置/后置条件
  3. repository 接口 — 如有持久化需求
  4. test 测试 — Given-When-Then + @DisplayName 中文

约束:
  - 编译期仅依赖 core，不依赖其它业务模块
  - 跨模块通信走 core 的 IDomainEvent + IExpand
  - 模型继承：有状态机 → BaseLifecycleEntity<S>；无状态机 → BaseEntity；资源 → AbstractResourceItem
  - 事件类型放 core event/types/{Module}EventTypes
  - 状态枚举实现 ILifecycle.StatusEnum，声明 allowedTransitions()

完成后:
  - 更新 PROJECT_STATUS.md 中对应模块的进度
  - 如有待审核事项: 在模块文档"遗留问题"中写 [?] 待审核 — {描述}
  - 如有阻塞问题: 写 [!] 阻塞 — {原因}
```

## 反馈标记约定

模块会话在实现中发现问题时，在模块文档的"遗留问题"章节使用以下前缀标记：

| 标记 | 含义 | 处理方式 |
|------|------|---------|
| `[√]` | 已裁定/已解决 | 记录裁定结果 |
| `[?]` | 待审核 | 协调者检查后裁定 |
| `[!]` | 阻塞 | 协调者介入调整设计 |
| `[×]` | 否决 | 不做，附否决原因 |

示例：

```markdown
## 遗留问题

### 待决策
1. [?] 待审核 — 称量数据来源走 IoT 还是 LIMS 直连天平？
2. [!] 阻塞 — core 的 IResourceItem 缺少批次号关联，称量无法区分批次
3. [√] 已裁定 — 批记录实时生成（边做边记），不在批完成后统一生成
```

## 协调者验收清单

模块会话完成后，协调者按以下清单验收：

```
□ mvn test -pl easy-factory-{module}     ← 模块测试全过
□ mvn test                                ← 全项目不破
□ PROJECT_STATUS.md 有更新记录            ← 有记录
□ 模型继承正确: BaseEntity vs BaseLifecycleEntity vs AbstractResourceItem
□ core 接口未改动（如有改动，需单独评估）
□ 事件类型命名符合 {module}.{entity}.{past_tense} 约定
□ 模块文档"遗留问题"中的 [?] / [!] 是否已处理
□ 新增的 IExpand key 前缀是否为 {module}.xxx
```

## 当前活跃的会话（2026-07-18）

| 会话 | 负责模块 | 状态 |
|------|---------|------|
| 协调者 | 全局设计/裁定 | 进行中 |
| 模块会话 | core（接口+枚举+基类） | v1 完成 |
| 模块会话 | APS/MPS/EAM/WMS/DMS/SCM 等 | 初步构建中 |
| 待分配 | mes（集成枢纽） | Phase 3 |
