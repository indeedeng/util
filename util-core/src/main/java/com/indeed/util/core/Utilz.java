package com.indeed.util.core;

import com.google.common.base.CharMatcher;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Various utilities.
 */
public final class Utilz {

    private static final Logger log = Logger.getLogger(Utilz.class);

    private static final String KEY = "Hr4!fRO*A";
    private static final String FILLER = "MK89ZA1";
    private static final Pattern SIMPLE_USER_PATTERN;

    static {
        final String START = "[_\\p{Alnum}]";
        final String MID = "[_\\p{Alnum}\\+\\-!#\\$%&\\*/=\\?\\^_`\\{\\|\\}~]";
        final String MANY_MID = MID + "+";
        final String SOME_MID = MID + "*";
        final String SEP = "[\\.']";
        // NOTE: non-capturing group (?:x) instead of (x)
        SIMPLE_USER_PATTERN = Pattern.compile(START + SOME_MID + "(?:" + SEP + MANY_MID + ")*");
    }

    private static final Pattern SIMPLE_DOMAIN_PATTERN = Pattern.compile("(\\p{Alnum}+([-]+\\p{Alnum}+)*[.])+(\\p{Alnum}+)");
    private static final Pattern LATIN1_PATTERN = Pattern.compile("\\p{L1}+");

    /**
     * Matches any hex char.
     */
    public static final CharMatcher HEX_CHAR = CharMatcher.anyOf("0123456789abcdef");

