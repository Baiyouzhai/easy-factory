package com.byz.factory.factory.physical;

/**
 * 产线节点 — 表示工位在产线上的一个位置。
 * <p>
 * 同一个工位可以出现在多条产线的不同位置。
 * 例如：W03 整粒工位在 Line-A 的第3位，在 Line-B 的第2位。
 *
 * @author 苏政
 */
public class LineNode {

    private final String workstationCode;
    private final int sequence;

    public LineNode(String workstationCode, int sequence) {
        if (sequence < 1) throw new IllegalArgumentException("sequence must be >= 1");
        this.workstationCode = workstationCode;
        this.sequence = sequence;
    }

    /** 引用的工位编码 */
    public String getWorkstationCode() { return workstationCode; }

    /** 在该产线上的顺序号（从1开始） */
    public int getSequence() { return sequence; }

    @Override
    public String toString() {
        return "[" + sequence + "] → " + workstationCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineNode n)) return false;
        return sequence == n.sequence && workstationCode.equals(n.workstationCode);
    }

    @Override
    public int hashCode() {
        return 31 * workstationCode.hashCode() + sequence;
    }
}
