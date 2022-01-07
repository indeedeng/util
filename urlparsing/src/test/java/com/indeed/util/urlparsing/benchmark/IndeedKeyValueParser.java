package com.indeed.util.urlparsing.benchmark;

import com.indeed.util.urlparsing.ParseUtils;
import com.indeed.util.urlparsing.QueryStringParser;
import com.indeed.util.urlparsing.QueryStringParserCallback;
import com.indeed.util.urlparsing.QueryStringParserCallbackBuilder;

/** @author: preetha */
public class IndeedKeyValueParser implements KeyValueParser {
    private static final QueryStringParserCallback<JobSearchLogRecord> uidParser =
            new QueryStringParserCallback<JobSearchLogRecord>() {
                @Override
                public void parseKeyValuePair(
                        String urlParams,
                        int keyStart,
                        int keyEnd,
                        int valueStart,
                        int valueEnd,
                        JobSearchLogRecord storage) {
                    storage.getUid().append(urlParams, valueStart, valueEnd);
                }
            };

    private static final QueryStringParserCallback<JobSearchLogRecord> timestampParser =
            new QueryStringParserCallback<JobSearchLogRecord>() {
                @Override
                public void parseKeyValuePair(
                        String urlParams,
                        int keyStart,
                        int keyEnd,
                        int valueStart,
                        int valueEnd,
                        JobSearchLogRecord storage) {
                    storage.setTimestamp(
                            ParseUtils.parseTimestampFromUIDString(
                                    urlParams, valueStart, valueEnd));
                }
            };

    private static final QueryStringParserCallback<JobSearchLogRecord> queryParser =
            new QueryStringParserCallback<JobSearchLogRecord>() {
                @Override
                public void parseKeyValuePair(
                        String urlParams,
                        int keyStart,
                        int keyEnd,
                        int valueStart,
                        int valueEnd,
                        JobSearchLogRecord storage) {
                    ParseUtils.urlDecodeInto(urlParams, valueStart, valueEnd, storage.getQuery());
                }
            };

    private static final QueryStringParserCallback<JobSearchLogRecord> locationParser =
            new QueryStringParserCallback<JobSearchLogRecord>() {
                @Override
                public void parseKeyValuePair(
                        String urlParams,
                        int keyStart,
                        int keyEnd,
                        int valueStart,
                        int valueEnd,
                        JobSearchLogRecord storage) {
                    ParseUtils.urlDecodeInto(
                            urlParams, valueStart, valueEnd, storage.getLocation());
                }
            };

    private static final QueryStringParserCallback<JobSearchLogRecord> intValueParser =
            new QueryStringParserCallback<JobSearchLogRecord>() {
                @Override
                public void parseKeyValuePair(
                        String urlParams,
                        int keyStart,
                        int keyEnd,
                        int valueStart,
                        int valueEnd,
                        JobSearchLogRecord storage) {
                    storage.setNumResults(
                            ParseUtils.parseUnsignedInt(urlParams, valueStart, valueEnd));
                }
            };

    @Override
    public void parse(String logentry) {
        QueryStringParserCallbackBuilder<JobSearchLogRecord> builder =
                new QueryStringParserCallbackBuilder<JobSearchLogRecord>();
        builder.addCallback("uid", uidParser);
        builder.addCallback("uid", timestampParser);
        builder.addCallback("q", queryParser);
        builder.addCallback("l", locationParser);
        builder.addCallback("totCnt", intValueParser);

        final QueryStringParserCallback<JobSearchLogRecord> jobSearchLogRecordParser =
                builder.buildCallback();
        final JobSearchLogRecord record = new JobSearchLogRecord();
        QueryStringParser.parseQueryString(logentry, jobSearchLogRecordParser, record);
    }
}
