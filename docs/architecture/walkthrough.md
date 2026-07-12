# 贯穿示例：阿莫西林片剂生产

本文档用"药厂生产阿莫西林片剂"这个具体案例，贯穿所有模块，展示数据如何在各系统间流转。

## 产品概述

```
产品: 阿莫西林片剂 500mg
批量: 100,000 片 / 批
执行工厂: FACTORY-01 (口服固体制剂车间)
```

## 阶段 1：PLM — 工艺路线设计

工艺工程师在 PLM 中创建阿莫西林片剂的工艺路线：

```
Blueprint: "阿莫西林片剂-工艺v1.0"
├── 工序001: 称量配料
│   ├── Action: 领取原料    (Execute: ADD, 从仓库领料)
│   ├── Action: 称量API     (Execute: USE, 称阿莫西林原料药 500g)
│   ├── Action: 称量辅料    (Execute: USE, 淀粉 4500g + 硬脂酸镁 50g)
│   └── Action: 复核称量    (Execute: NOTING, Control: INTERRUPT)
│
├── 工序002: 制粒
│   ├── Action: 湿法制粒    (Execute: CONVERT, 设备: 湿法制粒机 WG-001)
│   ├── Action: 干燥         (Execute: CHANGE, 参数: 60°C, 120min)
│   └── Action: 整粒         (Execute: CHANGE, 24目筛网)
│
├── 工序003: 总混
│   ├── Action: 加入润滑剂    (Execute: COMBINE, 硬脂酸镁50g)
│   └── Action: 混合         (Execute: CONVERT, 设备: 三维混合机 MX-001, 30min)
│
├── 工序004: 中间体检验  (← QMS 介入)
│   ├── Action: 取样         (Execute: SPLIT, 取20g送检)
│   ├── Action: 水分检测     (Execute: NOTING, QMS方法: 卡尔费休法)
│   ├── Action: 含量检测     (Execute: NOTING, QMS方法: HPLC)
│   └── Action: 质量判定     (Execute: NOTING, Control: INTERRUPT)
│
├── 工序005: 压片
│   ├── Action: 安装模具     (Execute: ADD, 设备: 压片机 PT-001, 模具: Φ12mm)
│   ├── Action: 调试参数     (Execute: CHANGE, 压力: 15kN, 转速: 30rpm)
│   ├── Action: 压片         (Execute: CONVERT, 颗粒 → 素片)
│   └── Action: 片重监测     (Execute: NOTING, QMS: 每15分钟取样10片)
│
├── 工序006: 包衣
│   ├── Action: 配制包衣液    (Execute: CREATE, 包衣粉 + 纯化水)
│   ├── Action: 包衣         (Execute: CHANGE, 设备: 包衣机 CY-001)
│   └── Action: 外观检查     (Execute: NOTING, QMS: 目检)
│
├── 工序007: 内包装
│   ├── Action: 铝塑包装     (Execute: CONVERT, 设备: 泡罩包装机)
│   └── Action: 批号打印     (Execute: CHANGE, 批号: B20260712-001)
│
└── 工序008: 外包装
    ├── Action: 装盒         (Execute: CONVERT)
    ├── Action: 赋码         (Execute: CHANGE, 药品追溯码)
    └── Action: 装箱入库     (Execute: TRANSFER, 目标: 成品库)
```

## 阶段 2：LIMS — 配方管理

基于 PLM 的工艺路线，配方工程师在 LIMS 中管理配方版本：

```
Formula: 阿莫西林片剂-配方v3 (关联 Blueprint v1.0)
├── batchSize: 100,000 片

Phase1: 称量
├── 阿莫西林 API       500g    (允差±0.5%, 危险等级: 高活性)
├── 淀粉                4500g   (允差±1%)
└── 硬脂酸镁(内加)      25g     (允差±5%)

Phase2: 总混
└── 硬脂酸镁(外加)      25g     (允差±5%, 投料方式: 过60目筛加入)

Phase3: 包衣
├── 包衣粉              200g    (允差±2%)
└── 纯化水              800g    (允差±5%)
```

## 阶段 3：ERP — 主数据同步

ERP 提供物料主数据，LIMS/PLM 通过编码关联：

