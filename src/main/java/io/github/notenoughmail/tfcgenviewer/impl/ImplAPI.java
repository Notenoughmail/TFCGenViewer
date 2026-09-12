package io.github.notenoughmail.tfcgenviewer.impl;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.SerializationInformation;
import io.github.notenoughmail.tfcgenviewer.api.registry.ISyncRegistries;
import io.github.notenoughmail.tfcgenviewer.api.registry.NetworkHolder;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.netty.buffer.ByteBuf;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.RockSettings;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.BiConsumer;

public class ImplAPI {

    public static synchronized void register(IGeneratorVisualizer<? ,? ,?, ?> generatorVisualizer) {
        GENS.computeIfAbsent(generatorVisualizer.generatorType(), c -> new ArrayList<>()).add(generatorVisualizer);
        GEN_IDS.put(generatorVisualizer.id(), generatorVisualizer);
    }

    public static <
            G extends ChunkGeneratorExtension,
            V extends IVisualizerType<G, ?, ?, ?>
            > List<IGeneratorVisualizer<G, ?, ?, V>> getVisualizersFor(G gen) {
        return TFCGenViewer.cast(GENS.get(gen.getClass()));
    }

    // I wish I could say this is the worst thing in the mod, but it's not
    // It is however *in service* of the worst thing in this mod
    public static <T> void getAllSync(ResourceKey<? extends Registry<T>> key, BiConsumer<ISyncRegistries, StreamCodec<FriendlyByteBuf, T>> ret) {
        class Info implements SerializationInformation {
            final MutableObject<ISyncRegistries> sync = new MutableObject<>();
            @Override
            public <R> void provide(ResourceKey<? extends Registry<R>> registry, StreamCodec<? super RegistryFriendlyByteBuf, R> codec) {
                if (registry == key) {
                    ret.accept(sync.getValue(), TFCGenViewer.cast(codec));
                }
            }
            void sync(ISyncRegistries sync) {
                this.sync.setValue(sync);
                sync.elementCodecForRegistry(this);
            }
        }
        final Info info = new Info();

        GENS.values().stream().flatMap(List::stream).forEach(info::sync);
        GenViewerAPI.VISUALIZER_REGISTRY.forEach(info::sync);
    }

    public static final Map<ResourceLocation, IGeneratorVisualizer<?, ?, ?, ?>> GEN_IDS = new HashMap<>();

    private static final Map<Class<? extends ChunkGeneratorExtension>, List<IGeneratorVisualizer<?, ?, ?, ?>>> GENS = new IdentityHashMap<>();

    public static final StreamCodec<ByteBuf, IGeneratorVisualizer<?, ?, ?, ?>> GENERATOR_VISUALIZER_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(GEN_IDS::get, IGeneratorVisualizer::id);

    private static final StreamCodec<RegistryFriendlyByteBuf, Block> BLOCK_CODEC = ByteBufCodecs.registry(Registries.BLOCK);
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<Block>> OPT_BLOCK_CODEC = BLOCK_CODEC.apply(ByteBufCodecs::optional);
    private static final StreamCodec<RegistryFriendlyByteBuf, List<String>> STRING_LIST_CODEC = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).cast();

    public static final StreamCodec<RegistryFriendlyByteBuf, RockSettings> ROCK_SETTINGS_CODEC = StreamCodec.of(
            (b, r) -> {
                BLOCK_CODEC.encode(b, r.raw());
                BLOCK_CODEC.encode(b, r.hardened());
                BLOCK_CODEC.encode(b, r.gravel());
                BLOCK_CODEC.encode(b, r.cobble());
                BLOCK_CODEC.encode(b, r.sand());
                BLOCK_CODEC.encode(b, r.sandstone());
                OPT_BLOCK_CODEC.encode(b, r.spike());
                OPT_BLOCK_CODEC.encode(b, r.loose());
                OPT_BLOCK_CODEC.encode(b, r.mossyLoose());
                b.writeBoolean(r.karst().orElse(false));
                b.writeBoolean(r.mafic().orElse(false));
            },
            b -> new RockSettings(
                    BLOCK_CODEC.decode(b),
                    BLOCK_CODEC.decode(b),
                    BLOCK_CODEC.decode(b),
                    BLOCK_CODEC.decode(b),
                    BLOCK_CODEC.decode(b),
                    BLOCK_CODEC.decode(b),
                    OPT_BLOCK_CODEC.decode(b),
                    OPT_BLOCK_CODEC.decode(b),
                    OPT_BLOCK_CODEC.decode(b),
                    Optional.of(b.readBoolean()),
                    Optional.of(b.readBoolean())
            )
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RockLayerSettings> ROCK_LAYER_SETTINGS_CODEC = NeoForgeStreamCodecs.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    ByteBufCodecs.STRING_UTF8,
                    NetworkHolder.slimStreamCodec(ROCK_SETTINGS_CODEC, RockSettings.KEY)
            ),
            RockLayerSettings.Data::rocks,
            ImplAPI.STRING_LIST_CODEC, RockLayerSettings.Data::bottom,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, RockLayerSettings.LayerData::id,
                    ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), RockLayerSettings.LayerData::layers,
                    RockLayerSettings.LayerData::new
            ).apply(ByteBufCodecs.list()), RockLayerSettings.Data::layers,
            ImplAPI.STRING_LIST_CODEC, RockLayerSettings.Data::oceanFloor,
            ImplAPI.STRING_LIST_CODEC, RockLayerSettings.Data::land,
            ImplAPI.STRING_LIST_CODEC, RockLayerSettings.Data::volcanic,
            ImplAPI.STRING_LIST_CODEC, RockLayerSettings.Data::uplift,
            RockLayerSettings.Data::new
    ).map(d -> RockLayerSettings.decode(d).getOrThrow(), r -> r.data);
    public static final StreamCodec<RegistryFriendlyByteBuf, Settings> SETTINGS_CODEC = StreamCodec.of(
            (b, s) -> {
                b.writeBoolean(s.flatBedrock());
                b.writeVarInt(s.spawnDistance());
                b.writeVarInt(s.spawnCenterX());
                b.writeVarInt(s.spawnCenterZ());
                b.writeVarInt(s.temperatureScale());
                b.writeFloat(s.temperatureConstant());
                b.writeVarInt(s.rainfallScale());
                b.writeFloat(s.rainfallConstant());
                ROCK_LAYER_SETTINGS_CODEC.encode(b, s.rockLayerSettings());
                b.writeFloat(s.continentalness());
                b.writeFloat(s.grassDensity());
                b.writeBoolean(s.finiteContinents());
            },
            b -> new Settings(
                    b.readBoolean(),
                    b.readVarInt(),
                    b.readVarInt(),
                    b.readVarInt(),
                    b.readVarInt(),
                    b.readFloat(),
                    b.readVarInt(),
                    b.readFloat(),
                    ROCK_LAYER_SETTINGS_CODEC.decode(b),
                    b.readFloat(),
                    b.readFloat(),
                    b.readBoolean()
            )
    );
}
