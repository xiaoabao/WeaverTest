package com.customization.hxbank.prtc.util;

/**
 * 字符串相关工具类
 * Created by YeShengtao on 2020/8/5 9:56
 */
public class StringUtils {

    // 为了兼容apache StringUtils
    public static String replace(String text, String repl, String with) {
        return replace(text, repl, with, -1);
    }

    public static String replace(String text, String repl, String with, int max) {
        if (!isEmpty(text) && !isEmpty(repl) && with != null && max != 0) {
            int start = 0;
            int end = text.indexOf(repl, start);
            if (end == -1) {
                return text;
            } else {
                int replLength = repl.length();
                int increase = with.length() - replLength;
                increase = Math.max(increase, 0);
                increase *= max < 0 ? 16 : (Math.min(max, 64));

                StringBuffer buf;
                for (buf = new StringBuffer(text.length() + increase); end != -1; end = text.indexOf(repl, start)) {
                    buf.append(text.substring(start, end)).append(with);
                    start = end + replLength;
                    --max;
                    if (max == 0) {
                        break;
                    }
                }

                buf.append(text.substring(start));
                return buf.toString();
            }
        } else {
            return text;
        }
    }


    /**
     * 按字符截取长度
     *
     * @param text      原文本
     * @param maxLength 截取长度（按字符）
     * @return
     */
    public static String subStringByChar(String text, Integer maxLength) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        //名称最多展示14个字符，一个汉字算两个字符，超过展示...
        StringBuilder sBuilder = new StringBuilder();
        char[] chars = text.toCharArray();
        int length = 0;
        for (char ch : chars) {
            boolean chineseChar = isChineseChar(ch) || isSymbol(ch);
            length = length + (chineseChar ? 2 : 1);
            if (length > maxLength) {
                break;
            }
            sBuilder.append(ch);
        }
        String testResult = sBuilder.toString();
        if (length > maxLength) {
            testResult = testResult;
        }
        return testResult;
    }

    /**
     * 是否中文符号
     *
     * @param ch
     * @return
     */
    public static boolean isSymbol(char ch) {
        if (isCnSymbol(ch)) return true;
        if (isEnSymbol(ch)) return true;

        if (0x2010 <= ch && ch <= 0x2017) return true;
        if (0x2020 <= ch && ch <= 0x2027) return true;
        if (0x2B00 <= ch && ch <= 0x2BFF) return true;
        if (0xFF03 <= ch && ch <= 0xFF06) return true;
        if (0xFF08 <= ch && ch <= 0xFF0B) return true;
        if (ch == 0xFF0D || ch == 0xFF0F) return true;
        if (0xFF1C <= ch && ch <= 0xFF1E) return true;
        if (ch == 0xFF20 || ch == 0xFF65) return true;
        if (0xFF3B <= ch && ch <= 0xFF40) return true;
        if (0xFF5B <= ch && ch <= 0xFF60) return true;
        if (ch == 0xFF62 || ch == 0xFF63) return true;
        if (ch == 0x0020 || ch == 0x3000) return true;
        return false;

    }


    public static boolean isCnSymbol(char ch) {
        if (0x3004 <= ch && ch <= 0x301C) return true;
        if (0x3020 <= ch && ch <= 0x303F) return true;
        return false;
    }

    public static boolean isEnSymbol(char ch) {

        if (ch == 0x40) return true;
        if (ch == 0x2D || ch == 0x2F) return true;
        if (0x23 <= ch && ch <= 0x26) return true;
        if (0x28 <= ch && ch <= 0x2B) return true;
        if (0x3C <= ch && ch <= 0x3E) return true;
        if (0x5B <= ch && ch <= 0x60) return true;
        if (0x7B <= ch && ch <= 0x7E) return true;

        return false;
    }



    /**
     * 是否汉字字符
     *
     * @param c
     * @return
     */
    public static boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }


    /**
     * 把中文转成Unicode码
     *
     * @param str
     * @return
     */
    public static String chinaToUnicode(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            int chr1 = (char) str.charAt(i);
            if (chr1 >= 19968 && chr1 <= 171941) {// 汉字范围 \u4e00-\u9fa5 (中文)
                result += "\\u" + Integer.toHexString(chr1);
            } else {
                result += str.charAt(i);
            }
        }
        return result;
    }

    public static String val(Object obj, String txt) {
        if (obj == null)
            return txt;
        return obj.toString();
    }

    public static String valNotEmpty(Object obj, String txt) {
        String val = val(obj);
        return isEmpty(val) ? txt : val;
    }

    public static String val(Object obj) {
        return val(obj, null);
    }


    /**
     * 截取字符串尾部指定长度
     *
     * @param str 字符串源
     * @param len 截取长度
     * @return 符串尾部指定长度
     */
    public static String tailSubstring(String str, int len) {
        if (len <= 0) return str;
        if (str.length() < len) throw new RuntimeException("截取长度大于字符串本身");
        return str.substring(str.length() - len);
    }

    public static String tailChar(String str) {
        return tailSubstring(str, 1);
    }

    public static String headChar(String str) {
        return str.substring(0, 1);
    }

    /**
     * 判断是否为空
     *
     * @param str 字符串源
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) return true;
        return false;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否相等
     *
     * @param str1 源串
     * @param str2 比较串
     * @return
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        else return str1.equals(str2);
    }

    /**
     * 判断字符串是否相等 忽略大小写
     *
     * @param str1 源串
     * @param str2 比较串
     * @return
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        else return str1.equalsIgnoreCase(str2);
    }

    /**
     * 比较Object 与字符串是否相等 会使用toString
     *
     * @param obj 比较源
     * @param str 对比串
     * @return
     */
    public static boolean equals(Object obj, String str) {
        if (obj == null) return equals(null, str);
        return equals(obj.toString(), str);
    }

    public static boolean equals(Object obj1, Object obj2) {
        if ((obj1 == null && obj2 != null) || (obj1 != null && obj2 == null)) {
            return false;
        }
        if (obj1 == null || obj1.equals(obj2)) {
            return true;
        }
        return equals(obj1.toString(), obj2.toString());
    }

    /**
     * 比较Object 与字符串是否相等 会使用toString 忽略大小写
     *
     * @param obj 比较源
     * @param str 对比串
     * @return
     */
    public static boolean equalsIgnoreCase(Object obj, String str) {
        if (obj == null) return equals(null, str);
        return equalsIgnoreCase(obj.toString(), str);
    }

    @FunctionalInterface
    public interface ComputeFn {
        String fn();
    }

    public static String compute(ComputeFn computeFn) {
        return computeFn.fn();
    }
}

