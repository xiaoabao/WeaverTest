package com.customization.hxbank.prtc.util;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 日期相关工具类
 * @Author Qfeng
 * @Date 2020-01-09 11:44:45
 */
public class DateUtils {

    public static String min(String date1, String date2) {
        try {
            if (StringUtils.isEmpty(date1)) return date2;
            if (StringUtils.isEmpty(date2)) return date1;
            long time1 = parse(date1).getTime();
            long time2 = parse(date2).getTime();
            return time1 < time2 ? date1 : date2;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String max(String date1, String date2) {
        try {
            if (StringUtils.isEmpty(date1)) return date2;
            if (StringUtils.isEmpty(date2)) return date1;
            long time1 = parse(date1).getTime();
            long time2 = parse(date2).getTime();
            return time1 > time2 ? date1 : date2;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static boolean checkBetween(String datetime, String date1, String date2) {
        try {
            long time1 = parse(date1).getTime();
            long time2 = parse(date2).getTime();
            long check = parse(datetime, "yyyy-MM-dd HH:mm:ss").getTime();
            return check >= time1 && check <= time2;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Date now() {
        return new Date();
    }

    public static Date parse(String date) throws ParseException {
        return parse(date, "yyyy-MM-dd");
    }

    public static Date parse(String date, String pattern) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.parse(date);
    }

    /**
     * 根据给定日期 和 格式串 返回指定格式日期时间
     * @param date 日期时间
     * @param partten 格式串
     * @return 指定格式日期时间
     */
    public static String date(Date date, String partten) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(partten);
        return dateFormat.format(date);
    }

    public static Date getDate(Date date, String pattern) throws ParseException {
        return new SimpleDateFormat(pattern).parse(date(date, pattern));
    }

    public static Date getDate(Date date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date(date, "yyyy-MM-dd"));
    }

    public static Date getDate() throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date(new Date(), "yyyy-MM-dd"));
    }

    public static Date getDate(String pattern) throws ParseException {
        return new SimpleDateFormat(pattern).parse(date(new Date(), pattern));
    }

    /**
     * 根据格式串返回当前日期时间指定格式
     * @param pattern 格式串
     * @return 当前日期时间指定格式
     */
    public static String date(String pattern) {
        return date(new Date(), pattern);
    }

    /**
     * 返回yyyy-MM-dd 格式指定日期时间
     * @param date 指定日期时间
     * @return yyyy-MM-dd 格式指定日期时间
     */
    public static String date(Date date) {
        return date(date, "yyyy-MM-dd");
    }

    /**
     * 返回yyyy-MM-dd 格式当前日期时间
     * @return yyyy-MM-dd 格式当前日期时间
     */
    public static String date() {
        return date("yyyy-MM-dd");
    }

    /**
     * 返回指定间隔天数的日期
     * @param date 指定日期
     * @param offset 指定偏移量(天)
     * @return 指定间隔天数的日期 (yyyy-MM-dd)
     */
    public static String date(Date date, int offset) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, offset);
        return date(calendar.getTime());
    }

    public static String offset(int offset) {
        return date(new Date(), offset);
    }

    /**
     * 返回指定间隔天数的当前日期
     * @param offset 指定偏移量(天)
     * @return 指定间隔天数的房前日期 (yyyy-MM-dd)
     */
    public static String date(int offset) {
        return date(new Date(), offset);
    }

    /**
     * 返回yyyy-MM-dd HH:mm:ss格式日期时间
     * @return yyyy-MM-dd HH:mm:ss格式日期时间
     */
    public static String datetime() {
        return date("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 返回HH:mm:ss格式时间
     * @return HH:mm:ss格式时间
     */
    public static String time() {
        return date("HH:mm:ss");
    }

    /**
     * 返回指定日期偏移天数的起始时间
     * @param date 日期
     * @param offset 偏移量
     * @return 指定日期偏移天数的起始时间
     */
    public static Date dayStart(Date date, int offset) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, offset);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 当前日期的起始时间
     * @return 当前日期的起始时间
     */
    public static Date dayStart() {
        return dayStart(new Date(), 0);
    }

    /**
     * 当前日期的偏移量起始时间
     * @param offset 偏移量
     * @return 当前日期的偏移量起始时间
     */
    public static Date dayStart(int offset) {
        return dayStart(new Date(), offset);
    }

    /**
     * 指定日期指定偏移天数结束时间
     * @param date 指定日期
     * @param offset 指定偏移天数
     * @return 指定日期指定偏移天数结束时间
     */
    public static Date dayEnd(Date date, int offset) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, offset);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 当前日期结束时间
     * @return 当前日期结束时间
     */
    public static Date dayEnd() {
        return dayEnd(new Date(), 0);
    }

    /**
     * 当前日期指定偏移天数结束时间
     * @param offset 偏移天数
     * @return 当前日期指定偏移天数结束时间
     */
    public static Date dayEnd(int offset) {
        return dayEnd(new Date(), offset);
    }

    /**
     * 时间戳
     * @return 时间戳
     */
    public static String timestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static boolean equals(Date date1, Date date2) {
        if (date1 == null && date2 != null)
            return false;
        if (date1 == date2)
            return true;
        if (date1.equals(date2))
            return true;
        return date1.getTime() == date2.getTime();
    }
}
