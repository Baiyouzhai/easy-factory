package com.byz.factory.testing;

import com.byz.factory.model.IProcess;

import java.io.PrintStream;

public class ProductProcessResult implements IProductProcessResult {

    protected IProcess productionProcess;
    protected IProcess checkProcess;

    @Override
    public IProcess getProductionProcess() {
        return productionProcess;
    }

    @Override
    public IProcess getCheckProcess() {
        return null;
    }

    public void setProductionProcess(IProcess process) {
        this.productionProcess = process;
    }

    @Override
    public boolean pass() {
        return false;
    }

    @Override
    public void printStackTrace() {
    }

    @Override
    public void printStackTrace(PrintStream out) {

    }

}
