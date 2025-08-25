package com.notenoughmail.tfcgenviewer.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

public class WidgetUtils {

    // Disgusting, but works the whole two times its needed
    // Returns an array with a length in the range [0, 5]
    public static <O, I> O[] wrapList(List<I> list, BiFunction<I, Boolean, O> mapper, int index, IntFunction<O[]> arrayMaker) {
        if (index == -1 || list.isEmpty()) {
            return arrayMaker.apply(0);
        } else {
            final int listSize = list.size();
            final O[] a = arrayMaker.apply(Math.min(5, listSize));
            if (listSize < 6) {
                for (int i = 0 ; i < a.length ; i++) {
                    a[i] = mapper.apply(list.get(i), i == index);
                }
            } else if (index == 0) {
                a[0] = mapper.apply(list.get(listSize - 2), false);
                a[1] = mapper.apply(list.get(listSize - 1), false);
                a[2] = mapper.apply(list.get(0), true);
                a[3] = mapper.apply(list.get(1), false);
                a[4] = mapper.apply(list.get(2), false);
            } else if (index == 1) {
                a[0] = mapper.apply(list.get(listSize - 1), false);
                a[1] = mapper.apply(list.get(0), false);
                a[2] = mapper.apply(list.get(1), true);
                a[3] = mapper.apply(list.get(2), false);
                a[4] = mapper.apply(list.get(3), false);
            } else if (index == listSize - 1) {
                a[0] = mapper.apply(list.get(index - 2), false);
                a[1] = mapper.apply(list.get(index - 1), false);
                a[2] = mapper.apply(list.get(index), true);
                a[3] = mapper.apply(list.get(0), false);
                a[4] = mapper.apply(list.get(1), false);
            } else if (index == listSize - 2) {
                a[0] = mapper.apply(list.get(index - 2), false);
                a[1] = mapper.apply(list.get(index - 1), false);
                a[2] = mapper.apply(list.get(index), true);
                a[3] = mapper.apply(list.get(index + 1), false);
                a[4] = mapper.apply(list.get(0), false);
            } else {
                a[0] = mapper.apply(list.get(index - 2), false);
                a[1] = mapper.apply(list.get(index - 1), false);
                a[2] = mapper.apply(list.get(index), true);
                a[3] = mapper.apply(list.get(index + 1), false);
                a[4] = mapper.apply(list.get(index + 2), false);
            }
            return a;
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

            AbstractWidget.renderScrollingString(graphics, font, block.getName(), x + 2, y + 5, right, y + 17, 0xFFFFFFFF);
        } else {
            AbstractWidget.renderScrollingString(graphics, font, textIfBlockIsNull, x + 2, y + 5, right - 2, y + 17, 0xFFFFFFFF);
        }
    }

    public static void resetScroll(AbstractSelectionList<?> list) {
        list.setScrollAmount(list.getScrollAmount());
    }
}
