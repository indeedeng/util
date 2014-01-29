package com.indeed.util.urlparsing;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * @author ahudson
 * @author jchien
 * @author: preetha
 */

public final class ParseUtils {
    /**
     * Parses out an int value from the provided string, equivalent to Integer.parseInt(s.substring(start, end)),
     * but has significantly less overhead, no object creation and later garbage collection required
     */
    public static int parseInt(String s, final int start, final int end) throws NumberFormatException {
        return parseSignedInt(s, start, end);
    }

    /**
     * Parses out an int value from the provided string, equivalent to Integer.parseInt(s.substring(start, end)),
     * but has significantly less overhead, no object creation and later garbage collection required.
     *
     * @throws {@link NumberFormatException} if it encounters any character that is not [-0-9].
     */
    public static int parseSignedInt(CharSequence s, final int start, final int end) throws NumberFormatException {
        if (s.charAt(start) == '-') {
            // negative!
            return -parseUnsignedInt(s, start + 1, end);
        } else {
            return  parseUnsignedInt(s, start, end);
        }
    }

    /**
     * Parses out an int value from the provided string, equivalent to Integer.parseInt(s.substring(start, end)),
     * but has significantly less overhead, no object creation and later garbage collection required.      *
     *
     */
    public static int parseUnsignedInt(CharSequence s, final int start, final int end) throws NumberFormatException {
        int ret = 0;
        for (int i = start; i < end; i++) {
            final char c = s.charAt(i);
            if (c < '0' || c > '9') {
                throw new NumberFormatException("Not a valid base-10 digit: " + c + " in " + s.subSequence(start, end));
            }
            final int val = c - '0';
            ret = ret * 10 + val;
        }
        return ret;
    }


    /**
     * Parses out a long value from the provided string, equivalent to Long.parseLong(s.substring(start, end)),
     * but has significantly less overhead, no object creation and later garbage collection required
     *
     * @throws {@link NumberFormatException} if it encounters any character that is not [-0-9].
     */
    public static long parseSignedLong(CharSequence s, final int start, final int end) throws NumberFormatException {
        if (s.charAt(start) == '-') {
            // negative!
            return -parseUnsignedLong(s, start + 1, end);
        } else {
            return  parseUnsignedLong(s, start, end);
        }
    }

    /**
     * Parses out a long value from the provided string, equivalent to Long.parseLong(s.substring(start, end)),
     * but has significantly less overhead, no object creation and later garbage collection required
     *
     * @throws {@link NumberFormatException} if it encounters any character that is not [0-9].
     */
    public static long parseUnsignedLong(CharSequence s, final int start, final int end) throws NumberFormatException {
        long ret = 0;
        for (int i = start; i < end; i++) {
            final char c = s.charAt(i);
            if (c < '0' || c > '9') {
                throw new NumberFormatException("Not a valid base-10 digit: " + c + " in " + s.subSequence(start, end));
            }
            final int val = c - '0';
            ret = ret * 10 + val;
        }
        return ret;
    }

    private static final char infinityChars[] = { 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y' };
    private static final char naNChars[] = { 'N', 'a', 'N' };

    /**
     * Parses out a float value from the provided string, more specialized than Float.parseFloat(s.substring(start, end))
     * but has significantly less overhead, no object creation and later garbage collection required
     * Does not support parsing the strings "NaN", "Infinity","-Infinity" and HexFloatingPointLiterals
     * @throws {@link NumberFormatException} if the input doesn't correspond to a float.
     */
    public static float parseFloat(String s, final int start, final int end) throws NumberFormatException {
        int i = start;
        final int sign;
        if (s.charAt(i) == '-') {
            // negative!
            sign = -1;
            i++;
        } else {
            sign = 1;
        }

        // keeping ret as a float causes problems, use a double for more precision
        double ret = 0;
        boolean decimalFound = false;
        double mult = 1;
        boolean isScientificNotation = false;

        for (; i < end; i++) {
            final char c = s.charAt(i);

            if (c == 'E' || c == 'e') {
                isScientificNotation = true;
                break;
            }

            if (c == 'N' || c == 'I') {
                final boolean isNanStart = (c == 'N');
                final char[] charsToMatch = c == 'N' ? naNChars : infinityChars;
                int j = 0;
                while ( i < end && j < charsToMatch.length) {
                    if (s.charAt(i) == charsToMatch[j]) {
                        i++;
                        j++;
                    } else {
                        throw new NumberFormatException("Unsupported format, unable to parse input string ");
                    }
                }

                if (i == end && j == charsToMatch.length) { //this means we matched either NaN or Infinity
                     if (isNanStart) {
                         return Float.NaN;
                     } else if (sign == 1) {
                         return Float.POSITIVE_INFINITY;
                     }  else {
                         return Float.NEGATIVE_INFINITY;
                     }
                }  else {
                   throw new NumberFormatException("Unsupported format, unable to parse input string ");
                }

            }

            if (decimalFound) {
                mult *= 10;
            }

            if (c >= '0' && c <= '9') {
                final int val = c - '0';
                ret = ret * 10 + val;
            } else if (c == '.') {
                decimalFound = true;
            } else {
                throw new NumberFormatException("Not a valid base-10 digit: " + c + " in " + s.substring(start, end));
            }
        }

        ret /= mult;

        if (isScientificNotation) {
            int exponent = parseSignedInt(s, ++i, end);
            ret *= Math.pow(10, exponent);
        }

        return (float)(ret * sign);
    }
    /**
     * Parses out the timestamp portion of the uid Strings used in the logrepo
     */
    public static long parseTimestampFromUIDString(String s, final int start, final int end) {
        long ret = 0;
        for (int i = start; i < end && i < start + 9; i++) {
            ret <<= 5;
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                ret |= c - '0';
            } else if (c >= 'a' && c <= 'v') {
                ret |= c - 'a' + 10;
            } else if (c >= 'A' && c <= 'V') {
                ret |= c - 'A' + 10;
            } else {
                throw new IllegalArgumentException(s.substring(start, end) + " is not a valid UID!");
            }
        }
        return ret;
    }

