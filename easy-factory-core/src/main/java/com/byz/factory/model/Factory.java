package com.byz.factory.model;

import java.util.List;

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
