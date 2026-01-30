package io.github.notenoughmail.tfcgenviewer.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.*;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TFCGVCommands {

    private static final Class<VisualizerPermissions.AncillaryPermission> ANCILLARY = VisualizerPermissions.AncillaryPermission.class;

    public static void registerCommands(RegisterCommandsEvent event) {
        final CommandBuildContext buildCtx = event.getBuildContext();
        event.getDispatcher().register(
                literal(TFCGenViewer.ID).requires(s -> s.hasPermission(3))
                        .then(literal("deny_ancillary")
                                .then(argument("ancillary_type", EnumArgument.enumArgument(ANCILLARY))
                                        .executes(ctx -> {
                                            final VisualizerPermissions.AncillaryPermission ancillary = ctx.getArgument("ancillary_type", ANCILLARY);
                                            VisualizerPermissions.get(ctx.getSource().getLevel()).unconditionallyDeny(ancillary);
                                            ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.deny_ancillary", ancillary.name()), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("allow_ancillary")
                                .then(argument("ancillary_type", EnumArgument.enumArgument(ANCILLARY))
                                        .executes(ctx -> {
                                            final VisualizerPermissions.AncillaryPermission ancillary = ctx.getArgument("ancillary_type", ANCILLARY);
                                            VisualizerPermissions.get(ctx.getSource().getLevel()).conditionallyAllow(ancillary);
                                            ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.allow_ancillary", ancillary.name()), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("set_ancillary")
                                .then(argument("ancillary_type", EnumArgument.enumArgument(ANCILLARY))
                                        .then(allowed()
                                                .executes(ctx -> {
                                                    final VisualizerPermissions.AncillaryPermission ancillary = ctx.getArgument("ancillary_type", ANCILLARY);
                                                    final boolean allowed = getAllowed(ctx);
                                                    VisualizerPermissions.get(ctx.getSource().getLevel()).setGlobal(ancillary, allowed);
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.set_ancillary", ancillary.name(), String.valueOf(allowed)), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(literal("set_individual_ancillary")
                                .then(argument("player", GameProfileArgument.gameProfile())
                                        .then(argument("can_draw_spawn", BoolArgumentType.bool())
                                                .then(argument("can_export_images", BoolArgumentType.bool())
                                                        .then(argument("can_see_coordinates", BoolArgumentType.bool())
                                                                .executes(ctx -> {
                                                                    final Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                                                                    final boolean spawn = BoolArgumentType.getBool(ctx, "can_draw_spawn"),
                                                                            export = BoolArgumentType.getBool(ctx, "can_export_images"),
                                                                            coords = BoolArgumentType.getBool(ctx, "can_see_coordinates");
                                                                    final VisualizerPermissions permissions = VisualizerPermissions.get(ctx.getSource().getLevel());
                                                                    profiles.forEach(p -> {
                                                                        permissions.overrideIndividual(p, spawn, export, coords);
                                                                        ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.individual_ancillary", String.valueOf(spawn), String.valueOf(export), String.valueOf(coords), p.getName()), true);
                                                                    });
                                                                    return profiles.size();
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("remove_individual_ancillary")
                                .then(argument("player", GameProfileArgument.gameProfile())
                                        .executes(ctx -> {
                                            final Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                                            final VisualizerPermissions permissions = VisualizerPermissions.get(ctx.getSource().getLevel());
                                            profiles.forEach(p -> {
                                                permissions.removeIndividual(p);
                                                ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.remove_individual_ancillary", p.getName()), true);
                                            });
                                            return profiles.size();
                                        })
                                )
                        )
                        .then(literal("deny_visualizer_type")
                                .then(viz(buildCtx)
                                        .executes(ctx -> {
                                            final Holder<IVisualizerType<?, ?, ?, ?>> viz = getViz(ctx);
                                            VisualizerPermissions.get(ctx.getSource().getLevel()).unconditionallyDeny(viz.value());
                                            ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.deny_visualizer_type", id(viz)), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("allow_visualizer_type")
                                .then(viz(buildCtx)
                                        .executes(ctx -> {
                                            final Holder<IVisualizerType<?, ?, ?, ?>> viz = getViz(ctx);
                                            VisualizerPermissions.get(ctx.getSource().getLevel()).conditionallyAllow(viz.value());
                                            ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.allow_visualizer_type", id(viz)), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("set_visualizer_type")
                                .then(viz(buildCtx)
                                        .then(allowed()
                                                .executes(ctx -> {
                                                    final Holder<IVisualizerType<?, ?, ?, ?>> viz = getViz(ctx);
                                                    final boolean allowed = getAllowed(ctx);
                                                    VisualizerPermissions.get(ctx.getSource().getLevel()).setGlobal(viz.value(), allowed);
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.set_visualizer_type", id(viz), String.valueOf(allowed)), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(literal("set_individual_visualizer_type")
                                .then(argument("player", GameProfileArgument.gameProfile())
                                        .then(viz(buildCtx)
                                                .then(allowed()
                                                        .executes(ctx -> {
                                                            final Holder<IVisualizerType<?, ?, ?, ?>> viz = getViz(ctx);
                                                            final boolean allowed = getAllowed(ctx);
                                                            final Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                                                            final VisualizerPermissions permissions = VisualizerPermissions.get(ctx.getSource().getLevel());
                                                            profiles.forEach(p -> {
                                                                permissions.overrideIndividual(p, viz.value(), allowed);
                                                                ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.individual_visualizer_type", id(viz), allowed, p.getName()), true);
                                                            });
                                                            return profiles.size();
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(literal("remove_individual_visualizer_type")
                                .then(argument("player", GameProfileArgument.gameProfile())
                                        .then(viz(buildCtx)
                                                .executes(ctx -> {
                                                    final Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                                                    final Holder<IVisualizerType<?, ?, ?, ?>> viz = getViz(ctx);
                                                    final VisualizerPermissions permissions = VisualizerPermissions.get(ctx.getSource().getLevel());
                                                    profiles.forEach(p -> {
                                                        permissions.removeIndividual(p, viz.value());
                                                        ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.remove_individual_visualizer_type", id(viz), p.getName()), true);
                                                    });
                                                    return profiles.size();
                                                })
                                        )
                                )
                        )
                        .then(literal("describe_visualizer_type")
                                .then(viz(buildCtx)
                                        .executes(ctx -> {
                                            final Holder<IVisualizerType<?, ?, ?, ?>> viz = getViz(ctx);
                                            ctx.getSource().sendSuccess(() -> Component.translatable("tfcgenviewer.command.describe_visualizer_type", viz.value().name(), id(viz)), false);
                                            ctx.getSource().sendSuccess(viz.value()::description, false);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("query_permissions")
                                .then(argument("player", GameProfileArgument.gameProfile())
                                        .executes(ctx -> {
                                            final Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                                            final VisualizerPermissions permissions = VisualizerPermissions.get(ctx.getSource().getLevel());
                                            profiles.forEach(p -> {
                                                final Set<IVisualizerType<?, ?, ?, ?>> visualizers = GenViewerAPI.VISUALIZER_REGISTRY.stream()
                                                        .filter(v -> permissions.isAllowed(p, v))
                                                        .collect(Collectors.toSet());
                                                final MutableComponent response = Component.translatable("tfcgenviewer.command.query_permissions.base", p.getName())
                                                        .append(CommonComponents.NEW_LINE)
                                                        .append(Component.translatable(
                                                                "tfcgenviewer.command.query_permissions.ancillaries",
                                                                String.valueOf(permissions.isAllowed(p, VisualizerPermissions.AncillaryPermission.SPAWN)),
                                                                String.valueOf(permissions.isAllowed(p, VisualizerPermissions.AncillaryPermission.EXPORT)),
                                                                String.valueOf(permissions.isAllowed(p, VisualizerPermissions.AncillaryPermission.COORDS))
                                                        ))
                                                        .append(CommonComponents.NEW_LINE);
                                                if (visualizers.isEmpty()) {
                                                    response.append(Component.translatable("tfcgenviewer.command.query_permissions.no_visualizers"));
                                                } else {
                                                    final ChatFormatting[] list = { ChatFormatting.DARK_GREEN, ChatFormatting.GREEN };
                                                    final int[] index = { 0 };
                                                    response.append(CommonComponents.NEW_LINE)
                                                            .append(Component.translatable("tfcgenviewer.command.query_permissions.visualizer_heading"));
                                                    for (IVisualizerType<?, ?, ?, ?> v : visualizers) {
                                                        final String id = GenViewerAPI.VISUALIZER_REGISTRY.getKey(v).toString();
                                                        response.append(CommonComponents.NEW_LINE).append(
                                                                Component.translatable(
                                                                        "tfcgenviewer.command.query_permissions.visualizer_entry",
                                                                        v.name(),
                                                                        id
                                                                ).withStyle(s -> s.withClickEvent(new ClickEvent(
                                                                        ClickEvent.Action.RUN_COMMAND,
                                                                        "/tfcgenviewer describe_visualizer_type %s".formatted(id)
                                                                )).withHoverEvent(new HoverEvent(
                                                                        HoverEvent.Action.SHOW_TEXT,
                                                                        Component.translatable("tfcgenviewer.command.query_permissions.click_to_describe", id)
                                                                )).withColor(list[index[0]++ % 2]))
                                                        );
                                                    }
                                                }
                                                ctx.getSource().sendSuccess(() -> response, true);
                                            });
                                            return profiles.size();
                                        })
                                )
                        )
        );
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Boolean> allowed() {
        return argument("allowed", BoolArgumentType.bool());
    }

    private static boolean getAllowed(CommandContext<CommandSourceStack> ctx) {
        return BoolArgumentType.getBool(ctx, "allowed");
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Holder.Reference<IVisualizerType<?, ?, ?, ?>>> viz(CommandBuildContext ctx) {
        return argument("visualizer_type", ResourceArgument.resource(ctx, GenViewerAPI.VISUALIZER));
    }

    private static Holder<IVisualizerType<?, ?, ?, ?>> getViz(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ResourceArgument.getResource(ctx, "visualizer_type", GenViewerAPI.VISUALIZER);
    }
    
    private static String id(Holder<?> holder) {
        return holder.getKey().location().toString();
    }
}
