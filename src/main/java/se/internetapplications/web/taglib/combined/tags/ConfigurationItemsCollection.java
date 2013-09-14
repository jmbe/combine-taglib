package se.internetapplications.web.taglib.combined.tags;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import se.internetapplications.web.taglib.combined.node.ConfigurationItem;

public class ConfigurationItemsCollection implements Iterable<ConfigurationItem>, Serializable {

    /**
     * Use LinkedHashMap since it maintains insertion order.
     */
    private Map<String, ConfigurationItem> nameToItem = Maps.newLinkedHashMap();

    public ConfigurationItemsCollection() {
    }

    public ConfigurationItemsCollection(final Collection<ConfigurationItem> items) {
        addAll(items);
    }

    @Override
    public Iterator<ConfigurationItem> iterator() {
        return nameToItem.values().iterator();
    }

    public void add(final ConfigurationItem configurationItem) {
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

    public int size() {
        return nameToItem.size();
    }

}
