package io.github.notenoughmail.tfcgenviewer.color;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public record SeedMakerRockColorsProvider(PackOutput.PathProvider pathProvider, CompletableFuture<HolderLookup.Provider> lookup, Path metaFile) implements DataProvider {

    public SeedMakerRockColorsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        this(new PackOutput(output.getOutputFolder().resolve("resourcepacks").resolve("tfc_seed_maker")), lookup, null);
    }

    private SeedMakerRockColorsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, @Nullable DataProvider dud) {
        this(output.createPathProvider(PackOutput.Target.RESOURCE_PACK, TFCGenViewer.ID + "/" + Colors.ROCK_COLORS.getName()), lookup, output.getOutputFolder().resolve("pack.mcmeta"));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookup.thenCompose(l -> {
                    final Map<ResourceLocation, ColorDefinition> colors = new HashMap<>();
                    ColorProvider.ROCK_COLORS.forEach((r, i) -> i.make(r, false, colors::put));
                    colors.put(Colors.UNKNOWN, ColorDefinition.of(
                            29, 32, 33,
                            Component.translatable("rock.tfcgenviewer.unknown"),
                            null
                    ));
                    return CompletableFuture.allOf(
                            Stream.concat(
                                    colors.entrySet()
                                            .stream()
                                            .map(e -> DataProvider.saveStable(output, l, ColorDefinition.CODEC, e.getValue(), pathProvider.json(e.getKey()))),
                                    Stream.of(
                                            DataProvider.saveStable(
                                                    output,
                                                    Util.make(new JsonObject(), top -> top.add("pack", Util.make(new JsonObject(), pack -> {
                                                        pack.addProperty("description", "Gives the Rocks visualizer the colors of TFCSeedMaker");
                                                        pack.addProperty("pack_format", 15);
                                                        pack.add("supported_formats", Util.make(new JsonArray(), a -> {
                                                            a.add(15);
                                                            a.add(65);
                                                        }));
                                                    }))),
                                                    metaFile
                                            )
                                    )
                            ).toArray(CompletableFuture[]::new)
                    );
                });
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
