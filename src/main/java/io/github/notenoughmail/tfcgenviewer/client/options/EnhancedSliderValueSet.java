package io.github.notenoughmail.tfcgenviewer.client.options;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.impl.util.RefreshableResettableOptionWidget;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.Mth;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public record EnhancedSliderValueSet<T extends Comparable<T>>(
        Codec<T> codec, // Only used for loading to/from the options file. Numeric types needn't be bound
        InclusiveRange<T> range,
        ToDoubleFunction<T> toSlider,
        DoubleFunction<T> fromSlider
) implements OptionInstance.SliderableValueSet<T> {

    public static final EnhancedSliderValueSet<Double> UNIT_DOUBLE = new EnhancedSliderValueSet<>(
            Codec.DOUBLE,
            new InclusiveRange<>(0D, 1D),
            d -> d,
            d -> d
    );

    public static EnhancedSliderValueSet<Integer> integer(
            int minInclusive,
            int maxInclusive
    ) {
        return new EnhancedSliderValueSet<>(
                Codec.INT,
                new InclusiveRange<>(minInclusive, maxInclusive),
                i -> {
                    if (i == minInclusive) {
                        return 0D;
                    } else {
                        return i == maxInclusive ?
                                1D :
                                Mth.map(i + 0.5D, minInclusive, maxInclusive + 1.0D, 0, 1);
                    }
                },
                d -> {
                    if (d >= 1.0D) {
                        d = 0.99999F;
                    }
                    return Mth.floor(Mth.map(d, 0D, 1D, minInclusive, maxInclusive));
                }
        );
    }

    public static EnhancedSliderValueSet<Double> doub(
            double minInclusive,
            double maxInclusive
    ) {
        return new EnhancedSliderValueSet<>(
                Codec.DOUBLE,
                new InclusiveRange<>(minInclusive, maxInclusive),
                d -> Mth.map(d, minInclusive, maxInclusive, 0D, 1D),
                d -> Mth.map(d, 0D, 1D, minInclusive, maxInclusive)
        );
    }

    @Override
    public double toSliderValue(T value) {
        return toSlider.applyAsDouble(value);
    }

    @Override
    public T fromSliderValue(double value) {
        return fromSlider.apply(value);
    }

    @Override
    public Optional<T> validateValue(T value) {
        return range.isValueInRange(value) ?
                Optional.of(value) :
                Optional.empty();
    }

    @Override
    public Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> tooltipSupplier, Options $, int x, int y, int width, Consumer<T> onValueChanged) {
        return option -> new SliderWidget<>(
                x, y, width, 20, this, option, tooltipSupplier, onValueChanged
        );
    }

    public static class SliderWidget<N extends Comparable<N>> extends AbstractSliderButton implements RefreshableResettableOptionWidget {

        private final EnhancedSliderValueSet<N> values;
        private final OptionInstance<N> instance;
        private final OptionInstance.TooltipSupplier<N> tooltipSupplier;
        private final Consumer<N> onChange;

        protected SliderWidget(
                int x,
                int y,
                int width,
                int height,
                EnhancedSliderValueSet<N> values,
                OptionInstance<N> instance,
                OptionInstance.TooltipSupplier<N> tooltipSupplier,
                Consumer<N> onChange
        ) {
            super(x, y, width, height, CommonComponents.EMPTY, values.toSliderValue(instance.get()));
            this.values = values;
            this.instance = instance;
            this.tooltipSupplier = tooltipSupplier;
            this.onChange = onChange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(instance.toString.apply(values.fromSliderValue(value)));
            setTooltip(tooltipSupplier.apply(values.fromSliderValue(value)));
        }

        @Override
        protected void applyValue() {
            final N n = values.fromSliderValue(value);
            if (!Objects.equals(n, instance.get())) {
                assert n != null;
                instance.set(n);
                onChange.accept(instance.get());
            }
        }

        @Override
        public void refreshFromInstance() {
            value = values.toSliderValue(instance.get());
            updateMessage();
        }

        @Override
        public void resetToDefaultValue() {
            instance.set(instance.initialValue);
            refreshFromInstance();
        }
    }
}
