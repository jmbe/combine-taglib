package se.intem.web.taglib.combined.configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import se.intem.web.taglib.combined.node.ConfigurationItem;

public class ConfigurationItemsCollection implements Iterable<ConfigurationItem>, Serializable {

    /**
     * Use LinkedHashMap since it maintains insertion order.
     */
    private Map<String, ConfigurationItem> nameToItem = Maps.newLinkedHashMap();
    private Optional<ConfigurationItemsCollection> parent = Optional.absent();
    private List<String> inlineScripts = Lists.newArrayList();
    private List<String> inlineScriptEarlies = Lists.newArrayList();
    private List<String> inlineStyles = Lists.newArrayList();
    private List<String> inlineStyleEarlies = Lists.newArrayList();

    public ConfigurationItemsCollection() {
    }

    public ConfigurationItemsCollection(final ConfigurationItemsCollection parent) {
        this.parent = Optional.fromNullable(parent);
    }

    public ConfigurationItemsCollection(final Collection<ConfigurationItem> items) {
        addAll(items);
    }

    @Override
    public Iterator<ConfigurationItem> iterator() {
        if (parent.isPresent()) {
            return Iterables.concat(parent.get().nameToItem.values(), nameToItem.values()).iterator();
        }
        return nameToItem.values().iterator();
    }

    public void add(final ConfigurationItem configurationItem) {
        if (parent.isPresent()) {
            if (parent.get().nameToItem.containsKey(configurationItem.getName())) {
                throw new IllegalStateException(String.format("Resource %s already exists in parent configuration.",
                        configurationItem.getName()));
            }
        }

        if (nameToItem.containsKey(configurationItem.getName())) {
            throw new IllegalStateException(String.format("Duplicate resource %s detected.",
                    configurationItem.getName()));
        }

        nameToItem.put(configurationItem.getName(), configurationItem);
    }

    public void addAll(final Collection<ConfigurationItem> items) {
        for (ConfigurationItem item : items) {
            add(item);
        }
    }

    @VisibleForTesting
    public int size() {
        return (parent.isPresent() ? parent.get().size() : 0) + nameToItem.size();
    }

    public void addInlineScript(final String js) {
        inlineScripts.add(js.replaceAll("</?script[^>]*>", ""));
    }

    public List<String> getInlineScripts() {
        return inlineScripts;
    }

    public void addInlineScriptEarly(final String js) {
        inlineScriptEarlies.add(js.replaceAll("</?script[^>]*>", ""));
    }

    public List<String> getInlineScriptEarlies() {
        return inlineScriptEarlies;
    }

    public void addInlineStyle(final String contents) {
        inlineStyles.add(contents.replaceAll("</?style[^>]*>", ""));
    }

    public List<String> getInlineStyles() {
        return inlineStyles;
    }

    public void addInlineStyleEarly(final String contents) {
        inlineStyleEarlies.add(contents.replaceAll("</?style[^>]*>", ""));
    }

    public List<String> getInlineStyleEarlies() {
        return inlineStyleEarlies;
    }
}