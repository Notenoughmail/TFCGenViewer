package com.notenoughmail.tfcgenviewer.util.custom;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.RockLayerSettingsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class RockSettingsEditor extends ObjectSelectionList<RockSettingsEditor.SettingsHolder> {

    public static final ResourceLocation BUTTON_DELETE = TFCGenViewer.identifier("textures/gui/delete.png");

    public static Component
            NO_SPIKE = Component.translatable("tfcgenviewer.rock_editor.no_spike"),
            NO_LOOSE = Component.translatable("tfcgenviewer.rock_editor.no_loose"),
            NO_MOSSY_LOOSE = Component.translatable("tfcgenviewer.rock_editor.no_mossy_loose");

    private final Map<String, RockLayerSettingsBuilder.MutableRockSettings> rockSettings;
    private final Map<String, SettingsHolder> rendering;

    public RockSettingsEditor(Minecraft minecraft, int width, int height, Map<String, RockLayerSettingsBuilder.MutableRockSettings> rockSettings, Font font) {
        super(minecraft, width, height, 24, height - 48, 188);
        this.rockSettings = rockSettings;
        rendering = new HashMap<>();
        rockSettings.forEach((n, mrs) -> addEntry(new SettingsHolder(n, mrs, font)));

    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return isFocused() ? NarrationPriority.FOCUSED : NarrationPriority.NONE;
    }

    @Override
    public int getRowWidth() {
        return width / 2;
    }

    class SettingsHolder extends ObjectSelectionList.Entry<SettingsHolder> {

        private final String rockName;
        private final RockLayerSettingsBuilder.MutableRockSettings mrs;
        private final Button delete;
        private final Font font;
        private final Component title;

        public SettingsHolder(String rockName, RockLayerSettingsBuilder.MutableRockSettings mrs, Font font) {
            this.rockName = rockName;
            this.mrs = mrs;
            delete = new ImageButton(0, 0, 20, 20, 0, 0, 20, BUTTON_DELETE, 32, 64, b -> {
                rockSettings.remove(rockName);
                removeEntry(this);
            });
            this.font = font;
            title = Component.literal(rockName);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int w, int h, int mouseX, int mouseY, boolean pHovering, float pPartialTick) {
            delete.setX(x);
            delete.setY(y);
            delete.render(graphics, mouseX, mouseY, pPartialTick);
            final int
                    textX = x + 24,
                    blockX = x + 2;
            text(title, textX, y + 2, graphics);
            y += 21;
            for (Block block : new Block[] {
                    mrs.raw,
                    mrs.hardened,
                    mrs.gravel,
                    mrs.cobble,
                    mrs.sandstone,
                    mrs.sandstone
            }) {
                renderBlock(block, blockX, y, graphics);
                y += 18;
            }
            if (mrs.spike == null) {
                text(NO_SPIKE, textX, y, graphics);
            } else {
                renderBlock(mrs.spike, blockX, y, graphics);
            }
            y += 18;
            if (mrs.loose == null) {
                text(NO_LOOSE, textX, y, graphics);
            } else {
                renderBlock(mrs.loose, blockX, y, graphics);
            }
            y += 18;
            if (mrs.mossyLoose == null) {
                text(NO_MOSSY_LOOSE, textX, y, graphics);
            } else {
                renderBlock(mrs.mossyLoose, blockX, y, graphics);
            }
        }

        private void text(Component text, int x, int y, GuiGraphics graphics) {
            graphics.drawString(font, text, x, y + 3, 0xFFFFFFFF);
        }

        private void renderBlock(Block block, int x, int y, GuiGraphics graphics) {
            graphics.renderFakeItem(block.asItem().getDefaultInstance(), x, y);
            text(block.getName(), x + 24, y + 3, graphics);
        }

        @Override
        public Component getNarration() {
            return title;
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            return delete.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }
}
