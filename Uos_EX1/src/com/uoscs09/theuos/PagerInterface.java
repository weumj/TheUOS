package com.uoscs09.theuos;

public interface PagerInterface {
	public void sendCommand(Type type, Object data);

	public enum Type {
		PAGE, SETTING
	}
}
