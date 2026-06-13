package io.github.notenoughmail.tfcgenviewer.impl.behavior;

import net.dries007.tfc.world.layer.framework.Area;
import net.dries007.tfc.world.layer.framework.AreaFactory;
import net.dries007.tfc.world.layer.framework.ConcurrentArea;

import java.util.function.IntFunction;

public class MaybeConcurrentArea<T> extends ConcurrentArea<T> {

    public static <T> ConcurrentArea<T> create(boolean parallel, AreaFactory factory, IntFunction<T> mappingFunction) {
        if (parallel) {
            return new ConcurrentArea<>(factory, mappingFunction);
        }
        return new MaybeConcurrentArea<>(factory, mappingFunction);
    }

    private final Area area;

    public MaybeConcurrentArea(AreaFactory factory, IntFunction<T> mappingFunction) {
        super(factory, mappingFunction);
        area = factory.get();
    }

    @Override
    public T get(int x, int z) {
        return mappingFunction.apply(area.get(x, z));
    }
}