   /* URL decode @param input in place
      The original encoded form will be replaced by the decoded form in the input
    */
    public static void urlDecodeInplace(StringBuilder input) {
        urlDecodeInto(input, 0, input.length(), input, true);
    }

    /* URL decode CharSequence @param input into result given start and end offsets
       Assumes UTF-8 encoding
       Avoids creating intermediate String objects unlike UrlDecoder in java.
     */
    public static void urlDecodeInto(CharSequence input, int start, int end, StringBuilder result) {
        urlDecodeInto(input, start, end, result, false);
    }



    private static void urlDecodeInto(CharSequence input, int start, int end, StringBuilder result, boolean inplace) {
        int writeHead = start;
        for (int i = start; i < end; i++) {
            char c = input.charAt(i);
            if (c == '%' && i + 2 < end) {
                char val = decodeHexPair(input.charAt(i+1), input.charAt(i+2));
                if ((val&0xE0) == 0xC0) {
                    if (i + 5 < end && input.charAt(i+3) == '%') {
                        char val2 = decodeHexPair(input.charAt(i+4), input.charAt(i+5));
                        if (val2 != INVALID_HEX && (val2&0xC0) == 0x80) {
                            // zimmerm%C3%A4dchen
                            c = (char)(((val&0x1F)<<6)|(val2&0x3F));
                            i += 5;
                        }
                    }
                } else if ((val&0xF0) == 0xE0) {
                    if (i + 8 < end && input.charAt(i+3) == '%' && input.charAt(i+6) == '%') {
                        char val2 = decodeHexPair(input.charAt(i+4), input.charAt(i+5));
                        char val3 = decodeHexPair(input.charAt(i+7), input.charAt(i+8));
                        if (val2 != INVALID_HEX && val3 != INVALID_HEX && (val2&0xC0) == 0x80 && (val3&0xC0) == 0x80) {
                            // Technologist+%E2%80%93+Full+Time
                            c = (char)(((val&0x0F)<<12)|((val2&0x3F)<<6)|(val3&0x3F));
                            i += 8;
                        }
                    }
                } else if ((val&0xF8) == 0xF0) {
                    //these are code points > 0XFFFF, they need a surrogate pair to represent them
                    if (i + 11 < end && input.charAt(i + 3) == '%' && input.charAt(i + 6) == '%' && input.charAt(i + 9) == '%') {
                        char val2 = decodeHexPair(input.charAt(i + 4), input.charAt(i + 5));
                        char val3 = decodeHexPair(input.charAt(i + 7), input.charAt(i + 8));
                        char val4 = decodeHexPair(input.charAt(i + 10), input.charAt(i + 11));
                        if (val2 != INVALID_HEX && val3 != INVALID_HEX && val4 != INVALID_HEX
                                && (val2 & 0xC0) == 0x80 && (val3 & 0xC0) == 0x80 && (val4 & 0xC0) == 0x80) {
                            final int codePoint =
                                    (((val & 0x0F) << 15)
                                            | ((val2 & 0x3F) << 12)
                                            | ((val3 & 0x3F) << 6)
                                            | (val4 & 0x3F));
                            if (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                                c = (char) codePoint;
                            } else {
                                final int offset = codePoint - Character.MIN_SUPPLEMENTARY_CODE_POINT;
                                final char highChar = (char) ((offset >>> 10) + Character.MIN_HIGH_SURROGATE);
                                final char lowChar = (char) ((offset & 0x3ff) + Character.MIN_LOW_SURROGATE);
                                if (!inplace) {
                                    result.append(highChar);
                                } else {
                                    result.setCharAt(writeHead++, highChar);
                                }
                                c = lowChar; // let normal machinery take over here
                            }
                            i += 11;
                        }
                    }
                } else if (val != INVALID_HEX) {
                    c = val;
                    i += 2;
                }
            } else if (c == '+') {
                c = ' ';
            }

            if (!inplace) {
                result.append(c);
            } else {
                result.setCharAt(writeHead++, c);
            }
        }
        if (inplace) {
            result.delete(writeHead, end);
        }
    }

    public static final char INVALID_HEX = (char)256;
    /*
     Decodes given hex pair into char
     Example - decodeHexPair('3','A') returns ':'
     */
    public static char decodeHexPair(char c1, char c2) {
        char ret = 0;
        if (c1 >= '0' && c1 <= '9') {
            ret |= c1-'0';
        } else if (c1 >= 'a' && c1 <= 'f') {
            ret |= c1-'a'+10;
        } else if (c1 >= 'A' && c1 <= 'F') {
            ret |= c1-'A'+10;
        } else {
            return INVALID_HEX;
        }
        ret <<= 4;
        if (c2 >= '0' && c2 <= '9') {
            ret |= c2-'0';
        } else if (c2 >= 'a' && c2 <= 'f') {
            ret |= c2-'a'+10;
        } else if (c2 >= 'A' && c2 <= 'F') {
            ret |= c2-'A'+10;
        } else {
            return INVALID_HEX;
        }
        return ret;
    }

}

