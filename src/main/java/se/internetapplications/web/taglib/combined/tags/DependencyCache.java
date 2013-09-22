package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class DependencyCache {

    private Cache<String, Iterable<String>> cache;

    public DependencyCache() {
        this.cache = CacheBuilder.newBuilder().build();
    }

    public Optional<Iterable<String>> get(final String key) {
        Iterable<String> nullable = cache.getIfPresent(key);
        return Optional.fromNullable(nullable);
    }

    public void put(final String key, final Iterable<String> requies) {
        cache.put(key, requies);
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
