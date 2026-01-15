package io.github.notenoughmail.tfcgenviewer.client.widget;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.impl.ColorDescriptors;
import io.github.notenoughmail.tfcgenviewer.impl.preview.Image;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public class PreviewPane extends AbstractWidget {

    public static final Component NARRATION_TITLE = Component.translatable("tfcgenviewer.widget.preview_pane.narration.title");

    public static final ResourceLocation PROGRESS_BAR = TFCGenViewer.id("textures/gui/progress_bar.png");
    public static final ResourceLocation PROCESSING = TFCGenViewer.id("textures/gui/throbber.png");
    public static final ResourceLocation ERROR = TFCGenViewer.id("textures/gui/gen_error.png");
    public static final ResourceLocation DISPLAY = TFCGenViewer.id("dynamic/preview");

    private static DynamicTexture getTexture(Image image) {
        final DynamicTexture texture = new DynamicTexture(image.getNative());
        Minecraft.getInstance().getTextureManager().register(DISPLAY, texture); // Automatically closes the old image
        return texture;
    }

    private final int size;
    private Mode tooltipMode;
    private State state;
    private final Supplier<Font> font;
    private final boolean allowCoordinates;
    private int tick = 0;
    private float progress;
    private DisplayState display;

    public PreviewPane(int x, int y, int size, Supplier<Font> font, boolean allowCoordinates) {
        super(x, y, size, size, Component.empty());
        this.size = size;
        tooltipMode = Mode.NONE;
        state = State.PROCESSING;
        this.font = font;
        this.allowCoordinates = allowCoordinates;
        resetProgress();
    }

    public void alterState(boolean error) {
        state = error ? State.ERROR : State.PROCESSING;
        resetProgress();
        display.close();
        display = null;
    }

    public void updateImage(Image image, ColorDescriptors colorDescriptors, IScale<?> scale, int x0, int z0) {
        state = State.DISPLAY;
        resetProgress();
        display = new DisplayState(image, colorDescriptors, scale, x0, z0, getTexture(image));
    }

    public void updateProgress(int xPixels, ImageSize size) {
        progress = (float) xPixels / size.sizeInPixels();
    }

    private void resetProgress() {
        progress = -1F;
    }

    @Deprecated
    public void setProgress(int progress) {

    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        switch (state) {
            case PROCESSING -> graphics.blit(PROCESSING, getX(), getY(), 0, ((tick >> 1) % 7) * size, size, size, size, size * 8);
            case ERROR -> graphics.blit(ERROR, getX(), getY(), 0, 0, size, size, size, size);
            case DISPLAY -> {
                if (isMouseOver(mouseX, mouseY)) {
                    switch (tooltipMode) {
                        case COORDS -> {
                            final int
                                    x0 = display.x0(),
                                    x1 = x0 + display.sizeInBlocks(),
                                    y0 = display.z0(),
                                    y1 = y0 + display.sizeInBlocks(),
                                    x = (int) Mth.map(mouseX, getX(), getX() + size, x0, x1),
                                    y = (int) Mth.map(mouseY, getY(), getY() + size, y0, y1);
                            graphics.renderTooltip(font.get(), Component.translatable("tfcgenviewer.widget.preview_pane.hover_pos", x, y), mouseX, mouseY);
                        }
                        case COLOR_DESC -> {
                            final int
                                    previewPixels = display.image().size(),
                                    xPixel = (int) Mth.map(mouseX, getX(), getX() + size, 0, previewPixels),
                                    yPixel = (int) Mth.map(mouseY, getY(), getY() + size, 0, previewPixels),
                                    color = display.texture().getPixels().getPixelRGBA(xPixel, yPixel);
                            graphics.renderTooltip(font.get(), display.colors().get(color), mouseX, mouseY);
                        }
                    }
                }
                graphics.blit(DISPLAY, getX(), getY(), 0, 0, size, size, display.image.size(), display.image.size());
            }
        }
        if (progress != -1F) {
            final int scale = Minecraft.getInstance().options.guiScale().get(); // [1, 4]
            final int leftPos = getX() + (getWidth() >> 1) - 51;
            final int yPos = getY() + getHeight() - (scale * 8);
            graphics.blit(
                    PROGRESS_BAR,
                    leftPos,
                    yPos,
                    0,
                    0,
                    102,
                    5,
                    128,
                    128
            );
            graphics.blit(
                    PROGRESS_BAR,
                    leftPos,
                    yPos,
                    0,
                    5,
                    (int) (progress * 102),
                    5,
                    128,
                    128
            );
        }
    }

    @Override
    protected boolean clicked(double pMouseX, double pMouseY) {
        final boolean click = super.clicked(pMouseX, pMouseY);
        if (click) tooltipMode = tooltipMode.next(allowCoordinates);
        return click;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE, NARRATION_TITLE);
    }

    public void tick() {
        tick++;
    }

    private enum Mode {
        NONE,
        COORDS,
        COLOR_DESC;

        private Mode next(boolean allowCoords) {
            return switch (this) {
                case NONE -> allowCoords ?
                        COORDS :
                        COLOR_DESC;
                case COORDS -> COLOR_DESC;
                case COLOR_DESC -> NONE;
            };
        }
    }

    private enum State {
        ERROR,
        PROCESSING,
        DISPLAY
    }

    private record DisplayState(Image image, ColorDescriptors colors, IScale<?> scale, int x0, int z0, DynamicTexture texture) {

        int sizeInBlocks() {
            return image.size() * scale.blocksPerPixel();
        }

        public void close() {

        }
    }
}
