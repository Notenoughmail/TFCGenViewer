package com.notenoughmail.tfcgenviewer.util.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public class SelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {

    private int scrollBarOffset;

    public SelectionList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
        scrollBarOffset = 4;
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    protected void setScrollBarOffset(int offset) {
        scrollBarOffset = offset;
    }

    @Override
    protected int getScrollbarPosition() {
        return x0 + width + scrollBarOffset;
    }

    public int getScrollBarScrunchFactor() {
        if (getMaxScroll() <= 0 || scrollBarOffset >= 0) {
            return 0;
        } else {
            return -scrollBarOffset + 4;
        }
    }
}
