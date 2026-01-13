package io.github.notenoughmail.tfcgenviewer.client.options;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionRequest;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.*;

public interface OptionOrder<T> extends OptionRequest.Order<T>, Supplier<OptionInstance<T>> {

    static B bool(String name, boolean initial, BooleanConsumer onChange) {
        return new B(name, initial, onChange, new Mut<>(), new Mut<>());
    }

    static I integer(String name, int initial, int min, int max, IntConsumer onChange) {
        return new I(name, initial, min, max, onChange, new Mut<>(), new Mut<>());
    }

    static D doub(String name, double initial, double min, double max, DoubleConsumer onChange) {
        return new D(name, initial, min, max, onChange, new Mut<>(), new Mut<>());
    }

    static <T> L<T> list(String name, T initial, List<T> values, Codec<T> codec, Consumer<T> onChange) {
        return new L<>(name, initial, values, codec, onChange, new Mut<>(), new Mut<>());
    }

    static <T extends Comparable<T>> C<T> comparable(String name, T initial, T min, T max, Codec<T> codec, ToDoubleFunction<T> toSlider, DoubleFunction<T> fromSlider, Consumer<T> onChange) {
        return new C<>(name, codec, initial, min, max, onChange, toSlider, fromSlider, new Mut<>(), new Mut<>());
    }

    default OptionInstance.TooltipSupplier<T> getTooltip() {
        return tooltip()
                .get(
                        f -> t -> Tooltip.create(f.apply(t)),
                        OptionInstance.noTooltip()
                );
    }

    default OptionInstance.CaptionBasedToString<T> getCaption(OptionInstance.CaptionBasedToString<T> def) {
        return caption()
                .get(
                        f -> f::apply,
                        def
                );
    }

    default OptionInstance.CaptionBasedToString<T> getCaption(Function<T, Component> def) {
        return getCaption((c, t) -> Options.genericValueLabel(c, def.apply(t)));
    }

    Mut<Function<T, Component>> tooltip();

    Mut<BiFunction<Component, T, Component>> caption();

    @Override
    default OptionRequest.Order<T> withTooltip(Function<T, Component> tooltipFactory) {
        tooltip().set(tooltipFactory);
        return this;
    }

    @Override
    default OptionRequest.Order<T> withDisplay(BiFunction<Component, T, Component> captionFactory) {
        caption().set(captionFactory);
        return this;
    }

    record B(
            String name,
            boolean initialValue,
            BooleanConsumer onChange,
            Mut<Function<Boolean, Component>> tooltip,
            Mut<BiFunction<Component, Boolean, Component>> caption
    ) implements OptionOrder<Boolean> {
        @Override
        public OptionInstance<Boolean> get() {
            return OptionInstance.createBoolean(
                    name,
                    getTooltip(),
                    getCaption(OptionInstance.BOOLEAN_TO_STRING),
                    initialValue,
                    onChange
            );
        }
    }

    record I(
            String name,
            int initial,
            int min,
            int max,
            IntConsumer onChange,
            Mut<Function<Integer, Component>> tooltip,
            Mut<BiFunction<Component, Integer, Component>> caption
    ) implements OptionOrder<Integer> {
        @Override
        public OptionInstance<Integer> get() {
            return new OptionInstance<>(
                    name,
                    getTooltip(),
                    getCaption(Options::genericValueLabel),
                    new OptionInstance.IntRange(min, max),
                    initial,
                    onChange
            );
        }
    }

    record D(
            String name,
            double initial,
            double min,
            double max,
            DoubleConsumer onChange,
            Mut<Function<Double, Component>> tooltip,
            Mut<BiFunction<Component, Double, Component>> caption
    ) implements OptionOrder<Double> {
        @Override
        public OptionInstance<Double> get() {
            return new OptionInstance<>(
                    name,
                    getTooltip(),
                    getCaption(d -> Component.literal(Double.toString(d))),
                    new SliderValue<>(
                            Codec.DOUBLE,
                            min,
                            max,
                            d -> d,
                            d -> d,
                            Optional.empty()
                    ),
                    initial,
                    onChange
            );
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
            Mut<Function<T, Component>> tooltip,
            Mut<BiFunction<Component, T, Component>> caption
    ) implements OptionOrder<T> {

        @Override
        public OptionInstance<T> get() {
            return new OptionInstance<>(
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
            );
        }
    }

    record L<T>(
            String name,
            T initial,
            List<T> values,
            Codec<T> codec,
            Consumer<T> onChange,
            Mut<Function<T, Component>> tooltip,
            Mut<BiFunction<Component, T, Component>> caption
    ) implements OptionOrder<T> {

        @Override
        public OptionInstance<T> get() {
            return new OptionInstance<>(
                    name,
                    getTooltip(),
                    getCaption(t -> Component.literal(t.toString())),
                    new OptionInstance.Enum<>(
                            values,
                            codec
                    ),
                    initial,
                    onChange
            );
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
                    .orElse(val -> min.compareTo(val) < 0 && max.compareTo(val) > 0)
                    .test(value) ? Optional.of(value) : Optional.empty();
        }
    }
}
