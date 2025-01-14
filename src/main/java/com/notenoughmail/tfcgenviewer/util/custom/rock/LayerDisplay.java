package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.google.common.collect.ImmutableList;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
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

public class LayerDisplay extends ContainerObjectSelectionList<LayerDisplay.Entry> {

    public static Component IN_EDITOR = Component.translatable("tfcgenviewer.rock_editor.layer_in_editor").withStyle(ChatFormatting.DARK_GRAY);

    private final Font font;
    private final Consumer<LayerType> sendToEditor;
    private final Supplier<LayerType> currentlyEditing;

    public LayerDisplay(Minecraft pMinecraft, int pWidth, int pHeight, MutableRockLayerSettings mrls, Font font, Consumer<LayerType> changeLayerEdit, Supplier<LayerType> currentlyEditing) {
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

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() - 12;
    }

    class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final ImageButton edit;
        private final List<String> values;
        private final @Nullable Component @NotNull [] valueDisplay;
        private final LayerType type;

        Entry(MutableRockLayerSettings mrls, LayerType type) {
            this.values = mrls.layers.get(type);
            this.type = type;
            edit = new ImageButton(0, 0, 20, 20, 20 ,0, 20, RockSettingsDisplay.GUI_ELEMENTS, 64, 64, b -> {
                sendToEditor.accept(this.type);
                refresh();
            });
            valueDisplay = new Component[3];
            reload();
        }

        // Used to update the display values
        void reload() {
            if (!values.isEmpty()) {
                valueDisplay[0] = Component.literal(values.get(0));
                if (values.size() > 1) {
                    valueDisplay[1] = Component.literal(values.get(1));
                }
                if (values.size() == 3) {
                    valueDisplay[2] = Component.literal(values.get(2));
                } else if (values.size() > 3) {
                    valueDisplay[2] = CommonComponents.ELLIPSIS;
                }
            }
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(edit);
        }

        @Override
        public void render(GuiGraphics graphics, int pIndex, int y, int x, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            if (type == currentlyEditing.get()) {
                text(type.title, x + 2, y + 5, graphics);
                text(IN_EDITOR, x + (pWidth - font.width(IN_EDITOR)) / 2, y + 32, graphics);
            } else {
                edit.setX(x);
                edit.setY(y);
                edit.render(graphics, pMouseX, pMouseY, pPartialTick);
                text(type.title, x + 24, y + 5, graphics);
                y += 21;
                for (Component c : valueDisplay) {
                    if (c == null) break;
                    text(c, x + 10, y, graphics);
                    y += 11;
                }
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
