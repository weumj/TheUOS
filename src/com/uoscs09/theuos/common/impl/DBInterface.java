package com.uoscs09.theuos.common.impl;

import java.util.List;

public interface DBInterface<T> {
	public boolean insert(T item);

	public int update(T item);

	public int insertOrUpdate(T item);

	public int delete(T item);

	public T read(String query);

	public List<T> readAll(String query);

	public void close();

}
