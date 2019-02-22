package com.aleaf.viplist;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;

/**
 * @author tory
 * @date 2018/12/25
 * @des:
 */
public class VipViewProfile {

    @MappingRes(R.styleable.VipViewProfile_vcp_level)
    public int level;
    @ColorInt
    @MappingRes(R.styleable.VipViewProfile_vcp_card_start_color)
    public int cardStartColor;
    @ColorInt
    @MappingRes(R.styleable.VipViewProfile_vcp_card_end_color)
    public int cardEndColor;
    @ColorInt
    @MappingRes(R.styleable.VipViewProfile_vcp_card_shadow_color)
    public int cardShadowColor;
    @DrawableRes
    @MappingRes(R.styleable.VipViewProfile_vcp_card_chara_drawable)
    public int cardCharaDrawableRes;

    public int headerMaskColor;
    public int iconColorTint;
    public int iconTextTint;


    public VipViewProfile(int level, int cardStartColor, int cardEndColor, int cardShadowColor, int cardCharaDrawableRes, int headerMaskColor, int iconColorTint, int iconTextTint) {
        this.level = level;
        this.cardStartColor = cardStartColor;
        this.cardEndColor = cardEndColor;
        this.cardShadowColor = cardShadowColor;
        this.cardCharaDrawableRes = cardCharaDrawableRes;
        this.headerMaskColor = headerMaskColor;
        this.iconColorTint = iconColorTint;
        this.iconTextTint = iconTextTint;
    }

    @Override
    public String toString() {
        return "VipViewProfile{" +
                "level=" + level +
                ", cardStartColor=#" + Integer.toHexString(cardStartColor) +
                ", cardEndColor=#" + Integer.toHexString(cardEndColor) +
                ", cardShadowColor=#" + Integer.toHexString(cardShadowColor) +
                ", cardCharaDrawableRes=0x" + Integer.toHexString(cardCharaDrawableRes) +
                ", headerMaskColor=#" + Integer.toHexString(headerMaskColor) +
                ", iconColorTint=#" + Integer.toHexString(iconColorTint) +
                ", iconTextTint=#" + Integer.toHexString(iconTextTint) +
                '}';
    }
}
