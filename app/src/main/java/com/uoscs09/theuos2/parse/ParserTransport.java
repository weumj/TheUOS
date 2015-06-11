package com.uoscs09.theuos2.parse;

import com.uoscs09.theuos2.tab.transport.TransportItem;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public class ParserTransport extends JerichoParser<ArrayList<TransportItem>> {
    private static final int BASIC = 0;
    private int howTo;

    @Override
    protected ArrayList<TransportItem> parseHttpBody(Source source) throws Exception {
        switch (howTo) {
            case BASIC:
                return parseMetroArrival(source);
            default:
                return null;
        }
    }

    private ArrayList<TransportItem> parseMetroArrival(Source source) {
        List<Element> row = source.getAllElements("row");
        ArrayList<TransportItem> list = new ArrayList<>();

        for (int i = 0; i < row.size(); i++) {
            Element e = row.get(i);
            String station = e.getAllElements("STATION_CD").get(0).getTextExtractor().toString();

            String time = e.getAllElements("ARRIVETIME").get(0).getTextExtractor().toString();

            String location = e.getAllElements("DESTSTATION_NAME").get(0).getTextExtractor().toString();

            list.add(new TransportItem(station, location, time));
        }
        return list;
    }

    public void setHowTo(int howTo) {
        this.howTo = howTo;
    }
}
