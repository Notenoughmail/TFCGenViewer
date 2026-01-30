package io.github.notenoughmail.tfcgenviewer.client.options;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionProvider;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Optional;
import java.util.function.*;

public interface OptionOrder<T> extends OptionProvider.Order<T> {

    static B bool(String name, boolean initial, BooleanConsumer onChange, Consumer<OptionInstance<?>> onFinalize) {
        return new B(name, initial, onChange, new Mut<>(), new Mut<>(), onFinalize);
    }

    static I integer(String name, int initial, int min, int max, IntConsumer onChange, Consumer<OptionInstance<?>> onFinalize) {
        return new I(name, initial, min, max, onChange, new Mut<>(), new Mut<>(), onFinalize);
    }

    static D doub(String name, double initial, double min, double max, DoubleConsumer onChange, Consumer<OptionInstance<?>> onFinalize) {
        return new D(name, initial, min, max, onChange, new Mut<>(), new Mut<>(), onFinalize);
    }

    static <T> L<T> list(String name, T initial, List<T> values, Codec<T> codec, Consumer<T> onChange, Consumer<OptionInstance<?>> onFinalize) {
        return new L<>(name, initial, values, codec, onChange, new Mut<>(), new Mut<>(), onFinalize);
    }

    static <T extends Comparable<T>> C<T> comparable(String name, T initial, T min, T max, Codec<T> codec, ToDoubleFunction<T> toSlider, DoubleFunction<T> fromSlider, Consumer<T> onChange, Consumer<OptionInstance<?>> onFinalize) {
        return new C<>(name, codec, initial, min, max, onChange, toSlider, fromSlider, new Mut<>(), new Mut<>(), onFinalize);
    }

    default OptionInstance.TooltipSupplier<T> getTooltip() {
        return tooltip()
                .get(
                        f -> t -> Tooltip.create(f.make(t)),
                        OptionInstance.noTooltip()
                );
    }

    default OptionInstance.CaptionBasedToString<T> getCaption(OptionInstance.CaptionBasedToString<T> def) {
        return caption()
                .get(
                        f -> f::make,
                        def
                );
    }

    default OptionInstance.CaptionBasedToString<T> getCaption(Function<T, Component> def) {
        return getCaption((c, t) -> Options.genericValueLabel(c, def.apply(t)));
    }

    Mut<OptionProvider.TooltipFactory<T>> tooltip();

    Mut<OptionProvider.DisplayFactory<T>> caption();

    @Override
    default OptionProvider.Order<T> withTooltip(OptionProvider.TooltipFactory<T> tooltipFactory) {
        tooltip().set(tooltipFactory);
        return this;
    }

    @Override
    default OptionProvider.Order<T> withDisplay(OptionProvider.DisplayFactory<T> captionFactory) {
        caption().set(captionFactory);
        return this;
    }

    record B(
            String name,
            boolean initialValue,
            BooleanConsumer onChange,
            Mut<OptionProvider.TooltipFactory<Boolean>> tooltip,
            Mut<OptionProvider.DisplayFactory<Boolean>> caption,
            Consumer<OptionInstance<?>> onFinalize
    ) implements OptionOrder<Boolean> {

        static final OptionInstance.CaptionBasedToString<Boolean> DEFAULT = OptionProvider.<Boolean>convertToFactory(b -> b ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)::make;

        @Override
        public void finish() {
            onFinalize.accept(new OptionInstance<>(
                    name,
                    getTooltip(),
                    getCaption(DEFAULT),
                    ImprovedEnumValueSet.BOOL,
                    initialValue,
                    onChange
            ));
        }
    }

    record I(
            String name,
            int initial,
            int min,
            int max,
            IntConsumer onChange,
            Mut<OptionProvider.TooltipFactory<Integer>> tooltip,
            Mut<OptionProvider.DisplayFactory<Integer>> caption,
            Consumer<OptionInstance<?>> onFinalize
    ) implements OptionOrder<Integer> {
        @Override
        public void finish() {
            onFinalize.accept(new OptionInstance<>(
                    name,
                    getTooltip(),
                    getCaption(Options::genericValueLabel),
                    new OptionInstance.IntRange(min, max),
                    initial,
                    onChange
            ));
        }
    }

