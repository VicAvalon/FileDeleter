package com.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Config;
import com.model.FileInfo;
import com.model.FilesAndFolders;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
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
    private static final int BIG_FILE_SIZE = _1MB_SIZE * 128;

    private static final String path = "deleter/";
    private static final String logInfoPath = path + "log/logger.info";
    private static Config config;

    private static StringBuilder stringBuilderForFileInfo = new StringBuilder();

    public FileUtil() {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            logger.info(new File(path + "/deleter.config").getAbsolutePath());
            this.config = objectMapper.readValue(new File(path + "/deleter.config"), Config.class);
            if (ObjectUtils.isEmpty(this.config.getScanFolder())) {
                logger.error("没有发现原始扫描需求，请在配置文件中配置，字段为scanFolder。");
                System.exit(0);
            }
            if (new File(this.config.getScanFolder()).isFile()) {
                logger.error("这是文件，需要文件夹，请重配置。");
                System.exit(0);
            }
            if (ObjectUtils.isEmpty(this.config.getNowScanFolder())) {
                this.config.setNowScanFolder(this.config.getScanFolder());
            }
            if (!this.config.getNowScanFolder().contains(this.config.getScanFolder())) {
                logger.warn("非需求文件夹扫描路径，重定向为需求扫描文件夹。");
                this.config.setNowScanFolder(this.config.getScanFolder());
            }
            this.config.setUtilStartScanTime(System.currentTimeMillis());
            this.config.setNowScanFolderModifyTime(new File(config.getNowScanFolder()).lastModified());
            if (!config.getNowScanFolder().equals(config.getScanFolder())) {
                this.config.setNowScanFolderIndex(this.findIndexOfFolder(this.config.getNowScanFolder()));
            }
        } catch (Exception e) {
            logger.error("配置文件不存在。" + e);
        }
    }

    /**
     * 返回给定路径下所有文件的集合
     * @param fullFilePath 给定路径（文件夹）
     * @return
     */
    public FilesAndFolders listThisFolderOfFilesAndFolders(String fullFilePath) {
        if (!new File(fullFilePath).isDirectory()) {
            logger.warn("这不是文件夹 ：" + fullFilePath);
            return null;
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!(config.getStatus() == 1)) {
                    try {
                        long duringTime =  System.currentTimeMillis() - config.getUtilStartScanTime();
                        logger.info("已耗时：" + duringTime + " 毫秒");
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        logger.error("显示扫描用时错误：" + e);
                    }
                }
            }
        }).start();
    }

    public void writeConfig2File() {
        try{
            FileUtils.writeStringToFile(new File(path + "/deleter.config"), JsonUtil.getInstance().convertObjectToJsonString(config));
        } catch (Exception e) {
            logger.error("写入配置文件失败。");
        }
    }

    public void scanFiles(List<String> fileList) {
        //start
        for (int i = 0; i < fileList.size(); i++) {
            String file = fileList.get(i);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileFullPath(file);
            fileInfo.setSize(new File(file).length());
            fileInfo.setLastModifyTime(new File(file).lastModified());
            //大于128mb文件不计算md5
            if (fileInfo.getSize() < BIG_FILE_SIZE) {
                try {
                    fileInfo.setMd5(MD5FileUtil.getFileMD5String(new File(file)));
                }catch (Exception e) {
                    logger.error("IO 错误：" + e);
                }
            }
            stringBuilderForFileInfo.append(fileInfo.toString() + "\r\n");

            //缓存的文件信息大于10kb就写文件
            if (stringBuilderForFileInfo.length() > _1KB_SIZE * 10) {
                try {
                    logger.debug("writing a 10 kb ..." + file);
                    FileUtils.writeStringToFile(new File(logInfoPath), stringBuilderForFileInfo.toString(), true);
                    this.writeConfig2File();
                    stringBuilderForFileInfo = new StringBuilder();
                    //信息日志文件大于10mb就另起一个日志文件
                    if (new File(logInfoPath).length() > 10 * _1MB_SIZE) {
                        FileUtils.moveFile(new File(logInfoPath),
                                new File(logInfoPath.substring(0, logInfoPath.lastIndexOf(".") + 1)
                                        + new Date().getTime()
                                        + logInfoPath.substring(logInfoPath.lastIndexOf("."), logInfoPath.length())));
                    }
                }catch (Exception e) {
                    logger.error("IO 错误：" + e);
                }
            }

            config.setNowScanFile(file);
            config.setNowScanFileModifyTime(new File(file).lastModified());
            config.setDuringTime(System.currentTimeMillis() - config.getUtilStartScanTime());

        }
        //end
    }

    public int findIndexOfFolder(String fullFilePath) {
        if (!new File(fullFilePath).isDirectory()) {
            logger.warn("这不是文件夹 ：" + fullFilePath);
            return -1;
        }
        if (fullFilePath.equals(config.getScanFolder())) {
            logger.warn("根路径无需寻找下标 ：" + fullFilePath);
            return -2;
        }
        String filePath = fullFilePath.substring(0, fullFilePath.lastIndexOf("\\"));
        FilesAndFolders filesAndFolders = this.listThisFolderOfFilesAndFolders(filePath);
        for (int i = 0; i < filesAndFolders.getFolders().size(); i++) {
            if (fullFilePath.equals(filesAndFolders.getFolders().get(i))) {
                return i;
            }
        }
        return 0;
    }

    public String getPreFolder(String fullFilePath) {
        return fullFilePath.substring(0, fullFilePath.lastIndexOf("\\"));
    }

    public void scanFolder() {
        //scan start
        config.setStatus(0);
        FilesAndFolders filesAndFolders = this.listThisFolderOfFilesAndFolders(config.getNowScanFolder());
        this.scanFiles(filesAndFolders.getFiles());

        while (true) {
            if (filesAndFolders.getFolders().size() == 0) {
                String preFolderPath = this.getPreFolder(config.getNowScanFolder());
                FilesAndFolders temp = this.listThisFolderOfFilesAndFolders(preFolderPath);
                if (temp.getFolders().size() - 1 == config.getNowScanFolderIndex()) {
                    while (temp.getFolders().size() - 1 == config.getNowScanFolderIndex()) {
                        config.setNowScanFolder(preFolderPath);
                        if (config.getNowScanFolder().equals(config.getScanFolder())) break;
                        int preFolderIndex = this.findIndexOfFolder(config.getNowScanFolder());
                        config.setNowScanFolderIndex(preFolderIndex);
                        preFolderPath = this.getPreFolder(config.getNowScanFolder());
                        temp = this.listThisFolderOfFilesAndFolders(preFolderPath);
                    }
                }
                if (temp.getFolders().size() > config.getNowScanFolderIndex() + 1) {
                    config.setNowScanFolderIndex(config.getNowScanFolderIndex() + 1);
                    config.setNowScanFolder(temp.getFolders().get(config.getNowScanFolderIndex()));
                }
                if (temp.getFolders().size() < config.getNowScanFolderIndex() + 1) {
                    logger.error("发生了什么错误。");
                }
            } else {
                config.setNowScanFolder(filesAndFolders.getFolders().get(0));
                config.setNowScanFolderIndex(0);
            }

            if (config.getNowScanFolder().equals(config.getScanFolder())) break;
            filesAndFolders = this.listThisFolderOfFilesAndFolders(config.getNowScanFolder());
            this.scanFiles(filesAndFolders.getFiles());
        }
        try {
            config.setStatus(1);
            FileUtils.writeStringToFile(new File(logInfoPath), stringBuilderForFileInfo.toString(), true);
            this.writeConfig2File();
        }catch (Exception e) {
            logger.error("IO 错误：" + e);
        }
        //scan end
    }

    public static void main(String[] args) {
        FileUtil fileUtil = new FileUtil();
        fileUtil.scanFolder();
        //sort start
        FilesAndFolders filesAndFolders = fileUtil.listThisFolderOfFilesAndFolders(path + "log");
        filesAndFolders.getFiles().forEach(file -> {
            try {
                List<FileInfo> fileInfos = new ArrayList<>();
                List<String> strings = FileUtils.readLines(new File(logInfoPath));
                strings.forEach(string -> {
                    try {
                        fileInfos.add(new ObjectMapper().readValue(string, FileInfo.class));
                    } catch (Exception e) {
                    }
                });
                fileInfos.sort(new Comparator<FileInfo>() {
                    @Override
                    public int compare(FileInfo o1, FileInfo o2) {
                        if (ObjectUtils.isEmpty(o1.getMd5()) || ObjectUtils.isEmpty(o2.getMd5())) {
                            return -1;
                        }
                        return o1.getMd5().compareTo(o2.getMd5());
                    }
                });
                int bigFileIndex = -1;
                List<FileInfo> bigFileInfos = new ArrayList<>();
                List<FileInfo> smallFileInfos = new ArrayList<>();
                for (int i = 0; i < fileInfos.size(); i++) {
                    if (ObjectUtils.isEmpty(fileInfos.get(i).getMd5())) {
                        bigFileInfos.add(fileInfos.get(i));
                    } else {
                        smallFileInfos.add(fileInfos.get(i));
                    }
                }
                bigFileInfos.sort(new Comparator<FileInfo>() {
                    @Override
                    public int compare(FileInfo o1, FileInfo o2) {
                        return (int)(o1.getSize() - o2.getSize());
                    }
                });

                FileUtils.writeStringToFile(new File(path + "sort/big/" + new File(logInfoPath).getName()),
                        JsonUtil.getInstance().convertObjectToJsonString(bigFileInfos));
                FileUtils.writeStringToFile(new File(path + "sort/small/" + new File(logInfoPath).getName()),
                        JsonUtil.getInstance().convertObjectToJsonString(smallFileInfos));

                //get same fileInfo
                StringBuilder stringBuilderForSameSmallFileInfo = new StringBuilder();
                for (int i = 0; i < smallFileInfos.size() - 1; i++) {
                    String temp = "";
                    if (smallFileInfos.get(i).getMd5().equals(smallFileInfos.get(i + 1).getMd5())) {
                        stringBuilderForSameSmallFileInfo.append(smallFileInfos.get(i).toString() + "\r\n");
                        temp = smallFileInfos.get(i + 1).toString() + "\r\n";
                    } else {
                        temp = "";
                    }
                    if (!ObjectUtils.isEmpty(temp)) {
                        stringBuilderForSameSmallFileInfo.append(temp);
                    }
                }
                if (smallFileInfos.get(smallFileInfos.size() - 1).getMd5().equals(smallFileInfos.get(smallFileInfos.size() - 2).getMd5())) {
                    stringBuilderForSameSmallFileInfo.append(smallFileInfos.get(smallFileInfos.size() - 1).toString() + "\r\n");
                }
                FileUtils.writeStringToFile(new File(path + "same/small/" + new File(logInfoPath).getName()),
                        stringBuilderForSameSmallFileInfo.toString());

                StringBuilder stringBuilderForSameBigFileInfo = new StringBuilder();
                for (int i = 0; i < bigFileInfos.size() - 1; i++) {
                    String temp = "";
                    if (bigFileInfos.get(i).getSize() == bigFileInfos.get(i + 1).getSize()) {
                        stringBuilderForSameBigFileInfo.append(bigFileInfos.get(i).toString() + "\r\n");
                        temp = bigFileInfos.get(i + 1).toString() + "\r\n";
                    } else {
                        temp = "";
                    }
                    if (!ObjectUtils.isEmpty(temp)) {
                        stringBuilderForSameBigFileInfo.append(temp);
                    }
                }
                if (bigFileInfos.get(bigFileInfos.size() - 1).getSize() == bigFileInfos.get(bigFileInfos.size() - 2).getSize()) {
                    stringBuilderForSameBigFileInfo.append(bigFileInfos.get(bigFileInfos.size() - 1).toString() + "\r\n");
                }
                FileUtils.writeStringToFile(new File(path + "same/big/" + new File(logInfoPath).getName()),
                        stringBuilderForSameBigFileInfo.toString());

            } catch (Exception e) {
            }
        });

        filesAndFolders = fileUtil.listThisFolderOfFilesAndFolders(path + "sort");
        boolean sortEnd = false;

        //sort end
    }
}
