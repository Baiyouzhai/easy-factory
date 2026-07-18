package com.byz.factory.operation.common;

/**
 * 通用操作模式 — 统一所有"输入→计算→输出"的运营分析模型。
 * <p>
 * 每个 Operation 是一个独立的计算单元，接收输入，返回结果。
 * 结果类型应继承 {@link OperationResult} 以获得耗时/消息/报告能力。
 *
 * @param <I> 输入类型
 * @param <O> 输出类型（应继承 OperationResult）
 * @author 苏政
 */
public interface IOperation<I, O extends OperationResult> {

    /**
     * 操作名称（人类可读，如 "产品-工厂匹配"）。
     *
     * @return 名称
     */
    String getName();

    /**
     * 操作分类（如 "能力检查", "时间计算", "资源需求", "产能分析"）。
     *
     * @return 分类
     */
    String getCategory();

    /**
     * 执行操作。
     *
     * @param input 输入参数
     * @return 结果
     */
    O execute(I input);

    /**
     * 执行并自动打印报告（调试/演示用）。
     *
     * @param input 输入参数
     * @return 结果
     */
    default O run(I input) {
        long start = System.currentTimeMillis();
        O result = execute(input);
        result.setElapsedMs(System.currentTimeMillis() - start);
        System.out.println(ReportPrinter.header(getName()));
        result.printReport();
        System.out.println();
        return result;
    }

}
