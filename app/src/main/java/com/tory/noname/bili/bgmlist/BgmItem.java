package com.tory.noname.bili.bgmlist;

import java.util.List;

/**
 * @Author: Tory
 * Create: 2016/10/1
 * Update: ${UPDATE}
 */
public class BgmItem {

    public String titleCN;

    public String titleJP;

    public String titleEN;

    public String officalSite;

    public int weekDayJP;

    public int weekDayCN;

    public String timeJP;

    public String timeCN;

    public List<String> onAirSite ;

    public boolean newBgm;

    public String showDate;

    public int bgmId;


    @Override
    public String toString() {
        return "BgmItem{" +
                "titleCN='" + titleCN + '\'' +
                ", titleJP='" + titleJP + '\'' +
                ", titleEN='" + titleEN + '\'' +
                ", officalSite='" + officalSite + '\'' +
                ", weekDayJP=" + weekDayJP +
                ", weekDayCN=" + weekDayCN +
                ", timeJP='" + timeJP + '\'' +
                ", timeCN='" + timeCN + '\'' +
                ", onAirSite=" + onAirSite +
                ", newBgm=" + newBgm +
                ", showDate='" + showDate + '\'' +
                ", bgmId=" + bgmId +
                '}';
    }
}
