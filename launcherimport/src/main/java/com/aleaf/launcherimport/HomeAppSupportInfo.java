package com.aleaf.launcherimport;

/**
 * @author tory
 * @date 2018/10/22
 * @des:
 */
public class HomeAppSupportInfo {

    public String title;
    public String aliasTitle;
    public String packageName;
    public String authority;
    public String readPermission;


    @Override
    public String toString() {
        return "HomeAppSupportInfo{" +
                "title='" + title + '\'' +
                ", aliasTitle='" + aliasTitle + '\'' +
                ", packageName='" + packageName + '\'' +
                ", authority='" + authority + '\'' +
                ", readPermission='" + readPermission + '\'' +
                '}';
    }
}
