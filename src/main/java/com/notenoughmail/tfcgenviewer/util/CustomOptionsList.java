package com.notenoughmail.tfcgenviewer.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.List;

/**
 * A simple, minimalist reimplementation of {@link net.minecraft.client.gui.components.OptionsList} that is a single column instead of two
 * <p>
 * It just works™
 */
public class CustomOptionsList extends ContainerObjectSelectionList<CustomOptionsList.Entry> {

    public CustomOptionsList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
        super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
    }

    public void add(OptionInstance<?>... options) {
        for (OptionInstance<?> option : options) {
            addEntry(new Entry(option, width, minecraft.options));
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return width - 4;
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final List<AbstractWidget> widget;

        public Entry(OptionInstance<?> option, int width, Options options) {
            this.widget = List.of(option.createButton(options, 2, 0, width - 8));
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            final AbstractWidget instance = widget.get(0);
            instance.setY(pTop);
            instance.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return widget;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return widget;
        }
    }
}
