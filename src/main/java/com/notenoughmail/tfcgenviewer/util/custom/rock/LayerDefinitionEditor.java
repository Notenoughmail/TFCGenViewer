package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.google.common.collect.ImmutableList;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LayerDefinitionEditor extends ContainerObjectSelectionList<LayerDefinitionEditor.Entry> {

    public static final Component
            LAYER_ID = Component.translatable("tfcgenviewer.rock_editor.layer_id").withStyle(ChatFormatting.DARK_GRAY),
            ROCK_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.layer_definition_rock").withStyle(ChatFormatting.DARK_GRAY),
            LAYER_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.layer_definition_layer").withStyle(ChatFormatting.DARK_GRAY),
            EDITOR_OCCUPIED = Component.translatable("tfcgenviewer.rock_editor.error.layer_definition_editor_has_values"),
            EMPTY_VALUES = Component.translatable("tfcgenviewer.rock_editor.error.cannot_map_empty_values"),
            EMPTY_ID = Component.translatable("tfcgenviewer.rock_editor.error.cannot_have_empty_layer_definition_id"),
            DELETE = Component.translatable("tfcgenviewer.rock_editor.delete_tooltip");

    private final Font font;
    private final Predicate<MutableRockLayerSettings.MutableLayerData> toDisplay;
    private final Consumer<Component> sendError;
    private final MutableRockLayerSettings mrls;

    public LayerDefinitionEditor(Minecraft pMinecraft, int pWidth, int pHeight, Font font, Predicate<MutableRockLayerSettings.MutableLayerData> toDisplay, Consumer<Component> sendError, MutableRockLayerSettings mrls) {
        super(pMinecraft, pWidth, pHeight, 24, pHeight - 24, 44);
        this.font = font;
        this.toDisplay = toDisplay;
        this.sendError = sendError;
        this.mrls = mrls;
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
        return x0 + super.getScrollbarPosition() - 8;
    }

    public boolean accept(MutableRockLayerSettings.MutableLayerData mld) {
        if (isOccupied()) {
            sendError.accept(EDITOR_OCCUPIED);
            return false;
        }
        clearEntries(); // Can be 'unoccupied' but have entries
        addEntry(new IdEntry(mld.id));
        mld.mapping.forEach((r, l) -> addEntry(new MapEntry(r, l)));
        return true;
    }

    public void add() {
        if (children().isEmpty()) {
            addEntry(new IdEntry(""));
        }
        addEntry(new MapEntry("", ""));
    }

    public void tick() {
        for (Entry entry : children()) {
            entry.tick();
        }
    }

    private void toDisplay() {
        final MutableRockLayerSettings.MutableLayerData mld = new MutableRockLayerSettings.MutableLayerData("");
        for (Entry entry : children()) {
            if (entry.toDisplay(mld)) return;
        }
        if (toDisplay.test(mld)) {
            clearEntries();
        }
    }

    private boolean isOccupied() {
        if (children().isEmpty()) {
            return false;
        }
        for (Entry entry : children()) {
            if (entry.isOccupied()) {
                return true;
            }
        }
        return false;
    }

    // TODO: When added with values, the text boxes of entries are 'scrolled' such that only the last two characters are visible | fix that
    static abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        abstract void tick();

        abstract boolean toDisplay(MutableRockLayerSettings.MutableLayerData mld);

        abstract boolean isOccupied();
    }

    class IdEntry extends Entry {

        private final EditBox id = new EditBox(font, 0, 0, 20, 16, LAYER_ID);
        private final ImageButton delete, confirm;

        IdEntry(String id) {
            this.id.setValue(id);
            this.id.setHint(LAYER_ID);
            delete = new ImageButton(0, 0, 20, 20, 0, 0, 20, RockSettingsDisplay.GUI_ELEMENTS, 64, 64, b -> clearEntries());
            delete.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.delete_tooltip.named", id)));
            this.id.setResponder(s -> delete.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.delete_tooltip.named", s))));
            confirm = new ImageButton(0, 0, 20, 20, 40, 0, 20, RockSettingsDisplay.GUI_ELEMENTS, 64, 64, b -> LayerDefinitionEditor.this.toDisplay());
            confirm.setTooltip(Tooltip.create(RockSettingsDisplay.CONFIRM));
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(delete, confirm, id);
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int y, int x, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            y += 10;
            delete.setX(x);
            delete.setY(y);
            delete.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            confirm.setX(x + 22);
            confirm.setY(y);
            confirm.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            id.setX(x + 44);
            id.setY(y + 2);
            id.setWidth(pWidth - 48);
            id.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(delete, confirm, id);
        }

        @Override
        void tick() {
            id.tick();
        }

        @Override
        boolean toDisplay(MutableRockLayerSettings.MutableLayerData mld) {
            if (id.getValue().isEmpty()) {
                sendError.accept(EMPTY_ID);
                return true;
            }
            mld.id = id.getValue();
            return false;
        }

        @Override
        boolean isOccupied() {
            return !id.getValue().isEmpty();
        }
    }

    class MapEntry extends Entry {

        private final EditBox rock, layer;
        private final ImageButton delete;

        MapEntry(String rock, String layer) {
            this.rock = new EditBox(font, 0, 0, 20, 16, ROCK_HINT);
            this.rock.setHint(ROCK_HINT);
            this.rock.setValue(rock);
            this.layer = new EditBox(font, 0, 0, 20, 16, LAYER_HINT);
            this.layer.setHint(LAYER_HINT);
            this.layer.setValue(layer);
            delete = new ImageButton(0, 0, 20, 20, 0, 0, 20, RockSettingsDisplay.GUI_ELEMENTS, 64, 64, b -> removeEntry(this));
            delete.setTooltip(Tooltip.create(DELETE));
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(rock, layer, delete);
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int y, int x, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            delete.setX(x);
            delete.setY(y);
            delete.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            rock.setX(x + 24);
            rock.setY(y + 2);
            rock.setWidth(pWidth - 28);
            rock.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            layer.setX(x + 2);
            layer.setY(y + 24);
            layer.setWidth(pWidth - 6);
            layer.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(rock, layer, delete);
        }

        @Override
        void tick() {
            rock.tick();
            layer.tick();
        }

        @Override
        boolean toDisplay(MutableRockLayerSettings.MutableLayerData mld) {
            final String
                    r = rock.getValue(),
                    l = layer.getValue();
            if (r.isEmpty() || l.isEmpty()) {
                sendError.accept(EMPTY_VALUES);
                return true;
            }
            if (mld.mapping.containsKey(r)) {
                sendError.accept(Component.translatable("tfcgenviewer.rock_editor.error.cannot_map_rock_to_multiple_layers", r));
                return true;
            }
            if (!mrls.rocks.containsKey(r)) {
                sendError.accept(Component.translatable("tfcgenviewer.rock_editor.error.unknown_rock_setting", r));
                return true;
            }
            if (!"bottom".equals(l) && !mrls.layerDefs.containsKey(l)) {
                sendError.accept(Component.translatable("tfcgenviewer.rock_editor.error.unknown_layer_def", l));
            }
            mld.mapping.put(r, l);
            return false;
        }

        @Override
        boolean isOccupied() {
            return !rock.getValue().isEmpty() && !layer.getValue().isEmpty();
        }
    }
}
