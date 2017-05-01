package com.tory.noname.bili.bean;

/**
 * @Author: Tory
 * Create: 2016/9/25
 * Update: 2016/9/25
 */
public class VideoItem {

    /**
     * av号
     */
    public int aid;

    public String copyright;

    public int typeid;

    public String typename;

    public String title;

    public String subtitle;

    public String play;

    public int review;

    public int video_review;

    public int favorites;

    public int mid;

    public String author;

    public String description;

    public String create;

    public String pic;

    public int credit;

    public int coins;

    public String duration;

    public int comment;

    public boolean badgepay;

    @Override
    public String toString() {
        return "VideoItem{" +
                "aid=" + aid +
                ", copyright='" + copyright + '\'' +
                ", typeid=" + typeid +
                ", typename='" + typename + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", play='" + play + '\'' +
                ", review=" + review +
                ", video_review=" + video_review +
                ", favorites=" + favorites +
                ", mid=" + mid +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", create='" + create + '\'' +
                ", pic='" + pic + '\'' +
                ", credit=" + credit +
                ", coins=" + coins +
                ", duration='" + duration + '\'' +
                ", comment=" + comment +
                ", badgepay=" + badgepay +
                '}';
    }
}
