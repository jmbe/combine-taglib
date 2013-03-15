package se.internetapplications.web.taglib.combined.node;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Random;

public class ResourceNode {

    private List<ResourceNode> edges;
    private String name;

    private static final Random random = new Random();

    /**
     * A virtual node is a node which simply collects other dependencies, but has no resource of its own, so it should
     * not be output to jsp.
     */
    private boolean virtual;

    /**
     * Is an actual node, i.e. not a virtual node.
     */
    private static final Predicate<ResourceNode> isActual = new Predicate<ResourceNode>() {
        public boolean apply(final ResourceNode input) {
            return !input.isVirtual();
        }
    };

    public ResourceNode() {
        this("anonymous-" + random.nextInt());
    }

    public ResourceNode(final String name) {
        this.edges = Lists.newArrayList();
        this.name = name;
    }

    public ResourceNode addEdges(final ResourceNode... nodes) {
        for (ResourceNode edge : nodes) {
            this.edges.add(edge);
        }

        return this;
    }

    public List<ResourceNode> resolve() {
        List<ResourceNode> resolved = Lists.newArrayList();
        List<ResourceNode> seen = Lists.newArrayList();
        resolve(resolved, seen);

        return FluentIterable.from(resolved).filter(isActual).toImmutableList();
    }

    /**
     * Algorithm from http://www.electricmonk.nl/docs/dependency_resolving_algorithm/dependency_resolving_algorithm.html
     */
    private void resolve(final List<ResourceNode> resolved, final List<ResourceNode> unresolved) {
        unresolved.add(this);
        for (ResourceNode node : edges) {
            if (!resolved.contains(node)) {
                if (unresolved.contains(node)) {
                    throw new IllegalStateException(String.format("Circular dependency detected: %s -> %s", this.name,
                            node.name));
                }

                node.resolve(resolved, unresolved);
            }
        }
        resolved.add(this);
        unresolved.remove(this);
    }

    @Override
    public String toString() {
        return name;
    }

    public ResourceNode setVirtual(final boolean virtual) {
        this.virtual = virtual;
        return this;
    }

    public boolean isVirtual() {
        return virtual;
    }
}
