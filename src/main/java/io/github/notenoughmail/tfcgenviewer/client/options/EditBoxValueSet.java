package io.github.notenoughmail.tfcgenviewer.client.options;

import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record EditBoxValueSet(Supplier<Font> font, Consumer<EditBox> onCreate, @Nullable Component hint) implements OptionInstance.ValueSet<String> {

    @Override
    public Function<OptionInstance<String>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<String> tooltipSupplier, Options options, int x, int y, int width, Consumer<String> onValueChanged) {
        return i -> {
            final EditBox editBox = new ValueEditBox(font.get(), x, y, width, i);
            if (hint != null) {
                editBox.setHint(hint);
            }
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

    public static class ValueEditBox extends EditBox {

        public ValueEditBox(Font font, int x, int y, int width, OptionInstance<String> instance) {
            super(font, x, y, width, 20, instance.caption);
            setValue(instance.get());
            setResponder(instance::set);
        }

        @Override
        public void setWidth(int width) {
            super.setWidth(width - 4);
        }

        @Override
        public void setX(int x) {
            super.setX(x + 2);
        }
    }
}
