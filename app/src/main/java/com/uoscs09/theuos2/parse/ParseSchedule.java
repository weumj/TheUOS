package com.uoscs09.theuos2.parse;

import com.uoscs09.theuos2.common.AsyncLoader;
import com.uoscs09.theuos2.tab.schedule.BoardItem;
import com.uoscs09.theuos2.tab.schedule.ScheduleItem;
import com.uoscs09.theuos2.tab.schedule.ScheduleItemWrapper;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ParseSchedule implements IParser<String, ScheduleItemWrapper> {
    // private static final String SCH_LIST = "schList";
    // private static final String BOARD_LIST = "boardList";

    @Override
    public ScheduleItemWrapper parse(String body) throws Exception {
        // schList와 boardList Element들을 가져옴.
        Source source = new Source(body);
        final List<Element> elements = source.getChildElements().get(1).getChildElements();

        FutureTask<ArrayList<ScheduleItem>> scheduleParseTask = new FutureTask<>(
                new Callable<ArrayList<ScheduleItem>>() {
                    private final ParserScheduleInner parser = new ParserScheduleInner();

                    @Override
                    public ArrayList<ScheduleItem> call() throws Exception {
                        String body = elements.get(0).toString();
                        return parser.parse(body);
                    }
                });
        AsyncLoader.excute(scheduleParseTask);

        FutureTask<ArrayList<BoardItem>> boardParseTask = new FutureTask<>(

                new Callable<ArrayList<BoardItem>>() {
                    private final ParserBoard parser = new ParserBoard();

                    @Override
                    public ArrayList<BoardItem> call() throws Exception {
                        String body = elements.get(1).toString();
                        return parser.parse(body);
                    }
                });
        AsyncLoader.excute(boardParseTask);

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

        return new ScheduleItemWrapper(scheduleList, boardList);
    }

    private static class ParserScheduleInner extends OApiParser<ScheduleItem, ScheduleItem> {
        private static final String[] PATTERN_ARRAY = {"content", "sch_date", "year", "month"};

        @Override
        public ArrayList<ScheduleItem> parse(String body) throws IOException {
            return parseToArrayList(body.split(LIST), PATTERN_ARRAY);
        }

        @Override
        protected void initItem(ArrayList<String> parsedStringList, ArrayList<ScheduleItem> returningList) {
            returningList.add(new ScheduleItem(parsedStringList.toArray(new String[4])));
        }

    }

    private static class ParserBoard extends OApiParser<BoardItem, BoardItem> {
        private static final String[] PATTERN_ARRAY = {"seq", "notice_dt", "title", "content"};

        @Override
        public ArrayList<BoardItem> parse(String body) throws IOException {
            return parseToArrayList(body.split(LIST), PATTERN_ARRAY);
        }

        @Override
        protected void initItem(ArrayList<String> parsedStringList, ArrayList<BoardItem> returningList) {
            returningList.add(new BoardItem(parsedStringList.toArray(new String[4])));
        }

    }

}
