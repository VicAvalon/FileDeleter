package com.util;

import com.sun.deploy.util.ArrayUtil;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collector;

public class FileUtil {

    private static final Logger logger = Logger.getLogger(FileUtil.class.getName());

    /**
     * 返回给定路径下所有文件的集合
     * @param fullFilePath 给定路径（文件夹）
     * @return
     */
    public File[] listThisFolderOfFile(String fullFilePath) {
        if (!new File(fullFilePath).isDirectory()) {
            logger.warning("这不是文件夹 ：" + fullFilePath);
        }
        File file = new File(fullFilePath);
        return file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
    }


    public static void main(String[] args) {
        String filepath = "D:\\Ebook";
        String nowScanFolder = "";
        String nowScanFile = "";
        List<String> files = new ArrayList<>();
        List<String> folders = new ArrayList<>();
        File file = new File(filepath);
        File[] nextFiles = file.listFiles();
        boolean hasDirectory;
        for(File nextFile : nextFiles) {
            if (nextFile.isDirectory()) {
                folders.add(nextFile.getAbsolutePath());
            }
            if (nextFile.isFile()) {
                files.add(nextFile.getAbsolutePath());
            }
        }
        Comparator<Object> comparator = Collator.getInstance(java.util.Locale.CHINA);
        String[] filesTemp = files.toArray(new String[]{});
        String[] foldersTemp = folders.toArray(new String[]{});
        Arrays.sort(files.toArray(new String[]{}), comparator);
        Arrays.sort(folders.toArray(new String[]{}), comparator);
        System.err.println("files...");
        for (int i = 0; i < files.size(); i++) {
            System.out.println(files.get(i));
        }
        System.out.println("folders...");
        for (int i = 0; i < folders.size(); i++) {
            System.err.println(folders.get(i));
        }
        nowScanFolder = filepath;
        //scan->

        //end

    }
}
