package io.github.notenoughmail.tfcgenviewer;

import io.github.notenoughmail.tfcgenviewer.api.cache.ClimateFeatureCache;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TagProvider extends TagsProvider<PlacedFeature> {

    protected TagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.PLACED_FEATURE, lookupProvider, TFCGenViewer.ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ClimateFeatureCache.VISUALIZABLE_FEATURES)
                .add(
                        key("vein/kaolin_disc"),
                        key("coral_mushroom"),
                        key("coral_tree"),
                        key("coral_claw")
                );
    }

    private static ResourceKey<PlacedFeature> key(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Helpers.identifier(path));
    }
}
