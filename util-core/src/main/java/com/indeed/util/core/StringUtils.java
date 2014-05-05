package com.indeed.util.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author ahudson
 */
public class StringUtils {

    private static final Pattern WHITE_SPACE = Pattern.compile("\\s+", Pattern.MULTILINE);

    /**
     * Guarantee that a string cannot be null, by substituting the empty string when it is.
     *
     * @param s possibly null string
     * @return not a null string
     */
    @Nonnull
    public static String nonnull(@Nullable String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    public static String printUnicodeBlockOf(String s) {
        String ret = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            System.out.println(
                    String.format(
                            "%s (\\u%s) is in block %s", c, Integer.toHexString(Character.codePointAt(s, i)), Character.UnicodeBlock.of(c)
                    )
            );
        }
        return ret;
    }

    public static String normalizeWhiteSpace(String input) {
        if (input == null) {
            return null;
        }
        return WHITE_SPACE.matcher(input).replaceAll(" ").trim();
    }

    // effectively the same as this with no garbage:
    //return StringUtils.normalizeWhiteSpace()
    public static void normalizeWhiteSpaceInPlace(@Nullable final StringBuilder s) {
        if (s == null || s.length() <= 0) {
            return;
        }
        final int start = 0;
        final int end = s.length();
        boolean wasWhitespace = false;
        boolean hasNonWhitespace = false;
        int writeHead = start;
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
                wasWhitespace = true;
            } else {
                hasNonWhitespace = true; // first non-WS character makes writing start happening
                if (wasWhitespace) {
                    // delayed writing of whitespace (does an effective rtrim too)
                    s.setCharAt(writeHead++, ' ');
                }
                s.setCharAt(writeHead++, c);
                wasWhitespace = false;
            }
            // ltrim()
            if (wasWhitespace && !hasNonWhitespace) {
                wasWhitespace = false;
            }
        }
        // finish trimming
        s.setLength(writeHead);
    }

    public static Map<String, String> decodeUrlParams(String in) {
        return decodeUrlParams(in, "&");
    }

    /**
     * Returns a Map of search preferences from the preferences cookie
     */
    public static Map<String, String> decodeUrlParams(String in, String delim) {
        try {
            Map<String, String> ret = new LinkedHashMap<String, String>();
            List<String> nvPairs = split(in, delim);
            for (String s : nvPairs) {
                Pair<String, String> nv = getNameValue(s);
                String name = nv.getFirst();
                String value = nv.getSecond();
                name = URLDecoder.decode(name, "UTF-8");
                if (ret.containsKey(name)) {
                    // only keep first occurrence of key
                    continue;
                }
                if (value != null) {
                    value = URLDecoder.decode(value, "UTF-8");
                } else if (name.length() == 0) {
                    continue;
                } else {
                    value = "";
                }
                ret.put(name, value);
            }
            return ret;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends some param(s) onto the end of a URL, taking care of adding '?' or '&', if required.  The {@code params} parameter should NOT
     * contain a leading '?' or '&', and should already be URLEncoded.
     *
     * @param url the URL on which to append
     * @param params the URLEncoded params to append with NO leading '?' or '&'
     */
    public static String appendUrlParams(String url, String params) throws MalformedURLException {
        if (params == null || params.length() == 0) {
            return url;
        }
        // I use URL's parsing to determine whether the URL has a query string already
        URL u = new URL(url);
        String query = u.getQuery();
        if (query == null) {
            // there is no ? on the URL yet
            return url + "?" + params;
        } else if (query.length() > 0) {
            // there is a ? on the URL, and at least one parameter
            if (!url.endsWith("&")) {
                url += "&";
            }
            return url + params;
        } else { // query.length == 0
            // do nothing, there is already a ? on the URL but no params
            return url + params;
        }
    }

    // Pattern to match our replacement operator e.g.  replace("foo","bar")
    private static final Pattern REPLACE_PATTERN = Pattern.compile("replace\\(\\\"([^\"]+)\\\",[ \\t]*\\\"([^\"]+)\\\"\\)");

    /**
     * Returns a rewritten JobResult url, based on the {@code urlAppend} rewrite parameter.
     * <p/>
     * This formerly lived in ClickUtils in jasx, but this version throws exceptions
     */
    public static String rewriteUrl(final String jobUrl, String urlAppend) throws PatternSyntaxException, MalformedURLException {
        // first append the optional appendUrl parameters
        String append = urlAppend != null ? urlAppend.trim() : "";
        String rewrittenUrl = baseRewriteUrl(jobUrl, append);
        if (append.contains("{job_url}")) {
            return append.replace("{job_url}", rewrittenUrl);
        } else if (append.contains("{urlenc_job_url}")) {
            return append.replace("{urlenc_job_url}", Utilz.urlEncodeNull(rewrittenUrl));
        } else {
            return rewrittenUrl;
        }
    }

    // low level operations
    private static String baseRewriteUrl(final String jobUrl, String append) throws PatternSyntaxException, MalformedURLException {
        // an example replacement operator:  replace("(http://www\.crap\.com/xx)/us/\d+", "$1/us/6969")
        // a simpler example: replace("/us/505", "/us/6969")
        Matcher m = REPLACE_PATTERN.matcher(append);
        if (m.find()) {
            String pat = m.group(1);
            String repl = m.group(2);
            return jobUrl.replaceAll(pat, repl);
        } else if (append.length() > 0 && !append.contains("{job_url}") && !append.contains("{urlenc_job_url}")) {
            // allow special rules for further ad rewriting to pass through
            if (append.startsWith("&") || append.startsWith("?")) {
                // strip off leading '&' or '?'
                append = append.substring(1);
            }
            return StringUtils.appendUrlParams(jobUrl, append);
        } else {
            return jobUrl;
        }
    }

    public static List<String> split(String in, String delim) {
        StringTokenizer tok = new StringTokenizer(in, delim);
        List<String> set = new ArrayList<String>();
        while (tok.hasMoreElements()) {
            set.add(tok.nextToken());
        }
        return set;
    }

    public static Pair<String, String> getNameValue(String nameValuePair) {
        int equalsIndex = nameValuePair.indexOf('=');
        if (equalsIndex == -1) {
            return new Pair<String, String>(nameValuePair, null);
        } else {
            return new Pair<String, String>(
                    nameValuePair.substring(0, equalsIndex), nameValuePair.substring(equalsIndex + 1)
            );
        }
    }

    /**
     * Parses string into an ordered list of integers, given a set of delimiter characters. {@code delim} cannot be null, but it can be
     * empty, in which case an empty list will be returned.
     *
     * @param in the input string to parse
     * @param delim the delimiters
     * @throws NumberFormatException if one of the values cannot be parsed as an integer
     */
    public static List<Integer> splitToInts(String in, String delim) throws NumberFormatException {
        List<Integer> list = new ArrayList<Integer>();
        for (StringTokenizer tok = new StringTokenizer(in, delim); tok.hasMoreTokens(); ) {
            list.add(Integer.valueOf(tok.nextToken()));
        }
        return list;
    }

    public static boolean isEmpty(@Nullable final String s) {
        return isEmpty((CharSequence) s);
    }
    /**
     * Returns true if a String is null or contains only whitespace
     */
    public static boolean isEmpty(@Nullable final CharSequence s) {
        if (s == null) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > ' ') {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public static <T extends CharSequence> T firstNonEmpty(@Nullable final T... candidates) {
        if (null == candidates) {
            return null;
        }

        for (final T candidate: candidates) {
            if (!isEmpty(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    /// Simple utility method to check char-sequence equality without generating temporary Strings
    public static boolean areEqual(@Nullable final CharSequence cs1, @Nullable final CharSequence cs2) {
        if (null == cs1) {
            return null == cs2;
        } else if (null == cs2) {
            return false;
        } else if (cs1.length() != cs2.length()) {
            return false;
        }

        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /// Simple utility method to check char-sequence case-insensitive equality without generating temporary Strings
    public static boolean areEqualIgnoreCase(@Nullable final CharSequence cs1, @Nullable final CharSequence cs2) {
        if (null == cs1) {
            return null == cs2;
        } else if (null == cs2) {
            return false;
        } else if (cs1.length() != cs2.length()) {
            return false;
        }

        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (Character.toLowerCase(cs1.charAt(i)) != Character.toLowerCase(cs2.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Replaces each key with its value in the specified text.  Keys should appear in the template as ${keyname}, case-sensitive.
     */
    public static String replaceTemplate(Map<String, String> map, String template) {
        // todo: throw an exception if not all ${key}s are contained in map.
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            String replacement = entry.getValue();
            template = template.replace(key, replacement);
        }
        return template;
    }

    /**
     * Returns the hostname part of a full URL.  The full URL should include the protocol, e.g. "http://www.xyz.com"
     *
     * @throws MalformedURLException
     */
    public static String getHostnameFromUrl(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        return url.getHost();
    }

    /**
     * Returns the username part of an email address before the @ sign.  If the email address given contains no at sign,
     * IllegalArgumentException is thrown.
     *
     * @return the part of an email address before the @ sign
     * @throws IllegalArgumentException
     */
    public static String getUsernameFromEmailAddress(String emailAddress) throws IllegalArgumentException {
        return splitEmailAddress(emailAddress)[0];
    }

    /**
     * Returns the hostname part of an email address after the @ sign.  If the email address given contains no at sign,
     * IllegalArgumentException is thrown.
     *
     * @return the part of an email address after the @ sign
     * @throws IllegalArgumentException
     */
    public static String getDomainFromEmailAddress(String emailAddress) throws IllegalArgumentException {
        return splitEmailAddress(emailAddress)[1];
    }

    static String[] splitEmailAddress(String emailAddress) throws IllegalArgumentException {
        int atSign = emailAddress.indexOf("@");
        if (atSign < 0) {
            throw new IllegalArgumentException("Invalid email address: " + emailAddress);
        }
        return new String[]{
                emailAddress.substring(0, atSign), emailAddress.substring(atSign + 1)
        };
    }

    /**
     * Joins a collection of objects with a given delimiter.
     *
     * @param items The items to join.
     * @param delim Placed between each item.
     * @return a string of joined items.
     */
    public static String join(Collection items, String delim) {
        return join(items, delim, null, null);
    }

    /**
     * Joins a collection of objects with a given delimiter.
     *
     * @param items The items to join.
     * @param delim Placed between each item.
     * @param prefix Placed before all the items.
     * @param suffix Placed after all the items.
     * @return a string of joined items.
     */
    public static String join(Collection items, String delim, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder(items.size() * 7);
        join(items, delim, prefix, suffix, sb);
        return sb.toString();
    }

    public static void join(Collection items, String delim, StringBuilder sb) {
        join(items, delim, null, null, sb);
    }

    public static void join(Collection items, String delim, String prefix, String suffix, StringBuilder sb) {
        if (prefix != null && !"".equals(prefix)) {
            sb.append(prefix);
        }
        for (Iterator it = items.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        if (suffix != null && !"".equals(suffix)) {
            sb.append(suffix);
        }
    }

    /**
     * Wrapper/adapter to allow varargs style invocation of join
     */
    public static String join(final String delimiter, final Object... items) {
        return join(Arrays.asList(items), delimiter);
    }

    /**
     * Joins an array of ints.
     *
     * @param delimiter Separates the entries.
     * @param items Items to join
     * @return A string of the items, separated by the delimiter.
     */
    @Nonnull
    public static String join(@Nonnull final String delimiter, @Nonnull final int[] items) {
        final StringBuilder sb =
                new StringBuilder(items.length * (10 + delimiter.length())); // big size since these are frequently jobIds and the like
        final int lastIndex = items.length - 1;
        for (int i = 0; i <= lastIndex; i++) {
            sb.append(items[i]);
            if (i < lastIndex) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * Joins an array of longs.
     *
     * @param delimiter Separates the entries.
     * @param items Items to join
     * @return A string of the items, separated by the delimiter.
     */
    @Nonnull
    public static String join(@Nonnull final String delimiter, @Nonnull final long[] items) {
        final StringBuilder sb =
                new StringBuilder(items.length * (10 + delimiter.length())); // big size since these are frequently jobIds and the like
        final int lastIndex = items.length - 1;
        for (int i = 0; i <= lastIndex; i++) {
            sb.append(items[i]);
            if (i < lastIndex) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * Returns a Set of trimmed, non-empty Strings read from an InputStream containing one word per line.  This method reads until
     * end-of-file but does not close the inputStream.
     *
     * @param inputStream the InputStream to read, which will NOT be closed by this method
     */
    public static Set<String> getNonEmptyLines(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        Set<String> words = new HashSet<String>();
        String line;
        while ((line = in.readLine()) != null) {
            String word = line.trim();
            if (word.length() > 0) {
                words.add(word);
            }
        }
        return words;
    }

    /**
     * Replaces characters with ASCII below 32, but allows 9 (tab), 10 (LF), and 13 (CR).
     */
    @Nonnull
    public static String removeBadAscii(@Nonnull final String s) {
        char[] ch = null;
        // valid low-ascii characters are: 9 (tab), 10 (lf), 13 (cr)
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // ASCII code 160 causes problems sometimes, so we convert to a space
            if (c < 32 && c != 9 && c != 10 && c != 13) {
                // replace bad lower ASCII with a space
                if (ch == null) {
                    ch = s.toCharArray();
                }
                ch[i] = ' ';
            }
        }
        if (ch == null) {
            return s;
        } else {
            return new String(ch);
        }
    }

    /**
     * Replaces characters with ASCII below 32, but allows 9 (tab), 10 (LF), and 13 (CR).
     * <p/>
     * return true if the string was changed
     */
    public static boolean removeBadAsciiInPlace(@Nonnull final StringBuilder s) {
        final int start = 0;
        final int end = s.length();
        // valid low-ascii characters are: 9 (tab), 10 (lf), 13 (cr)
        boolean changed = false;
        int writeHead = start;
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            // ASCII code 160 causes problems sometimes, so we convert to a space
            if (c < 32 && c != 9 && c != 10 && c != 13) {
                // replace bad lower ASCII with a space
                c = ' ';
                changed = true;
            }
            s.setCharAt(writeHead++, c);
        }
        return changed;
    }

    // true if string was changed
    public static boolean stringLowerInPlace(@Nonnull final StringBuilder s) {
        final int start = 0;
        final int end = s.length();
        boolean changed = false;
        int writeHead = start;
        for (int i = start; i < end; i++) {
            final char rawC = s.charAt(i);
            final char c = Character.toLowerCase(rawC);
            if (c != rawC) {
                changed = true;
            }
            s.setCharAt(writeHead++, c);
        }
        return changed;
    }

    // true if string was changed
    public static boolean stringUpperInPlace(@Nonnull final StringBuilder s) {
        final int start = 0;
        final int end = s.length();
        boolean changed = false;
        int writeHead = start;
        for (int i = start; i < end; i++) {
            final char rawC = s.charAt(i);
            final char c = Character.toUpperCase(rawC);
            if (c != rawC) {
                changed = true;
            }
            s.setCharAt(writeHead++, c);
        }
        return changed;
    }

    // true if every character in a string is its own uppercase (also true if the string is empty
    public static boolean isStringUppercased(@Nonnull final CharSequence s) {
        final int end = s.length();
        for (int i = 0; i < end; i++) {
            final char c = s.charAt(i);
            final char upperC = Character.toUpperCase(c);
            if (c != upperC) {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove all leading and trailing whitespace, and replace any embedded whitespace with a single space.
     *
     * @param string An input string.
     * @return the same string without excessive whitespace.
     */
    public static String shrinkWhitespace(String string) {
        int length = string.length();
        char[] newLocationText = new char[length];
        // Don't preserve any whitespace at the beginning, so set the initial condition to inWhitespace = true
        boolean inWhitespace = true;
        int newLocationIndex = 0;
        for (int i = 0; i < length; ++i) {
            char locationChar = string.charAt(i);
            // Determine whether this character is whitespace or one of the other Unicode space characters
            boolean isWhitespace = Character.isWhitespace(locationChar) || Character.isSpaceChar(locationChar);
            if (!isWhitespace) {
                // Always keep non-whitespace
                newLocationText[newLocationIndex++] = locationChar;
            } else if (!inWhitespace) {
                // Add a ' ' if and only if this is the first whitespace character following a block of non-whitespace
                newLocationText[newLocationIndex++] = ' ';
            }
            inWhitespace = isWhitespace;
        }
        // Remove trailing whitespace.
        if (newLocationIndex > 0 && inWhitespace) {
            newLocationIndex--;
        }
        return new String(newLocationText, 0, newLocationIndex);
    }

    /**
     * http://www.unicode.org/Public/UNIDATA/NormalizationTest.txt is probably the content you want to check.
     *
     * @param text
     * @return the normalized text. It only contains half-width English letters/numbers and full width CJK characters. For other languages,
     *         you may want to check the link above. Other possible choices would be NFC, NFD, NFKD.
     */
    public static String normalizeLettersToCanonicalLength(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC);
    }

    //If you want to have a more powerful tool for normalizing the full/half width problem, check normalizeLettersToCanonicalLength.
    public static String normalizeFullwidthPunctuation(String text) {
        char[] chars = text.toCharArray();
        int k = 0;
        for (char c : chars) {
            int codePoint = Character.codePointAt(text, k);
            chars[k] = normalizeFullwidthPunctuation(c, codePoint);
            k++;
        }
        return new String(chars);
    }

    public static char normalizeFullwidthPunctuation(char c, int codePoint) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS && codePoint < 65382) {
            /** fullwidth forms of ascii letters, numbers and punctuations are from code point 65281 to 65382*/
            int i = (int)c;
            i = i - 65248;
            c = (char)i;
        }
        return c;
    }

    /**
     * Returns a formatted string using the specified format string and arguments, after URLEncoding each argument using UTF-8 character
     * encoding.
     * <p/>
     * <p>The locale always used is the one returned by {@link java.util.Locale#getDefault() Locale.getDefault()}.
     *
     * @param format A {@link java.util.Formatter format string}
     * @param args see {@link String#format(String, Object...)}
     */
    public static String formatAndUrlEncodeUtf8(String format, Object... args) {
        try {
            return formatAndUrlEncode("UTF-8", format, args);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a formatted string using the specified format string and arguments, after URLEncoding each argument using the specified
     * encoding.
     * <p/>
     * <p>The locale always used is the one returned by {@link java.util.Locale#getDefault() Locale.getDefault()}.
     *
     * @param encoding the name of the supported character encoding
     * @param format A {@link java.util.Formatter format string}
     * @param args arguments to be formatted - see {@link String#format(String, Object...)}
     * @throws UnsupportedEncodingException if the encoding specified is invalid
     */
    public static String formatAndUrlEncode(String encoding, String format, Object... args) throws UnsupportedEncodingException {
        return formatAndUrlEncode(Locale.getDefault(), encoding, format, args);
    }

    /**
     * Returns a formatted string using the specified locale, format string, and arguments, after URLEncoding each argument using the
     * specified encoding.
     *
     * @param locale The locale to apply during formatting. If l is null then no localization is applied.
     * @param encoding the name of the supported character encoding
     * @param format A {@link java.util.Formatter format string}
     * @param args arguments to be formatted - see {@link String#format(String, Object...)}
     * @throws UnsupportedEncodingException if the encoding specified is invalid
     */
    public static String formatAndUrlEncode(Locale locale, String encoding, String format, Object... args)
            throws UnsupportedEncodingException {
        // We replace the args array elements in place rather than make a new copy
        // because it shouldn't affect the caller to this varargs function
        final int n = args.length;
        for (int i = 0; i < n; i++) {
            if (args[i] != null)    // null will be handled by String.format()
            {
                args[i] = URLEncoder.encode(args[i].toString(), encoding);
            }
        }
        return String.format(locale, format, args);
    }

    /**
     * For debugging, returns a "dump" style string with the contents of {@code s} formatted in two columns - left column is 12 raw
     * characters, right column is Unicode U+0000 representation.  Useful when trying to debug character encoding issues.
     */
    public static String toUnicodeDump(String s) {
        StringBuilder buf = new StringBuilder(s.length());
        StringBuilder buf2 = new StringBuilder(128);
        final int LEFT_COLUMN_SIZE = 12;
        int n = s.length();
        int i = 0;
        while (i < n) {
            int end = i + LEFT_COLUMN_SIZE;
            for (int j = i; j < end; j++, i++) {
                if (j < n) {
                    int cp = s.codePointAt(j);
                    if (Character.isValidCodePoint(cp) && !Character.isISOControl(cp)) {
                        buf.append(s.charAt(j));
                    } else {
                        // turn invalid chars and control chars into a period
                        buf.append('.');
                    }
                    String unicodeValue = Utilz.leftpad(Integer.toHexString(cp), 4, '0');
                    buf2.append("U+").append(unicodeValue).append(' ');
                } else {
                    buf.append(' ');
                }
            }
            buf.append(" | ").append(buf2).append('\n');
            buf2.setLength(0);
        }
        return buf.toString();
    }

    /**
     * Returns true if the given string starts with the given sub-string, ignoring case.
     *
     * @param str string to search
     * @param start sub-string to compare start of str against
     * @return true if string starts with sub-string (case-insensitive)
     * @see java.lang.String#startsWith(String)
     */
    public static boolean startsWithIgnoreCase(final String str, final String start) {
        return str.regionMatches(true, 0, start, 0, start.length());
    }

    /**
     * Returns true if the given string ends with the given sub-string, ignoring case.
     *
     * @param str string to search
     * @param end sub-string to compare end of str against
     * @return true if string ends with sub-string (case-insensitive)
     * @see java.lang.String#endsWith(String)
     */
    public static boolean endsWithIgnoreCase(final String str, final String end) {
        final int fromIndex = str.length() - end.length();
        return str.regionMatches(true, fromIndex, end, 0, end.length());
    }

    /**
     * Returns the first index of the given sub-string ("needle") in the given string, ignoring case; returns -1 if sub-string not present
     * in string. <p> For relatively short needles (13 chars in baseline testing) and strings that are already all upper-case, it would be
     * faster to use the str.indexOf(needle.toUpperCase()) approach -- baseline testing shows this method can be up to 50% slower. However,
     * that approach requires the additional overhead of storing strings all upper-cased. When that is not feasible, this method is
     * significantly faster (30% faster for an average string length of 60 in baseline testing) than
     * str.toUpperCase().indexOf(needle.toUpperCase()), and perhaps most importantly, it avoids the object creation overhead associated with
     * .toUpperCase(). </p>
     *
     * @param str string to search
     * @param needle sub-string to search for in str
     * @return first index of sub-string in str, -1 if not found
     * @see java.lang.String#indexOf(String)
     */
    public static int indexOfIgnoreCase(final String str, final String needle) {
        return indexOfIgnoreCase(str, 0, needle);
    }

    /**
     * Returns the first index of the given sub-string ("needle") in the given string after the given index, ignoring case; returns -1 if
     * sub-string not found after that index in string.
     *
     * @param str string to search
     * @param fromIndex index from which to search in str
     * @param needle sub-string to search for in str
     * @return first index of sub-string in str after fromIndex, -1 if not found
     * @see java.lang.String#indexOf(String, int)
     */
    public static int indexOfIgnoreCase(final String str, final int fromIndex, final String needle) {
        final int needleLength = needle.length();
        final int len = str.length();
        for (int i = fromIndex; i + needleLength <= len; i++) {
            if (str.regionMatches(true, i, needle, 0, needleLength)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the last index of the given sub-string ("needle") in the given string, ignoring case; returns -1 if sub-string not present in
     * string. <p> For relatively short needles (13 chars in baseline testing) and strings that are already all upper-case, it would be
     * faster to use the str.lastIndexOf(needle.toUpperCase()) approach -- baseline testing shows this method can be up to 50% slower.
     * However, that approach requires the additional overhead of storing strings all upper-cased. When that is not feasible, this method is
     * significantly faster (60% faster for an average string length of 60 in baseline testing) than
     * str.toUpperCase().lastIndexOf(needle.toUpperCase()), and perhaps most importantly, it avoids the object creation overhead associated
     * with .toUpperCase(). </p>
     *
     * @param str string to search
     * @param needle sub-string to search for in str
     * @return last index of sub-string in str, -1 if not found
     * @see java.lang.String#indexOf(String, int)
     */
    public static int lastIndexOfIgnoreCase(final String str, final String needle) {
        return lastIndexOfIgnoreCase(str, str.length(), needle);
    }

    /**
     * Returns the last index of the given sub-string ("needle") in the given string before the given index, ignoring case; returns -1 if
     * sub-string not found before that index in string.
     *
     * @param str string to search
     * @param fromIndex index from which to search in str
     * @param needle sub-string to search for in str
     * @return last index of sub-string in str before fromIndex, -1 if not found
     * @see java.lang.String#indexOf(String, int)
     */
    public static int lastIndexOfIgnoreCase(final String str, final int fromIndex, final String needle) {
        final int needleLength = needle.length();
        for (int i = fromIndex - needleLength; i >= 0; i--) {
            if (str.regionMatches(true, i, needle, 0, needleLength)) {
                return i;
            }
        }
        return -1;
    }

    public static String joinNonEmpty(final String delimiter, final String... pieces) {
        final StringBuilder sb = new StringBuilder();
        joinNonEmpty(sb, delimiter, pieces);
        return sb.toString();
    }

    public static StringBuilder joinNonEmpty(final StringBuilder sb, final String delimiter, final String... toAppend) {
        for (final String s : toAppend) {
            if (StringUtils.isEmpty(s)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(s);
        }
        return sb;
    }

    /**
     * Determines whether the given string test has the same prefix as reference.  Iterates over the characters in reverse order to make it fail sooner
     */
    public static boolean startsWithReverse(final CharSequence prefix, final CharSequence test) {
        if (test.length() < prefix.length()) {
            return false;
        }
        for (int i = prefix.length() - 1; i >= 0; i--) {
            if (prefix.charAt(i) != test.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the sequence is a valid Java or JavaScript identifier, but with additional strictness
     * on the type of characters (no '$' allowed; just letters, numbers, and underscores).
     *
     * @param expression An expression which may be an identifier but may not.
     * @return Whether the entire expression is a simple identifier.
     */
    public static boolean isSimpleIdentifier(final CharSequence expression) {
        if (isEmpty(expression)) {
            return false;
        }

        final char firstChar = expression.charAt(0);
        if (firstChar != '_' && !Character.isLetter(firstChar)) {
            return false;
        }

        for (int i = 1; i < expression.length(); i++) {
            final char subsequentChar = expression.charAt(i);
            if (subsequentChar != '_' && !Character.isLetter(subsequentChar) && !Character.isDigit(subsequentChar)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Finds the nth occurrence of a particular string in a given input and returns it's index
     * @param input string to search
     * @param needle substring to search for
     * @param n which occurrence to look for
     * @return The index of the nth occurrence of some input or -1 if it does not exist
     */
    public static int nthIndexOf(@Nonnull final String input, @Nonnull final String needle, final int n) {
        if (n < 1 || input.length() == 0 || needle.length() == 0) {
            return -1;
        }
        int occurrence = 0;
        int pos = input.indexOf(needle, 0);
        while (pos > -1 && ++occurrence < n) {
            pos = input.indexOf(needle, pos + 1);
        }
        return pos;
    }

    public static boolean is16bitUnicodeSafe(final String s) {
        for (int i = 0; i < s.length(); i++) {  //  it's okay to increment one at a time because we will never increment past a code point bigger than a character
            final int codePoint = s.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                return false;
            }
        }
        return true;
    }

    public static String stripSupplementaryCodePoints(final String s) {
        if (is16bitUnicodeSafe(s)) {
            return s;
        }
        final StringBuilder replacement = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            final int codePoint = s.codePointAt(i);
            if (!Character.isSupplementaryCodePoint(codePoint)) {
                replacement.appendCodePoint(codePoint);
            }
            i += Character.charCount(codePoint);
        }
        return replacement.toString();
    }
}