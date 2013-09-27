package se.intem.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.ResourceType;
import se.intem.web.taglib.combined.node.CombineCommentParser;
import se.intem.web.taglib.combined.node.ConfigurationItem;

public class DependencyCache {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(DependencyCache.class);

    private Cache<String, DependencyCacheEntry> cache;

    private CombineCommentParser jsParser;

    public DependencyCache() {
        this.cache = CacheBuilder.newBuilder().build();
        this.jsParser = new CombineCommentParser();
    }

    public Optional<DependencyCacheEntry> get(final String key) {
        DependencyCacheEntry nullable = cache.getIfPresent(key);
        return Optional.fromNullable(nullable);
    }

    public void put(final String key, final DependencyCacheEntry entry) {
        cache.put(key, entry);
    }

    public void readDependenciesFromResources(final ServletContext servletContext, final ConfigurationItem ci) {
        if (ci.isRemote() || ci.isEmpty()) {
            return;
        }

        String cacheKey = ci.getName();

        boolean hasChanges = false;

        Optional<DependencyCacheEntry> optional = get(cacheKey);
        if (optional.isPresent()) {
            DependencyCacheEntry cached = optional.get();
            if (cached.requiresRefresh(ci, servletContext)) {
                hasChanges = true;
            }
        } else {
            hasChanges = true;
        }

        if (hasChanges) {
            /* Rebuild cache */
            if (!ci.shouldBeCombined()) {
                log.info("Reading dependencies for uncombined resource {}.", ci.getName());
            } else {
                log.info("Changes detected for {}. Rebuilding dependency cache...", ci.getName());
            }

            long lastread = new Date().getTime();
            Map<ResourceType, List<ManagedResource>> realPaths = ci.getRealPaths(servletContext);
            Set<Entry<ResourceType, List<ManagedResource>>> entrySet = realPaths.entrySet();

            LinkedHashSet<String> requires = Sets.newLinkedHashSet();

            for (Entry<ResourceType, List<ManagedResource>> entry : entrySet) {

                for (ManagedResource mr : entry.getValue()) {
                    log.debug("Parsing {}", mr.getName());
                    try {
                        List<String> found = jsParser.parse(mr.getInput()).getRequiresList();
                        requires.addAll(found);
                    } catch (IOException e) {
                        log.error("Could not parse js", e);
                    }
                }
            }

            put(cacheKey, new DependencyCacheEntry(lastread, requires, ci));

        }

        optional = get(cacheKey);
        if (optional.isPresent()) {
            ci.addRequires(optional.get().getRequires());
        }

    }

    public static DependencyCache get() {
        return InstanceHolder.instance;
    }

    /**
     * http://en.wikipedia.org/wiki/Singleton_pattern#The_solution_of_Bill_Pugh
     */
    private static class InstanceHolder {
        private static final DependencyCache instance = new DependencyCache();
    }
}
