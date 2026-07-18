package com.byz.factory.shared;

public interface Dict {

    /**
     * 执行(动作, 反应) — 描述动作对资源的操作语义。
     */
    enum Execute {
        // ========== 基础操作 ==========

        /**
         * 无(默认, 缺省) — 空操作，占位或跳过
         */
        Nothing,
        /**
         * 创建 — 从无到有产生新资源<br/>
         * 如: (无) -> 铁矿石10
         */
        Create,
        /**
         * 添加 — 增加输入资源的数量作为输出<br/>
         * 如: 铁矿石10 -> 铁矿石20
         */
        Add,
        /**
         * 使用(消耗) — 减少输入资源的数量作为输出<br/>
         * 如: 铁矿石10 -> 铁矿石1
         */
        Use,

        // ========== 变换操作 ==========

        /**
         * 改变 — 改变资源属性，产生不同形式的输出<br/>
         * 如: 铁矿石 -> 铁 (化学反应/物理变化)
         */
        Change,
        /**
         * 转换(变换) — 调整输入内容数量并产生多种输出<br/>
         * 如: 铁矿石10 -> 铁矿石0 + 铁8 + 废料A1 + 废料B1
         */
        Convert,
        /**
         * 拆分 — 将一批资源拆分为多个独立批次<br/>
         * 如: 母批100kg -> 子批A 60kg + 子批B 40kg
         */
        Split,
        /**
         * 合并 — 将多个中间体合并为一个<br/>
         * 如: 中间体A 50kg + 中间体B 50kg -> 混合物 100kg
         */
        Combine,

        // ========== 流程控制操作 ==========

        /**
         * 转移 — 将资源从一个工位/工序转移到另一个<br/>
         * 如: 工序A的半成品 -> 工序B的输入
         */
        Transfer,
        /**
         * 暂存(挂起) — 暂停处理，等待外部条件满足<br/>
         * 如: 等待温度达到目标值、等待质检结果
         */
        Hold,
    }

    /**
     * 控制
     */
    enum Control {
        /**
         * 无(默认, 缺省)
         */
        Default,
        /**
         * 中断
         */
        Interrupt,
        /**
         * 计数
         */
        Count,
        /**
         * 定时
         */
        Timing,
        /**
         * 循环
         */
        Repeat,
    }

    /**
     * 重要性
     */
    enum Importance {
        /**
         * 可选
         */
        Optional,
        /**
         * 必要
         */
        Require,
    }

    /**
     * 资源组 (4M1E + Product)
     */
    enum SourceGroup {
        /**
         * 其它
         */
        Other,
        /**
         * 人员(人力)
         */
        Personnel,
        /**
         * 机器(设备/仪器/量检具)
         */
        Machine,
        /**
         * 物料(材料/原料/辅料)
         */
        Material,
        /**
         * 方法(工艺/SOP/检验标准)
         */
        Method,
        /**
         * 环境(温湿度/洁净度/压差)
         */
        Environment,
        /**
         * 产品(成品/半成品) — 生产活动的输出
         */
        Product,
    }

    /**
     * 资源类型
     */
    enum SourceType {
        Other,

        // ── 设备类 ──
        Machine,
        Equipment,
        Instrument,
        Tooling,

        // ── 人员类 ──
        Operator,
        Technician,
        Inspector,

        // ── 物料类 ──
        RawMaterial,
        WIP,
        FinishedGood,
        Packaging,
        Consumable,

        // ── 方法类 ──
        SOP,
        Specification,
        Drawing,

        // ── 环境类 ──
        Temperature,
        Humidity,
        Pressure,
        Cleanliness,
    }

}