```
ERP物料主数据 → 本地缓存(erp_material_cache)
├── MAT-001: 阿莫西林原料药 (ROH, 批次管理, 保质期36月, GHS: 高活性)
├── MAT-002: 淀粉           (ROH, 批次管理, 保质期24月)
├── MAT-003: 硬脂酸镁       (ROH, 批次管理, 保质期24月)
├── MAT-004: 包衣粉         (ROH, 批次管理, 保质期12月)
├── MAT-005: 纯化水         (ROH, 非批次管理)
├── MAT-006: 铝箔           (ROH, 批次管理)
├── MAT-007: 纸盒           (ROH, 非批次管理)
├── MAT-008: 说明书         (ROH, 非批次管理)
└── MAT-100: 阿莫西林片剂   (FERT, 批次管理, 保质期24月)
```

库存检查（工单下达前实时的 ERP 查询）：
```
物料    需求     可用库存  状态
MAT-001  500g    2000g    ✅
MAT-002  4500g   50000g   ✅
MAT-003  50g     500g     ✅
MAT-004  200g    1000g    ✅
```

## 阶段 4：MES — 工单执行

### 4.1 创建工单

```json
{
  "workOrderNo": "WO-20260712-001",
  "product": "阿莫西林片剂 500mg",
  "blueprint": "阿莫西林片剂-工艺v1.0",
  "formula": "阿莫西林片剂-配方v3",
  "batchNo": "B20260712-001",
  "quantity": 100000,
  "factoryCode": "FACTORY-01",
  "status": "CREATED"
}
```

### 4.2 工序流转

```
WO-20260712-001 工单执行时间线:

08:00  下达 → 工单状态: RELEASED
08:05  开工 → 工单状态: IN_PROGRESS

08:10  工序001-称量配料 开始
       ├── 08:15  动作:领取原料 → ERP物料发放  ← ERP交互
       ├── 08:20  动作:称量API → LIMS称量任务   ← LIMS交互
       │   实际称量: 500.2g, 偏差: 0.04% ✅ (允差0.5%)
       │   ...称量记录写入 lims_weighing_item
       ├── 08:25  动作:称量辅料
       │   淀粉: 4501g, 偏差0.02% ✅
       │   硬脂酸镁: 25.05g, 偏差0.2% ✅
       └── 08:30  工序001完成 → mes_process_record(status=COMPLETED)

08:35  工序002-制粒 开始
       ├── 动作:湿法制粒
       │   → Equip获取设备状态: WG-001 = IDLE ✅
       │   → IoT下发设备参数: 搅拌速度150rpm, 切刀1000rpm
       │   → IoT采集实时数据: 电流、温度
       │   → 设备状态: WG-001 = RUNNING
       ├── 动作:干燥
       │   → IoT下发: 进风温度60°C, 时间120min
       │   → IoT采集: 温度曲线 (每分钟1个点)
       └── 10:45  工序002完成

10:50  工序003-总混 开始
       ├── 动作:加入润滑剂 (硬脂酸镁25g过60目筛)
       └── 动作:混合
           → Equip: MX-001 = RUNNING
           → IoT: 转速10rpm, 时间30min

11:25  工序004-中间体检验 开始  ← QMS核心介入点
       ├── 动作:取样
       │   → 从母批中取20g样品 (Execute: SPLIT)
       │   → 样品标签: QC-SAMPLE-001
       │
       ├── 动作:水分检测
       │   → QMS获取检验方案: IPQC-PLAN-001
       │   → 项目: 水分, 方法: 卡尔费休法, 标准: ≤3.0%
       │   → 实测值: 2.1% → qms_inspection_record(judgement=PASS)
       │
       ├── 动作:含量检测
       │   → 项目: 阿莫西林含量, 方法: HPLC, 标准: 95.0%-105.0%
       │   → 实测值: 98.3% → qms_inspection_record(judgement=PASS)
       │
       └── 动作:质量判定 (Control: INTERRUPT)
           → 全部通过 → 放行 ✅

           # 如果失败怎么办？
           # 假设水分 = 3.5% (> 3.0%上限)
           # → judgement = FAIL
           # → QMS自动创建偏差: DEV-001 "水分超标"
           # → MES工序004状态: INTERRUPTED (中断)
           # → 偏差处置: REWORK (返工 → 回工序002重新干燥)
           # → 返工完成 → 偏差关闭 → MES工序恢复

12:00  工序005-压片 开始
       ├── 动作:安装模具 → Equip: PT-001, 模具Φ12mm
       ├── 动作:调试参数 → IoT下发: 压力15kN, 转速30rpm
       ├── 动作:压片
       │   → 每15分钟自动取样10片做片重差异
       │   → SPC实时监控  ← QMS SPC
       │
       └── 15:00  压片完成, 片重SPC无异常

15:05  工序006-包衣 ...
       └── ... (略)

16:00  工序007-内包装 ...
       ├── 动作:铝塑包装
       │   → 10片/板, 设备: 泡罩包装机
       └── 动作:批号打印 → B20260712-001

16:30  工序008-外包装 ...
       └── 动作:装箱入库
           → TRANSFER: 成品 → 成品库
           → ERP成品入库回传 ← ERP交互

17:00  完工
       → MES: 工单状态 COMPLETED
       → LIMS: 批记录生成 → 送审
       → ERP: 成品库存 +100,000片, 物料消耗回传
```

