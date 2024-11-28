package com.notenoughmail.tfcgenviewer.util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.config.ServerConfig;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Arrays;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TFCGVCommands {

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                literal(TFCGenViewer.ID).requires(s -> s.hasPermission(3))
                        .then(literal("set")
                                .then(argument("type", EnumArgument.enumArgument(TypeTarget.class))
                                        .then(argument("value", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    final var enumClazz = ctx.getArgument("type", TypeTarget.class).clazz.get();
                                                    return SharedSuggestionProvider.suggest(Arrays.stream(enumClazz.getEnumConstants()).map(Enum::name), builder);
                                                }).executes(ctx -> {
                                                    final TypeTarget target = ctx.getArgument("type", TypeTarget.class);
                                                    final String val = StringArgumentType.getString(ctx, "value");
                                                    final Enum<?> value;
                                                    try {
                                                        value = Enum.valueOf(cast(target.clazz.get()), val);
                                                    } catch (IllegalArgumentException ignored) {
                                                        throw new SimpleCommandExceptionType(Component.translatable("tfcgenviewer.commands.set.fail", val, target.name())).create();
                                                    }
                                                    target.config.get().set(cast(value));
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.commands.set.success", target.name(), value.name()), true);
                                                    return 1;
                                                }))
                                )
                        )
                        .then(literal("get")
                                .then(argument("type", EnumArgument.enumArgument(TypeTarget.class))
                                        .executes(ctx -> {
                                            final TypeTarget target = ctx.getArgument("type", TypeTarget.class);
                                            ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.commands.get", target.name(), target.config.get().get().name()), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("add")
                                .then(argument("category", EnumArgument.enumArgument(Permissions.Category.class))
                                        .then(argument("player", GameProfileArgument.gameProfile())
                                                .suggests((ctx, builder) -> {
                                                    final PlayerList playerList = ctx.getSource().getServer().getPlayerList();
                                                    return SharedSuggestionProvider.suggest(
                                                            playerList.getPlayers().stream().filter(p ->
                                                                    !PermissionHolder.isPlayerPresent(p, ctx.getArgument("category", Permissions.Category.class))
                                                            ).map(p -> p.getGameProfile().getName()), builder);
                                                }).executes(ctx -> {
                                                    final PermissionHolder holder = PermissionHolder.get(ctx.getSource().getServer());
                                                    final Permissions.Category category = ctx.getArgument("category", Permissions.Category.class);
                                                    int i = 0;
                                                    for (GameProfile profile : GameProfileArgument.getGameProfiles(ctx, "player")) {
                                                        if (!holder.isPresent(category, profile)) {
                                                            holder.add(category, profile);
                                                            ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.commands.add.success", ComponentUtils.getDisplayName(profile), category.name()), true);
                                                            i++;
                                                        }
                                                    }
                                                    if (i == 0) {
                                                        throw new SimpleCommandExceptionType(Component.translatable("tfcgenviewer.commands.add.fail", category.name())).create();
                                                    }
                                                    return i;
                                                })
                                        )
                                )
                        )
                        .then(literal("remove")
                                .then(argument("category", EnumArgument.enumArgument(Permissions.Category.class))
                                        .then(argument("player", GameProfileArgument.gameProfile())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                        PermissionHolder
                                                                .get(ctx.getSource().getServer())
                                                                .get(ctx.getArgument("category", Permissions.Category.class))
                                                                .stream()
                                                                .map(GameProfile::getName),
                                                        builder
                                                )).executes(ctx -> {
                                                    final PermissionHolder holder = PermissionHolder.get(ctx.getSource().getServer());
                                                    final Permissions.Category category = ctx.getArgument("category", Permissions.Category.class);
                                                    int i = 0;
                                                    for (GameProfile profile : GameProfileArgument.getGameProfiles(ctx, "player")) {
                                                        if (holder.isPresent(category, profile)) {
                                                            holder.remove(category, profile);
                                                            ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.commands.remove.success", ComponentUtils.getDisplayName(profile), category.name()), true);
                                                            i++;
                                                        }
                                                    }
                                                    if (i == 0) {
                                                        throw new SimpleCommandExceptionType(Component.translatable("tfcgenviewer.commands.remove.fail", category.name())).create();
                                                    }
                                                    return i;
                                                })
                                        )
                                )
                        )
        );
    }

    public enum TypeTarget {
        view_permission(() -> Permissions.Universal.class, () -> ServerConfig.viewPermission),
        export(Permissions.Category.export),
        seed(Permissions.Category.seed),
        coords(Permissions.Category.coords),
        climate(Permissions.Category.climate),
        rocks(Permissions.Category.rocks),
        biomes(Permissions.Category.biomes),
        rivers(Permissions.Category.rivers);

        private final Supplier<Class<? extends Enum<?>>> clazz;
        private final Supplier<ForgeConfigSpec.EnumValue<?>> config;

        <T extends Enum<T>> TypeTarget(Supplier<Class<T>> clazz, Supplier<ForgeConfigSpec.EnumValue<T>> config) {
            this.clazz = cast(clazz);
            this.config = cast(config);
        }

        TypeTarget(Permissions.Category category) {
            this(() -> Permissions.Type.class, () -> ServerConfig.permissionsByType.get(category));
        }
    }

    private static <T> T cast(Object o) {
        return (T) o;
    }
}
