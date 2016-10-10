package com.model;

import com.fasterxml.jackson.core.JsonParser;
import com.util.JsonUtil;
import jdk.nashorn.internal.parser.JSONParser;

public class Config {
    private String nowScanFolder;
    private long nowScanFolderModifyTime;
    private String nowScanFile;
    private long nowScanFileModifyTime;
    private long utilStartScanTime;

    public String getNowScanFolder() {
        return nowScanFolder;
    }

    public void setNowScanFolder(String nowScanFolder) {
        this.nowScanFolder = nowScanFolder;
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

    @Override
    public String toString() {
        return JsonUtil.getInstance().convertObjectToJsonString(this);
    }
}
