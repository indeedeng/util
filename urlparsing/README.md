util-urlparsing

## About

`urlparsing` is a set of classes for efficiently parsing key value params from query strings in URLs without unnecessary intermediate object creation. It also includes number parsing methods in `ParseUtils` that can parse floats, ints and longs from a query string. Benchmarks are in the test folder along with sample data.

## Motivation

Java versions 1.6 and lower have a [significant flaw](http://stackoverflow.com/questions/1281549/memory-leak-traps-in-the-java-standard-api/1281569#1281569) that leads to inefficient memory usage when using the `String.substring` method. We process large strings containing key value pairs of log event data, where we only need much smaller substrings from it. The flaw in the older JVM versions is that they keep the larger char[] array around even after no reference to the original String exists, which led to unnecessary out of memory errors in our log processing code. `QueryStringParser` was written to solve this problem.
Note that this issue has been addressed in the newest versions of Java 1.7.

Furthermore, our query parsing benchmark shows nearly 4X speedup over a naive Java implementation using `String.split` under significant heap space constraints. It can parse million key-value pairs in under 3 seconds given a max heap of only `-Xmx64M`. Our number parsing benchmark shows over 2X speedup compared to equivalent methods like `Integer.parseInt` and `Float.parseFloat`

## Usage

The class `QueryStringParser` has a parse method that accepts a callback interface`QueryStringParserCallBack<T>`. To parse any query string, you'll need to implement that interface. When the parse method finds a key, it uses the callback to provide start and end offsets in the string for the key and value.  Implementors of this interface can then set the parsed value in the provided `<T>`storage class.

Multiple callbacks can be chained together using `QueryStringParserCallbackBuilder`. The advantage of using a callback pattern here is that you can parse just the keys you are interested in from a longer query string.

For example:

```java
 public class Foo {
        String stringValue;
        int intValue;
    }

    private static final QueryStringParserCallback<Foo> stringValueParser = new QueryStringParserCallback<Foo>() {
        @Override
        public void parseKeyValuePair(String urlParams, int keyStart, int keyEnd, int valueStart, int valueEnd, Foo storage) {
            storage.stringValue = urlParams.substring(valueStart, valueEnd);
        }
    };

    private static final QueryStringParserCallback<Foo> intValueParser = new QueryStringParserCallback<Foo>() {
        @Override
        public void parseKeyValuePair(String urlParams, int keyStart, int keyEnd, int valueStart, int valueEnd, Foo storage) {
            storage.intValue = ParseUtils.parseUnsignedInt(urlParams, valueStart, valueEnd);
        }
    };

 public void parse(String logentry) {
        QueryStringParserCallbackBuilder<Storage> builder = new QueryStringParserCallbackBuilder<Foo>();
        builder.addCallback("stringKey", stringValueParser);
        builder.addCallback("intKey", intValueParser);

        final String queryString = "a=x&b=y&foo=bar&stringKey=hello&intKey=111&foobar=1";

        final QueryStringParserCallback<Storage> queryStringParser = builder.buildCallback();
        final Foo foo = new Foo();
        QueryStringParser.parseQueryString(queryString, queryStringParser, foo);
        assert foo.intValue == 111;
        assert foo.stringValue.equals("hello");

    }
        ..
```

In the above parse method, foo.stringValue will be set to "hello" and storage.intValue will be set to 111. Note that the rest of the keys are essentially ignored because we only added callbacks for two of them.

## ParseUtils
ParseUtils includes static utility methods to parse integers, longs and floating points from strings efficiently. It also includes a method to url-decode strings. All these methods avoid intermediate string object creation when parsing numbers from strings. Use them inside the query parser callback described above. The following examples illustrate this.

This example parses an integer inside a callback registered for the "userid" key using `ParseUtils.parseInt`.  It avoids an intermediate object created by `queryString.substring(valueStart, valueEnd)`, which is unavoidable if using `Integer.parseInt(s)` to parse instead.

```java

QueryStringParserCallbackBuilder<SomeObject> builder = new QueryStringParserCallbackBuilder<SomeObject>();
builder.addCallback("userid", new QueryStringParserCallback<SomeObject>() {
    @Override
    public void parseKeyValuePair(String queryString, int keyStart, int keyEnd, int valueStart, int valueEnd, SomeObject storage) {
        final int userId = ParseUtils.parseInt(queryString, valueStart, valueEnd);
        storage.setUserId(userId);
        }
});

QueryStringParser.parseQueryString(s, builder.buildCallback(), foo);

```


This example url decodes the string value in "q"

```java

 String s = "userid=12345&foo=bar&yo=lo&q=hello+world";
 ...
 QueryStringParserCallbackBuilder<SomeObject> builder = new QueryStringParserCallbackBuilder<SomeObject>();
 builder.addCallback("q", new QueryStringParserCallback<SomeObject>() {
   @Override
    public void parseKeyValuePair(String queryString, int keyStart, int keyEnd, int valueStart, int valueEnd, SomeObject storage) {
            final StringBuilder urlDecodedQuery = new StringBuilder();
            ParseUtils.urlDecodeInto(queryString, valueStart, valueEnd, urlDecodedQuery );
            assert urlDecodedQuery.equals("hello world");
         }
 });

QueryStringParser.parseQueryString(s, builder.buildCallback() , foo );

```

## Custom delimiters
`QueryStringParser` also has a parse method that accepts custom delimiters, instead of the default "&" and "=". For example if you had data like:

```
foo:bar%rad:boo%baz:quz
```
you could parse it using:
```
QueryStringParser.parseQueryString(queryString, queryStringParser, foo, "%", ":");
```
## Dependencies

- guava (16.0.1 ok)
- log4j
- it.unimi.dsi's fastutil
- junit-dep (4.X)
