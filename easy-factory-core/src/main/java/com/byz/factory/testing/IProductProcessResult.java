package com.byz.factory.testing;

import com.byz.factory.core.IProcess;

import java.io.PrintStream;

public interface IProductProcessResult {

    /**
     * 工序
     *
     * @return 工序
     */
    IProcess getProductionProcess();

    /**
     * 检查工序
     *
     * @return 检查工序
     */
    IProcess getCheckProcess();

    boolean pass();

    default String getCheckResult() {
        IProcess productionProcess = getProductionProcess();
        IProcess checkProcess = getCheckProcess();
        return "";
    }

    void printStackTrace();  void printStackTrace(PrintStream out);


}
