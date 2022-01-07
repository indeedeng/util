package com.indeed.util.urlparsing.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author: preetha */
public class StringSplitKeyValueParser implements KeyValueParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringSplitKeyValueParser.class);

    @Override
    public void parse(String log) {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = log.split("&");
        try {
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                final String key = URLDecoder.decode(pair.substring(0, idx));
                final String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                query_pairs.put(key, value);
            }
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Unable to url decode ", ex);
        }
    }
}
