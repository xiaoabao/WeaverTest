package com.customization.hxbank.prtc.util;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Created by YeShengtao on 2020/7/30 16:50
 */
public class PropUtils {


    /**
     * 返回properties文件中指定的参数值
     *
     * @param filename filename
     * @param key      key
     * @return
     */
    public static String get(String filename, String key) {
        try {
            Properties properties = new Properties();
//            File file = new File(filename + ".properties");
            properties.load(ClassLoader.getSystemResourceAsStream(filename + ".properties"));
//            properties.load(new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)));
            return properties.getProperty(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回properties文件中符合匹配规则的的参数值集合
     *
     * @param filename filename
     * @param regex    regex
     * @return Map<String, String>
     */
    public static Map<String, String> regexMap(String filename, String regex) {
        Map<String, String> map = Maps.newHashMap();
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.getSystemResourceAsStream(filename + ".properties"));
//            File file = new File(filename + ".properties");
//            properties.load(new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)));
            properties.stringPropertyNames().forEach(x -> {
                if (x.matches(regex)) {
                    map.put(x, properties.getProperty(x));
                }
            });
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return map;
        }
    }

    /**
     * 返回properties文件中符合匹配规则的的参数值集合
     *
     * @param filename filename
     * @param regex    regex
     * @param callable callable
     * @return Map<String, String>
     */
    public static Map<String, String> regexMap(String filename, String regex, Function<String, String> callable) {
        Map<String, String> map = Maps.newHashMap();
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.getSystemResourceAsStream(filename + ".properties"));
//            File file = new File(filename + ".properties");
//            properties.load(new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)));
            properties.stringPropertyNames().forEach(x -> {
                if (x.matches(regex)) {
                    map.put(callable.apply(x), properties.getProperty(x));
                }
            });
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return map;
        }
    }

    @Test
    public void test01() {

    }
}
