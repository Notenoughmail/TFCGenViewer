package io.github.notenoughmail.tfcgenviewer.api;

@FunctionalInterface
public interface BlockEvaluationFunction<T> {

    T evaluate(int blockX, int blockZ);
}
