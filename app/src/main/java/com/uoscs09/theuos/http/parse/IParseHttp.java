package com.uoscs09.theuos.http.parse;

/** 필요한 정보를 parsing하는 interface*/
public interface IParseHttp<T> {
	/** 주어진 정보를 parsing하여 List로 반환한다.*/
	public T parse() throws Exception;
}
