package com.notenoughmail.tfcgenviewer.color;

import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.dries007.tfc.util.DataManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class UnregisteredColorsHandler<T> extends SimplePreparableReloadListener<T> {

    protected static JsonObject parse(Reader reader) {
        return GsonHelper.fromJson(DataManager.GSON, reader, JsonObject.class);
    }

    protected final String directory;
    protected final int directoryLength;

    protected UnregisteredColorsHandler(String directory) {
        this.directory = TFCGenViewer.ID + "/" + directory;
        directoryLength = this.directory.length() + 1;
    }

    @Override
    protected final T prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push(TFCGenViewer.ID);
        final FileToIdConverter converter = FileToIdConverter.json(directory);
        final T handled = handle(
                converter.listMatchingResources(resourceManager)
                        .entrySet()
                        .stream()
                        .map(e -> Map.entry(
                                e.getKey().withPath(s -> s.substring(directoryLength, s.length() - 5)),
                                e.getValue()
                        ))
                        .collect(Collectors.toSet()),
                profiler
        );
        profiler.pop();
        return handled;
    }

    protected abstract T handle(Set<Map.Entry<ResourceLocation, Resource>> entries, ProfilerFiller profiler);
}
