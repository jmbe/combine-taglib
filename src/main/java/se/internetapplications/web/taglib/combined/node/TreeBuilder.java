package se.internetapplications.web.taglib.combined.node;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TreeBuilder {

    private CombineObjectMapper mapper;

    public TreeBuilder() {
        this.mapper = new CombineObjectMapper();
    }

    List<ConfigurationItem> parse(final InputStream stream) throws JsonParseException, JsonMappingException,
            IOException {

        List<ConfigurationItem> config = mapper.readValue(stream, new TypeReference<List<ConfigurationItem>>() {
        });

        return config;
    }

    public Map<String, ResourceNode> build(final InputStream stream) throws JsonParseException, JsonMappingException,
            IOException {
        List<ConfigurationItem> items = parse(stream);
        return build(items);
    }

    public Map<String, ResourceNode> build(final List<ConfigurationItem> items) {
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
                    throw new IllegalStateException(String.format("Could not find dependency: %s -> %s",
                            current.getName(), string));
                }
                current.addEdges(edge);
            }
        }

        return nodes;
    }

    public List<ConfigurationItem> resolve(final List<ConfigurationItem> configurationItems) {
        Map<String, ResourceNode> build = build(configurationItems);

        ResourceNode root = new ResourceNode();
        root.setVirtual(true);

        for (ConfigurationItem configurationItem : configurationItems) {
            if (!configurationItem.isOptional()) {
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
