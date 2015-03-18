package com.uoscs09.theuos.http.parse;

import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.tab.schedule.BoardItem;
import com.uoscs09.theuos.tab.schedule.ScheduleItem;
import com.uoscs09.theuos.tab.schedule.ScheduleItemWrapper;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ParseSchedule extends JerichoParse<ScheduleItemWrapper> {
    // private static final String SCH_LIST = "schList";
    // private static final String BOARD_LIST = "boardList";

    public ParseSchedule(String htmlBody) {
        super(htmlBody);
    }

    @Override
    protected ArrayList<ScheduleItemWrapper> parseHttpBody(Source source)
            throws IOException {
        // schList와 boardList Element들을 가져옴.
        final List<Element> elements = source.getChildElements().get(1)
                .getChildElements();

        FutureTask<ArrayList<ScheduleItem>> scheduleParseTask = new FutureTask<>(
                new Callable<ArrayList<ScheduleItem>>() {
                    @Override
                    public ArrayList<ScheduleItem> call() throws Exception {
                        String body = elements.get(0).toString();
                        return new ParseScheduleInner(body).parse();
                    }
                });
        AsyncLoader.excute(scheduleParseTask);

        FutureTask<ArrayList<BoardItem>> boardParseTask = new FutureTask<>(
                new Callable<ArrayList<BoardItem>>() {
                    @Override
                    public ArrayList<BoardItem> call() throws Exception {
                        String body = elements.get(1).toString();
                        return new ParseBoard(body).parse();
                    }
                });
        AsyncLoader.excute(boardParseTask);

        ArrayList<ScheduleItemWrapper> list = new ArrayList<>();
        ArrayList<ScheduleItem> scheduleList;
        ArrayList<BoardItem> boardList;

        // block current thread and wait for result
        try {
            scheduleList = scheduleParseTask.get();
        } catch (Exception e) {
            e.printStackTrace();
            scheduleList = new ArrayList<>();
        }
        try {
            boardList = boardParseTask.get();
        } catch (Exception e) {
            e.printStackTrace();
            boardList = new ArrayList<>();
        }

        // FIXME 인터페이스를 잘못 설계했기때문에 결과값을 이렇게 반환해야함.
        list.add(new ScheduleItemWrapper(scheduleList, boardList));
        return list;
    }

    private static class ParseScheduleInner extends OApiParse<ScheduleItem, ScheduleItem> {
        private static final String[] PATTERN_ARRAY = {"content", "sch_date", "year", "month"};

        protected ParseScheduleInner(String body) {
            super(body);
        }

        @Override
        public ArrayList<ScheduleItem> parse() throws IOException {
            return parseToArrayList(body.split(LIST), PATTERN_ARRAY);
        }

        @Override
        protected void initItem(ArrayList<String> parsedStringList, ArrayList<ScheduleItem> returningList) {
            returningList.add(new ScheduleItem(parsedStringList.toArray(new String[4])));
        }

    }

    private static class ParseBoard extends OApiParse<BoardItem, BoardItem> {
        private static final String[] PATTERN_ARRAY = {"seq", "notice_dt", "title", "content"};

        protected ParseBoard(String body) {
            super(body);
        }

        @Override
        public ArrayList<BoardItem> parse() throws IOException {
            return parseToArrayList(body.split(LIST), PATTERN_ARRAY);
        }

        @Override
        protected void initItem(ArrayList<String> parsedStringList, ArrayList<BoardItem> returningList) {
            returningList.add(new BoardItem(parsedStringList.toArray(new String[4])));
        }

    }

}
