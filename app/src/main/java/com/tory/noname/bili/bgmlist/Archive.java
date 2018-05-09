package com.tory.noname.bili.bgmlist;

/**
 * @Author: tory
 * Create: 2017/3/26
 * Update: ${UPDATE}
 */
public class Archive{

    public String path;
    public String version;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    @Override
    public String toString() {
        return "Archive{" +
                "path='" + path + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
