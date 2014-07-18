package com.uoscs09.theuos;

public interface PagerInterface {
	public Object sendCommand(Type type, Object data);

	public enum Type {
		PAGE, SETTING, INDEX
	}
}
