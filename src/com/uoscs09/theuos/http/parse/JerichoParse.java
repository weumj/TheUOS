package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.List;

import net.htmlparser.jericho.Source;

/** JerichoParser를 사용하는 파서 */
public abstract class JerichoParse<T> implements IParseHttp {
	protected String htmlBody;

	protected JerichoParse(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public List<T> parse() throws IOException {
		Source source = new Source(htmlBody);
		return parseHttpBody(source);
	}

	protected abstract List<T> parseHttpBody(Source source) throws IOException;
}
