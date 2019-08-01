package com.indeed.util.urlparsing;

// $Id$
/**
 * @author ahudson
 */
public interface QueryStringParserCallback<T> {
    /**
     * This callback is used by URLParamsParser and is called for each key/value pair in the urlParams String
     *
     * @param queryString The raw queryString (e.g. {@code "key1=value1&key2=value2&key3=value3&key4=value4")}
     * @param keyStart Index into queryString where the key in this key/value begins (inclusive)
     * @param keyEnd Index into queryString where the key in this key/value ends (exclusive)
     * @param valueStart Index into queryString where the value in this key/value begins (inclusive)
     * @param valueEnd Index into queryString where the value in this key/value ends (exclusive)
     * @param storage Object where to store parsed values, specific to each callback implementation
     */
    void parseKeyValuePair(String queryString, int keyStart, int keyEnd, int valueStart, int valueEnd, T storage);
}
