package se.intem.web.taglib.combined.node;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

public class ResourceNode {

    private LinkedHashSet<ResourceNode> edges;
    private String name;

    private static final Random random = new Random();

    /**
     * A virtual node is a node which simply collects other dependencies, but has no resource of its own, so it should
     * not be output to jsp.
     */
    private boolean virtual;
    private ConfigurationItem item;
    private LinkedHashSet<ResourceNode> optionals;

    /**
     * Is an actual node, i.e. not a virtual node.
     */
    private static final Predicate<ResourceNode> isActual = new Predicate<ResourceNode>() {
        public boolean apply(final ResourceNode input) {
            return !input.isVirtual();
        }
    };

    private static final Function<ResourceNode, String> toName = new Function<ResourceNode, String>() {
        public String apply(final ResourceNode input) {
            return input.getName();
        }
    };

    public ResourceNode() {
        this("anonymous-" + random.nextInt());
    }

    public ResourceNode(final String name) {
        this(name, null);
    }

    public ResourceNode(final String name, final ConfigurationItem item) {
        this.edges = Sets.newLinkedHashSet();
        this.optionals = Sets.newLinkedHashSet();
        this.name = name;
        this.item = item;

    }

    public ResourceNode addEdges(final ResourceNode... dependencies) {
        for (ResourceNode edge : dependencies) {
            this.edges.add(edge);
        }

        return this;
    }

    public ResourceNode addOptionalEdges(final ResourceNode... optional) {
        for (ResourceNode resourceNode : optional) {
            this.optionals.add(resourceNode);
        }
        return this;
    }

    public List<ResourceNode> resolve() {
        List<ResourceNode> resolved = Lists.newArrayList();
        List<ResourceNode> seen = Lists.newArrayList();
        resolve(resolved, seen);

        return FluentIterable.from(resolved).filter(isActual).toList();
    }

    /**
     * Algorithm from http://www.electricmonk.nl/docs/dependency_resolving_algorithm/dependency_resolving_algorithm.html
     */
    private void resolve(final List<ResourceNode> resolved, final List<ResourceNode> unresolved) {
        unresolved.add(this);
        for (ResourceNode node : edges) {
            if (this.equals(node)) {
                continue;
            }

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

        ImmutableList<String> edgeNames = FluentIterable.from(edges).transform(toName).toList();
        ImmutableList<String> optionalNames = FluentIterable.from(optionals).transform(toName).toList();

        String format = String.format("%s  R%s O%s", name, edgeNames, optionalNames);
        return format;
    }

    public ResourceNode setVirtual(final boolean virtual) {
        this.virtual = virtual;
        return this;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public String getName() {
        return name;
    }

    public ConfigurationItem getItem() {
        return item;
    }

    public Iterable<ResourceNode> getOptionals() {
        return optionals;
    }

    public void promoteToRequired(final ResourceNode optional) {
        optionals.remove(optional);
        edges.add(optional);
    }

    Iterable<ResourceNode> getEdges() {
        return edges;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ResourceNode)) {
            return false;
        }

        ResourceNode that = (ResourceNode) obj;

        return Objects.equal(this.name, that.name);
    }
}
