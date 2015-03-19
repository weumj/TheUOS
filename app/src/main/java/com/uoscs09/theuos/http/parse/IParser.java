package com.uoscs09.theuos.http.parse;

/** 필요한 정보를 parsing 하는 interface*/
public interface IParser<Param, Return> {

	/** 주어진 정보를 parsing 한다.*/
	public Return parse(Param param) throws Exception;
}
