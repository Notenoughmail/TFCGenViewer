package io.github.notenoughmail.tfcgenviewer.client.options;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.impl.util.RefreshableResettableOptionWidget;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record EnhancedEnumValueSet<T>(Codec<T> codec, List<T> values, boolean displayOnlyValue) implements OptionInstance.ValueSet<T> {

    public static final EnhancedEnumValueSet<Boolean> BOOL = new EnhancedEnumValueSet<>(
            Codec.BOOL,
            List.of(true, false),
            false
    );

    @Override
    public Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> tooltipSupplier, Options options, int x, int y, int width, Consumer<T> onValueChanged) {
        return option -> new EnumWidget<>(
                x, y, width, 20, option, values, onValueChanged, tooltipSupplier, displayOnlyValue
        );
    }

    @Override
    public Optional<T> validateValue(T value) {
        return values.contains(value) ?
                Optional.of(value) :
                Optional.empty();
    }

    public static class EnumWidget<E> extends CycleButton<E> implements RefreshableResettableOptionWidget {

        private final OptionInstance<E> instance;

        public EnumWidget(int x, int y, int width, int height, OptionInstance<E> option, List<E> values, Consumer<E> onChange, OptionInstance.TooltipSupplier<E> tooltipSupplier, boolean displayOnlyValue) {
            super(
                    x, y, width, height,
                    displayOnlyValue ?
                            option.toString.apply(option.get()) :
                            CommonComponents.optionNameValue(
                                    option.caption,
                                    option.toString.apply(option.get())
                            ),
                    option.caption,
                    values.indexOf(option.get()),
                    option.get(),
                    ValueListSupplier.create(values),
                    option.toString,
                    CycleButton::createDefaultNarrationMessage,
                    (b, val) -> {
                        option.set(val);
                        onChange.accept(val);
                    },
                    tooltipSupplier,
                    displayOnlyValue
            );
            instance = option;
        }

        @Override
        public void refreshFromInstance() {
            setValue(instance.get());
        }

        @Override
        public void resetToDefaultValue() {
            instance.set(instance.initialValue);
            refreshFromInstance();
        }
    }
}
