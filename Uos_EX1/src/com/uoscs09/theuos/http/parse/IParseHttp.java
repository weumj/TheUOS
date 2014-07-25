package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.List;
/** 필요한 정보를 parsing하는 interface*/
public interface IParseHttp {
	/** 주어진 정보를 parsing하여 List로 반환한다.*/
	public List<?> parse() throws IOException;
}
