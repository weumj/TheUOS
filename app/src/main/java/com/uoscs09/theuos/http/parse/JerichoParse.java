package com.uoscs09.theuos.http.parse;

import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.util.ArrayList;

/** JerichoParser를 사용하는 파서 */
public abstract class JerichoParse<T> implements IParseHttp<ArrayList<T>> {
	protected String htmlBody;

	protected JerichoParse(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public ArrayList<T> parse() throws IOException {
		Source source = new Source(htmlBody);
		return parseHttpBody(source);
	}

	protected abstract ArrayList<T> parseHttpBody(Source source) throws IOException;
}
