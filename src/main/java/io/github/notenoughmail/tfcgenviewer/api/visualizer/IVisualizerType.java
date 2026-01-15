package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionRequest;
import io.github.notenoughmail.tfcgenviewer.impl.ColorDescriptors;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface IVisualizerType<
        G extends ChunkGeneratorExtension,
        C,
        S extends IScale<?>,
        O extends IVisualizerType.Options<O>
        > {

    ResourceLocation id();

    O createOptions(RegistryAccess registryAccess, G generator, ImageSize scale);

    default void addOptions(OptionRequest optionRequest, O options) {}

    default void additionalSynchronization(SynchronizationRequest synchronizationRequest) {}

    C createCache(RegistryAccess registryAccess, G generator, ImageSize size, long worldSeed);

    void draw(
            int imageX,
            int imageY,
            MutableImage image,
            int xPos,
            int zPos,
            DrawInfo<G, C, S, O> info
    );

    default void afterComplete(MutableImage image, DrawInfo<G, C, S, O> info) {}

    @Nullable
    default Component previewInfo(DrawInfo<G, C, S, O> info) {
        return null;
    }

    Component colorKey(RegistryAccess registryAccess, C cache);

    Component name();

    boolean isPermitted(ServerPlayer player);

    record DrawInfo<G extends ChunkGeneratorExtension, C, S extends IScale<?>, O extends Options<O>>(
            G generator,
            C cache,
            RegistryAccess registryAccess,
            ColorDescriptors colorDescriptors,
            ImageSize size,
            S scale,
            O options
    ) {}

    enum NoneOpt implements Options<NoneOpt> {
        INSTANCE;

        @Override
        public NoneOpt copy() {
            return INSTANCE;
        }
    }

    interface Options<O extends Options<O>> {

        static <T> BiFunction<Component, T, Component> genericCaption(Function<T, Component> mapper) {
            return (c, t) -> Component.translatable("options.generic_value", c, mapper.apply(t));
        }

        /**
         * Get a copy of this option instance that will not be modified by player inputs while drawing a preview
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
