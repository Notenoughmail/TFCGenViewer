package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.google.common.collect.ImmutableList;
import com.notenoughmail.tfcgenviewer.util.GuiElement;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import com.notenoughmail.tfcgenviewer.util.WidgetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class RockSettingsDisplay extends ContainerObjectSelectionList<RockSettingsDisplay.SettingsHolder> {

    public static Component
            NO_SPIKE = Component.translatable("tfcgenviewer.rock_editor.no_spike"),
            NO_LOOSE = Component.translatable("tfcgenviewer.rock_editor.no_loose"),
            NO_MOSSY_LOOSE = Component.translatable("tfcgenviewer.rock_editor.no_mossy_loose"),
            CONFIRM  = Component.translatable("tfcgenviewer.rock_editor.confirm");

    private final Map<String, MutableRockLayerSettings.MutableRockSettings> rockSettings;
    private final Font font;
    private final BiPredicate<String, MutableRockLayerSettings.MutableRockSettings> toEditor;
    private final Consumer<Component> errorMessage;

    public RockSettingsDisplay(Minecraft minecraft, int width, int height, Map<String, MutableRockLayerSettings.MutableRockSettings> rockSettings, Font font, BiPredicate<String, MutableRockLayerSettings.MutableRockSettings> toEditor, Consumer<Component> errorMessage) {
        super(minecraft, width, height, 24, height + 24, 188);
        this.rockSettings = rockSettings;
        this.font = font;
        this.toEditor = toEditor;
        this.errorMessage = errorMessage;
        rockSettings.forEach((n, mrs) -> addEntry(new SettingsHolder(n, mrs)));
        setRenderBackground(false);
        setRenderSelection(false);
        setRenderTopAndBottom(false);
    }

    public boolean add(String name, MutableRockLayerSettings.MutableRockSettings mrs) {
        if (rockSettings.containsKey(name)) {
            errorMessage.accept(Component.translatable("tfcgenviewer.rock_editor.error.rock_already_exists", name));
            return false;
        }

        for (var entry : rockSettings.entrySet()) {
            if (entry.getValue().raw == mrs.raw) {
                errorMessage.accept(Component.translatable("tfcgenviewer.rock_editor.error.rock_setting_already_has_raw_block", entry.getKey(), mrs.raw.getName()));
                return false;
            }
        }

        rockSettings.put(name, mrs);
        addEntry(new SettingsHolder(name, mrs));
        return true;
    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics) {
        pGuiGraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
        pGuiGraphics.blit(Screen.BACKGROUND_LOCATION, x0 + 5, y0, x1 - 5, y1, x1 - x0 - 10, y1 - y0, 32, 32);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return isFocused() ? NarrationPriority.FOCUSED : NarrationPriority.NONE;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() - 12;
    }

    class SettingsHolder extends ContainerObjectSelectionList.Entry<SettingsHolder> {

        private final MutableRockLayerSettings.MutableRockSettings mrs;
        private final Button delete, edit;
        private final Component title;
        private final Block[] simpleRenders;

        public SettingsHolder(String rockName, MutableRockLayerSettings.MutableRockSettings mrs) {
            this.mrs = mrs;
            delete = GuiElement.REMOVE.button(b -> {
                rockSettings.remove(rockName);
                removeEntry(this);
                setScrollAmount(getScrollAmount());
            });
            delete.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.delete_tooltip.named", rockName)));
            edit = GuiElement.EDIT.button(b -> {
                if (toEditor.test(rockName, mrs)) {
                    rockSettings.remove(rockName);
                    removeEntry(this);
                    setScrollAmount(getScrollAmount());
                }
            });
            edit.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.edit_tooltip", rockName)));
            title = Component.literal(rockName);
            simpleRenders = new Block[] {
                    mrs.raw,
                    mrs.hardened,
                    mrs.gravel,
                    mrs.cobble,
                    mrs.sand,
                    mrs.sandstone
            };
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int w, int h, int mouseX, int mouseY, boolean pHovering, float pPartialTick) {
            delete.setX(x);
            delete.setY(y);
            delete.render(graphics, mouseX, mouseY, pPartialTick);
            edit.setX(x + 24);
            edit.setY(y);
            edit.render(graphics, mouseX, mouseY, pPartialTick);
            final int
                    textX = x + 24,
                    blockX = x + 2,
                    right = x + w - 2;
            text(title, textX + 24, y + 2, graphics);
            y += 21;
            for (Block block : simpleRenders) {
                WidgetUtils.renderBlock(block, blockX, y, graphics, font, right, null);
                y += 18;
            }
            WidgetUtils.renderBlock(mrs.spike, blockX, y, graphics, font, right, NO_SPIKE);
            y += 18;
            WidgetUtils.renderBlock(mrs.loose, blockX, y, graphics, font, right, NO_LOOSE);
            y += 18;
            WidgetUtils.renderBlock(mrs.mossyLoose, blockX, y, graphics, font, right, NO_MOSSY_LOOSE);
        }

        private void text(Component text, int x, int y, GuiGraphics graphics) {
            graphics.drawString(font, text, x, y + 3, 0xFFFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(delete, edit);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(delete, edit);
        }
    }
}
