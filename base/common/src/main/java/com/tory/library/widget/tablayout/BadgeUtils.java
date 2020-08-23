package com.tory.library.widget.tablayout;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.badge.BadgeDrawable;

public class BadgeUtils {

    public static final boolean USE_COMPAT_PARENT = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2;

    private BadgeUtils() {
        // Private constructor to prevent unwanted construction.
    }

    /**
     * Updates a badge's bounds using its center coordinate, {@code halfWidth} and {@code halfHeight}.
     *
     * @param rect       Holds rectangular coordinates of the badge's bounds.
     * @param centerX    A badge's center x coordinate.
     * @param centerY    A badge's center y coordinate.
     * @param halfWidth  Half of a badge's width.
     * @param halfHeight Half of a badge's height.
     */
    public static void updateBadgeBounds(
            @NonNull Rect rect, float centerX, float centerY, float halfWidth, float halfHeight) {
        rect.set(
                (int) (centerX - halfWidth),
                (int) (centerY - halfHeight),
                (int) (centerX + halfWidth),
                (int) (centerY + halfHeight));
    }

    /*
     * Attaches a BadgeDrawable to its associated anchor and update the BadgeDrawable's coordinates
     * based on the anchor.
     * For API 18+, the BadgeDrawable will be added as a view overlay.
     * For pre-API 18, the BadgeDrawable will be set as the foreground of a FrameLayout that is an
     * ancestor of the anchor.
     */
    public static void attachBadgeDrawable(
            @NonNull BadgeDrawable badgeDrawable,
            @NonNull View anchor,
            @NonNull FrameLayout compatBadgeParent) {
        setBadgeDrawableBounds(badgeDrawable, anchor, compatBadgeParent);
        if (USE_COMPAT_PARENT) {
            compatBadgeParent.setForeground(badgeDrawable);
        } else {
            anchor.getOverlay().add(badgeDrawable);
        }
    }

    /*
     * Detaches a BadgeDrawable to its associated anchor.
     * For API 18+, the BadgeDrawable will be removed from its anchor's ViewOverlay.
     * For pre-API 18, the BadgeDrawable will be removed from the foreground of a FrameLayout that is
     * an ancestor of the anchor.
     */
    public static void detachBadgeDrawable(
            @Nullable BadgeDrawable badgeDrawable,
            @NonNull View anchor,
            @NonNull FrameLayout compatBadgeParent) {
        if (badgeDrawable == null) {
            return;
        }
        if (USE_COMPAT_PARENT) {
            compatBadgeParent.setForeground(null);
        } else {
            anchor.getOverlay().remove(badgeDrawable);
        }
    }

    /**
     * Sets the bounds of a BadgeDrawable to match the bounds of its anchor (for API 18+) or its
     * anchor's FrameLayout ancestor (pre-API 18).
     */
    public static void setBadgeDrawableBounds(
            @NonNull BadgeDrawable badgeDrawable,
            @NonNull View anchor,
            @NonNull FrameLayout compatBadgeParent) {
        Rect badgeBounds = new Rect();
        View badgeParent = USE_COMPAT_PARENT ? compatBadgeParent : anchor;
        badgeParent.getDrawingRect(badgeBounds);
        badgeDrawable.setBounds(badgeBounds);
        badgeDrawable.updateBadgeCoordinates(anchor, compatBadgeParent);
    }

}
