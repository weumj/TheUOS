package com.uoscs09.theuos.parse;

import net.htmlparser.jericho.Source;

import java.util.ArrayList;

/** JerichoParser 를 사용하는 파서 */
public abstract class JerichoParser<T> implements IParser<String, ArrayList<T>> {

	public ArrayList<T> parse(String param) throws Exception {
		return parseHttpBody(new Source(param));
	}

	protected abstract ArrayList<T> parseHttpBody(Source source) throws Exception;
}
