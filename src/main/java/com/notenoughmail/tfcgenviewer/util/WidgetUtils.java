package com.notenoughmail.tfcgenviewer.util;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class WidgetUtils {

    public static final ResourceLocation GUI_ELEMENTS = TFCGenViewer.identifier("textures/gui/common_gui_elements.png");

    // Disgusting, but works the whole two times its needed
    public static <O, I> List<O> wrapList(List<I> list, BiFunction<I, Boolean, O> mapper, int index) {
        if (index == -1) {
            return List.of();
        } else if (list.size() < 6) {
            final List<O> out = new ArrayList<>(list.size());
            for (int i = 0 ; i < list.size() ; i++) {
                O o = mapper.apply(list.get(i), i == index);
                out.add(o);
            }
            return out;
        } else if (index == 0) {
            return List.of(
                    mapper.apply(list.get(list.size() - 2), false),
                    mapper.apply(list.get(list.size() - 1), false),
                    mapper.apply(list.get(0), true),
                    mapper.apply(list.get(1), false),
                    mapper.apply(list.get(2), false)
            );
        } else if (index == 1) {
            return List.of(
                    mapper.apply(list.get(list.size() - 1), false),
                    mapper.apply(list.get(0), false),
                    mapper.apply(list.get(1), true),
                    mapper.apply(list.get(2), false),
                    mapper.apply(list.get(3), false)
            );
        } else if (index == list.size() - 1) {
            return List.of(
                    mapper.apply(list.get(index - 2), false),
                    mapper.apply(list.get(index - 1), false),
                    mapper.apply(list.get(index), true),
                    mapper.apply(list.get(0), false),
                    mapper.apply(list.get(1), false)
            );
        } else if (index == list.size() - 2) {
            return List.of(
                    mapper.apply(list.get(index - 2), false),
                    mapper.apply(list.get(index - 1), false),
                    mapper.apply(list.get(index), true),
                    mapper.apply(list.get(index + 1), false),
                    mapper.apply(list.get(0), false)
            );
        } else {
            return List.of(
                    mapper.apply(list.get(index - 2), false),
                    mapper.apply(list.get(index - 1), false),
                    mapper.apply(list.get(index), true),
                    mapper.apply(list.get(index + 1), false),
                    mapper.apply(list.get(index + 2), false)
            );
        }
    }

    @Nullable
    public static ItemStack getBlockStack(@Nullable Block block) {
        if (block == null) return null;

        if (block instanceof LiquidBlock liquidBlock) {
            final Item bucket = liquidBlock.getFluid().getSource().getBucket();
            if (bucket != null && bucket != Items.AIR) {
                return bucket.getDefaultInstance();
            }
        }
        try {
            final Item item = block.asItem(); // The inserted forge call for extensions can throw an error, joy
            if (item != null && item != Items.AIR) {
                return item.getDefaultInstance();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public static void renderBlock(@Nullable Block block, int x, int y, GuiGraphics graphics, Font font, int right, @Nullable Component textIfBlockIsNull) {
        assert block != null || textIfBlockIsNull != null : "If a block is null, the text to render in such case should not be null!";
        if (block != null) {
            @Nullable final ItemStack stack = getBlockStack(block);
            if (stack != null) {
                graphics.renderFakeItem(stack, x, y + 2);
            } else {
                GuiElement.UNKNOWN.render(graphics, x - 2, y);
            }

            AbstractWidget.renderScrollingString(graphics, font, block.getName(), x + 22, y + 5, right, y + 17, 0xFFFFFFFF);
        } else {
            AbstractWidget.renderScrollingString(graphics, font, textIfBlockIsNull, x + 2, y + 5, right - 2, y + 17, 0xFFFFFFFF);
        }
    }
}
