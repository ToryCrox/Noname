package com.tory.library.widget.tablayout;

import androidx.annotation.NonNull;

/**
     * A callback interface that must be implemented to set the text and styling of newly created
     * tabs.
     */
    public interface TabConfigurationStrategy {
        /**
         * Called to configure the tab for the page at the specified position. Typically calls {@link
         * MTabLayout.Tab#setText(CharSequence)}, but any form of styling can be applied.
         *
         * @param tab      The Tab which should be configured to represent the title of the item at the given
         *                 position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        void onConfigureTab(@NonNull MTabLayout.Tab tab, int position);
    }