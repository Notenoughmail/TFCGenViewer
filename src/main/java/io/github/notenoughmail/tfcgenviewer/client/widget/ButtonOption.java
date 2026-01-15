package io.github.notenoughmail.tfcgenviewer.client.widget;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ButtonOption extends OptionInstance<Boolean> {

    private final Component text;
    private final Button.OnPress onPress;

    public ButtonOption(String caption, Button.OnPress onPress) {
        this(caption, null, onPress);
    }

    public ButtonOption(String caption, @Nullable Component text, Button.OnPress onPress) {
        super(caption, noTooltip(), (c, bool) -> c, BOOLEAN_VALUES, false, bool -> {});
        this.text = text == null ? this.caption : text;
        this.onPress = onPress;
    }

    @Override
    public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth, Consumer<Boolean> pOnValueChanged) {
        return Button.builder(text, onPress).bounds(pX, pY, pWidth, 20).build();
    }
}
