package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.google.common.collect.ImmutableList;
import com.notenoughmail.tfcgenviewer.util.*;
import com.notenoughmail.tfcgenviewer.util.custom.SelectionList;
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

public class LayerDefinitionDisplay extends SelectionList<LayerDefinitionDisplay.Entry> {

    public static final Component
            SELF_REFERENCE = Component.translatable("tfcgenviewer.rock_editor.error.self_referencing_definition"),
            COMPLEX_DEF_ORDER = Component.translatable("tfcgenviewer.rock_editor.error.layer_definition_mapping_too_complex");

    public static void sortLayerDefs(OrderedMap<String, MutableRockLayerSettings.MutableLayerData> map, Consumer<Component> onError) {
        sortLayerDefs(map, onError, 0);
    }

    private static void sortLayerDefs(OrderedMap<String, MutableRockLayerSettings.MutableLayerData> map, Consumer<Component> onError, int depth) {
        boolean goAgain = false;
        final MutableRockLayerSettings.MutableLayerData[] datas = map.values().toArray(MutableRockLayerSettings.MutableLayerData[]::new);
        final OrderedMap<String, MutableRockLayerSettings.MutableLayerData> workingSpace = new OrderedMapImpl<>();
        for (int i = 0 ; i < datas.length ; i++) {
            final MutableRockLayerSettings.MutableLayerData data = datas[i];
            if (i == 0) {
                workingSpace.put(data.id, data);
            } else {
                int maxI = 0, minI = workingSpace.size();
                final MutableRockLayerSettings.MutableLayerData[] transientDatas = workingSpace.values().toArray(MutableRockLayerSettings.MutableLayerData[]::new);
                for (final MutableRockLayerSettings.MutableLayerData mD : transientDatas) {
                    final boolean m2d = mD.mapsTo(data.id), d2m = data.mapsTo(mD.id);
                    final int mI = workingSpace.indexOf(mD.id);
                    if (m2d && d2m) {
                        onError.accept(Component.translatable("tfcgenviewer.rock_editor.error.circular_layer_definition_reference", data.id, mD.id));
                        return;
                    } else if (d2m) {
                        maxI = Math.max(maxI, mI + 1);
                    } else if (m2d) {
                        minI = Math.min(minI, mI);
                    }
                }
                if (maxI > minI) {
                    goAgain = true;
                }
                workingSpace.put(Math.max(minI, maxI), data.id, data);
            }
        }
        map.clear();
        map.putAll(workingSpace);
        if (goAgain) {
            if (depth > 5) {
                onError.accept(COMPLEX_DEF_ORDER);
                return;
            }
            sortLayerDefs(map, onError, depth + 1);
        }
    }

    private final Font font;
    private final MutableRockLayerSettings mrls;
    private final Predicate<MutableRockLayerSettings.MutableLayerData> toEditor;
    private final Consumer<Component> sendError;
    private final BiConsumer<Component, Integer> sendTimedError;

    public LayerDefinitionDisplay(Minecraft pMinecraft, int pWidth, int pHeight, Font font, MutableRockLayerSettings mrls, Predicate<MutableRockLayerSettings.MutableLayerData> toEditor, BiConsumer<Component, Integer> sendError) {
        super(pMinecraft, pWidth, pHeight, 24, pHeight - 24, 55);
        this.font = font;
        this.mrls = mrls;
        refreshEntries();
        this.toEditor = toEditor;
        this.sendTimedError = sendError;
        this.sendError = c -> sendError.accept(c, 60);
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

    public boolean add(MutableRockLayerSettings.MutableLayerData mld) {
        if (mld.mapping.containsValue(mld.id)) {
            sendError.accept(SELF_REFERENCE);
            return false;
        }

        if (mrls.layerDefs.containsKey(mld.id)) {
            sendError.accept(Component.translatable("tfcgenviewer.rock_editor.error.layer_already_exists", mld.id));
            return false;
        }

        final List<String> unknownLayers = new ArrayList<>();
        for (String layer : mld.mapping.values()) {
            if (!"bottom".equals(layer) && mrls.layerDefs.indexOf(layer) == -1) {
                unknownLayers.add(layer);
            }
        }
        // Not technically critical, until it comes to actually validating the RockLayerSettings
        if (!unknownLayers.isEmpty()) sendError.accept(Component.translatable("tfcgenviewer.rock_editor.error.unknown_layer_definitions", String.join(", ", unknownLayers)));

        mrls.layerDefs.put(mld.id, mld);

        sortLayerDefs(mrls.layerDefs, c -> sendTimedError.accept(c, 100));

        refreshEntries();
        return true;
    }

    public void refreshEntries() {
        clearEntries();
        mrls.layerDefs.forEach((id, mld) -> addEntry(new Entry(id, mld)));
    }

    class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final ImageButton delete, edit;
        private final Component name;
        private final @Nullable Component @NotNull [] valueDisplay;

        Entry(String id, MutableRockLayerSettings.MutableLayerData mld) {
            name = Component.literal(id);
            delete = GuiElement.REMOVE.button(b -> {
                mrls.layerDefs.remove(id);
                removeEntry(this);
                WidgetUtils.resetScroll(LayerDefinitionDisplay.this);
            });
            delete.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.delete_tooltip.named", id)));
            edit = GuiElement.EDIT.button(b -> {
                if (toEditor.test(mld)) {
                    mrls.layerDefs.remove(id);
                    removeEntry(this);
                    WidgetUtils.resetScroll(LayerDefinitionDisplay.this);
                }
            });
            edit.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.edit_tooltip", id)));
            valueDisplay = new Component[3];
            valueDisplay[0] = valueDisplay[1] = valueDisplay[2] = null;
            final List<Map.Entry<String, String>> values = List.copyOf(mld.mapping.entrySet());
            switch (values.size()) {
                case 0:
                    break;
                case 3:
                    valueDisplay[2] = mapping(values.get(2));
                case 2:
                    valueDisplay[1] = mapping(values.get(1));
                case 1:
                    valueDisplay[0] = mapping(values.get(0));
                    break;
                default:
                    valueDisplay[0] = mapping(values.get(0));
                    valueDisplay[1] = mapping(values.get(1));
                    valueDisplay[2] = CommonComponents.ELLIPSIS;
                    break;
            }
        }

        private static Component mapping(Map.Entry<String, String> entry) {
            return Component.translatable("tfcgenviewer.rock_editor.layer_definition_mapping", entry.getKey(), entry.getValue());
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(delete, edit);
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int y, int x, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            delete.setX(x + 10);
            delete.setY(y);
            delete.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            edit.setX(x + 34);
            edit.setY(y);
            edit.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            text(name, x + 58, y + 5, pGuiGraphics);
            if (valueDisplay[0] == null) return;
            y += 21;
            text(valueDisplay[0], x + 12, y, pGuiGraphics);
            if (valueDisplay[1] == null) return;
            y += 11;
            text(valueDisplay[1], x + 12, y, pGuiGraphics);
            if (valueDisplay[2] == null) return;
            y += 11;
            text(valueDisplay[2], x + 12, y, pGuiGraphics);
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
