package io.github.notenoughmail.tfcgenviewer.client.widget;

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
 * A simple, minimalist reimplementation of {@link net.minecraft.client.gui.components.OptionsList OptionsList} that is a single column instead of two
 * <p>
 * It just works™
 */
public class SingleColumnOptionsList extends SelectionList<SingleColumnOptionsList.Entry> {

    public SingleColumnOptionsList(Minecraft pMinecraft, int pWidth, int pHeight, int y, int pItemHeight) {
        super(pMinecraft, pWidth, pHeight, y, pItemHeight);
        setScrollBarOffset(-4);
    }

    public void add(OptionInstance<?>... options) {
        for (OptionInstance<?> option : options) {
            add(option);
        }
    }

    public void add(OptionInstance<?> option) {
        addEntry(new Entry(option, width, minecraft.options));
    }

    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final List<AbstractWidget> widget;

        public Entry(OptionInstance<?> option, int width, Options options) {
            final AbstractWidget instance = option.createButton(options, 2, 0, width - 8);
            if (instance.getHeight() > (itemHeight - 2)) {
                // Limit widget height to be within the bounds of the entry
                instance.setHeight(itemHeight - 2);
            }
            this.widget = List.of(instance);
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            final AbstractWidget instance = widget.getFirst();
            instance.setX(left + 2);
            instance.setY(top);
            instance.setWidth(width - 4 - getScrollBarScrunchFactor());
            instance.render(pGuiGraphics, mouseX, mouseY, partialTick);
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
