// $Id$
package com.indeed.util.urlparsing;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class for building a more complicated and efficient URLParamCallback that calls other callbacks registered for specific keys.  The
 * generated callback does not create any garbage during calls to parseKeyValuePair.
 *
 * @author ahudson
 * @author preetha
 */
public class QueryStringParserCallbackBuilder<T> {
    private static class KeyCallbackPair<T> {
        private final String key;

        private final QueryStringParserCallback<T> callback;

        public KeyCallbackPair(String key, QueryStringParserCallback<T> callback) {
            this.key = key;
            this.callback = callback;
        }

        public String getKey() {
            return key;
        }

        public QueryStringParserCallback<T> getCallback() {
            return callback;
        }
    }

    public ArrayList<KeyCallbackPair<T>> callbacks = new ArrayList<KeyCallbackPair<T>>();

    public void addCallback(String key, QueryStringParserCallback<T> callback) {
        callbacks.add(new KeyCallbackPair<T>(key, callback));
    }

    public QueryStringParserCallback<T> buildCallback() {
        return new CompositeCallback<T>(callbacks);
    }

    private static class CompositeCallback<T> implements QueryStringParserCallback<T> {
        // map from hash of the targeted param key to the slot where it resides
        private final Int2IntOpenHashMap slotMap;

        // handles hash collisions and multiple callbacks for the same key
        private final int[] nextSlot;

        private final String[] keys;

        // callbacks, uses objects due to generic array creation issues
        private final Object[] callbacks;

        public CompositeCallback(Collection<KeyCallbackPair<T>> keyCallbackPairs) {
            slotMap = new Int2IntOpenHashMap(keyCallbackPairs.size());
            nextSlot = new int[keyCallbackPairs.size()];
            keys = new String[keyCallbackPairs.size()];
            callbacks = new Object[keyCallbackPairs.size()];

            int i = 0;
            for (KeyCallbackPair<T> keyCallbackPair : keyCallbackPairs) {
                String key = keyCallbackPair.getKey();
                int hash = hash(key, 0, key.length());
                if (slotMap.containsKey(hash))  {
                    nextSlot[i] = slotMap.get(hash);
                } else {
                    nextSlot[i] = -1;
                }
                slotMap.put(hash, i);
                keys[i] = key;
                callbacks[i] = keyCallbackPair.getCallback();
                i++;
            }
        }

        public void parseKeyValuePair(String queryString, int keyStart, int keyEnd, int valueStart, int valueEnd, T storage) {
            // equiv to String key = queryString.substring(keyStart, keyEnd);
            int length = keyEnd-keyStart;
            int hash = hash(queryString, keyStart, keyEnd); // equiv to key.hashCode();
            Integer slot = slotMap.get(hash);
            while (slot != null && slot != -1) {
                String currentKey = keys[slot];
                if (currentKey.length() == length && queryString.startsWith(currentKey, keyStart)) { // equiv to keys[slot].equals(key)
                    // call the callback
                    QueryStringParserCallback<T> callback = (QueryStringParserCallback<T>)callbacks[slot];
                    callback.parseKeyValuePair(queryString, keyStart, keyEnd, valueStart, valueEnd, storage);
                }
                slot = nextSlot[slot];
            }
        }

        private static int hash(String string, int start, int end) {
            int ret = 0;
            for (int i = start; i < end; i++) {
                ret = 31 * ret + string.charAt(i);
            }
            return ret;
        }
    }
}