package com.notenoughmail.tfcgenviewer.util.custom.rock;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.function.Consumer;

public interface IAmATabWithNonWidgetChildren {

    <T extends GuiEventListener & Renderable> void visitNonWidgets(Consumer<T> visitor);
}
