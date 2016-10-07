package com.tory.noname.bili.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * @Author: Tory
 * Create: 2016/9/17
 * Update: 2016/9/17
 */
public class CategoryMeta implements Parcelable{


    public static int TYPE_NOMAL = 0;
    public static int TYPE_LIVE = 1;
    public static int TYPE_GAME= 2;
    public static int TYPE_RANK= 3;
    public static int TYPE_BGM_LIST= 4;

    public int tid;
    public String typename;
    public String captionname;
    public int coverRes;
    public int type;

    public CategoryMeta parent;
    public List<CategoryMeta> child;

    public CategoryMeta(){}

    public CategoryMeta(int tid, String typename, int coverRes, int type) {
        this.tid = tid;
        this.typename = typename;
        this.coverRes = coverRes;
        this.type = type;
    }

    protected CategoryMeta(Parcel in) {
        tid = in.readInt();
        typename = in.readString();
        captionname = in.readString();
        coverRes = in.readInt();
        type = in.readInt();

    }

    public static final Creator<CategoryMeta> CREATOR = new Creator<CategoryMeta>() {
        @Override
        public CategoryMeta createFromParcel(Parcel in) {
            return new CategoryMeta(in);
        }

        @Override
        public CategoryMeta[] newArray(int size) {
            return new CategoryMeta[size];
        }
    };

    @Override
    public String toString() {
        return "CategoryMeta{" +
                "tid=" + tid +
                ", typename='" + typename + '\'' +
                ", captionname='" + captionname + '\'' +
                ", parent.tid=" + (parent != null ? parent.tid : -1)+
                ", child=" + child +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tid);
        dest.writeString(typename);
        dest.writeString(captionname);
        dest.writeInt(coverRes);
        dest.writeInt(type);
    }
}
