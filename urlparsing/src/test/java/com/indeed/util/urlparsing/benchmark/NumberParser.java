package com.indeed.util.urlparsing.benchmark;

/**
 * @author: preetha
 */
public interface NumberParser {

    float parseFloat(String line, int start, int end);

    int parseInt(String line, int start, int end);
}
