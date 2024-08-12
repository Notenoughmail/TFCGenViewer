package com.notenoughmail.tfcgenviewer.util;

import com.notenoughmail.tfcgenviewer.config.ServerConfig;
import net.minecraft.server.level.ServerPlayer;

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
    public static final byte full = (byte) 0b11110111;

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
            return b; // Unimplemented
        }),
        DISALLOWED((p, b) -> b),
        ALWAYS(full, (p, b) -> b),
        ALLOW_LIST((p, b) -> {
            return b; // Unimplemented
        }),
        DENY_LIST(full, (p, b) -> {
            return b; // Unimplemented
        });

        private final byte base;
        private final PermissionApplicator permissions;

        Universal(PermissionApplicator permissions) {
            this((byte) 0, permissions);
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

    public enum Category {
        SEED_COMMAND,
        ALLOWLIST,
        DENYLIST,
        NEVER,
        ALWAYS
    }
}
