package io.github.notenoughmail.tfcgenviewer.api.color;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A simple cached color key. The cached value will be cleared automatically during resource reload
 */
public final class ColorKey implements CachedColorKey {

    public static ColorKey of(Consumer<MutableComponent> appender) {
        return new ColorKey(() -> Util.make(Component.empty(), appender));
    }

    private final Supplier<Component> source;
    private Component cache;

    ColorKey(Supplier<Component> source) {
        this.source = source;
        ReloadListener.INSTANCE.keys.add(this);
    }

    @Override
    public Component colorKey() {
        if (cache == null) {
            cache = source.get();
        }
        return cache;
    }

    public void clearCache() {
        cache = null;
    }

    @ApiStatus.Internal
    public enum ReloadListener implements PreparableReloadListener {
        INSTANCE;

        final List<ColorKey> keys = new ArrayList<>();

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.supplyAsync(() -> keys)
                    .thenCompose(preparationBarrier::wait)
                    .thenAccept(l -> l.forEach(ColorKey::clearCache));
        }

        @Override
        public String getName() {
            return "ColorKeys";
        }
    }
}
