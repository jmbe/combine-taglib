package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.List;

public class DependencyCache {

    private Cache<String, List<String>> cache;

    public DependencyCache() {
        this.cache = CacheBuilder.newBuilder().build();
    }

    public Optional<List<String>> get(final String key) {
        List<String> nullable = cache.getIfPresent(key);
        return Optional.of(nullable);
    }

    public void put(final String key, final List<String> requies) {
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
