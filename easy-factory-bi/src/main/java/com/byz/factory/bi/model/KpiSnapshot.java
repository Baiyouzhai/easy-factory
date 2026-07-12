package com.byz.factory.bi.model;

import com.byz.data.DataExpand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class KpiSnapshot extends DataExpand {
    private String period;            // 统计周期
    private BigDecimal planCompletionRate;  // 计划完成率
    private BigDecimal firstPassRate;       // 一次合格率
    private BigDecimal oee;                 // OEE综合效率
    private BigDecimal mttr;               // 平均维修时间
    private BigDecimal mtbf;               // 平均故障间隔
    private BigDecimal batchYield;         // 批次收率
    private BigDecimal deviationClosure;   // 偏差关闭率
}
