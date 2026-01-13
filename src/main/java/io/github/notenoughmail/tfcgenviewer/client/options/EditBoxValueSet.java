package io.github.notenoughmail.tfcgenviewer.client.options;

import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record EditBoxValueSet(Font font, Consumer<EditBox> onCreate) implements OptionInstance.ValueSet<String> {

    @Override
    public Function<OptionInstance<String>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<String> tooltipSupplier, Options options, int x, int y, int width, Consumer<String> onValueChanged) {
        return i -> {
            final EditBox editBox = new EditBox(font, x, y, width, 20, i.caption) {
                @Override
                public void setWidth(int width) {
                    super.setWidth(width - 4);
                }

                @Override
                public void setX(int x) {
                    super.setX(x + 2);
                }
            };
            editBox.setValue(i.get());
            editBox.setResponder(i::set);
            onCreate.accept(editBox);
            return editBox;
        };
    }

    @Override
    public Optional<String> validateValue(String value) {
        return Optional.of(value);
    }

    @Override
    public Codec<String> codec() {
        return Codec.STRING;
    }
}
