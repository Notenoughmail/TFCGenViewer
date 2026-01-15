package com.notenoughmail.tfcgenviewer.util.custom.rock;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.Objects;
import java.util.function.Consumer;

public class SlightlyImprovedTabManager<T extends GuiEventListener & Renderable & NarratableEntry> extends TabManager {

    private final Consumer<T> add, remove;

    public SlightlyImprovedTabManager(Consumer<T> add, Consumer<T> remove, Consumer<AbstractWidget> superAdd, Consumer<AbstractWidget> superRemove) {
        super(superAdd, superRemove);
        this.add = add;
        this.remove = remove;
    }

    @Override
    public void setCurrentTab(Tab pTab, boolean pPlayClickSound) {
        if (!Objects.equals(pTab, currentTab)) {
            if (currentTab instanceof IAmATabWithNonWidgetChildren tabExtension) {
                tabExtension.visitNonWidgets(remove);
            }
            if (pTab instanceof IAmATabWithNonWidgetChildren tabExtension) {
                tabExtension.visitNonWidgets(add);
            }
        }
        super.setCurrentTab(pTab, pPlayClickSound);
    }
}
