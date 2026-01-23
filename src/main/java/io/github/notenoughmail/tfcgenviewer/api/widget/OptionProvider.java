package io.github.notenoughmail.tfcgenviewer.api.widget;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public interface OptionProvider {

    // CYCLE BUTTON

    /**
     * Begin a cycle-button {@link net.minecraft.client.OptionInstance OptionInstance} order
     * @param name The lang key of the option's title/caption
     * @param initial The initial value
     * @param values The valid values for the option
     * @param codec A codec for the values
     * @param onChange Respond to player-made changes
     * @return An order which can have its tooltip of display changed. Must be {@link Order#finish() finished} to be added
     *         to preview screens
     */
    <T> Order<T> order(String name, T initial, List<T> values, Codec<T> codec, Consumer<T> onChange);

    /**
     * Begin a boolean toggle {@link net.minecraft.client.OptionInstance OptionInstance} order
     * @param name The lang key of the option's title/caption
     * @param initial The initial value
     * @param onChange Respond to player-made changes
     * @return An order which can have its tooltip or display changed. Must be {@link Order#finish() finished} to be added
     *         to preview screens
     */
    Order<Boolean> orderBool(String name, boolean initial, BooleanConsumer onChange);

    // SLIDER

    /**
     * Begin a slider-value {@link net.minecraft.client.OptionInstance OptionInstance} order
     * @param name The lang key of the option's title/caption
     * @param initial The initial value
     * @param min The minimum selectable value
     * @param max The maximum selectable value
     * @param codec A {@link T} codec
     * @param toSlider Convert a {@link T} to a slider value, in the range [0, 1]
     * @param fromSlider Convert the slider value, in the range [0, 1], to a {@link T}
     * @param onChange Respond to player made changes
     * @return An order which can have its tooltip or display changed. Must be {@link Order#finish() finished} to be added
     *         to preview screens
     */
    <T extends Comparable<T>> Order<T> order(String name, T initial, T min, T max, Codec<T> codec, ToDoubleFunction<T> toSlider, DoubleFunction<T> fromSlider, Consumer<T> onChange);

    /**
     * Begin a int slider-value {@link net.minecraft.client.OptionInstance OptionInstance} order
     * @param name The lang key of the option's title/caption
     * @param initial The initial value
     * @param min The minimum selectable value
     * @param max The maximum selectable value
     * @param onChange Respond to player-made changes
     * @return An order which can have its tooltip or display changed. Must be {@link Order#finish() finished} to be added
     *         to preview screens
     */
    Order<Integer> orderInt(String name, int initial, int min, int max, IntConsumer onChange);

    /**
     * Begin a double slider-value {@link net.minecraft.client.OptionInstance OptionInstance} order
     * @param name The lang key of the option's title/caption
     * @param initial The initial value
     * @param min The minimum selectable value
     * @param max The maximum selectable value
     * @param onChange Respond to player-made changes
     * @return An order which can have its tooltip or display changed. Must be {@link Order#finish() finished} to be added
     *         to preview screens
     */
    Order<Double> orderDouble(String name, double initial, double min, double max, DoubleConsumer onChange);

    default <T> DisplayFactory<T> genericDisplay(Function<T, Component> formatter) {
        return IVisualizerType.Options.genericDisplay(formatter);
    }

    /**
     * A {@link net.minecraft.client.OptionInstance OptionInstance} builder. Must be {@link #finish() finished} to add to preview screens
     * @param <T>
     */
    interface Order<T> {

        /**
         * Add a tooltip to the option
         * @return this
         */
        Order<T> withTooltip(TooltipFactory<T> tooltipFactory);

        /**
         * Add a constant tooltip to the option
         * @return this
         */
        default Order<T> withContantTooltip(Component text) {
            return withTooltip(t -> text);
        }

        /**
         * Override the default display of the option
         * @return this
         */
        Order<T> withDisplay(DisplayFactory<T> displayFactory);

        /**
         * Create and add the option to the list of options available to the player
         */
        void finish();
    }

    @FunctionalInterface
    interface TooltipFactory<T> {
        Component make(T value);
    }

    @FunctionalInterface
    interface DisplayFactory<T> {
        Component make(Component title, T value);
    }
}
