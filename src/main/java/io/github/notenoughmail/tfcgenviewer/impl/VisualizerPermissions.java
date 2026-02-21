package io.github.notenoughmail.tfcgenviewer.impl;

import com.mojang.authlib.GameProfile;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class VisualizerPermissions extends SavedData {

    private static final String NAME = TFCGenViewer.ID + "_visualizer_permissions";
    private static final Factory<VisualizerPermissions> FACTORY = new Factory<>(VisualizerPermissions::new, VisualizerPermissions::load);

    private static final String
            GLOBAL_DENY = "global_deny",
            GLOBAL_PERMISSIONS = "global_permissions",
            INDIVIDUAL_PERMISSIONS = "individual_permissions",
            ANCILLARY_DENY = "ancillary_deny",
            ANCILLARY = "ancillary",
            INDIVIDUAL_ANCILLARY = "individual_ancillary"
            ;

    private static final BiConsumer<IVisualizerType<?, ?, ?, ?>, Consumer<String>> VIZ_SERIALIZER = (v, c) -> c.accept(GenViewerAPI.VISUALIZER_REGISTRY.getKey(v).toString());
    private static final BiConsumer<String, Consumer<IVisualizerType<?, ?, ?, ?>>> VIZ_DESERIALIZER = (s, c) -> {
        final ResourceLocation id = ResourceLocation.tryParse(s);
        if (id != null) {
            final IVisualizerType<?, ?, ?, ?> viz = GenViewerAPI.VISUALIZER_REGISTRY.get(id);
            if (viz != null) {
                c.accept(viz);
            }
        }
    };
    private static final Function<CompoundTag, GameProfile> PROFILE_DESERIALIZER = c -> new GameProfile(
            c.getUUID("uuid"),
            c.getString("name")
    );
    private static final Function<GameProfile, CompoundTag> PROFILE_SERIALIZER = p -> {
        final CompoundTag c = new CompoundTag();
        c.putUUID("uuid", p.getId());
        c.putString("name", p.getName());
        return c;
    };

    public static ForPlayer forPlayer(ServerPlayer player) {
        return new ForPlayer(player, get(player.serverLevel()), !(player.getServer() instanceof DedicatedServer));
    }

    public static VisualizerPermissions get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    private static VisualizerPermissions load(CompoundTag nbt, HolderLookup.Provider provider) {
        final VisualizerPermissions permissions = new VisualizerPermissions();

        if (nbt.contains("disabled") && nbt.getBoolean("disabled")) permissions.disabled = true;

        if (nbt.contains(GLOBAL_DENY)) {
            final ListTag list = nbt.getList(GLOBAL_DENY, Tag.TAG_STRING);
            for (int i = 0 ; i < list.size() ; i++) {
                VIZ_DESERIALIZER.accept(list.getString(i), permissions.globalDeny::add);
            }
        }

        if (nbt.contains(GLOBAL_PERMISSIONS)) {
            final CompoundTag global = nbt.getCompound(GLOBAL_PERMISSIONS);
            for (String k : global.getAllKeys()) {
                VIZ_DESERIALIZER.accept(k, v -> permissions.globalPermissions.put(v, global.getBoolean(k)));
            }
        }

        if (nbt.contains(INDIVIDUAL_PERMISSIONS)) {
            final ListTag individual = nbt.getList(INDIVIDUAL_PERMISSIONS, Tag.TAG_COMPOUND);
            individual.forEach(tag -> {
                if (tag instanceof CompoundTag c) {
                    final CompoundTag perms = c.getCompound("permissions");
                    final Map<IVisualizerType<?, ?, ?, ?>, Boolean> map = new IdentityHashMap<>();
                    for (String k : perms.getAllKeys()) {
                        VIZ_DESERIALIZER.accept(k, v -> map.put(v, perms.getBoolean(k)));
                    }
                    if (!map.isEmpty()) {
                        final GameProfile profile = PROFILE_DESERIALIZER.apply(c.getCompound("profile"));
                        permissions.individualPermissions.put(profile, map);
                    }
                }
            });
        }

        permissions.globalAncillaryDeny = nbt.getByte(ANCILLARY_DENY);
        permissions.globalAncillary = nbt.getByte(ANCILLARY);

        if (nbt.contains(INDIVIDUAL_ANCILLARY)) {
            final ListTag list = nbt.getList(INDIVIDUAL_ANCILLARY, Tag.TAG_COMPOUND);
            list.forEach(t -> {
                if (t instanceof CompoundTag c) {
                    permissions.individualAncillaries.put(
                            PROFILE_DESERIALIZER.apply(c.getCompound("profile")),
                            c.getByte("permissions")
                    );
                }
            });
        }

        return permissions;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("disabled", disabled);
        if (!globalDeny.isEmpty()) {
            final ListTag list = new ListTag(globalDeny.size());
            globalDeny.forEach(v -> VIZ_SERIALIZER.accept(v, TFCGenViewer.transformConsumer(StringTag::valueOf, list::add)));
            tag.put(GLOBAL_DENY, list);
        }
        if (!globalPermissions.isEmpty()) {
            final CompoundTag c = new CompoundTag();
            globalPermissions.forEach((v, b) -> VIZ_SERIALIZER.accept(v, s -> c.putBoolean(s, b)));
            tag.put(GLOBAL_PERMISSIONS, c);
        }
        if (!individualPermissions.isEmpty()) {
            final ListTag list = new ListTag();
            individualPermissions.forEach((p, m) -> {
                if (!m.isEmpty()) {
                    final CompoundTag c = new CompoundTag();
                    c.put("profile", PROFILE_SERIALIZER.apply(p));
                    final CompoundTag permissions = new CompoundTag();
                    m.forEach((v, b) -> VIZ_SERIALIZER.accept(v, s -> permissions.putBoolean(s, b)));
                    c.put("permissions", permissions);
                    list.add(c);
                }
            });
            tag.put(INDIVIDUAL_PERMISSIONS, list);
        }
        tag.putByte(ANCILLARY_DENY, globalAncillaryDeny);
        tag.putByte(ANCILLARY, globalAncillary);
        if (!individualAncillaries.isEmpty()) {
            final ListTag list = new ListTag();
            individualAncillaries.forEach((p, b) -> {
                final CompoundTag c = new CompoundTag();
                c.put("profile", PROFILE_SERIALIZER.apply(p));
                c.putByte("permissions", b);
                list.add(c);
            });
            tag.put(INDIVIDUAL_ANCILLARY, list);
        }
        return tag;
    }

    public boolean isAllowed(ServerPlayer player, IVisualizerType<?, ?, ?, ?> visualizerType, boolean isIntegratedServer) {
        return isAllowed(player.getGameProfile(), visualizerType, isIntegratedServer);
    }

    public boolean isAllowed(GameProfile profile, IVisualizerType<?, ?, ?, ?> visualizerType, boolean isIntegratedServer) {
        if (disabled) return true;
        if (globalDeny.contains(visualizerType)) {
            return false;
        }
        final Map<IVisualizerType<?, ?, ?, ?>, Boolean> individual = individualPermissions.get(profile);
        if (individual != null && individual.containsKey(visualizerType)) {
            return individual.get(visualizerType);
        }
        return globalPermissions.getOrDefault(visualizerType, isIntegratedServer);
    }

    public boolean isAllowed(ServerPlayer player, AncillaryPermission ancillary) {
        return isAllowed(player.getGameProfile(), ancillary);
    }

    public boolean isAllowed(GameProfile profile, AncillaryPermission ancillary) {
        if (disabled) return true;
        if (ancillary.test(globalAncillaryDeny)) {
            return false;
        }
        final byte permission = individualAncillaries.getOrDefault(profile, globalAncillary);
        return ancillary.test(permission);
    }

    public void unconditionallyDeny(IVisualizerType<?, ?, ?, ?> visualizerType) {
        globalDeny.add(visualizerType);
        setDirty();
    }

    public void conditionallyAllow(IVisualizerType<?, ?, ?, ?> visualizerType) {
        globalDeny.remove(visualizerType);
        setDirty();
    }

    public void unconditionallyDeny(AncillaryPermission ancillary) {
        globalAncillaryDeny |= ancillary.key;
        setDirty();
    }

    public void conditionallyAllow(AncillaryPermission ancillary) {
        unconditionallyDeny(ancillary); // Ensure the permission was denied before xoring
        globalAncillaryDeny ^= ancillary.key;
        setDirty();
    }

    public void setGlobal(IVisualizerType<?, ?, ?, ?> visualizerType, boolean allowed) {
        globalPermissions.put(visualizerType, allowed);
        setDirty();
    }

    public void setGlobal(AncillaryPermission ancillary, boolean allowed) {
        if (allowed) {
            globalAncillary |= ancillary.key;
        } else {
            globalAncillary ^= ancillary.key;
        }
        setDirty();
    }

    public void overrideIndividual(GameProfile profile, IVisualizerType<?, ?, ?, ?> visualizerType, boolean allowed) {
        individualPermissions.computeIfAbsent(profile, p -> new IdentityHashMap<>()).put(visualizerType, allowed);
        setDirty();
    }

    public void removeIndividual(GameProfile profile, IVisualizerType<?, ?, ?, ?> visualizerType) {
        final Map<IVisualizerType<?, ?, ?, ?>, Boolean> individual = individualPermissions.get(profile);
        if (individual != null) {
            individual.remove(visualizerType);
            setDirty();
        }
    }

    public void overrideIndividual(GameProfile profile, boolean spawn, boolean export, boolean coords) {
        byte permission = 0;
        if (spawn) {
            permission |= AncillaryPermission.SPAWN.key;
        }
        if (export) {
            permission |= AncillaryPermission.EXPORT.key;
        }
        if (coords) {
            permission |= AncillaryPermission.COORDS.key;
        }
        individualAncillaries.put(profile, permission);
        setDirty();
    }

    public void removeIndividual(GameProfile profile) {
        if (individualAncillaries.remove(profile) != null) setDirty();
    }

    public void disable() {
        if (!disabled) setDirty();
        disabled = true;
    }

    public void enable() {
        if (disabled) setDirty();
        disabled = false;
    }

    public boolean disabled() {
        return disabled;
    }

    private final Set<IVisualizerType<?, ?, ?, ?>> globalDeny = new HashSet<>();
    private byte globalAncillaryDeny = 0;

    private final Map<IVisualizerType<?, ?, ?, ?>, Boolean> globalPermissions = new IdentityHashMap<>();
    private byte globalAncillary = 0;

    private final Map<GameProfile, Map<IVisualizerType<?, ?, ?, ?>, Boolean>> individualPermissions = new HashMap<>();
    private final Map<GameProfile, Byte> individualAncillaries = new HashMap<>();

    private boolean disabled = false;

    public record ForPlayer(ServerPlayer player, VisualizerPermissions permissions, boolean isIntegratedServer) implements Predicate<IVisualizerType<?, ?, ?, ?>> {

        @Override
        public boolean test(IVisualizerType<?, ?, ?, ?> visualizerType) {
            return permissions.isAllowed(player, visualizerType, isIntegratedServer);
        }

        public boolean mayDrawSpawn() {
            return permissions.isAllowed(player, AncillaryPermission.SPAWN);
        }

        public boolean mayExport() {
            return permissions.isAllowed(player, AncillaryPermission.EXPORT);
        }

        public boolean maySeeCoords() {
            return permissions.isAllowed(player, AncillaryPermission.COORDS);
        }
    }

    public enum AncillaryPermission {
        SPAWN,
        EXPORT,
        COORDS
        ;

        final byte key = (byte) (1 << ordinal());

        public boolean test(byte store) {
            return (store & key) != 0;
        }
    }
}
