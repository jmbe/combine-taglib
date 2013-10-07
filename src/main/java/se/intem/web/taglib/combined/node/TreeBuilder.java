package se.intem.web.taglib.combined.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.intem.web.taglib.combined.configuration.ConfigurationItemsCollection;
import se.intem.web.taglib.combined.configuration.DependencyCache;
import se.intem.web.taglib.combined.configuration.DependencyCacheEntry;

public class TreeBuilder {

    private CombineObjectMapper mapper;
    private DependencyCache dependencyCache;

    public TreeBuilder() {
        this.mapper = new CombineObjectMapper();
        this.dependencyCache = DependencyCache.get();
    }

    public ConfigurationItemsCollection parse(final InputStream stream) throws IOException {

        List<ConfigurationItem> items = mapper.readValue(stream, new TypeReference<List<ConfigurationItem>>() {
        });

        return new ConfigurationItemsCollection(items);
    }

    public Map<String, ResourceNode> build(final InputStream stream) throws IOException {
        ConfigurationItemsCollection items = parse(stream);
        return build(items);
    }

    public Map<String, ResourceNode> build(final ConfigurationItemsCollection items) {
        Map<String, ResourceNode> nodes = Maps.newHashMap();

        /* Maps a @provides to the actual resource group containing that resource. */
        Map<String, ResourceNode> aliases = Maps.newHashMap();

        /* Pass 1: Populate map */
        for (ConfigurationItem item : items) {
            ResourceNode node = new ResourceNode(item.getName(), item);
            nodes.put(item.getName(), node);

            /* Check if there are registered @provides for this node */
            Optional<DependencyCacheEntry> optional = dependencyCache.get(item.getName());
            if (optional.isPresent()) {
                Iterable<String> provides = optional.get().getProvides();
                for (String aliasName : provides) {
                    ResourceNode currentAlias = aliases.get(aliasName);
                    if (currentAlias == null) {
                        aliases.put(aliasName, node);
                    } else if (currentAlias != node) {
                        throw new IllegalStateException(String.format(
                                "Duplicate alias '%s' is a member of both '%s' and '%s'.", aliasName,
                                currentAlias.getName(), node.getName()));
                    }
                }
            }
        }

        /* Pass 2: populate dependencies */
        for (ConfigurationItem item : items) {
            ResourceNode current = nodes.get(item.getName());

            /* Add required dependencies */
            for (String required : item.getRequires()) {
                ResourceNode edge = nodes.get(required);
                if (edge == null) {

                    edge = aliases.get(required);

                    if (edge == null) {
                        throw new IllegalStateException(String.format("Could not find dependency: %s requires '%s'",
                                current.getName(), required));
                    }
                }
                current.addEdges(edge);
            }

            /* Add optional dependencies */
            Iterable<String> optionals = item.getOptional();

            Optional<DependencyCacheEntry> cached = dependencyCache.get(item.getName());
            if (cached.isPresent()) {
                optionals = Iterables.concat(optionals, cached.get().getOptionals());
            }

            for (String optional : optionals) {
                ResourceNode edge = nodes.get(optional);
                if (edge == null) {
                    throw new IllegalStateException(String.format(
                            "Unknown optional dependency: %s optionally requires '%s'", current.getName(), optional));
                }
                current.addOptionalEdges(edge);
            }
        }

        return nodes;
    }

    public List<ConfigurationItem> resolve(final ConfigurationItemsCollection configurationItemsCollection) {
        Map<String, ResourceNode> build = build(configurationItemsCollection);

        ResourceNode root = new ResourceNode();
        root.setVirtual(true);

        /**
         * Pass 0. Populate optionals map.
         */
        /* Optional resource -> List of nodes who will use it if included. */
        Multimap<ResourceNode, ResourceNode> optionals = ArrayListMultimap.create();
        for (ResourceNode node : build.values()) {
            for (ResourceNode optionalNode : node.getOptionals()) {
                optionals.put(optionalNode, node);
            }
        }

        /**
         * Pass 1. Resolve without optionals.
         */
        for (ConfigurationItem configurationItem : configurationItemsCollection) {
            if (!configurationItem.isLibrary()) {
                root.addEdges(build.get(configurationItem.getName()));
            }
        }

        /* List contains only required resources now. */
        List<ResourceNode> resolved = root.resolve();

        /**
         * Pass 2. Promote optionals that were included anyway and resolve again.
         */
        Collection<Entry<ResourceNode, ResourceNode>> entries = optionals.entries();
        for (Entry<ResourceNode, ResourceNode> entry : entries) {
            ResourceNode optional = entry.getKey();
            if (resolved.contains(optional)) {
                Collection<ResourceNode> collection = optionals.get(optional);
                for (ResourceNode node : collection) {
                    node.promoteToRequired(optional);
                }
            }
        }

        /* List contains same nodes, but re-ordered so that optional nodes will load before nodes that depend on them. */
        resolved = root.resolve();

        return FluentIterable.from(resolved).transform(new Function<ResourceNode, ConfigurationItem>() {

            public ConfigurationItem apply(final ResourceNode input) {
                return input.getItem();
            }
        }).toList();

    }
}
