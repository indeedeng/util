package com.indeed.util.urlparsing;

/**
 * URLParamsParser that does not generate *any* garbage in the process of parsing URL Parameters
 *
 * @author ahudson
 * @author preetha
 */
public class QueryStringParser {
    /**
     * @param queryString The raw urlParams string (e.g. {@code
     *     "key1=value1&key2=value2&key3=value3&key4=value4"})
     * @param callback Callback which is called for each key/value pair
     * @param storage Storage object, passed to each callback call
     */
    public static <T> void parseQueryString(
            String queryString, QueryStringParserCallback<T> callback, T storage) {
        parseQueryString(queryString, callback, storage, 0, queryString.length(), "&", "=");
    }

    /**
     * @param queryString The raw urlParams string (e.g. {@code
     *     "key1=value1&key2=value2&key3=value3&key4=value4"})
     * @param callback Callback which is called for each key/value pair
     * @param storage Storage object, passed to each callback call
     * @param qsStart index into queryString param where queryString actually starts
     * @param qsEnd index into queryString param where queryString actually ends
     * @param pairDelim String delimeter that occurs between keyvalue pairs, e.g. {@code "&"}
     * @param kvDelim String delimeted that occurs between a key and its value, e.g. "="
     */
    public static <T> void parseQueryString(
            String queryString,
            QueryStringParserCallback<T> callback,
            T storage,
            int qsStart,
            int qsEnd,
            String pairDelim,
            String kvDelim) {
        int kvPairStart = qsStart;
        while (kvPairStart <= qsEnd) {
            // find where this key value pair ends
            int kvPairEnd = queryString.indexOf(pairDelim, kvPairStart);
            if (kvPairEnd < 0 || kvPairEnd > qsEnd) {
                // ends at the end of the string
                kvPairEnd = qsEnd;
            }

            int equalPos = queryString.indexOf(kvDelim, kvPairStart);
            if (equalPos < 0 || equalPos > kvPairEnd) {
                // no = found in this key value pair, treat it as a key with an empty string value
                callback.parseKeyValuePair(
                        queryString, kvPairStart, kvPairEnd, kvPairEnd, kvPairEnd, storage);
            } else {
                callback.parseKeyValuePair(
                        queryString,
                        kvPairStart,
                        equalPos,
                        equalPos + kvDelim.length(),
                        kvPairEnd,
                        storage);
            }

            kvPairStart = kvPairEnd + pairDelim.length();
        }
    }
}
