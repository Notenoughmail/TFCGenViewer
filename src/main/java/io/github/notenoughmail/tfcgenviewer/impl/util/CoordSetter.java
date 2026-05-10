package io.github.notenoughmail.tfcgenviewer.impl.util;

@FunctionalInterface
public interface CoordSetter {

    CoordSetter NONE = (x, z) -> {};

    void set(int x, int z);
}
