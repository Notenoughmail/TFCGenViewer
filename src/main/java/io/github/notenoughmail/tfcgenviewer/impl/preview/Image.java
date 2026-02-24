package io.github.notenoughmail.tfcgenviewer.impl.preview;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.impl.mixin.accessor.NativeImageAccessor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.FastColor;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.Lazy;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;

public class Image implements MutableImage {

    private static final Supplier<File> EXPORT_DIR = Lazy.of(() -> FMLPaths.getOrCreateGameRelativePath(Path.of("screenshots", "tfcgenviewer")).toFile());

    private final NativeImage image;
    private final int maxPixel;

    public Image(int size) {
        maxPixel = size - 1;
        image = new NativeImage(size, size, false);
    }

    public NativeImage getNative() {
        return image;
    }

    public int size() {
        return maxPixel + 1;
    }

    @Override
    public boolean inRange(int z) {
        return z >= 0 && z <= maxPixel;
    }

    private void set(int x, int y, int abgrColor) {
        synchronized (image) {
            if (isAllocated()) {
                image.setPixelRGBA(x, y, abgrColor);
            }
        }
    }

    private void blend(int x, int y, int abgrColor) {
        synchronized (image) {
            if (isAllocated()) {
                image.blendPixel(x, y, abgrColor);
            }
        }
    }

    @Override
    public void setPixel(int x, int y, int abgrColor) {
        if (inRange(x) && inRange(y)) {
            final int alpha = FastColor.ABGR32.alpha(abgrColor);
            if (alpha == 0xFF) {
                set(x, y, abgrColor);
            } else if (alpha != 0) {
                blend(x, y, abgrColor);
            }
        }
    }

    @Override
    public void hLine(int x0, int x1, int y, int width, int abgrColor) {
        final int alpha = FastColor.ABGR32.alpha(abgrColor);
        if (alpha != 0) {
            final boolean blend = alpha != 0xFF;
            final int min = Math.max(Math.min(x0, x1), 0);
            final int max = Math.min(Math.max(x0, x1), maxPixel);
            if (width <= 0) {
                if (inRange(y)) {
                    for (int x = min ; x < max ; x++) {
                        if (blend) {
                            blend(x, y, abgrColor);
                        } else {
                            set(x, y, abgrColor);
                        }
                    }
                }
            } else {
                int top = y - width, bottom = y + width;
                if (inRange(top) || inRange(bottom)) {
                    top = Math.max(top, 0);
                    bottom = Math.min(bottom, maxPixel);
                    for (int x = min; x < max; x++) {
                        for (int i = top; i < bottom; i++) {
                            if (blend) {
                                blend(x, i, abgrColor);
                            } else {
                                set(x, i, abgrColor);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void vLine(int y0, int y1, int x, int width, int abgrColor) {
        final int alpha = FastColor.ABGR32.alpha(abgrColor);
        if (alpha != 0) {
            final boolean blend = alpha != 0xFF;
            final int min = Math.max(Math.min(y0, y1), 0);
            final int max = Math.min(Math.max(y0, y1), maxPixel);
            if (width <= 0) {
                if (inRange(x)) {
                    for (int y = min ; y < max ; y++) {
                        if (blend) {
                            blend(x, y, abgrColor);
                        } else {
                            set(x, y, abgrColor);
                        }
                    }
                }
            } else {
                int left = x - width, right = x + width;
                if (inRange(left) || inRange(right)) {
                    left = Math.max(left, 0);
                    right = Math.min(right, maxPixel);
                    for (int y = min ; y < max ; y++) {
                        for (int i = left ; i < right ; i++) {
                            if (blend) {
                                blend(i, y, abgrColor);
                            } else {
                                set(i, y, abgrColor);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getABGRColor(int x, int y) {
        if (inRange(x) && inRange(y)) {
            synchronized (image) {
                if (isAllocated()) {
                    return image.getPixelRGBA(x, y);
                }
            }
        }
        return 0;
    }

    public void close() {
        synchronized (image) {
            image.close();
        }
    }

    @Override
    public boolean isAllocated() {
        return ((NativeImageAccessor) (Object) image).tfcgenviewer$GetPixels() != 0L;
    }

    public void export(String name) {
        synchronized (image) {
            try {
                image.writeToFile(new File(EXPORT_DIR.get(), name));
            } catch (Exception e) {
                TFCGenViewer.LOGGER.error("Unable to write preview %s to disk!".formatted(name), e);
            }
        }
    }

    // This has a problem where while the image may be allocated when *requesting* the upload,
    // the image can be unallocated afterward, before the image is uploaded on the render thread
    public void upload(DynamicTexture tex) {
        synchronized (image) {
            if (isAllocated()) {
                tex.setPixels(image);
                tex.upload();
            }
        }
    }
}
