package io.github.notenoughmail.tfcgenviewer;

import net.dries007.tfc.util.data.DataManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class DataManagerProvider<T> implements DataProvider {

    private final Map<DataManager<T>, Map<ResourceLocation, T>> colors = new HashMap<>();

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
            make();
            colors.forEach(DataManager::bindValues);
            return CompletableFuture.allOf(
                    colors.entrySet()
                            .stream()
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

    @Override
    public String getName() {
        return "DataProvider[%s]".formatted(name);
    }

    protected abstract void make();

    protected void makeFor(DataManager<T> manager, Consumer<Provider<T>> colors) {
        colors.accept(this.colors.computeIfAbsent(manager, m -> new HashMap<>())::put);
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
