package com.notenoughmail.tfcgenviewer.util;

import com.notenoughmail.tfcgenviewer.config.ServerConfig;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public class Permissions {


    /**
     * 128 - Climate (rain & temp)
     * <p>
     * 64 - Rock(Type)s
     * <p>
     * 32 - Biomes & Inland Height
     * <p>
     * 16 - Biome Altitude & Rivers and Mountains
     * <p>
     * 8 - Unused
     * <p>
     * 4 - May see coordinates
     * <p>
     * 2 - Seed visible
     * <p>
     * 1 - Can export
     */
    public static final byte full = (byte) 0b11111111;
    public static final byte none = (byte) 0;

    public static byte get(ServerPlayer player) {
        return ServerConfig.viewPermission.get().getPermission(player);
    }

    public static boolean isEmpty(byte permissions) {
        return (permissions & 0b11110000) == 0;
    }

    public static boolean canExport(byte permissions) {
        return (permissions & 1) != 0;
    }

    public static boolean canSeeSeed(byte permissions) {
        return (permissions & 2) != 0;
    }

    public static boolean canSeeCoordinates(byte permissions) {
        return (permissions & 4) != 0;
    }

    public enum Universal {
        SEED_COMMAND((p, b) -> {
            if (p.hasPermissions(2)) {
                b |= full;
            }
            return b;
        }),
        BY_CATEGORY((p, b) -> {
            for (var entry : ServerConfig.permissionsByType.entrySet()) {
                final Category category = entry.getKey();
                final Type type = entry.getValue().get();

                if (type == Type.ALWAYS) {
                    b |= category.overlay;
                } else if (type == Type.SEED_COMMAND && p.hasPermissions(2)) {
                    b |= category.overlay;
                } else if (type == Type.ALLOW_LIST && PermissionHolder.get(p).isPresent(category, p)) {
                    b |= category.overlay;
                } else if (type == Type.DENY_LIST && !PermissionHolder.get(p).isPresent(category, p)) {
                    b |= category.overlay;
                }
            }
            return b;
        }),
        NEVER((p, b) -> b),
        ALWAYS(full, (p, b) -> b),
        ALLOW_LIST((p, b) -> {
            if (PermissionHolder.get(p).isPresent(Category.universal, p)) {
                return full;
            }
            return b;
        }),
        DENY_LIST(full, (p, b) -> {
            if (PermissionHolder.get(p).isPresent(Category.universal, p)) {
                return none;
            }
            return b;
        });

        private final byte base;
        private final PermissionApplicator permissions;

        Universal(PermissionApplicator permissions) {
            this(none, permissions);
        }

        Universal(byte base, PermissionApplicator permissions) {
            this.base = base;
            this.permissions = permissions;
        }

        public byte getPermission(ServerPlayer player) {
            return permissions.apply(player, base);
        }
    }

    @FunctionalInterface
    interface PermissionApplicator {
        byte apply(ServerPlayer player, byte base);
    }

    public enum Type {
        SEED_COMMAND,
        ALLOW_LIST,
        DENY_LIST,
        NEVER,
        ALWAYS
    }

    public enum Category {
        universal(0, (String) null),
        export(1, s -> " The permission type the player requires in order to export the preview"),
        seed(2, s -> " The permission type the player requires for the world seed to be seen in the preview"),
        coords(4, s -> " The permission type the player requires in order to view the world coordinates in the preview"),
        climate(128, "rainfall and temperature"),
        rocks(64, "rock and rock type"),
        biomes(32, "biome and inland height"),
        rivers(16, "river and biome altitude");

        public static final Category[] VALUES = values();

        public final byte overlay;
        public final UnaryOperator<String> configInfo;

        Category(int overlay, String info) {
            this.overlay = (byte) overlay;
            configInfo = s -> s.formatted(info);
        }

        Category(int overlay, UnaryOperator<String> info) {
            this.overlay = (byte) overlay;
            configInfo = info;
        }

        @Nullable
        public static Category get(String name) {
            try {
                return valueOf(name);
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
