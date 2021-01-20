package com.customization.hxbank.prtc.util;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by YeShengtao on 2020/9/27 11:13
 */
public class FileUtils {

    public static List<File> allFile(File file) {
        return allFile(file, -1);
    }

    /**
     * 获取指定目录下的所有的文件
     * @param file 指定目录
     * @param dept dept 递归深度 为-1时 则表示递归所有层级, 1表示获取目录下第一层
     * @return List<File>
     */
    public static List<File> allFile(File file, int dept) {
        if (!file.exists()) {
            throw new RuntimeException("文件目录不存在");
        }
        if (!file.isDirectory()) {
            throw new RuntimeException("file is not directory");
        }
        return recursionFile(file, dept, File::isFile);
    }

    public static List<File> recursionFile(File file, int dept, Function<File, Boolean> callable) {
        List<File> list = Lists.newArrayList();
        if (dept == 0) {
            return list;
        }
        List<File> nextFileList = Lists.newArrayList();
        List<File> tempList = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(x -> {
            // 如果是目录添加到集合中
            if (x.isDirectory()) {
                nextFileList.add(x);
            }
            if (callable.apply(x)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        int newDept = -1;
        if (dept != -1) {
            newDept = dept - 1;
        }
        list.addAll(tempList);
        for (File tempFile : nextFileList) {
            list.addAll(recursionFile(tempFile, newDept, callable));
        }
        return list;
    }

    @Test
    public void test01() {
        File file  = new File("C:\\Users\\Qfeng\\Desktop\\upload\\prtc");
        System.out.println(file.isDirectory());
        System.out.println(file.listFiles().length);
        Arrays.stream(file.listFiles()).forEach(x -> {
            System.out.println(x.isFile());
        });
        List<File> list = allFile(file);
        list.forEach(x -> {
            System.out.println(x.getAbsolutePath());
            System.out.println(x.getParentFile().getName());
            System.out.println(x.getAbsolutePath());
        });
        System.out.println(1);
    }

    @Test
    public void test02() {
        File file = new File("C:\\Users\\Qfeng\\Desktop\\upload\\prtc\\123454-333333-fieldname\\1111.txt");
        System.out.println(file.isFile());
    }

}