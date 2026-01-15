package io.github.notenoughmail.tfcgenviewer;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class LangProvider extends LanguageProvider {

    private final List<String> tree = new ArrayList<>();
    private final Node node;

    public LangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        node = new Node() {
            @Override
            public Node add(String leaf, String text) {
                final String key = String.join(".", tree) + (leaf.isEmpty() ? leaf : "." + leaf);
                LangProvider.this.add(key, text);
                return this;
            }

            @Override
            public Node branch(String branch, Consumer<Node> inBranch) {
                return LangProvider.this.branch(branch, inBranch);
            }
        };
    }

    protected Node branch(String branch, Consumer<Node> inBranch) {
        tree.addLast(branch);
        inBranch.accept(node);
        tree.removeLast();
        return node;
    }

    public interface Node {
        Node add(String leaf, String text);
        Node branch(String branch, Consumer<Node> inBranch);
    }
}
