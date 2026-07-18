package com.byz.factory.factory.physical;

import java.time.Duration;

/**
 * 产线衔接 — 描述两个工序之间的物理衔接参数。
 * <p>
 * 替代 LineNode：产线不再"拥有"工位，而是描述工序如何串联。
 *
 * @author 苏政
 */
public record LineConnection(
    String fromProcessCode,      // 前工序
    String toProcessCode,        // 后工序
    Duration transferTime,       // 转运时间
    int bufferCapacity           // 中间缓冲容量（0=零库存, -1=无限）
) {
    public LineConnection {
        if (transferTime == null) transferTime = Duration.ZERO;
    }

    public static LineConnection of(String from, String to) {
        return new LineConnection(from, to, Duration.ZERO, 0);
    }

    public static LineConnection of(String from, String to, Duration transfer) {
        return new LineConnection(from, to, transfer, 0);
    }
}
