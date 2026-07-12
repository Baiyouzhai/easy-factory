package com.byz.factory.core;

import com.byz.factory.testing.IProductChecker;
import com.byz.factory.testing.ProductChecker;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工厂
 * @author 苏政
 */
public class Factory implements IFactory {
	
	protected List<IProcess> processes;

	@Override
	public List<IProcess> getProcesses() {
		return processes;
	}
	public void setProcesses(List<IProcess> processes) {
		this.processes = processes;
	}

}
