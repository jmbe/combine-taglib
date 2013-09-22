package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class DependencyCache {

    private Cache<String, DependencyCacheEntry> cache;

    public DependencyCache() {
        this.cache = CacheBuilder.newBuilder().build();
    }

    public Optional<DependencyCacheEntry> get(final String key) {
        DependencyCacheEntry nullable = cache.getIfPresent(key);
        return Optional.fromNullable(nullable);
    }

    public void put(final String key, final DependencyCacheEntry entry) {
        cache.put(key, entry);
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
