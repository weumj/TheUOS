package com.uoscs09.theuos2.parse;

import net.htmlparser.jericho.Source;

/** JerichoParser 를 사용하는 파서 */
public abstract class JerichoParser<T> extends IParser.Base<String, T> {

	public T parse(String param) throws Exception {
		return parseHtmlBody(new Source(param));
	}

	protected abstract T parseHtmlBody(Source source) throws Exception;
}
