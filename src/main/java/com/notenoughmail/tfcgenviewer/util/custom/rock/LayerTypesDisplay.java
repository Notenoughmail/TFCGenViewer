package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.google.common.collect.ImmutableList;
import com.notenoughmail.tfcgenviewer.util.GuiElement;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import com.notenoughmail.tfcgenviewer.util.custom.SelectionList;
import net.minecraft.ChatFormatting;
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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LayerTypesDisplay extends SelectionList<LayerTypesDisplay.Entry> {

    public static Component IN_EDITOR = Component.translatable("tfcgenviewer.rock_editor.layer_in_editor").withStyle(ChatFormatting.GRAY);

    private final Font font;
    private final Consumer<LayerType> sendToEditor;
    private final Supplier<LayerType> currentlyEditing;

    public LayerTypesDisplay(Minecraft pMinecraft, int pWidth, int pHeight, MutableRockLayerSettings mrls, Font font, Consumer<LayerType> changeLayerEdit, Supplier<LayerType> currentlyEditing) {
        super(pMinecraft, pWidth, pHeight, 24, pHeight + 24, 55); // 20 high button + 3 lines of text of height 9 with 2 spacing
        this.font = font;
        sendToEditor = changeLayerEdit;
        this.currentlyEditing = currentlyEditing;
        setRenderBackground(false);
        setRenderSelection(false);
        setRenderTopAndBottom(false);
        addEntry(new Entry(mrls, LayerType.BOTTOM));
        addEntry(new Entry(mrls, LayerType.OCEAN));
        addEntry(new Entry(mrls, LayerType.VOLCANIC));
        addEntry(new Entry(mrls, LayerType.LAND));
        addEntry(new Entry(mrls, LayerType.UPLIFT));
        setScrollBarOffset(-8);
    }

    private void refresh() {
        for (Entry entry : children()) {
            entry.reload();
        }
    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics) {
        pGuiGraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
        pGuiGraphics.blit(Screen.BACKGROUND_LOCATION, x0 + 5, y0, x1 - 5, y1, x1 - x0 - 10, y1 - y0, 32, 32);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final ImageButton edit;
        private final List<String> values;
        private final @Nullable Component @NotNull [] valueDisplay;
        private final LayerType type;

        Entry(MutableRockLayerSettings mrls, LayerType type) {
            this.values = mrls.layers.get(type);
            this.type = type;
            edit = GuiElement.EDIT.button(b -> {
                sendToEditor.accept(this.type);
                refresh();
            });
            edit.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.edit_tooltip", type.title)));
            valueDisplay = new Component[3];
            reload();
        }

        // Used to update the display values
        void reload() {
            valueDisplay[0] = valueDisplay[1] = valueDisplay[2] = null;
            switch (values.size()) {
                case 0:
                    break;
                case 3:
                    valueDisplay[2] = Component.literal(values.get(2));
                case 2:
                    valueDisplay[1] = Component.literal(values.get(1));
                case 1:
                    valueDisplay[0] = Component.literal(values.get(0));
                    break;
                default:
                    valueDisplay[0] = Component.literal(values.get(0));
                    valueDisplay[1] = Component.literal(values.get(1));
                    valueDisplay[2] = CommonComponents.ELLIPSIS;
                    break;
            }
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(edit);
        }

        @Override
        public void render(GuiGraphics graphics, int pIndex, int y, int x, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            if (type == currentlyEditing.get()) {
                text(type.title, x + 12, y + 5, graphics);
                text(IN_EDITOR, x + (pWidth - font.width(IN_EDITOR)) / 2, y + 32, graphics);
            } else {
                edit.setX(x + 10);
                edit.setY(y);
                edit.render(graphics, pMouseX, pMouseY, pPartialTick);
                text(type.title, x + 34, y + 5, graphics);
                if (valueDisplay[0] == null) return;
                y += 21;
                text(valueDisplay[0], x + 12, y, graphics);
                if (valueDisplay[1] == null) return;
                y += 11;
                text(valueDisplay[1], x+ 12, y, graphics);
                if (valueDisplay[2] == null) return;
                y += 11;
                text(valueDisplay[2], x + 12, y, graphics);
            }
        }

        private void text(Component text, int x, int y, GuiGraphics graphics) {
            graphics.drawString(font, text, x, y, 0xFFFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(edit);
        }
    }
}
