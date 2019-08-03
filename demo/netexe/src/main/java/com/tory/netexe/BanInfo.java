package com.tory.netexe;

import com.google.gson.annotations.SerializedName;

public class BanInfo {
    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("cover")
    private String cover;
    @SerializedName("description")
    private String description;
    @SerializedName("source")
    private String source;
    @SerializedName("url")
    private String url;
    @SerializedName("weekday")
    private int weekday;
    @SerializedName("playtime")
    private String playtime;
    @SerializedName("isSubscribable")
    private boolean isSubscribable;
    @SerializedName("episode")
    private String episode;
    @SerializedName("status")
    private int status;

    //是否追番
    private boolean isFollowed;
    private boolean isAlarmFollowed;

    public BanInfo(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPlaytime() {
        return playtime;
    }

    public void setPlaytime(String playtime) {
        this.playtime = playtime;
    }

    public boolean isSubscribable() {
        return isSubscribable;
    }

    public void setSubscribable(boolean subscribable) {
        isSubscribable = subscribable;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    @Override
    public String toString() {
        return "Ban{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", cover='" + cover + '\'' +
                ", description='" + description + '\'' +
                ", source='" + source + '\'' +
                ", url='" + url + '\'' +
                ", weekday=" + weekday +
                ", playtime='" + playtime + '\'' +
                ", isSubscribable=" + isSubscribable +
                ", episode='" + episode + '\'' +
                ", status=" + status +
                '}';
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAlarmFollowed() {
        return isAlarmFollowed;
    }

    public void setAlarmFollowed(boolean alarmFollowed) {
        isAlarmFollowed = alarmFollowed;
    }
}