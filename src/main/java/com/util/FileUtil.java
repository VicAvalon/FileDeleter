package com.util;

import com.model.Config;
import com.model.FileInfo;
import com.model.FilesAndFolders;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.util.locale.provider.TimeZoneNameUtility;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class.getName());
    private static final int _1MB_SIZE = 1024 * 1024;
    private static final int _1KB_SIZE = 1024;
    private static final String logInfoPath = "D:\\Ebook\\log\\logger.info";

    private static Config config = new Config();

    /**
     * 返回给定路径下所有文件的集合
     * @param fullFilePath 给定路径（文件夹）
     * @return
     */
    public FilesAndFolders listThisFolderOfFilesAndFolders(String fullFilePath) {
        if (!new File(fullFilePath).isDirectory()) {
            logger.warn("这不是文件夹 ：" + fullFilePath);
        }
        List<String> files = new ArrayList<>();
        List<String> folders = new ArrayList<>();
        File file = new File(fullFilePath);
        File[] nextFiles = file.listFiles();
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

        return new FilesAndFolders(folders, files);
    }

    public void showDuringTime() {
        long duringTime =  System.currentTimeMillis() - config.getUtilStartScanTime();
        if ((duringTime / 1000 != 0) && (duringTime / 1000 % 2 == 0)) {
            logger.info("已耗时： " + duringTime/1000);
            System.out.println("已耗时： " + duringTime/1000);
        }
    }

    public void scanFiles(List<String> fileList) {
        //start
        StringBuilder stringBuilder = new StringBuilder();
        fileList.forEach(file -> {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileFullPath(file);
            fileInfo.setSize(new File(file).length());
            fileInfo.setLastModifyTime(new File(file).lastModified());
            //大于128mb文件不计算md5
            if (fileInfo.getSize() < 500 * _1MB_SIZE)
                try {
                    fileInfo.setMd5(MD5FileUtil.getFileMD5String(new File(file)));
                }catch (Exception e) {
                    logger.error("IO 错误：" + e);
                }
            stringBuilder.append(fileInfo.toString() + "\r\n");

            this.showDuringTime();

            //缓存的文件信息大于10kb就写文件
            if (stringBuilder.length() > _1KB_SIZE * 10) {
                try {
                    System.out.println("writing a 10 kb ..." + file);
                    FileUtils.writeStringToFile(new File(logInfoPath), stringBuilder.toString(), true);
                    //信息日志文件大于10mb就另起一个日志文件
                    if (new File(logInfoPath).length() > 10 * _1MB_SIZE) {
                        FileUtils.moveFile(new File(logInfoPath),
                                new File(logInfoPath.substring(0, logInfoPath.lastIndexOf(".") + 1)
                                        + new Date().getTime()
                                        + logInfoPath.substring(logInfoPath.lastIndexOf("."), logInfoPath.length())));
                    }

                    this.showDuringTime();
                }catch (Exception e) {
                    logger.error("IO 错误：" + e);
                }
            }
        });
        //end
    }


    public static void main(String[] args) {
        String filepath = "D:\\Ebook";
        String nowScanFolder = "";
        String nowScanFile = "";
        FileUtil fileUtil = new FileUtil();
        config.setUtilStartScanTime(System.currentTimeMillis());
        config.setNowScanFolder(filepath);
        config.setNowScanFolderModifyTime(new File(filepath).lastModified());

        FilesAndFolders filesAndFolders = fileUtil.listThisFolderOfFilesAndFolders(config.getNowScanFolder());
        fileUtil.scanFiles(filesAndFolders.getFiles());

    }
}
