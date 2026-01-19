package io.github.notenoughmail.tfcgenviewer.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public class SelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {

    private int scrollBarOffset;

    public SelectionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        scrollBarOffset = 4;
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {}

    @Override
    public int getRowWidth() {
        return width;
    }

    public void setScrollBarOffset(int offset) {
        scrollBarOffset = offset;
    }

    @Override
    protected int getScrollbarPosition() {
        return getX() + width + scrollBarOffset;
    }

    public int getScrollBarScrunchFactor() {
        if (getMaxScroll() <= 0 || scrollBarOffset >= 0) {
            return 0;
        } else {
            return -scrollBarOffset + 4;
        }
    }
}
