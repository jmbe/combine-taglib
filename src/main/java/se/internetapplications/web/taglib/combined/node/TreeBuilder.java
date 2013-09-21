package se.internetapplications.web.taglib.combined.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import se.internetapplications.web.taglib.combined.tags.ConfigurationItemsCollection;

public class TreeBuilder {

    private CombineObjectMapper mapper;

    public TreeBuilder() {
        this.mapper = new CombineObjectMapper();
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

        /* Pass 1: Populate map */
        for (ConfigurationItem item : items) {
            nodes.put(item.getName(), new ResourceNode(item.getName(), item));
        }

        /* Pass 2: populate dependencies */
        for (ConfigurationItem item : items) {
            ResourceNode current = nodes.get(item.getName());
            List<String> requires = item.getRequires();

            for (String string : requires) {
                ResourceNode edge = nodes.get(string);
                if (edge == null) {
                    throw new IllegalStateException(String.format("Could not find dependency: %s requires '%s'",
                            current.getName(), string));
                }
                current.addEdges(edge);
            }
        }

        return nodes;
    }

    public List<ConfigurationItem> resolve(final ConfigurationItemsCollection configurationItemsCollection) {
        Map<String, ResourceNode> build = build(configurationItemsCollection);

        ResourceNode root = new ResourceNode();
        root.setVirtual(true);

        for (ConfigurationItem configurationItem : configurationItemsCollection) {
            if (!configurationItem.isLibrary()) {
                root.addEdges(build.get(configurationItem.getName()));
            }
        }

        List<ResourceNode> resolved = root.resolve();

        return FluentIterable.from(resolved).transform(new Function<ResourceNode, ConfigurationItem>() {

            public ConfigurationItem apply(final ResourceNode input) {
                return input.getItem();
            }
        }).toList();

    }

}
