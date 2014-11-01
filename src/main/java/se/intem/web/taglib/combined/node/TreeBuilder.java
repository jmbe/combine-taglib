package se.intem.web.taglib.combined.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.configuration.ConfigurationItemsCollection;
import se.intem.web.taglib.combined.configuration.DependencyCache;
import se.intem.web.taglib.combined.configuration.DependencyCacheEntry;

public class TreeBuilder {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(TreeBuilder.class);

    private CombineObjectMapper mapper;
    private DependencyCache dependencyCache;

    public TreeBuilder() {
        this.mapper = new CombineObjectMapper();
        this.dependencyCache = DependencyCache.get();
    }

    public ConfigurationItemsCollection parse(final InputStream stream) throws IOException {
        Preconditions.checkNotNull(stream);

        List<ConfigurationItem> items = mapper.readValue(stream, new TypeReference<List<ConfigurationItem>>() {
        });

        return new ConfigurationItemsCollection(items);
    }

    public Map<String, ResourceNode> build(final InputStream stream) throws IOException {
        Preconditions.checkNotNull(stream);

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

        // contractEdges(nodes.values());

        return nodes;
    }

    public List<ConfigurationItem> resolve(final ConfigurationItemsCollection configurationItemsCollection) {
        Map<String, ResourceNode> build = build(configurationItemsCollection);

        ResourceNode base = new ResourceNode("virtual");
        base.setVirtual(true);

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
                base.addEdges(build.get(configurationItem.getName()));
            }
        }

        /* List contains only required resources now. */
        List<ResourceNode> resolved = base.resolve();

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
        resolved = base.resolve();

        logDependencyHierarchy(resolved);

        return FluentIterable.from(resolved).transform(new Function<ResourceNode, ConfigurationItem>() {

            public ConfigurationItem apply(final ResourceNode input) {
                return input.getItem();
            }
        }).toList();

    }

    private void contractEdges(final Iterable<ResourceNode> resolved) {

        List<ResourceNode> sorted = Lists.newArrayList(resolved);
        Collections.sort(sorted);

        for (ResourceNode node : sorted) {
            if (node.isVirtual()) {
                throw new IllegalStateException(String.format(
                        "Virtual node '%s' is not expected in edge contraction. ", node.getName()));
            }

            if (node.getSatisfies().size() == 1) {
                ResourceNode satisfies = node.getSatisfies().get(0);
                if (!satisfies.isRoot()) {
                    log.debug("(1) {} is only used once in {}", node.getName(), satisfies.getName());
                }
            } else if (!node.isRoot() && node.getSatisfies().size() == 0) {
                log.debug("( ) {} is never used", node.getName());
            }
        }
    }

    private void logDependencyHierarchy(final List<ResourceNode> resolved) {
        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug("Dependency tree of {} members:", resolved.size());
        logDependencyHierarchy(resolved, null, "", 0);

    }

    private void logDependencyHierarchy(final Iterable<ResourceNode> resolved, final ResourceNode parent,
            final String prefix, final int depth) {

        if (!log.isDebugEnabled()) {
            return;
        }

        List<ResourceNode> sorted = Lists.newArrayList(resolved);
        Collections.sort(sorted);

        if (sorted.isEmpty()) {
            return;
        }

        if (depth > 8) {
            /* Abort outputting very deep hierarchies */
            log.debug(prefix + "<...>");
            return;
        }

        int count = 0;

        for (ResourceNode node : sorted) {

            if (Strings.nullToEmpty(prefix).isEmpty() && node.getItem().isLibrary()) {
                continue;
            }

            if (node.equals(parent)) {
                /* Break endless loop */
                continue;
            }

            boolean isLast = ++count == sorted.size();

            String level = node.getName();
            String padding = "";

            List<ResourceNode> children = Lists.newArrayList(node.getRequires());

            String sameLineSeparator = " ── ";

            /* These three symbols must all have same length. */
            String lineContinuation = "│  ";
            String lastNodeSeparator = "└─ ";
            String moreChildrenSeparator = "├─ ";

            /* Grow tree horizontally if there is only one child */
            while (children.size() == 1) {
                padding += Strings.repeat(" ", sameLineSeparator.length() + node.getName().length());

                node = children.get(0);
                level += sameLineSeparator + node.getName();

                children = Lists.newArrayList(node.getRequires());
            }

            if (isLast) {
                log.debug(prefix.replace(moreChildrenSeparator, lastNodeSeparator) + level);
            } else {
                log.debug(prefix + level);
            }

            String p = prefix.replace(moreChildrenSeparator, lineContinuation);
            if (isLast) {
                p = p.replaceAll(Pattern.quote(lineContinuation) + "$", Strings.repeat(" ", lineContinuation.length()));
            }

            logDependencyHierarchy(children, node, p + padding + moreChildrenSeparator, depth + 1);
        }
    }
}
