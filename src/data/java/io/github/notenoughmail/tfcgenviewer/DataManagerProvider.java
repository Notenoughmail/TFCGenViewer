package io.github.notenoughmail.tfcgenviewer;

import net.dries007.tfc.util.data.DataManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class DataManagerProvider implements DataProvider {

    private final Map<DataManager<?>, Map<ResourceLocation, ?>> colors = new HashMap<>();

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookup;
    private final String name;

    public DataManagerProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, String name) {
        this.output = output;
        this.lookup = lookup;
        this.name = name;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookup.thenCompose(l -> {
            make(l);
            colors.forEach(DataManagerProvider::bind);
            return CompletableFuture.allOf(
                    colors.entrySet()
                            .stream()
                            .map(DataManagerProvider::cast)
                            .map(e -> {
                                final PackOutput.PathProvider path = this.output.createPathProvider(PackOutput.Target.RESOURCE_PACK, TFCGenViewer.ID + "/" + e.getKey().getName());
                                return CompletableFuture.allOf(
                                        e.getValue()
                                                .entrySet()
                                                .stream()
                                                .map(e_ -> DataProvider.saveStable(output, l, e.getKey().codec(), e_.getValue(), path.json(e_.getKey())))
                                                .toArray(CompletableFuture[]::new)
                                );
                            })
                            .toArray(CompletableFuture[]::new)
            );
        });
    }

    private static void bind(DataManager<?> manager, Map<ResourceLocation, ?> values) {
        manager.bindValues(TFCGenViewer.cast(values));
    }

    private static <T> Map.Entry<DataManager<T>, Map<ResourceLocation, T>> cast(Map.Entry<DataManager<?>, Map<ResourceLocation, ?>> entry) {
        return new AbstractMap.SimpleImmutableEntry<>(TFCGenViewer.cast(entry.getKey()), TFCGenViewer.cast(entry.getValue()));
    }

    @Override
    public String getName() {
        return "DataProvider[%s]".formatted(name);
    }

    protected abstract void make(HolderLookup.Provider lookup);

    protected <T> void makeFor(DataManager<T> manager, Consumer<Provider<T>> colors) {
        colors.accept(compute(manager)::put);
    }

    private <T> Map<ResourceLocation, T> compute(DataManager<T> manager) {
        return TFCGenViewer.cast(colors.computeIfAbsent(manager, m -> new HashMap<>()));
    }

    public interface Provider<T> {
        default void accept(String path, T data) {
            accept(TFCGenViewer.id(path), data);
        }
        default void accept(DataManager.Reference<T> ref, T data) {
            accept(ref.id(), data);
        }
        void accept(ResourceLocation id , T data);
    }
}
