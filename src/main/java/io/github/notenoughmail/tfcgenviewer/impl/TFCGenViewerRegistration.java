package io.github.notenoughmail.tfcgenviewer.impl;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCVisualizer;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.TFCBiomeVisualizer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TFCGenViewerRegistration {

    public static void init(IEventBus modBus) {
        VISUALIZERS.register(modBus);
    }

    private static final DeferredRegister<ITFCVisualizer<?>> VISUALIZERS = DeferredRegister.create(GenViewerAPI.TFC_VISUALIZER_REGISTRY, TFCGenViewer.ID);

    public static Supplier<TFCBiomeVisualizer> BIOME = visualizer("biome", TFCBiomeVisualizer::new);

    private static <T extends ITFCVisualizer<?>> Supplier<T> visualizer(String name, Supplier<T> supplier) {
        return register(VISUALIZERS, name, supplier);
    }

    private static <T, R extends T> Supplier<R> register(DeferredRegister<T> register, String name, Supplier<R> supplier) {
        return register.register(name, supplier);
    }
}
