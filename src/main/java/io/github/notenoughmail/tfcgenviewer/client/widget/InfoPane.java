package io.github.notenoughmail.tfcgenviewer.client.widget;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.Supplier;

// TODO: 1.21.1 | The compass is wonky with very wide panes
// For all intents and purposes, a holder for a scrollable view of a wrapped text component
public class InfoPane extends AbstractScrollWidget {

    public static final Component NARRATION_TITLE = Component.translatable("tfcgenviewer.narration.info_pane.title");
    public static final Component ERROR = Component.translatable("tfcgenviewer.preview_info.error");

    public static final ResourceLocation COMPASS = TFCGenViewer.id("compass");

    private List<FormattedCharSequence> lines;
    private int maxLengthOfContent;
    private final Supplier<Font> font;
    private boolean fontAvailable, showCompass;

    public InfoPane(int x, int y, int width, int height, Supplier<Font> font) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.font = font;
        lines = List.of();
        maxLengthOfContent = height;
    }

    // Ugly hack, but eh
    public void fontAvailable(IVisualizerType<?, ?, ?, ?> viz) {
        if (!fontAvailable) {
            fontAvailable = true;
            setGenerating(viz);
        }
    }

    public void setGenerating(IVisualizerType<?, ?, ?, ?> viz) {
        if (fontAvailable) {
            setMessage(Component.translatable("tfcgenviewer.preview_info.generating", viz.name()));
        }
        showCompass = false;
    }

    public void setError() {
        setMessage(ERROR);
        showCompass = false;
    }

    @Override
    public void setMessage(Component message) {
        lines = font.get().split(message, getWidth() - 8 - 4);
        updateLengthOfContent();
        setScrollAmount(0);
        showCompass = true;
    }

    @Override
    public void setRectangle(int width, int height, int x, int y) {
        super.setRectangle(width, height, x, y);
        updateLengthOfContent();
    }

    private void updateLengthOfContent() {
        maxLengthOfContent = lines.size() * font.get().lineHeight + getWidth() + 13;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (!scrollbarVisible() && showCompass) {
            final int compassSize = getWidth() - 4;
            graphics.blitSprite(
                    COMPASS,
                    getX() + 2,
                    getY() + getHeight() - 2 - compassSize,
                    compassSize,
                    compassSize
            );
        }
    }

    @Override
    protected int getInnerHeight() {
        return maxLengthOfContent;
    }

    @Override
    protected double scrollRate() {
        return 9;
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int elementY = getY() + 4;
        for (FormattedCharSequence chars : lines) {
            graphics.drawString(font.get(), chars, getX() + 2, elementY, 0xFFFFFFFF, false);
            elementY += 9;
        }
        if (scrollbarVisible() && showCompass) {
            elementY += 9;
            final int compassSize = getWidth() - 4;
            graphics.blitSprite(
                    COMPASS,
                    getX() + 2,
                    elementY,
                    compassSize,
                    compassSize
            );
        }
    }

    @Override
    protected void renderBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x7F000000);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE, NARRATION_TITLE);
    }
}