    record D(
            String name,
            double initial,
            double min,
            double max,
            DoubleConsumer onChange,
            Mut<OptionProvider.TooltipFactory<Double>> tooltip,
            Mut<OptionProvider.DisplayFactory<Double>> caption,
            Consumer<OptionInstance<?>> onFinalize
    ) implements OptionOrder<Double> {
        @Override
        public void finish() {
            onFinalize.accept(new OptionInstance<>(
                    name,
                    getTooltip(),
                    getCaption(d -> Component.literal(Double.toString(d))),
                    new SliderValue<>(
                            Codec.DOUBLE,
                            min,
                            max,
                            d -> Mth.map(d, min, max, 0D, 1D),
                            d -> Mth.map(d, 0D, 1D, min, max),
                            Optional.of(d -> d <= max && d >= min)
                    ),
                    initial,
                    onChange
            ));
        }
    }

    record C<T extends Comparable<T>>(
            String name,
            Codec<T> codec,
            T initial,
            T min,
            T max,
            Consumer<T> onChange,
            ToDoubleFunction<T> toSlider,
            DoubleFunction<T> fromSlider,
            Mut<OptionProvider.TooltipFactory<T>> tooltip,
            Mut<OptionProvider.DisplayFactory<T>> caption,
            Consumer<OptionInstance<?>> onFinalize
    ) implements OptionOrder<T> {
        @Override
        public void finish() {
            onFinalize.accept(new OptionInstance<>(
                    name,
                    getTooltip(),
                    getCaption(t -> Component.literal(t.toString())),
                    new SliderValue<>(
                            codec,
                            min,
                            max,
                            toSlider,
                            fromSlider,
                            Optional.empty()
                    ),
                    initial,
                    onChange
            ));
        }
    }

    record L<T>(
            String name,
            T initial,
            List<T> values,
            Codec<T> codec,
            Consumer<T> onChange,
            Mut<OptionProvider.TooltipFactory<T>> tooltip,
            Mut<OptionProvider.DisplayFactory<T>> caption,
            Consumer<OptionInstance<?>> onFinalize
    ) implements OptionOrder<T> {
        @Override
        public void finish() {
            onFinalize.accept(new OptionInstance<>(
                    name,
                    getTooltip(),
                    getCaption(t -> Component.literal(t.toString())),
                    new ImprovedEnumValueSet<>(
                            values,
                            codec
                    ),
                    initial,
                    onChange
            ));
        }
    }

    class Mut<T> {
        private T val;
        void set(T val) { this.val = val; }
        <R> R get(Function<T, R> mapper, R def) { return val == null ? def : mapper.apply(val); }
    }

    record SliderValue<T extends Comparable<T>>(Codec<T> codec, T min, T max, ToDoubleFunction<T> toSlider, DoubleFunction<T> fromSlider, Optional<Predicate<T>> validator) implements OptionInstance.SliderableValueSet<T> {

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
            return validator
                    .orElse(val -> min.compareTo(val) <= 0 && max.compareTo(val) >= 0)
                    .test(value) ? Optional.of(value) : Optional.empty();
        }
    }

    record ImprovedEnumValueSet<T>(List<T> values, Codec<T> codec) implements OptionInstance.ValueSet<T> {

        public static final ImprovedEnumValueSet<Boolean> BOOL = new ImprovedEnumValueSet<>(List.of(true, false), Codec.BOOL);

        @Override
        public Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> tooltipSupplier, Options options, int x, int y, int width, Consumer<T> onValueChanged) {
            return instance -> CycleButton.builder(instance.toString)
                    .withValues(values)
                    .withInitialValue(instance.get())
                    .displayOnlyValue() // <-- Effectively the only change
                    .create(x, y, width, 20, instance.caption, (button, value) -> {
                        instance.set(value);
                        onValueChanged.accept(value);
                    });
        }

        @Override
        public Optional<T> validateValue(T value) {
            return values.contains(value) ? Optional.of(value) : Optional.empty();
        }
    }
}
