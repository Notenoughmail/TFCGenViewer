package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.BlockEvaluationFunction;
import io.github.notenoughmail.tfcgenviewer.api.ColorTooltips;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionProvider;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A visualizer type is tied to a single {@link IGeneratorVisualizer} and must be stateless. It is responsible for
 * drawing on an {@link MutableImage image} to visualize a feature about the chunk generator type it handles
 * <p>
 * The order of operations proceeds as follows
 * <ul>
 *     <li>
 *         If the player is attempting to visualize in-world
 *         <ul>
 *             <li>{@link #additionalSynchronization(SynchronizationRequest) additionalSynchronization} if the visualizer type is permitted</li>
 *         </ul>
 *     </li>
 *     <li>
 *         Upon the visualizer type being selected in the preview screen
 *         <ul>
 *             <li>{@link #createOptions(RegistryAccess) createOptions}</li>
 *             <li>{@link #addOptions(OptionProvider, Options) addOptions}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         Upon the preview screen first opening or the <i>Apply</i> button being clicked
 *         <ul>
 *             <li>{@link Options#copy()}</li>
 *             <li>{@link #createCache(RegistryAccess, ChunkGeneratorExtension, ImageSize, long, O) createCache}</li>
 *             <li>{@link #draw(int, int, MutableImage, int, int, DrawInfo) draw}</li>
 *             <li>{@link #afterComplete(MutableImage, DrawInfo) afterComplete}</li>
 *             <li>{@link #appendToFileName(Consumer, Options) appendToFileName}</li>
 *             <li>{@link #additionalPreviewInfo(DrawInfo) additionalPreviewInfo}</li>
 *             <li>{@link #colorKey(RegistryAccess, Object) colorKey}</li>
 *         </ul>
 *     </li>
 * </ul>
 */
public interface IVisualizerType<
        G extends ChunkGeneratorExtension,
        C,
        S extends IScale<?>,
        O extends IVisualizerType.Options<O>
        > {

    /**
     * Create a {@link Options} instance which will store information about player-specified {@link #draw(int, int, MutableImage, int, int, DrawInfo) draw}
     * settings. {@link NoneOpt} should be returned when the visualizer type has no options
     * <p>
     * See also: {@link #addOptions(OptionProvider, Options) addOptions}
     */
    O createOptions(RegistryAccess registryAccess);

    /**
     * Add options to the preview screen which the player can change
     * @param optionProvider Provider of {@link net.minecraft.client.OptionInstance OptionInstance} builders
     * @param options The options made in {@link #createOptions(RegistryAccess) createOptions}
     */
    default void addOptions(OptionProvider optionProvider, O options) {}

    /**
     * Request server-only registry information to be synchronized to the client
     */
    default void additionalSynchronization(SynchronizationRequest synchronizationRequest) {}

    /**
     * Get the codec used to {@link #additionalSynchronization(SynchronizationRequest) sync} server-only registry contents
     */
    @Nullable
    default <T> Codec<T> elementCodecForRegistry(ResourceKey<? extends Registry<T>> registry) {
        return null;
    }

    /**
     * Create the cache object which will be available during drawing via {@link DrawInfo}
     */
    C createCache(RegistryAccess registryAccess, G generator, ImageSize size, long worldSeed, O options);

    /**
     * Set the color of a pixel on the image. May set/modify pixels not at the given pixel. Called for <strong>every</strong>
     * pixel in the image
     * @param imageX The x coordinate on the image
     * @param imageY The y coordinate of the image
     * @param image The image
     * @param xPos The x position, 'in-world', at the scale of the {@link DrawInfo#scale() scale}'s
     *             {@link IScale#blocksPerPixel() blocksPerPixel}
     * @param zPos The z position, 'in-world', at the scale of the {@link DrawInfo#scale() scale}'s
     *             {@link IScale#blocksPerPixel() blocksPerPixel}
     * @param info All other {@link DrawInfo draw information}
     */
    void draw(
            int imageX,
            int imageY,
            MutableImage image,
            int xPos,
            int zPos,
            DrawInfo<G, C, S, O> info
    );

    /**
     * Optionally perform some actions after all pixels of the image have been drawn
     */
    default void afterComplete(MutableImage image, DrawInfo<G, C, S, O> info) {}

    /**
     * Optionally provide extra information to display on the right information pane of the preview screens
     */
    @Nullable
    default Component additionalPreviewInfo(DrawInfo<G, C, S, O> info) {
        return null;
    }

    /**
     * Optionally append information to the file name of exported images
     */
    default void appendToFileName(Consumer<String> fileNameAppender, O options) {}

    /**
     * The color key of the visualizer type. Generally static, but may use registry and cache information
     */
    Component colorKey(RegistryAccess registryAccess, C cache);

    /**
     * The formatted name of this visualizer type
     */
    Component name();

    /**
     * A brief, formatted description of what this visualizer type displays
     */
    Component description();

    /**
     * The maximum period of time, in milliseconds, a single pixel will be allowed to process for before being skipped.
     * This will immediately cause the preview generation to end exceptionally if in a dev environment
     */
    default int timeoutMillis() {
        return TFCGenViewer.defaultMaxMillisecondsToDrawPixel.getAsInt();
    }

    /**
     * The BGR color to fill a pixel with upon generation timing out
     */
    default int timeoutFillBGR() {
        return 0;
    }

    /**
     * If the draw requests of this visualizer type can be lowly parallelized. A max of 5 draws will be processed simultaneously
     */
    default boolean shouldDrawInParallel(O options, ImageSize size) {
        return false;
    }

    /**
     * A collection of relevant objects which are provided during drawing of a preview image
     * @param colorTooltips The tooltip cache. Use {@link #addTooltip(ColorDefinition)} or {@link io.github.notenoughmail.tfcgenviewer.api.color.ColorGradientDefinition#color(double, DrawInfo) ColorGradientDefinition#color}
     *                      to add tooltips
     * @param cache The cache, as created in {@link #createCache(RegistryAccess, ChunkGeneratorExtension, ImageSize, long, O) createCache}
     * @param size The image size, guaranteed to be {@link IScale#sizes() possessed} by the scale
     */
    record DrawInfo<G extends ChunkGeneratorExtension, C, S extends IScale<?>, O extends Options<O>>(
            G generator,
            C cache,
            RegistryAccess registryAccess,
            ColorTooltips colorTooltips,
            ImageSize size,
            S scale,
            O options
    ) {

        /**
         * Add the color to the tooltips if not already present
         */
        public void addTooltip(ColorDefinition color) {
            colorTooltips.addColorTooltip(color);
        }

        /**
         * If the position is, per the {@link SolarCalculator}, in the northern hemisphere
         * @param zPos The pixel-resolution z position
         */
        public boolean isNorthernHemisphere(int zPos) {
            return SolarCalculator.getInNorthernHemisphere(pixelResolutionToBlock(zPos, true), settings().temperatureScale());
        }

        /**
         * Convert the given pixel resolution position to block scale
         * @param pixelResolutionPosition The coordinate in pixel resolution
         * @param center If the returned block coordinate should be shifted to be in the mid-point of the pixel
         */
        public int pixelResolutionToBlock(int pixelResolutionPosition, boolean center) {
            return scale.pixelResolutionToBlock(pixelResolutionPosition, center);
        }

        /**
         * Perform an action at block scale
         */
        public <T> T evaluateAtBlockPosition(boolean center, int pixelResolutionX, int pixelResolutionZ, BlockEvaluationFunction<T> function) {
            return scale.evaluateAtBlockPosition(center, pixelResolutionX, pixelResolutionZ, function);
        }

        /**
         * The generator settings
         */
        public Settings settings() {
            return generator.settings();
        }

        /**
         * The generator rock settings
         */
        public RockLayerSettings rockLayerSettings() {
            return generator.rockLayerSettings();
        }
    }

    /**
     * A noop {@link Options} instance. Visualizer types with no options <strong>must</strong> use this
     */
    enum NoneOpt implements Options<NoneOpt> {
        INSTANCE;

        @Override
        public NoneOpt copy() {
            return this;
        }
    }

    interface Options<O extends Options<O>> {

        /**
         * Create a deep copy of this options object. The copy will be available in {@link DrawInfo} params and is
         * guaranteed to be unmodified by player interactions in the preview screens
         */
        O copy();

        static <O extends Options<O>> O copy(O original) {
            if (original == NoneOpt.INSTANCE) {
                return original.copy();
            } else {
                final O copy = original.copy();
                if (copy == original) {
                    throw new IllegalArgumentException("An options copy should not be the same object");
                }
                return copy;
            }
        }
    }
}
