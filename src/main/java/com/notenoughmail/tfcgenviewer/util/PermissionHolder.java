package com.notenoughmail.tfcgenviewer.util;

import com.mojang.authlib.GameProfile;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class PermissionHolder extends SavedData {

    private static final String NAME = TFCGenViewer.ID + "_permissions";

    public static PermissionHolder get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(PermissionHolder::load, PermissionHolder::new, NAME);
    }

    public static PermissionHolder get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PermissionHolder::load, PermissionHolder::new, NAME);
    }

    public static PermissionHolder get(ServerPlayer player) {
        return player.level().getServer().overworld().getDataStorage().computeIfAbsent(PermissionHolder::load, PermissionHolder::new, NAME);
    }

    public static boolean isPlayerPresent(ServerPlayer player, Permissions.Category category) {
        return get(player).isPresent(category, player);
    }

    private final EnumMap<Permissions.Category, Set<GameProfile>> perms;

    private PermissionHolder() {
        perms = new EnumMap<>(Permissions.Category.class);
        for (Permissions.Category category : Permissions.Category.VALUES) {
            perms.put(category, new HashSet<>());
        }
    }

    public boolean isPresent(Permissions.Category category, ServerPlayer player) {
        return isPresent(category, player.getGameProfile());
    }

    public boolean isPresent(Permissions.Category category, GameProfile profile) {
        return perms.get(category).contains(profile);
    }

    public void add(Permissions.Category category, GameProfile profile) {
        if (perms.get(category).add(profile)) {
            setDirty();
        }
    }

    public void remove(Permissions.Category category, GameProfile profile) {
        if (perms.get(category).remove(profile)) {
            setDirty();
        }
    }

    public Set<GameProfile> get(Permissions.Category category) {
        return perms.get(category);
    }

    private static PermissionHolder load(CompoundTag nbt) {
        final PermissionHolder holder = new PermissionHolder();
        nbt.getAllKeys().forEach(name -> {
            final Permissions.Category category = Permissions.Category.get(name);
            if (category != null) {
                add(nbt.getList(name, Tag.TAG_COMPOUND), holder.perms.get(category));
            }
        });
        return holder;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        perms.forEach((cat, profiles) -> tag.put(cat.name(), save(profiles)));
        return tag;
    }

    private static void add(ListTag nbt, Set<GameProfile> profiles) {
        nbt.forEach(tag -> {
            if (tag instanceof CompoundTag obj) {
                profiles.add(new GameProfile(
                        obj.hasUUID("uuid") ? obj.getUUID("uuid") : null,
                        obj.getString("name")
                ));
            }
        });
    }

    private static ListTag save(Set<GameProfile> profiles) {
        final ListTag tag = new ListTag();
        profiles.forEach(profile -> {
            final CompoundTag obj = new CompoundTag();
            if (profile.getId() != null) {
                obj.putUUID("uuid", profile.getId());
            }
            obj.putString("name", profile.getName() != null ? profile.getName() : "Unknown");
            tag.add(obj);
        });
        return tag;
    }
}
