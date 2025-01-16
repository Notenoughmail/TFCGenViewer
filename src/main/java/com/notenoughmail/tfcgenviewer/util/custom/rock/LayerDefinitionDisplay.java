package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.google.common.collect.ImmutableList;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LayerDefinitionDisplay extends ContainerObjectSelectionList<LayerDefinitionDisplay.Entry> {

    public static final Component SELF_REFERENCE = Component.translatable("tfcgenviewer.rock_editor.error.self_referencing_definition");

    private final Font font;
    private final MutableRockLayerSettings mrls;
    private final Predicate<MutableRockLayerSettings.MutableLayerData> toEditor;
    private final Consumer<Component> sendError;
    private final BiConsumer<Component, Integer> sendTimedError;

    public LayerDefinitionDisplay(Minecraft pMinecraft, int pWidth, int pHeight, Font font, MutableRockLayerSettings mrls, Predicate<MutableRockLayerSettings.MutableLayerData> toEditor, BiConsumer<Component, Integer> sendError) {
        super(pMinecraft, pWidth, pHeight, 24, pHeight - 24, 55);
        this.font = font;
        this.mrls = mrls;
        refreshOrder();
        this.toEditor = toEditor;
        this.sendTimedError = sendError;
        this.sendError = c -> sendError.accept(c, 60);
        setRenderBackground(false);
        setRenderSelection(false);
        setRenderTopAndBottom(false);
    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics) {
        pGuiGraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
        pGuiGraphics.blit(Screen.BACKGROUND_LOCATION, x0 + 5, y0, x1 - 5, y1, x1 - x0 - 10, y1 - y0, 32, 32);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() - 12;
    }

    public boolean add(MutableRockLayerSettings.MutableLayerData mld) {
        if (mld.mapping.containsValue(mld.id)) {
            sendError.accept(SELF_REFERENCE);
            return false;
        }

        if (mrls.layerDefs.containsKey(mld.id)) {
            sendError.accept(Component.translatable("tfcgenviewer.rock_editor.error.layer_already_exists", mld.id));
            return false;
        }

        int requiresIndex = 0; // Must be after this
        final List<String> unknownLayers = new ArrayList<>();
        for (String layer : mld.mapping.values()) {
            if (!"bottom".equals(layer)) {
                final int index = mrls.layerDefs.indexOf(layer);
                if (index == -1) {
                    unknownLayers.add(layer);
                } else {
                    requiresIndex = Math.max(requiresIndex, index);
                }
            }
        }
        // Not technically critical, until it comes to actually validating the RockLayerSettings
        if (!unknownLayers.isEmpty()) sendError.accept(Component.translatable("tfcgenviewer.rock_editor.error.unknown_layer_definitions", String.join(", ", unknownLayers)));

        int requiredByIndex = mrls.layerDefs.size() - 1; // Must be before this
        for (int i = 0 ; i < mrls.layerDefs.size() ; i++) {
            final var val = mrls.layerDefs.getValue(i);
            if (val != null && val.mapping.containsValue(mld.id)) {
                requiredByIndex = Math.min(requiredByIndex, i);
                if (requiredByIndex == requiresIndex) {
                    sendTimedError.accept(Component.translatable("tfcgenviewer.rock_editor.error.circular_layer_definition_reference", mld.id, mrls.layerDefs.getKey(requiredByIndex)), 100);
                    break; // TODO: Should this, and below, just return false instead of breaking? Unless its circular or the topology is very weird, shuffling definitions around should suffice
                }
                if (requiredByIndex < requiresIndex) {
                    sendTimedError.accept(Component.translatable("tfcgenviewer.rock_editor.error.layer_definition_order_issue", mld.id, mrls.layerDefs.getKey(requiredByIndex)), 100);
                    break;
                }
            }
        }

        mrls.layerDefs.put(requiresIndex + 1, mld.id, mld);
        refreshOrder();
        return true;
    }

    private void refreshOrder() {
        clearEntries();
        mrls.layerDefs.forEach((id, mld) -> addEntry(new Entry(id, mld)));
    }

    // TODO: Either a send to top button or a button that reorders things based on requirement indexes
    class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final ImageButton delete, edit;
        private final Component name;
        private final @Nullable Component @NotNull [] valueDisplay;

        Entry(String id, MutableRockLayerSettings.MutableLayerData mld) {
            name = Component.literal(id);
            Map<String, String> values = mld.mapping;
            delete = new ImageButton(0, 0, 20, 20, 0, 0, 20, RockSettingsDisplay.GUI_ELEMENTS, 64, 64, b -> {
                mrls.layerDefs.remove(id);
                removeEntry(this);
            });
            delete.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.delete_tooltip.named", id)));
            edit = new ImageButton(0, 0, 20, 20, 20, 0, 20, RockSettingsDisplay.GUI_ELEMENTS, 64, 64, b -> {
                if (toEditor.test(mld)) {
                    mrls.layerDefs.remove(id);
                    removeEntry(this);
                }
            });
            edit.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.edit_tooltip", id)));
            valueDisplay = new Component[3];
            valueDisplay[0] = valueDisplay[1] = valueDisplay[2] = null;
            if (!values.isEmpty()) {
                int i = 0;
                for (Map.Entry<String, String> val : values.entrySet()) {
                    if (i > 2) break;
                    valueDisplay[i] = Component.translatable("tfcgenviewer.rock_editor.layer_definition_mapping", val.getKey(), val.getValue());
                    i++;
                }
                if (values.size() > 3) {
                    valueDisplay[2] = CommonComponents.ELLIPSIS;
                }
            }
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(delete, edit);
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int y, int x, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            delete.setX(x);
            delete.setY(y);
            delete.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            edit.setX(x + 24);
            edit.setY(y);
            edit.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            text(name, x + 48, y + 5, pGuiGraphics);
            y += 21;
            for (Component c : valueDisplay) {
                if (c == null) break;
                text(c, x + 10, y, pGuiGraphics);
                y += 11;
            }
        }

        private void text(Component text, int x, int y, GuiGraphics graphics) {
            graphics.drawString(font, text, x ,y, 0xFFFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(delete, edit);
        }
    }
}
