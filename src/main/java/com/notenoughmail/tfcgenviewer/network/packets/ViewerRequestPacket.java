package com.notenoughmail.tfcgenviewer.network.packets;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.network.TFCGVChannel;
import com.notenoughmail.tfcgenviewer.util.Permissions;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.feature.vein.IVeinConfig;
import net.dries007.tfc.world.feature.vein.VeinConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


public enum ViewerRequestPacket {
    INSTANCE;

    public static final Component VIEWING_DISALLOWED = Component.translatable("tfcgenviewer.message.viewing_disallowed").withStyle(ChatFormatting.RED);
    public static final Component NON_TFC_WORLD = Component.translatable("tfcgenviewer.message.non_tfc_world").withStyle(ChatFormatting.YELLOW);

    public void handle(@Nullable ServerPlayer player) {
        if (player != null) {
            final byte permission = Permissions.get(player);
            if (!Permissions.isEmpty(permission)) {
                if (player.level() instanceof ServerLevel sl && sl.getChunkSource().getGenerator() instanceof TFCChunkGenerator gen) {
                    final boolean syncFeatures = (permission & Permissions.CLIMATE_CHARACTERISTICS) != 0;
                    TFCGVChannel.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new ViewerResponsePacket(
                                    permission,
                                    sl.getSeed(),
                                    gen.settings(),
                                    syncFeatures ? getFeatures(sl.registryAccess()) : Map.of(),
                                    syncFeatures ? getBiomes(sl.registryAccess()) : Map.of(),
                                    syncFeatures ? getBiomeTags(sl.registryAccess()) : Map.of()
                            )
                    );
                } else {
                    player.sendSystemMessage(NON_TFC_WORLD);
                }
            } else {
                player.sendSystemMessage(VIEWING_DISALLOWED);
            }
        }
    }

    private static Map<ResourceKey<PlacedFeature>, PlacedFeature> getFeatures(RegistryAccess registryAccess) {
        return TFCGenViewer.ofEntryStream(
                visualizableFeatures(registryAccess)
                        .map(holder -> Map.entry(holder.unwrapKey().orElseThrow(), holder.value()))
        );
    }

    private static Stream<Holder<PlacedFeature>> visualizableFeatures(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(Registries.PLACED_FEATURE)
                .get(TFCGenViewer.VISUALIZABLE_FEATURES)
                .stream()
                .flatMap(HolderSet.Named::stream);
    }

    private static Map<ResourceKey<Biome>, Biome> getBiomes(RegistryAccess registryAccess) {
        final HolderLookup.RegistryLookup<Biome> lookup = registryAccess.lookupOrThrow(Registries.BIOME);
        return TFCGenViewer.ofEntryStream(
                TFCBiomes.getAllKeys()
                        .stream()
                        .map(key -> Map.entry(key, lookup.getOrThrow(key).get()))
        );
    }

    private static Map<TagKey<Biome>, List<ResourceKey<Biome>>> getBiomeTags(RegistryAccess registryAccess) {
        final HolderLookup.RegistryLookup<Biome> lookup = registryAccess.lookupOrThrow(Registries.BIOME);
        return TFCGenViewer.ofEntryStream(
                visualizableFeatures(registryAccess)
                        .map(Holder::get)
                        .map(PlacedFeature::feature)
                        .map(Holder::get)
                        .map(ConfiguredFeature::config)
                        .filter(IVeinConfig.class::isInstance)
                        .map(IVeinConfig.class::cast)
                        .map(IVeinConfig::config)
                        .map(VeinConfig::biomes)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .distinct()
                        .map(tag -> Map.entry(
                                tag,
                                lookup.get(tag)
                                        .stream()
                                        .flatMap(HolderSet.Named::stream)
                                        .map(Holder::unwrapKey)
                                        .map(Optional::orElseThrow)
                                        .distinct()
                                        .toList()
                        ))
        );
    }
}
