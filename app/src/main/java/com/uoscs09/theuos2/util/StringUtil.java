package com.uoscs09.theuos2.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class StringUtil {
    public static final String ENCODE_EUC_KR = "EUC-KR";
    public static final String ENCODE_UTF_8 = "UTF-8";

    public static String encodeEucKr(String s) {
        return encode(s, ENCODE_EUC_KR);
    }

    public static String encodeUTF8(String s) {
        return encode(s, ENCODE_UTF_8);
    }

    public static String encode(String s, String encoding) {
        try {
            return URLEncoder.encode(s, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    }

    public static String remove(String target, String remove) {
        return target.replace(remove, "");
    }

    public static String removeRegex(String target, String removeRegex) {
        return target.replaceAll(removeRegex, "");
    }

    public static String replaceHtmlCode(String target) {
        return target.replace("&amp;", "&")
                .replace("&gt;", ">")
                .replace("&#41;", ")")
                .replace("&lt;", "<")
                .replace("&#40;", "(")
                .replace("&quot;", "\"");
    }

    // from class Html
    public static String escapeHtml(CharSequence text) {
        StringBuilder out = new StringBuilder();
        withinStyle(out, text, 0, text.length());
        return out.toString();
    }

    // from class Html
    private static void withinStyle(StringBuilder out, CharSequence text, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);

            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c >= 0xD800 && c <= 0xDFFF) {
                if (c < 0xDC00 && i + 1 < end) {
                    char d = text.charAt(i + 1);
                    if (d >= 0xDC00 && d <= 0xDFFF) {
                        i++;
                        int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                        out.append("&#").append(codepoint).append(";");
                    }
                }
            } else if (c > 0x7E || c < ' ') {
                out.append("&#").append((int) c).append(";");
            } else if (c == ' ') {
                while (i + 1 < end && text.charAt(i + 1) == ' ') {
                    out.append("&nbsp;");
                    i++;
                }

                out.append(' ');
            } else {
                out.append(c);
            }
        }
    }
}
