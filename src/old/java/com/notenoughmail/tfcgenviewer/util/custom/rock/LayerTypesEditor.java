package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.google.common.collect.ImmutableList;
import com.notenoughmail.tfcgenviewer.util.GuiElement;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import io.github.notenoughmail.tfcgenviewer.client.widget.SelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LayerTypesEditor extends SelectionList<LayerTypesEditor.Entry> {

    private final MutableRockLayerSettings mrls;
    private final Font font;
    private final Supplier<LayerType> currentlyEditing;
    private final Consumer<Component> sendErrors;

    public LayerTypesEditor(Minecraft pMinecraft, int pWidth, int pHeight, Supplier<LayerType> currentlyEditing, MutableRockLayerSettings mrls, Font font, Consumer<Component> sendErrors) {
        super(pMinecraft, pWidth, pHeight, 24, pHeight + 24, 20);
        this.mrls = mrls;
        this.font = font;
        this.currentlyEditing = currentlyEditing;
        this.sendErrors = sendErrors;
        setRenderBackground(false);
        setRenderSelection(false);
        setRenderTopAndBottom(false);
        setScrollBarOffset(-8);
    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics) {
        pGuiGraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
        pGuiGraphics.blit(Screen.BACKGROUND_LOCATION, x0 + 5, y0, x1 - 5, y1, x1 - x0 - 10, y1 - y0, 32, 32);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public boolean add(String ref) {
        switch (currentlyEditing.get()) {
            case NONE -> {}
            case BOTTOM -> {
                if (!mrls.rocks.containsKey(ref)) {
                    sendErrors.accept(Component.translatable("tfcgenviewer.rock_editor.error.unknown_rock_setting", ref));
                }
            }
            default -> {
                if (!mrls.layerDefs.containsKey(ref)) {
                    sendErrors.accept(Component.translatable("tfcgenviewer.rock_editor.error.unknown_layer_def", ref));
                }
            }
        }

        @Nullable
        final List<String> refs = mrls.layers.get(currentlyEditing.get());
        if (refs != null) {
            if (refs.contains(ref)) {
                sendErrors.accept(Component.translatable("tfcgenviewer.rock_editor.error.layer_already_has", currentlyEditing.get().title, ref));
                return false;
            }
            refs.add(ref);
            reload();
        }
        return true;
    }

    public void reload() {
        clearEntries();
        @Nullable
        final List<String> refs = mrls.layers.get(currentlyEditing.get());
        if (refs != null) {
            refs.forEach(r -> addEntry(new Entry(r)));
        }
    }

    void remove(String ref) {
        @Nullable
        final List<String> refs =mrls.layers.get(currentlyEditing.get());
        if (refs != null) {
            refs.remove(ref);
        }
    }

    class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final Component display;
        private final ImageButton delete;

        Entry(String ref) {
            display = Component.literal(ref);
            delete = GuiElement.REMOVE.button(b -> {
                removeEntry(this);
                remove(ref);
            });
            delete.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.delete_tooltip.named", ref)));
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(delete);
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int y, int x, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            delete.setX(x + 2);
            delete.setY(y);
            delete.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            pGuiGraphics.drawString(font, display, x + 24, y + 5, 0xFFFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(delete);
        }
    }
}
