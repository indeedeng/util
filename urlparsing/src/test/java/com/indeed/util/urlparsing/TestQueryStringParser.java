package com.indeed.util.urlparsing;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author ahudson
 * @author preetha
 */

public class TestQueryStringParser {

    private static final class URLParamsBuilder {
        private boolean first = true;

        private final StringBuilder urlParams = new StringBuilder();

        public void append(String s, int start, int end) {
            if (!first) {
                urlParams.append('&');
            }
            first = false;
            urlParams.append(s, start, end);
        }

        public void reset() {
            first = true;
            urlParams.delete(0, urlParams.length());
        }

        @Override
        public String toString() {
            return urlParams.toString();
        }
    }

    private static final class URLBuilderCallback implements QueryStringParserCallback<URLParamsBuilder> {
        @Override
        public void parseKeyValuePair(
                String urlParams, int keyStart, int keyEnd, int valueStart, int valueEnd, URLParamsBuilder paramsBuilder
        ) {
            paramsBuilder.append(urlParams, keyStart, valueEnd);
        }
    }

    private static void runTest(String input) {
        URLParamsBuilder paramsBuilder = new URLParamsBuilder();
        QueryStringParser.parseQueryString(input, new URLBuilderCallback(), paramsBuilder);
        assertEquals(input, paramsBuilder.toString());
        paramsBuilder = new URLParamsBuilder();
        QueryStringParser.parseQueryString(input, new URLBuilderCallback(), paramsBuilder, 0, input.length(), "&", "=");
        assertEquals(input, paramsBuilder.toString());
    }

    @Test
    public void testAllKeysParsing() {
        runTest("&&a=b&35lk=sadjlkj&xxx&yyy&blah=blah=blah&&&");
        runTest("a=b&x7x&x&x35lk=sadjlkj&blah=blah=blah&x=y");
    }

    @Test
    public void testSomeKeysParsing() {
        QueryStringParserCallbackBuilder<URLParamsBuilder> callbackBuilder = new QueryStringParserCallbackBuilder<URLParamsBuilder>();
        URLParamsBuilder paramsBuilder = new URLParamsBuilder();
        callbackBuilder.addCallback("x35lk", new URLBuilderCallback());
        callbackBuilder.addCallback("x", new URLBuilderCallback());
        QueryStringParser.parseQueryString("a=b&x7x&x&x35lk=sadjlkj&blah=blah=blah&x=y", callbackBuilder.buildCallback(), paramsBuilder);
        assertEquals("x&x35lk=sadjlkj&x=y", paramsBuilder.toString());
    }

    private static final class MutableInt {
        int value = 0;
    }

    private static final class TestRecord {
        MutableInt re = new MutableInt();
        MutableInt pe = new MutableInt();
        MutableInt pl = new MutableInt();
        MutableInt pt = new MutableInt();
        MutableInt zero = new MutableInt();
    }

    private static final class MutableIntParser implements QueryStringParserCallback<Object> {
        final MutableInt storage;

        private MutableIntParser(MutableInt storage) {
            this.storage = storage;
        }

        @Override
        public void parseKeyValuePair(String qs, int keyStart, int keyEnd, int valueStart, int valueEnd, Object ignored) {
            storage.value = ParseUtils.parseInt(qs, valueStart, valueEnd);
        }
    }
    /*
    This tests parsing with a pair-delimiter %26 and key-value delimiter %3D
    */
    @Test
    public void testCustomDelimiter() {
        final String url = "a%3Djspls%26tk%3D13l9q49sk065g2tu%26re%3D22%26pe%3D547%26pl%3D688%26pt%3D1235";
        final TestRecord record = new TestRecord();
        final QueryStringParserCallbackBuilder<Object> builder = new QueryStringParserCallbackBuilder<Object>();
        builder.addCallback("re", new MutableIntParser(record.re));
        builder.addCallback("pe", new MutableIntParser(record.pe));
        builder.addCallback("pl", new MutableIntParser(record.pl));
        builder.addCallback("pt", new MutableIntParser(record.pt));
        builder.addCallback("26re", new MutableIntParser(record.zero));

        QueryStringParserCallback<Object> callback = builder.buildCallback();
        QueryStringParser.parseQueryString(url, callback, record.re, 0, url.length(), "%26", "%3D");
        assertEquals(22, record.re.value);
        assertEquals(547, record.pe.value);
        assertEquals(688, record.pl.value);
        assertEquals(1235, record.pt.value);
        assertEquals(0, record.zero.value);
    }

}