    /**
     * Returns a given value, restricting it to a given [min, max] range
     */
    public static int restrict(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    /**
     * @deprecated Just use {@link String#format(String, Object...)} instead.
     */
    @Deprecated
    public static String sprintf(String format, Object... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        ps.printf(format, args);
        return out.toString();
    }

    public static void mark() {
        Date date = new Date();
        System.out.println("MARK: " + date.getTime() + " " + date);
    }

    /**
     * reading valid integer parameter
     *
     * @param argument expected value
     * @param defaultValue default value
     * @return valid integer or default value
     */
    public static int getIntegerParameter(String argument, int defaultValue) {
        try {
            return argument != null
                   ? Integer.parseInt(argument)
                   : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Loads a UTF-8 text file, returning an array of strings, one string per input line, ignoring
     * any line that begins with "#", or has length outside the range [minlength, maxlength].
     * Note: This method does <em>not</em> close the InputStream.
     *
     * @throws IOException
     */
    public static String[] loadTextFile(InputStream is, int minlength, int maxlength) throws IOException {
        final char UTF8_BOM = 0xFEFF;
        List<String> list = new ArrayList<String>();
        BufferedReader input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        boolean firstLine = true;
        while ((line = input.readLine()) != null) {
            line = line.trim();
            int len = line.length();
            if (firstLine) {
                // look for UTF-8 byte order mark and skip it (workaround lame java bug).
                if (line.charAt(0) == UTF8_BOM) {
                    line = line.substring(1);
                }
                firstLine = false;
            }
            if (len >= minlength && len <= maxlength && !line.startsWith("#")) {
                list.add(line);
            }
        }
        input.close();
        return list.toArray(new String[list.size()]);
    }

    public static String getFirstChars(String src, int length) {
        String suffix = "";
        if (src.length() >= length) {
            suffix = "...";
        } else {
            length = src.length();
        }
        return src.substring(0, length) + suffix;
    }

    /**
     * Simple XOR Encryption
     *
     * @param str String
     * @return String
     */
    public static String encryptString(@Nonnull final String str) {
        final StringBuilder sb = new StringBuilder(str);

        final int lenStr = str.length();
        final int lenKey = KEY.length();

        //
        // For each character in our string, encrypt it...
        for (int i = 0, j = 0; i < lenStr; i++, j++) {
            if (j >= lenKey) {
                j = 0; // Wrap 'round to beginning of key string.
            }

            //
            // XOR the chars together. Must cast back to char to avoid compile error.
            //
            sb.setCharAt(i, (char) (str.charAt(i) ^ KEY.charAt(j)));
        }

        return sb.toString();
    }

    /**
     * Returns true iff a String looks like a valid email address
     */
    public static boolean isValidEmail(@Nullable final String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        assert email != null;

        try {
            final InternetAddress emailAddress = new InternetAddress(email, true);
            emailAddress.validate();

            /*
             * Address till now is RFC-822 compliant
             * However "a@b" and "joe" are valid RFC-822 email addresses, so include additional check:
             */
            final String[] tokens = email.split("@");
            return tokens.length == 2 &&    //Check if email address is of form blah@blah
                   !StringUtils.isEmpty(tokens[0]) && //Check if first part is not empty
                   !StringUtils.isEmpty(tokens[1]) &&  //Check if second part is not empty
                   SIMPLE_USER_PATTERN.matcher(tokens[0]).matches() && // Chceck if first part looks like a valid username
                   SIMPLE_DOMAIN_PATTERN.matcher(tokens[1]).matches() && //Check if second part looks like a valid domain
                   LATIN1_PATTERN.matcher(email).matches(); //TODO: Remove this when we support Unicode emails
        } catch (AddressException ignored) {
            return false;
        }
    }

    public static final Pattern VALID_HOST =
        Pattern.compile("(([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)" +
                        "|(([a-zA-Z0-9\\-]+\\.)+))" +
                        "([a-zA-Z]{2,4}|[0-9]{1,3})$");

    public static boolean isValidHostname(String s) {
        return VALID_HOST.matcher(s).matches();
    }

    /**
     * Validates that a URL is syntactically valid
     */
    public static boolean isValidUrl(String url) {
        if (url == null) {
            throw new NullPointerException();
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }
        try {
            final URL testURL = new URL(url);
            if (testURL.getHost() == null || testURL.getHost().length() == 0) {
                return false;
            }
            // URL class pretty lax about hostname part...
            if (!isValidHostname(testURL.getHost())) {
                return false;
            }
            return !(!"http".equals(testURL.getProtocol()) && !"https".equals(testURL.getProtocol()));
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Returns true if a String is *not* null or empty
     */
    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    /**
     * Returns true if a String is null or empty
     *
     * @deprecated use {@link StringUtils#isEmpty(String)}
     */
    @Deprecated
    public static boolean isEmpty(String s) {
        return s == null || "".equals(s.trim());
    }

    public static boolean anyEmpty(final String... ss) {
        for (final String s : ss) {
            if (isEmpty(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a byte array to a hex string.  The String returned
     * will be of length exactly {@code bytes.length * 2}.
     */
    @Nonnull
    public static String toHex(@Nonnull final byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hexDigits = Integer.toHexString((int) b & 0x00ff);
            if (hexDigits.length() == 1) {
                buf.append('0');
            }
            buf.append(hexDigits);
        }
        return buf.toString();
    }

    @Nonnull
    public static String toHex(long n) {
        final int REQUIRED_LENGTH = 16;
        String hex = Long.toHexString(n);
        while (hex.length() < REQUIRED_LENGTH) {
            hex = '0' + hex;
        }
        return hex;
    }

    @Nonnull
    public static String toHex(@Nonnull final BigInteger bi, int requiredLength) {
        String hex = bi.toString(16);
        while (hex.length() < requiredLength) {
            hex = '0' + hex;
        }
        return hex;
    }

    /**
     * Converts a hex String to a byte array.  The String length must
     * be a multiple of 2.
     */
    @Nonnull
    public static byte[] fromHex(@Nonnull String s) {
        if (s.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string length must be a multiple of 2: " + s);
        }
        final byte[] b = new byte[s.length() / 2];
        s = s.toUpperCase();
        for (int i = 0; i < b.length; ++i) {
            int offset = i * 2;
            int hi = Character.digit(s.charAt(offset), 16);
            int lo = Character.digit(s.charAt(offset + 1), 16);
            b[i] = (byte) ((hi << 4) | lo);
        }

        return b;
    }

    public static long longFromHex(@Nonnull final String s) {
        if (s.length() == 16) {
            final char highest = s.charAt(0);
            int highestDigit = Character.digit(highest, 16); // get the highest place digit
            if (highestDigit >= 8) {
                final int signBitDroppedFromDigit = highestDigit & 0x7; // drop the sign bit
                final String signBitDroppedFromHighest = Integer.toHexString(signBitDroppedFromDigit); // convert back to hex
                final String signBitDropped = signBitDroppedFromHighest + s.substring(1); // replace it on the input
                final long signBitMissing = Long.parseLong(signBitDropped, 16); // parse it
                final long signBitRestored = signBitMissing | 0x8000000000000000L; // flip the high (sign) bit back
                return signBitRestored;
            }
        }
        return Long.parseLong(s, 16);
    }

    /**
     * returns the first 64 bits of the md5 digest as a long.
     *
     * @param bytes the data to digest
     * @return the md5 digest of the given data, encoded in a 64 bit long
     */
    public static long get64BitMD5(@Nonnull byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            byte[] digest = md.digest();
            long ret = 0;
            for (int i = 0; i < 8; i++) {
                ret = (ret << 8) | (((long) digest[i]) & 0xFFl);
            }
            return ret;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * returns the first 64 bits of the md5 digest as a long
     */
    public static long get64BitMD5(@Nonnull String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes(UTF_8));
            byte[] digest = md.digest();
            long ret = 0;
            for (int i = 0; i < 8; i++) {
                ret = (ret << 8) | (((long) digest[i]) & 0xFFl);
            }
            return ret;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Returns the MD5 digest for a given String.  The digest is returned
     * as a String of up to 32 hexadecimal characters.
     */
    @Nonnull
    public static String getMD5(@Nonnull String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes(UTF_8));
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static String getMD5(@Nonnull String text, @Nonnull String salt) {
        return getMD5(text + salt);
    }

    /**
     * Makes a hash using <code>uid</code> such that the hash will change every
     * <code>windowSize</code> seconds starting from an offset based on a hash that is a multiple
     * of <code>windowSize / numOffsets</code>, using a given time.
     *
     * @param uid the string to generate the hash for
     * @param windowSize the width of the window in seconds
     * @param numOffsets the number of offsets per window
     * @param time the time in seconds
     * @return returns the hash or an empty string if MD5 is unavailable
     */
    public static String makeRSSHash(String uid, int windowSize, int numOffsets, long time) {
        int winNum = Math.abs(uid.hashCode()) % numOffsets;  // get an offset number
        long offset = ((long) windowSize) * winNum / numOffsets; // calculate the offset length
        long extra = (time - offset) / windowSize; // this is the extra info to append to uid
        final String temp = uid + extra;  // this is the new string to hash
        final String RSS_HASH_SALT = "RSS_HASH_SALT";

        // hash-it
        return Utilz.getMD5(temp, RSS_HASH_SALT);
    }

    /**
     * make a salted recommendation hash using email or accountId
     *
     * @param email email address or accountId in string.
     * @return 64 bit long
     */
    public static long makeRecosHash(String email) {
        final String RECOS_HASH_SALT = "SeCrEt_recos_SALT";
        return Utilz.get64BitMD5(email + RECOS_HASH_SALT);
    }

    /**
     * Returns true iff there are no lower case letters in a String.  Non-letter characters are ignored.
     */
    public static boolean isAllcaps(String s) {
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (Character.isLetter(ch) && Character.isLowerCase(ch)) {
                return false;
            }
        }
        return true;
    }

    public static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }
        char[] ch = s.toCharArray();
        boolean up = true;
        for (int i = 0; i < ch.length; i++) {
            if (up) {
                ch[i] = Character.toUpperCase(ch[i]);
                up = false;
            } else {
                ch[i] = Character.toLowerCase(ch[i]);
                if (ch[i] == ' ' || ch[i] == '&') {
                    up = true;
                }
            }
        }
        return new String(ch);
    }

    public static String toCityStateCase(String cityState) {
        String s = toTitleCase(cityState);
        int i = s.lastIndexOf(",");

        if (i != -1) {
            // capitalize the state abbreviation
            String city = s.substring(0, i);
            String state = s.substring(i).toUpperCase();
            return city + state;
        } else {
            return s;
        }
    }

    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("[A-Z][A-Z]", Pattern.CASE_INSENSITIVE);

    public static boolean isValidCountryCode(String countryCode) {
        return countryCode != null && COUNTRY_CODE_PATTERN.matcher(countryCode).matches();
    }

    /**
     * If String <code>s</code> is longer than <code>length</code>, it is truncated to <code>length</code>,
     * otherwise <code>s</code> is return as-is.
     */
    @Nonnull
    public static String truncate(@Nonnull String s, int length) {
        if (s.length() > length) {
            s = s.substring(0, length);
        }
        return s;
    }

    @Nonnull
    public static String truncateAtSpace(@Nonnull String s, int len) {
        int last = s.lastIndexOf(" ", len);
        String st = s.length() > len && last > 0 ? s.substring(0, last) + "..." : s;
        return st;
    }

    public static String urlEncode(@Nonnull String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("UTF-8 encoding vanished", e); // this should never happen
            return null;
        }
    }

    private static BitSet urlSafeCharacters = calculateUrlSafeCharacterBitset();

    /**
     * "Inspired" by {@link URLEncoder}
     * <p/>
     * The list of characters that are not encoded has been
     * determined as follows:
     * <p/>
     * RFC 2396 states:
     * -----
     * Data characters that are allowed in a URI but do not have a
     * reserved purpose are called unreserved.  These include upper
     * and lower case letters, decimal digits, and a limited set of
     * punctuation marks and symbols.
     * <p/>
     * unreserved  = alphanum | mark
     * <p/>
     * mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     * <p/>
     * Unreserved characters can be escaped without changing the
     * semantics of the URI, but this should not be done unless the
     * URI is being used in a context that does not allow the
     * unescaped character to appear.
     * -----
     * <p/>
     * It appears that both Netscape and Internet Explorer escape
     * all special characters from this list with the exception
     * of "-", "_", ".", "*". While it is not clear why they are
     * escaping the other characters, perhaps it is safest to
     * assume that there might be contexts in which the others
     * are unsafe if not escaped. Therefore, we will use the same
     * list. It is also noteworthy that this is consistent with
     * O'Reilly's "HTML: The Definitive Guide" (page 164).
     * <p/>
     * As a last note, Internet Explorer does not encode the "@"
     * character which is clearly not unreserved according to the
     * RFC. We are being consistent with the RFC in this matter,
     * as is Netscape.
     */
    private static BitSet calculateUrlSafeCharacterBitset() {
        final BitSet urlSafeCharacters = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            urlSafeCharacters.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            urlSafeCharacters.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            urlSafeCharacters.set(i);
        }
        urlSafeCharacters.set('-');
        urlSafeCharacters.set('_');
        urlSafeCharacters.set('.');
        urlSafeCharacters.set('*');
        return urlSafeCharacters;
    }


    /**
     * It has come to my attention that {@link URLEncoder#encode(String, String)} creates a bunch of objects even when it does nothing.
     */
    public static String urlEncodeLazily(final String s) {
        if ((isUrlSafe(s))) {
            return s;
        }
        return Utilz.urlEncode(s);
    }

    public static boolean isUrlSafe(@Nonnull final String s) {
        for (int i = 0; i < s.length(); i++) {
            final int codePoint = s.codePointAt(i);
            if (!urlSafeCharacters.get(codePoint)) {
                return false;
            }
            if (Character.isSupplementaryCodePoint(codePoint)) {
                i++;
            }
        }
        return true;
    }

    public static String urlEncodeNull(@Nullable String s) {
        if (s == null) {
            s = "";
        }
        return urlEncode(s);
    }

    public static String urlDecode(@Nonnull String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("UTF-8 encoding vanished", e); // this should never happen
            return null;
        }
    }

    /**
     * Left-pads a String with spaces so it is length <code>n</code>.  If the String
     * is already at least length n, no padding is done.
     */
    @Nonnull
    public static String leftpad(@Nonnull String s, int n) {
        return leftpad(s, n, ' ');
    }

    /**
     * Left-pads a String with the specific padChar so it is length <code>n</code>.  If the String
     * is already at least length n, no padding is done.
     */
    @Nonnull
    public static String leftpad(@Nonnull String s, int n, char padChar) {
        int diff = n - s.length();
        if (diff <= 0) {
            return s;
        }
        StringBuilder buf = new StringBuilder(n);

        for (int i = 0; i < diff; ++i) {
            buf.append(padChar);
        }
        buf.append(s);
        return buf.toString();
    }

    /**
     * Counts the number of times a specified char appears in a String.
     */
    public static int occurrences(@Nonnull String s, char ch) {
        int c = 0;
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == ch) {
                c++;
            }
        }
        return c;
    }

    /**
     * Returns the next hour after the specified time, i.e., 3:28am would return 4:00am
     */
    public static long getNextHour(long currentTimeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        calendar.add(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTimeInMillis();
    }

    private static final Random r = new Random();

    /**
     * Returns <code>n</code> random elements from an array.  The array is not modified.
     */
    public static List<String> getRandomElements(String[] samples, int n) {
        String[] copy = new String[samples.length];
        System.arraycopy(samples, 0, copy, 0, copy.length);
        List<String> list = new ArrayList<String>(n);

        int len = copy.length;
        for (int i = len; i > 1 && list.size() < n; i--) {
            swap(copy, i - 1, r.nextInt(i));
            list.add(copy[i - 1]);
        }

        return list;
    }

    /**
     * Swaps two elements in an array
     */
    private static void swap(@Nonnull String[] array, int i, int j) {
        String tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    /**
     * Joins a Collection of Strings together with a given divider.
     * Note that this adds trailing delimiters which is usually undesirable.
     *
     * @deprecated Try {@link StringUtils#join(java.util.Collection, String)} instead.
     */
    @Deprecated
    public static String join(Collection<String> keywords, String divider) {
        StringBuilder buf = new StringBuilder(keywords.size() * 7);   // estimate the size
        for (String s : keywords) {
            buf.append(s).append(divider);
        }
        return buf.toString().trim();
    }

    public static String join(String[] keywords, String delim) {
        StringBuilder sb = new StringBuilder(keywords.length * 7);
        for (int i = 0; i < keywords.length; i++) {
            if (i != 0) {
                sb.append(delim);
            }
            sb.append(keywords[i]);
        }
        return sb.toString();
    }

    /**
     * Encrypts an integer (e.g. topic or comment ID) for a URL so the user
     * can't immediately recognize it.
     */
    public static String encryptIntegerURL(int id) {
        return toHex(encryptString(FILLER + id).getBytes(UTF_8));
    }

    /**
     * Decrypts an integer (e.g. topic or comment ID) for a URL so the user
     * can't immediately recognize it.
     */
    public static int decryptIntegerURL(String id) throws NumberFormatException {
        return Integer.valueOf(encryptString(new String(fromHex(id))).
            substring(FILLER.length()));
    }

    /* Dash Encoding:
     *
     * Zeta-Jones <=> Zeta--Jones
     * Blade - The Last Installment <=> Blade---The-Last-Installment
     * Wrongo -Weird => Wrongo---Weird (decodes to => Wrongo - Weird)
     * Wrongo- Weird => Wrongo---Weird (decodes to => Wrongo - Weird)
     */
    private static Pattern dashes = Pattern.compile("--+"); // "--" => "-"
    private static Pattern blanks = Pattern.compile("\\s\\s+"); // "   " => " "

    private static Pattern hyphen = Pattern.compile("(?<=[^-\\s])-(?=[^-\\s])"); // like "Zeta-Jones"
    private static Pattern dash = Pattern.compile("[\\s]-[\\s]|-[\\s]|[\\s]-"); // like "Blade - The Last Installment"
    private static Pattern blank = Pattern.compile("\\s+");


    public static String dashEncode(String s) {
        if (s == null) {
            return s;
        }
        s = blank.matcher(
            hyphen.matcher(
                dash.matcher(
                    dashes.matcher(
                        blanks.matcher(s.trim()).replaceAll(" ") // compress embedded whitespace "   " => " "
                    ).replaceAll("-") // trim and compress multiple dashes "---" => "-"
                ).replaceAll("---") // replace dash with surrounding white space => "---"
            ).replaceAll("--") // replace single "-" => "--"
        ).replaceAll("-"); // replace blanks with "-"
        return s;
    }


    public static String dashNormalize(String s) {
        if (s == null) {
            return s;
        }
        s = dash.matcher(
            dashes.matcher(
                blanks.matcher(s.trim()).replaceAll(" ") // trim and compress embedded whitespace "   " => " "
            ).replaceAll("-") // compress multiple dashes "---" => "-"
        ).replaceAll(" - "); // regularize dash with surrounding white space => " - "
        return s;
    }


    /* Dash Decoding:
     *
     * notes - hyphenated words are usually tokenized as seperate words. "A" is special:
     *
     * a-plus == a - plus => +plus	stopword "a"
     *
     * whereas:
     *
     * b-plus == b - plus => +b +plus
     *
     * Hyphenated words with numbers in them are "part-numbers" parsed as one token:
     *
     * a-1 => +a-1      like a part number. Any hyphenated letter-number combo will do it.
     *
     */

    private static Pattern encodedHyphen = Pattern.compile("--"); // like "Zeta-Jones"
    private static Pattern encodedDash = Pattern.compile("---"); // like "Blade - The Last Installment"
    private static Pattern encodedBlank = Pattern.compile("(?<=[^-])-(?=[^-])");

    public static String dashDecode(String s) {
        StringBuffer sb = new StringBuffer(s.length() * 2);
        sb.append(
            encodedHyphen.matcher(
                encodedDash.matcher(
                    encodedBlank.matcher(s.trim()).replaceAll(" ")
                ).replaceAll(" - ")
            ).replaceAll("-"));
        return sb.toString();
    }

    /**
     * Returns a string with {@code maxLength} random numeric (decimal) digits.
     */
    public static String getRandomDigits(int maxLength) {
        if (maxLength <= 0) {
            return "";
        }

        Random rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        // first digit cannot be '0'
        int firstDigit;
        while ((firstDigit = rnd.nextInt(10)) == 0) {
            // noop
        }
        sb.append(firstDigit);
        for (int i = 1; i < maxLength; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Take a collection of Integer objects and create an array of primitive ints.
     */
    @Nonnull
    public static int[] toIntArray(@Nonnull Collection<Integer> intCollection) {
        int[] ret = new int[intCollection.size()];
        int i = 0;
        for (Integer value : intCollection) {
            ret[i++] = value;
        }
        return ret;
    }

    /**
     * Store an integer as an array of bytes.
     */
    @Nonnull
    public static byte[] intToBytes(int data) {
        return new byte[]{
            (byte) ((data >> 24) & 0xff),
            (byte) ((data >> 16) & 0xff),
            (byte) ((data >> 8) & 0xff),
            (byte) ((data) & 0xff),
        };
    }

    /**
     * Undo {@link #intToBytes(int)} and reconstruct the int value.
     */
    public static int bytesToInt(@Nullable byte[] data) {
        if (data == null || data.length != 4) {
            return 0x0;
        }
        return (0xff & data[0]) << 24 |
               (0xff & data[1]) << 16 |
               (0xff & data[2]) << 8 |
               (0xff & data[3]);
    }

    public static int bytesToInt(@Nullable byte[] data, final int offset) {
        if (data == null) {
            return 0x0;
        }
        return (0xff & data[offset + 0]) << 24 |
               (0xff & data[offset + 1]) << 16 |
               (0xff & data[offset + 2]) << 8 |
               (0xff & data[offset + 3]);
    }

    public static void intToBytes(final int data, @Nonnull final byte[] buffer, final int offset) {
        buffer[offset + 0] = (byte) ((data >> 24) & 0xff);
        buffer[offset + 1] = (byte) ((data >> 16) & 0xff);
        buffer[offset + 2] = (byte) ((data >> 8) & 0xff);
        buffer[offset + 3] = (byte) ((data) & 0xff);
    }

    /**
     * Store a long as an array of bytes.
     */
    @Nonnull
    public static byte[] longToBytes(long data) {
        final byte[] bytes = new byte[8];
        longToBytes(data, bytes, 0);
        return bytes;
    }

    /**
     * Store a long into an array of bytes.
     */
    public static void longToBytes(final long data, @Nonnull final byte[] buffer, final int offset) {
        buffer[offset + 0] = (byte) ((data >> 56) & 0xff);
        buffer[offset + 1] = (byte) ((data >> 48) & 0xff);
        buffer[offset + 2] = (byte) ((data >> 40) & 0xff);
        buffer[offset + 3] = (byte) ((data >> 32) & 0xff);
        buffer[offset + 4] = (byte) ((data >> 24) & 0xff);
        buffer[offset + 5] = (byte) ((data >> 16) & 0xff);
        buffer[offset + 6] = (byte) ((data >> 8) & 0xff);
        buffer[offset + 7] = (byte) ((data) & 0xff);
    }

    /**
     * Undo {@link #longToBytes(long)} and reconstruct the long value.
     */
    public static long bytesToLong(@Nullable final byte[] data, final int offset) {
        if (data == null) {
            return 0x0;
        }
        return (long) (0xff & data[offset + 0]) << 56 |
               (long) (0xff & data[offset + 1]) << 48 |
               (long) (0xff & data[offset + 2]) << 40 |
               (long) (0xff & data[offset + 3]) << 32 |
               (long) (0xff & data[offset + 4]) << 24 |
               (long) (0xff & data[offset + 5]) << 16 |
               (long) (0xff & data[offset + 6]) << 8 |
               (long) (0xff & data[offset + 7]);
    }

    /**
     * Undo {@link #longToBytes(long)} and reconstruct the long value.
     */
    public static long bytesToLong(@Nullable byte[] data) {
        if (data == null || data.length != 8) {
            return 0x0;
        }
        return bytesToLong(data, 0);
    }

    public static long parse8ByteHashString(final String hash, final int start) {
        if (hash.length() - 16 < start) {
            throw new IllegalArgumentException("String " + hash + " doesn't have at least 16 chars after " + start);
        }
        long l = 0;
        for (int i = start; i < start + 16; i++) {
            //  0-15
            final int digit = Character.digit(hash.charAt(i), 16);
            if (digit == -1) {
                throw new IllegalArgumentException(hash.charAt(i) + " is not a valid hexadecimal digit");
            }
            l = (l << 4) | digit;
        }
        return l;
    }

    /**
     * @param hash must be a 32-char hexadecimal
     */
    public static long[] parse16ByteHashString(final String hash) {
        final long l0 = parse8ByteHashString(hash, 0);
        final long l1 = parse8ByteHashString(hash, 16);
        return new long[]{l0, l1};
    }

    public static long[] parseUnvalidated16ByteHashString(final String hash) {
        try {
            return parse16ByteHashString(hash);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Compares 2 large integer strings (too large to convert to longs). These may be
     * in any radix but must both be in the SAME radix.
     * Returns positive if the first is larger, negative if the second is larger,
     * and 0 if they are equal. Nulls and any amount of white space are treated as
     * less than 0 for purposes of the comparison.
     * <B>NOTE:</B> This method does NOT validate that the strings are actually numbers;
     * after checking for nullity and length, it does a case-insensitive lexical comparison,
     * which will give the correct result if both numbers are in the same radix.
     *
     * @param num1 A positive integer string in any radix, or null.
     * @param num2 A positive integer string in the same radix as num1, or null.
     * @return A positive number if the first argument is larger, a negative argument is
     *         larger, or 0 if they are equal.
     */
    public static int compareAsBigInts(String num1, String num2) {
        num1 = (num1 == null) ? "" : num1.trim();
        num2 = (num2 == null) ? "" : num2.trim();

        if (num1.length() != num2.length()) {
            return num1.length() - num2.length();
        } else {
            return num1.compareToIgnoreCase(num2);
        }
    }

    /**
     * Count the number of distinct runs of non-whitespace characters
     * Note that this will probably not do what you want in CJK languages
     */
    public static int countWords(@Nonnull final String q) {
        int i = 0;
        int words = 0;
        //  count runs of non-whitespace characters
        for (; i < q.length(); i++) {
            //  burn through whitespace
            for (; (i < q.length()) && (Character.isWhitespace(q.charAt(i))); i++) { /* intentionally empty */ }
            if (i < q.length()) {
                words++;
                //  burn through the rest of the word
                for (; (i < q.length()) && (!Character.isWhitespace(q.charAt(i))); i++) { /* intentionally empty */ }
            }
        }
        return words;
    }

    /**
     * Saner looking equivalent of "a.equals(b)" that isn't NPE prone.
     *
     * @param a an object or null
     * @param b an object or null
     * @return true if they are equal
     */
    public static boolean eq(@Nullable final Object a, @Nullable final Object b) {
        if (a == null && b == null) {
            return true;
        } else if (a != null && b != null) {
            return a.equals(b);
        } else {
            return false;
        }
    }

    /**
     * Saner looking equivalent of "a.compareTo(b)" that isn't NPE prone
     *
     * @param a an object or null
     * @param b an object or null
     * @return output of comparator on a,b
     */
    public static <T extends Comparable<T>> int cmpNullable(@Nullable Comparable<T> a, @Nullable T b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return a.compareTo(b);
    }

    /**
     * utility for chunking a potentially large IN-list of something, see {#getChunkStart} and {#getChunkEnd}
     *
     * @param count total number of things
     * @param chunkSize number of items per chunk
     * @return number of chunks, rounded up by 1 if there are some stray items
     */
    public static int getNumChunks(@Nonnegative final int count, @Nonnegative final int chunkSize) {
        if (count <= 0 || chunkSize <= 0) {
            return 0; // do something sane if someone is insane
        }
        final int div = count / chunkSize;
        if (count % chunkSize == 0) {
            return div;
        } else {
            return div + 1;
        }
    }

    /**
     * utility for chunking a potentially large IN-list of something
     *
     * @param count total number of things
     * @param chunkSize number of items per chunk
     * @param chunkNum index of the chunk (0-indexed), see {#getNumChunks}
     * @return starting position in original list, or -1 if there's some problem
     */
    public static int getChunkStart(@Nonnegative final int count, @Nonnegative final int chunkSize, @Nonnegative final int chunkNum) {
        if (count <= 0 || chunkNum < 0 || chunkSize <= 0) {
            return -1; // do something sane if someone is insane
        }

        final int from = chunkNum * chunkSize;
        if (from >= count) {
            return -1; // too high!
        }
        return from;
    }

    /**
     * utility for chunking a potentially large IN-list of something
     *
     * @param count total number of things
     * @param chunkSize number of items per chunk
     * @param chunkNum index of the chunk (0-indexed), see {#getNumChunks}
     * @return ending position in original list, or -1 if there's some problem with inputs
     */
    public static int getChunkEnd(@Nonnegative final int count, @Nonnegative final int chunkSize, @Nonnegative final int chunkNum) {
        if (count <= 0 || chunkNum < 0 || chunkSize <= 0) {
            return -1; // do something sane if someone is insane
        }

        final int to = (chunkNum + 1) * chunkSize;
        if (to > count) {
            // don't max out on me!
            return count;
        }
        return to;
    }

    /**
     * Simple implementation of SQL COALESCE function for java values.
     * <p/>
     * NOTE: No parallel implementation for Iterable values, since
     * {@link com.google.common.collect.Iterables#find(Iterable,
     * com.google.common.base.Predicate) } with
     * {@link com.google.common.base.Predicates#notNull} works
     * there.
     *
     * @param values Zero or more
     * @param <T> The most recent common ancestor of the given values.
     * @return The first non-<code>null</code> value from the arguments.
     *         <code>null</code>, if there are no nonnull values in the
     *         arguments list (including for empty args lists).
     */
    @Nullable
    public static <T> T coalesce(final T... values) {
        T result = null;

        for (final T value : values) {
            if (null != value) {
                result = value;
                break;
            }
        }

        return result;
    }

    /**
     * Safe version of {#coalesce}, assuming the final element in the list is compiler-guaranteed to be nonnull.
     *
     * @return First nonnull element in the list of arguments
     */
    @SuppressWarnings({"JavaDoc"})
    @Nonnull
    public static <T> T coalesceSafely(@Nullable final T t1, @Nonnull final T fallback) {
        return null == t1 ? fallback : t1;
    }

    /**
     * Safe version of {#coalesce}, assuming the final element in the list is compiler-guaranteed to be nonnull.
     *
     * @return First nonnull element in the list of arguments
     */
    @SuppressWarnings({"JavaDoc"})
    @Nonnull
    public static <T> T coalesceSafely(@Nullable final T t1, @Nullable final T t2, @Nonnull final T fallback) {
        @SuppressWarnings({"unchecked"}) @Nullable final T candidate = Utilz.coalesce(t1, t2);
        return null == candidate ? fallback : candidate;
    }

    /**
     * Safe version of {#coalesce}, assuming the final element in the list is compiler-guaranteed to be nonnull.
     *
     * @return First nonnull element in the list of arguments
     */
    @SuppressWarnings({"JavaDoc"})
    @Nonnull
    public static <T> T coalesceSafely(@Nullable final T t1, @Nullable final T t2, @Nullable final T t3, @Nonnull final T fallback) {
        @SuppressWarnings({"unchecked"}) @Nullable final T candidate = Utilz.coalesce(t1, t2, t3);
        return null == candidate ? fallback : candidate;
    }

    /**
     * Safe version of {#coalesce}, assuming the final element in the list is compiler-guaranteed to be nonnull.
     *
     * @return First nonnull element in the list of arguments
     */
    @SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
    @Nonnull
    public static <T> T coalesceSafely(@Nullable final T t1, @Nullable final T t2, @Nullable final T t3, @Nullable final T t4, @Nonnull final T fallback) {
        @SuppressWarnings({"unchecked"}) @Nullable final T candidate = Utilz.coalesce(t1, t2, t3, t4);
        return null == candidate ? fallback : candidate;
    }

    /**
     * Safe version of {#coalesce}, assuming the final element in the list is compiler-guaranteed to be nonnull.
     *
     * @return First nonnull element in the list of arguments
     */
    @SuppressWarnings({"JavaDoc", "UnusedDeclaration", "unchecked"})
    @Nonnull
    public static <T> T coalesceSafely(@Nullable final T t1, @Nullable final T t2, @Nullable final T t3, @Nullable final T t4, @Nullable final T t5, final T... others) {
        @Nullable
        final T result;

        @SuppressWarnings({"unchecked"}) @Nullable final T candidate = Utilz.coalesce(t1, t2, t3, t4, t5);
        if (null != candidate) {
            result = candidate;

        } else {
            result = Utilz.coalesce(others);

            if (null == result) {
                throw new NullPointerException();
            }
        }

        return result;
    }

    /**
     * Map equivalent of {@link StringUtils#isEmpty}, for
     * projects that do not need/want the rest of commons
     * collections.
     *
     * @param map
     * @return <code>true</code>, if <code>map</code> is
     *         <code>null</code> or empty.
     */
    public static boolean isEmpty(@Nullable final Map<?, ?> map) {
        return null == map || map.isEmpty();
    }
}
