package io.github.notenoughmail.tfcgenviewer.client.widget;

import io.github.notenoughmail.tfcgenviewer.impl.util.RefreshableResettableOptionWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * A simple, minimalist reimplementation of {@link net.minecraft.client.gui.components.OptionsList OptionsList} that is a single column instead of two
 * <p>
 * It just works™
 */
public class SingleColumnOptionsList extends SelectionList<SingleColumnOptionsList.Entry> {

    private final List<RefreshableResettableOptionWidget> refreshableResettableWidgets;

    public SingleColumnOptionsList(Minecraft pMinecraft, int pWidth, int pHeight, int y, int pItemHeight) {
        super(pMinecraft, pWidth, pHeight, y, pItemHeight);
        setScrollBarOffset(-4);
        refreshableResettableWidgets = new ArrayList<>();
    }

    public void clear() {
        children().clear();
        refreshableResettableWidgets.clear();
    }

    public void refreshFromInstances() {
        refreshableResettableWidgets.forEach(RefreshableResettableOptionWidget::refreshFromInstance);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            final Entry entry = getEntryAtPosition(mouseX, mouseY);
            if (entry != null) {
                return entry.resetValueIfPossible();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void add(OptionInstance<?>... options) {
        for (OptionInstance<?> option : options) {
            add(option);
        }
    }

    public void add(OptionInstance<?> option) {
        add(option, false, () -> true);
    }

    public void addDynamic(OptionInstance<?> option, BooleanSupplier active) {
        add(option, true, active);
    }

    private void add(OptionInstance<?> option, boolean withBackground, BooleanSupplier active) {
        addEntry(new Entry(option, width, minecraft.options, withBackground, active));
    }

    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final List<AbstractWidget> widget;
        private final boolean withBackground;
        private final BooleanSupplier active;

        public Entry(OptionInstance<?> option, int width, Options options, boolean withBackground, BooleanSupplier active) {
            final AbstractWidget instance = option.createButton(options, 2, 0, width - 8);
            if (instance.getHeight() > (itemHeight - 2)) {
                // Limit widget height to be within the bounds of the entry
                instance.setHeight(itemHeight - 2);
            }
            this.widget = List.of(instance);
            this.withBackground = withBackground;
            this.active = active;
            if (instance instanceof RefreshableResettableOptionWidget rr) {
                refreshableResettableWidgets.add(rr);
            }
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            if (withBackground)
            {
                graphics.fill(left, top - 2, left + width - getScrollBarScrunchFactor(), top + height + 1, 0xFF007F7F);
            }
            final AbstractWidget instance = widget.getFirst();
            instance.active = active.getAsBoolean();
            instance.setX(left + 2);
            instance.setY(top);
            instance.setWidth(width - 4 - getScrollBarScrunchFactor());
            instance.render(graphics, mouseX, mouseY, partialTick);
        }

        public boolean resetValueIfPossible() {
            if (widget.getFirst() instanceof RefreshableResettableOptionWidget refreshable) {
                refreshable.resetToDefaultValue();
                playDownSound(minecraft.getSoundManager());
                return true;
            }
            return false;
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
