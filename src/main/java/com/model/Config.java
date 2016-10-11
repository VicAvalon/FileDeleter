package com.model;

import com.fasterxml.jackson.core.JsonParser;
import com.util.JsonUtil;
import jdk.nashorn.internal.parser.JSONParser;

public class Config {
    //原始需要扫描的文件夹
    private String scanFolder;
    private String nowScanFolder;
    private int nowScanFolderIndex = 0;
    private long nowScanFolderModifyTime;
    private String nowScanFile;
    private int nowScanFileIndex = 0;
    private long nowScanFileModifyTime;
    private long utilStartScanTime;
    private long duringTime;

    public String getScanFolder() {
        return scanFolder;
    }

    public void setScanFolder(String scanFolder) {
        this.scanFolder = scanFolder;
    }

    public String getNowScanFolder() {
        return nowScanFolder;
    }

    public void setNowScanFolder(String nowScanFolder) {
        this.nowScanFolder = nowScanFolder;
    }

    public int getNowScanFolderIndex() {
        return nowScanFolderIndex;
    }

    public void setNowScanFolderIndex(int nowScanFolderIndex) {
        this.nowScanFolderIndex = nowScanFolderIndex;
    }

    public long getNowScanFolderModifyTime() {
        return nowScanFolderModifyTime;
    }

    public void setNowScanFolderModifyTime(long nowScanFolderModifyTime) {
        this.nowScanFolderModifyTime = nowScanFolderModifyTime;
    }

    public String getNowScanFile() {
        return nowScanFile;
    }

    public void setNowScanFile(String nowScanFile) {
        this.nowScanFile = nowScanFile;
    }

    public int getNowScanFileIndex() {
        return nowScanFileIndex;
    }

    public void setNowScanFileIndex(int nowScanFileIndex) {
        this.nowScanFileIndex = nowScanFileIndex;
    }

    public long getNowScanFileModifyTime() {
        return nowScanFileModifyTime;
    }

    public void setNowScanFileModifyTime(long nowScanFileModifyTime) {
        this.nowScanFileModifyTime = nowScanFileModifyTime;
    }

    public long getUtilStartScanTime() {
        return utilStartScanTime;
    }

    public void setUtilStartScanTime(long utilStartScanTime) {
        this.utilStartScanTime = utilStartScanTime;
    }

    public long getDuringTime() {
        return duringTime;
    }

    public void setDuringTime(long duringTime) {
        this.duringTime = duringTime;
    }

    @Override
    public String toString() {
        return JsonUtil.getInstance().convertObjectToJsonString(this);
    }
}
