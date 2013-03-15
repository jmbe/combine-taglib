package se.internetapplications.web.taglib.combined.node;

import com.google.common.collect.Lists;

import java.util.List;

public class ResourceNode {

    private List<ResourceNode> edges;
    private String name;

    public ResourceNode(final String name) {
        this.edges = Lists.newArrayList();
        this.name = name;
    }

    public void addEdge(final ResourceNode... nodes) {
        for (ResourceNode edge : nodes) {
            this.edges.add(edge);
        }
    }

    public List<ResourceNode> resolve() {
        List<ResourceNode> resolved = Lists.newArrayList();
        List<ResourceNode> seen = Lists.newArrayList();
        dep_resolve(resolved, seen);
        return resolved;
    }

    public void dep_resolve(final List<ResourceNode> resolved, final List<ResourceNode> unresolved) {
        unresolved.add(this);
        for (ResourceNode node : edges) {
            if (!resolved.contains(node)) {
                if (unresolved.contains(node)) {
                    throw new IllegalStateException(String.format("Circular dependency detected: %s -> %s", this.name,
                            node.name));
                }

                node.dep_resolve(resolved, unresolved);
            }
        }
        resolved.add(this);
        unresolved.remove(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
