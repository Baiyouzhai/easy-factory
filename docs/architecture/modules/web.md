# easy-factory-web — Web 门户

## 模块定位

聚合所有业务模块，提供统一的 Web 入口。基于 Spring Boot 构建，作为各模块 REST API 的统一网关和前端宿主。

## 架构模式

采用**模块化单体**起步，后续可按需拆分为微服务：

```
                    easy-factory-web
                    (Spring Boot)
                          │
              ┌───────────┼───────────┐
              │           │           │
         MES API     QMS API     PLM API  ...
              │           │           │
         ┌────▼────┐ ┌───▼────┐ ┌───▼────┐
         │ MES模块  │ │QMS模块 │ │PLM模块 │
         │(Service)│ │(Service)│ │(Service)│
         └────┬────┘ └───┬────┘ └───┬────┘
              │           │           │
              └───────────┼───────────┘
                          │
                   ┌──────▼──────┐
                   │  core 领域   │
                   └─────────────┘
```

## 项目结构

```
easy-factory-web/
├── pom.xml
├── src/main/java/com/byz/factory/web/
│   ├── EasyFactoryApplication.java     — 启动类
│   ├── config/
│   │   ├── WebConfig.java              — Web 配置 (CORS/拦截器)
│   │   ├── SwaggerConfig.java          — API 文档配置
│   │   └── ModuleAutoConfig.java       — 各模块自动装配
│   ├── controller/
│   │   ├── mes/
│   │   │   ├── WorkOrderController.java
│   │   │   └── ProcessController.java
│   │   ├── qms/
│   │   │   ├── InspectionController.java
│   │   │   └── DeviationController.java
│   │   ├── plm/
│   │   │   └── BlueprintController.java
│   │   ├── equip/
│   │   │   └── EquipmentController.java
│   │   ├── lims/
│   │   │   └── FormulaController.java
│   │   └── iot/
│   │       └── DeviceController.java
│   ├── dto/                             — 请求/响应 DTO
│   ├── common/
│   │   ├── Result.java                  — 统一响应体
│   │   ├── PageResult.java              — 分页响应
│   │   └── GlobalExceptionHandler.java  — 全局异常处理
│   └── security/                        — 认证授权 (后续)
├── src/main/resources/
│   ├── application.yml                  — 主配置
│   ├── application-dev.yml              — 开发环境
│   ├── application-prod.yml             — 生产环境
│   └── static/                          — 前端静态资源
└── src/test/
    └── java/com/byz/factory/web/
        └── EasyFactoryApplicationTests.java
```

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": "2026-07-12T10:30:00Z",
  "traceId": "a1b2c3d4"
}
```

## 模块装配策略

各模块通过 Spring `@Configuration` + `@ConditionalOnClass` 实现按需装配：

```java
@Configuration
@ConditionalOnClass(MesService.class)
public class MesAutoConfiguration {
    @Bean
    public MesService mesService() { ... }
}
```

web 模块 POM 中可选引入各模块依赖，未引入的模块其 Controller 和 Service 不会加载。

## API 文档

使用 SpringDoc (OpenAPI 3.0) 生成 API 文档：
- 开发环境: `http://localhost:8080/swagger-ui.html`
- 分组按模块: MES / QMS / PLM / Equip / LIMS / IoT / ERP

## 认证与授权 (规划)

```
认证: JWT Token (无状态)
授权: RBAC (角色-权限)

角色预设:
├── ADMIN       — 系统管理员
├── ENGINEER    — 工艺工程师/配方工程师
├── OPERATOR    — 操作工
├── INSPECTOR   — 质检员
├── SUPERVISOR   — 班组长/车间主任
└── VIEWER      — 只读用户（审计/访客）
```

## 前端 (规划)

- 技术栈: Vue 3 / React (待定)
- 嵌入方式: Spring Boot 静态资源 或 独立部署 (Nginx 反向代理)
- 微前端: 按模块拆分子应用 (MES/QMS/PLM各自独立前端)

## 外部接口汇总

| 模块 | 基础路径 | 主要资源 |
|------|---------|---------|
| MES | `/api/mes/work-orders` | work-orders, processes, actions |
| QMS | `/api/qms/inspections` `/api/qms/deviations` `/api/qms/capas` | inspection-plans, inspection-records, deviations, capas |
| PLM | `/api/plm/blueprints` | blueprints, process-templates, parameters, bom, change-requests |
| Equip | `/api/equip/equipments` | equipments, recipes, parameters, oee |
| LIMS | `/api/lims/formulas` `/api/lims/weighing-tasks` `/api/lims/batch-records` | formulas, weighing-tasks, batch-records |
| IoT | `/api/iot` | devices, commands, alarms |
| ERP | `/api/erp` | materials, inventory, transactions |
| SCM | `/api/scm` | suppliers, purchase-orders |
| DMS | `/api/dms` | documents, approval-workflows, audit-trails |
| WMS | `/api/wms` | receipts, picking-tasks, inventory |
| MPS | `/api/mps` | plans, demands, capacity-check |
| APS | `/api/aps` | schedules |
| EAM | `/api/eam` | assets, maintenance-orders, calibrations |
| Andon | `/api/andon` | calls, escalation-rules, dashboard |
| BI | `/api/bi` | dashboards (production/quality/oee/inventory), kpi, batch-report |
| CRM | `/api/crm` | customers, orders, complaints |

## 当前实现（2026-07-25）

- [x] Spring Boot 3.x 启动类 `EasyFactoryApplication`
- [x] 统一响应体 `Result<T>` + 分页 `PageResult<T>`
- [x] 全局异常处理 `GlobalExceptionHandler`
- [x] CORS 全局配置 `WebConfig`
- [x] 20 个 REST Controller，覆盖全部 16 个业务模块（259 个端点）
- [x] SpringDoc OpenAPI 3.0（按模块 16 个 Group）
- [x] application.yml 配置
- [x] 37 个单元测试
- [ ] Service 实现类（Phase 5）
- [ ] 认证授权 JWT + RBAC
- [ ] DTO 对象（请求/响应分离）
- [ ] 前端 Vue 3
