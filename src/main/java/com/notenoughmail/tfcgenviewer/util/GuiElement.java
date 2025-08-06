package com.notenoughmail.tfcgenviewer.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;

public enum GuiElement {
    UNKNOWN(20, 20, 0, 40, false),
    EDIT(20, 0),
    REMOVE(0, 0),
    CONFIRM(40, 0);

    private static final int texSize = 64;

    private final int width, height, uOffset, vOffset;
    private final boolean selectable;

    GuiElement(int uOffset, int vOffset) {
        this(20, 20, uOffset, vOffset, true);
    }

    GuiElement(int width, int height, int uOffset, int vOffset, boolean offsetOnSelect) {
        this.width = width;
        this.height = height;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.selectable = offsetOnSelect;
    }

    public void render(GuiGraphics graphics, int x, int y, int zBlitOffset) {
        graphics.blit(WidgetUtils.GUI_ELEMENTS, x, y, zBlitOffset, uOffset, vOffset, width, height, texSize, texSize);
    }

    public void render(GuiGraphics graphics, int x, int y) {
        render(graphics, x, y, 0);
    }

    public ImageButton button(Button.OnPress onPress) {
        return button(0, 0, onPress);
    }

    public ImageButton button(int x, int y, Button.OnPress onPress) {
        return new ImageButton(x, y, width, height, uOffset, vOffset, selectable ? height : 0, WidgetUtils.GUI_ELEMENTS, texSize, texSize, onPress);
    }
}
