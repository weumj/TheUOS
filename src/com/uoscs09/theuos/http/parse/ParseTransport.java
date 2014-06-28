package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import com.uoscs09.theuos.tab.transport.TransportItem;

public class ParseTransport extends JerichoParse<TransportItem> {
	private int how;

	protected ParseTransport(String htmlBody, int how) {
		super(htmlBody);
	}

	@Override
	protected List<TransportItem> parseHttpBody(Source source)
			throws IOException {
		switch (how) {
		case ParseFactory.Value.BASIC:
			return parseMetroArrival(source);
		default:
			return null;
		}
	}

	private List<TransportItem> parseMetroArrival(Source source) {
		List<Element> row = source.getAllElements("row");
		ArrayList<TransportItem> list = new ArrayList<TransportItem>();
		for (int i = 0; i < row.size(); i++) {
			Element e = row.get(i);
			String station = e.getAllElements("STATION_CD").get(0)
					.getTextExtractor().toString();
			String time = e.getAllElements("ARRIVETIME").get(0)
					.getTextExtractor().toString();
			String location = e.getAllElements("DESTSTATION_NAME").get(0)
					.getTextExtractor().toString();
			list.add(new TransportItem(station, location, time));
		}
		return list;
	}
}
