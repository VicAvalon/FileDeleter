package com.model;

import java.io.File;
import java.util.List;

public class FilesAndFolders {

    //都是绝对路径
    private List<String> files;

    private List<String> folders;

    public FilesAndFolders(List<String> folders, List<String> files) {
        this.files = files;
        this.folders = folders;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getFolders() {
        return folders;
    }

    public void setFolders(List<String> folders) {
        this.folders = folders;
    }
}
