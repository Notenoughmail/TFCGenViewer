package com.notenoughmail.tfcgenviewer.network.packets;

import com.mojang.serialization.Codec;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.ClientHandoff;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ViewerResponsePacket(
        byte fullPermissions,
        long seed,
        Settings levelSettings,
        Map<ResourceKey<PlacedFeature>, PlacedFeature> visualizableFeatures,
        Map<ResourceKey<Biome>, Biome> biomeInfo,
        Map<TagKey<Biome>, List<ResourceKey<Biome>>> biomeTags
) {

    private static final Codec<List<HolderSet<PlacedFeature>>> BIOME_FEATURES_CODEC = holderSetCodec(Registries.PLACED_FEATURE).listOf();
    private static final Codec<BiomeGenerationSettings> BIOME_GENERATION_SETTINGS_NETWORK_CODEC = BIOME_FEATURES_CODEC.xmap(
            list -> new BiomeGenerationSettings(Map.of(), list),
            BiomeGenerationSettings::features
    );

    private static final Biome.ClimateSettings EMPTY_CLIMATE_SETTINGS = new Biome.ClimateSettings(false, 0F, Biome.TemperatureModifier.NONE, 0F);
    private static final BiomeSpecialEffects EMPTY_SPECIAL_EFFECTS = new BiomeSpecialEffects(
            0,
            0,
            0,
            0,
            Optional.empty(),
            Optional.empty(),
            BiomeSpecialEffects.GrassColorModifier.NONE,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    private static final Codec<Biome> BIOME_NETWORK_CODEC = BIOME_GENERATION_SETTINGS_NETWORK_CODEC.xmap(
            settings -> new Biome(EMPTY_CLIMATE_SETTINGS, EMPTY_SPECIAL_EFFECTS, settings, MobSpawnSettings.EMPTY),
            Biome::getGenerationSettings
    );

    // TODO: 1.21.1 | A Map<ResourceKey<? extends Registry<R>>, Map<TagKey<R>, Collection<Pair<ResourceKey<R>, R>>>> may be effective
    public static ViewerResponsePacket decode(FriendlyByteBuf data) {
        final byte permissions = data.readByte();
        final long seed = data.readLong();
        final Settings settings = data.readWithCodec(NbtOps.INSTANCE, Settings.CODEC.codec());
        final Map<ResourceKey<PlacedFeature>, PlacedFeature> features = data.readMap(
                buf -> buf.readResourceKey(Registries.PLACED_FEATURE),
                buf -> buf.readWithCodec(NbtOps.INSTANCE, PlacedFeature.DIRECT_CODEC)
        );
        final Map<ResourceKey<Biome>, Biome> biomes = data.readMap(
                buf -> buf.readResourceKey(Registries.BIOME),
                buf -> buf.readJsonWithCodec(BIOME_NETWORK_CODEC)
        );
        final Map<TagKey<Biome>, List<ResourceKey<Biome>>> biomeTags = data.readMap(
                buf -> TagKey.create(Registries.BIOME, buf.readResourceLocation()),
                buf -> buf.readList(elmBuf -> elmBuf.readResourceKey(Registries.BIOME))
        );
        return new ViewerResponsePacket(permissions, seed, settings, features, biomes, biomeTags);
    }

    public void encode(FriendlyByteBuf data) {
        data.writeByte(fullPermissions);
        data.writeLong(seed);
        data.writeWithCodec(NbtOps.INSTANCE, Settings.CODEC.codec(), levelSettings);
        data.writeMap(
                visualizableFeatures,
                FriendlyByteBuf::writeResourceKey,
                (buf, feature) -> buf.writeWithCodec(NbtOps.INSTANCE, PlacedFeature.DIRECT_CODEC, feature)
        );
        data.writeMap(
                biomeInfo,
                FriendlyByteBuf::writeResourceKey,
                (buf, biome) -> buf.writeJsonWithCodec(BIOME_NETWORK_CODEC, biome) // Use json as #writeWithCodec is only usable with CompoundTags
        );
        data.writeMap(
                biomeTags,
                (buf, tag) -> buf.writeResourceLocation(tag.location()),
                (buf, list) -> buf.writeCollection(list, FriendlyByteBuf::writeResourceKey)
        );
    }

    public void handle(@Nullable ServerPlayer player) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandoff.viewWorld(this);
        }
    }

    // Holders created through this do not have a holder owner, which is fine
    private static <T> Codec<HolderSet<T>> holderSetCodec(ResourceKey<? extends Registry<T>> registryKey) {
        return ResourceKey.codec(registryKey).listOf()
                .xmap(
                        keys -> HolderSet.direct(key -> Holder.Reference.createStandAlone(null, key), keys),
                        set -> set.stream().map(Holder::unwrapKey).map(Optional::orElseThrow).toList()
                );
    }
}
