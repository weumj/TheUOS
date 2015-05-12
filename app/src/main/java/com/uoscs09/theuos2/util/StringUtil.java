package com.uoscs09.theuos2.util;

public final class StringUtil {
    public static final String STR_HOUR = "hour";
    public static final String STR_MIN = "min";
    public static final String STR_AM = "오전 ";
    public static final String STR_PM = "오후 ";
    public final static String SPACE = " ";
    public final static String NEW_LINE = "\n";
    public final static String NULL = "";
    public static final String ENCODE_EUC_KR = "EUC-KR";
    public static final String ENCODE_UTF_8 = "UTF-8";
    public final static String AMP = "&";
    public final static String CODE_R_PRNTSIS_CODE = "&#40;";
    public final static String CODE_R_PRNTSIS = "(";
    public final static String CODE_L_PRNTSIS_CODE = "&#41;";
    public final static String CODE_L_PRNTSIS = ")";
    public final static String CODE_AMP_CODE = "&amp;";
    public final static String CODE_LT_CODE = "&lt;";
    public final static String CODE_LT = "<";
    public final static String CODE_GT_CODE = "&gt;";
    public final static String CODE_GT = ">";
    public final static String CODE_SPACE = "&nbsp;";
    public final static String CODE_QUOT_CODE = "&quot;";
    public final static String CODE_QUOT = "\"";

    public static String remove(String target, String remove) {
        return target.replace(remove, NULL);
    }

    public static String removeRegex(String target, String removeRegex) {
        return target.replaceAll(removeRegex, NULL);
    }

    public static String replaceHtmlCode(String target) {
        return target.replace(CODE_AMP_CODE, AMP)
                .replace(CODE_GT_CODE, CODE_GT)
                .replace(CODE_L_PRNTSIS_CODE, CODE_L_PRNTSIS)
                .replace(CODE_LT_CODE, CODE_LT)
                .replace(CODE_R_PRNTSIS_CODE, CODE_R_PRNTSIS)
                .replace(CODE_QUOT_CODE, CODE_QUOT);
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
