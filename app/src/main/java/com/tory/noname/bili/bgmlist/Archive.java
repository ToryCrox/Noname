package com.tory.noname.bili.bgmlist;

import androidx.annotation.NonNull;

/**
 * @Author: tory
 * Create: 2017/3/26
 * Update: ${UPDATE}
 */
public class Archive implements Comparable{

    public String path;
    public long version;

    public int year;
    public int quarter;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Archive archive = (Archive) o;

        if (version != archive.version) return false;
        if (year != archive.year) return false;
        if (quarter != archive.quarter) return false;
        return path != null ? path.equals(archive.path) : archive.path == null;

    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (int) (version ^ (version >>> 32));
        result = 31 * result + year;
        result = 31 * result + quarter;
        return result;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        Archive archive = (Archive) another;

        return year * 100 + quarter - (archive.year * 100 + archive.quarter);
    }

    @Override
    public String toString() {
        return "Archive{" +
                "path='" + path + '\'' +
                ", version=" + version +
                '}';
    }
}