## 阶段 5：QMS — 质量贯穿

### 检验方案 (qms_inspection_plan)

```json
{
  "code": "IPQC-PLAN-001",
  "productCode": "阿莫西林片剂",
  "processCode": "PROCESS-004",
  "inspectionType": "IPQC",
  "items": [
    {"code": "MOISTURE", "name": "水分", "method": "卡尔费休法", "lsl": 0, "usl": 3.0, "unit": "%"},
    {"code": "ASSAY", "name": "含量", "method": "HPLC", "lsl": 95.0, "usl": 105.0, "unit": "%"}
  ]
}
```

### SPC 数据（片重差异，工序005压片）

```
时间    样本1(mg) 样本2(mg) ... 样本10(mg)  均值  范围   判定
15:15   502      498          505        501   7     ✅
15:30   503      497          508        502  11     ✅
15:45   504      496          510        503  14     ⚠️ Rule3: 连续递增
16:00   510      495          520        508  25     ❌ 超出USL

16:00 QMS触发报警 → 设备停机检查 → 偏差DEV-002 "压片片重偏移"
     → CAPA: 检查压片机送料均匀性 → 调整参数 → 重新开机
```

## 阶段 6：IoT — 设备数据

### 制粒机 WG-001 的数据采集

```
时间     参数                    设定值  实际值  质量
10:00   Temperature_Inlet        60.0   60.2   GOOD
10:01   Temperature_Inlet        60.0   59.8   GOOD
10:02   Temperature_Inlet        60.0   63.0   BAD  ← 超差!
       → Alarm: CRITICAL, "进风温度超限" → 联动Equip: 设备暂停
10:03   Temperature_Inlet        60.0   60.1   GOOD  (已恢复)
...
```

### 设备 OEE 计算

```
压片机 PT-001 当班OEE:
├── 可用率(A): 计划480min - 停机40min = 440min / 480min = 91.7%
├── 性能率(P): 实际产出95,000片 / 理论产出110,000片 = 86.4%
├── 质量率(Q): 合格品94,000片 / 总产出95,000片 = 98.9%
└── OEE = 91.7% × 86.4% × 98.9% = 78.4%
```

## 阶段 7：批记录归档

```json
{
  "batchNo": "B20260712-001",
  "productCode": "阿莫西林片剂 500mg",
  "blueprint": "阿莫西林片剂-工艺v1.0",
  "formula": "阿莫西林片剂-配方v3",
  "batchSize": 100000,
  "actualYield": 98750,
  "yieldPercent": 98.75,
  "traceRecords": [
    // 8个工序的所有动作记录 + 称量记录 + 检验记录 + 设备参数 + 偏差
  ],
  "deviations": ["DEV-002: 压片片重偏移(已关闭)"],
  "reviewedBy": "质量主管",
  "approvedBy": "质量受权人",
  "status": "APPROVED"
}
```

## 数据流转全景

```
ERP(物料主数据) ──→ LIMS(配方) ──→ MES(工单)
                      │                │
                      │           ┌────┴────┐
                      │           │ 工序流转  │
                      │           └────┬────┘
                      │                │
              ┌───────┴────────┬───────┴────────┬──────────┐
              ▼                ▼                ▼          ▼
          称量记录         检验触发         设备指令    物流操作
         (LIMS)          (QMS)            (IoT)      (ERP)
              │                │                │          │
              ▼                ▼                ▼          ▼
          批记录 ←────────── 追溯链 ←──────── OEE     库存
              │
              ▼
           质量受权人审批 → 放行
```

这就是 easy-factory 全模块串联的真实样子。
