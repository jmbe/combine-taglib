package se.intem.web.taglib.combined.configuration;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.CombinedResourceRepository;
import se.intem.web.taglib.combined.ResourceType;
import se.intem.web.taglib.combined.node.CombineCommentParser;
import se.intem.web.taglib.combined.node.ConfigurationItem;
import se.intem.web.taglib.combined.node.ParseResult;
import se.intem.web.taglib.combined.resources.CombinedBundle;
import se.intem.web.taglib.combined.resources.RemoteBundle;
import se.intem.web.taglib.combined.resources.ResourceGroup;
import se.intem.web.taglib.combined.resources.ResourceName;

public class DependencyCache {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(DependencyCache.class);

    private Cache<String, DependencyCacheEntry> cache;

    private CombineCommentParser commentParser;

    private CombinedResourceRepository repository;

    public DependencyCache() {
        this.cache = CacheBuilder.newBuilder().build();
        this.commentParser = new CombineCommentParser();
        this.repository = CombinedResourceRepository.get();
    }

    public Optional<DependencyCacheEntry> get(final String key) {
        DependencyCacheEntry nullable = cache.getIfPresent(key);
        return Optional.fromNullable(nullable);
    }

    public void put(final String key, final DependencyCacheEntry entry) {
        cache.put(key, entry);
    }

    public void readDependenciesFromResources(final ServletContext servletContext, final ConfigurationItem ci) {
        String cacheKey = ci.getName();
        Optional<DependencyCacheEntry> optional = get(cacheKey);

        if ((ci.isAllRemote() || ci.isEmpty()) && !optional.isPresent()) {
            return;
        }

        if (optional.isPresent() && !ci.isReloadable()) {
            /* entry exists and is not reloadable - nothing to do */
            return;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();

        boolean hasChanges = false;
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
                log.debug("Reading dependencies for uncombined resource {}.", ci.getName());
            } else {
                log.debug("Changes detected for {}. Rebuilding dependency cache...", ci.getName());
            }

            long lastread = new Date().getTime();
            Map<ResourceType, List<ManagedResource>> realPaths = ci.getRealPaths(servletContext);
            Set<Entry<ResourceType, List<ManagedResource>>> entrySet = realPaths.entrySet();

            LinkedHashSet<String> requires = Sets.newLinkedHashSet();
            LinkedHashSet<String> provides = Sets.newLinkedHashSet();
            LinkedHashSet<String> optionals = Sets.newLinkedHashSet();

            ResourceGroup group = new ResourceGroup();

            int counter = 0;
            for (Entry<ResourceType, List<ManagedResource>> entry : entrySet) {

                RemoteBundle currentRemote = null;
                CombinedBundle currentLocal = null;

                for (ManagedResource mr : entry.getValue()) {

                    if (mr.isRemote()) {
                        /* Finish previous local */
                        if (currentLocal != null) {
                            repository.addCombinedResource(currentLocal);
                            currentLocal = null;
                        }

                        /* Start new remote if necessary */
                        if (currentRemote == null) {
                            currentRemote = new RemoteBundle(entry.getKey());
                            group.addBundle(currentRemote);
                        }

                        currentRemote.addPath(mr.getRequestPath());
                        continue;
                    }

                    /* Finish previous remote */
                    currentRemote = null;

                    /* Start new local if necessary */
                    if (currentLocal == null) {
                        ResourceName name = new ResourceName(ci.getName()).derive(counter++);
                        currentLocal = new CombinedBundle(name, entry.getKey(), lastread);
                        group.addBundle(currentLocal);
                    }

                    log.debug("Parsing {}", mr.getName());
                    try {
                        ParseResult parsed = commentParser.parse(mr.getInput(),
                                createLinePreprocessors(entry.getKey(), mr));

                        currentLocal.addContents(parsed.getContents());

                        Iterables.addAll(requires, parsed.getRequires());
                        Iterables.addAll(provides, parsed.getProvides());
                        Iterables.addAll(optionals, parsed.getOptionals());
                    } catch (IOException e) {
                        log.error("Could not parse js", e);
                    }
                }

                /* Finish any still open local bundle */
                if (currentLocal != null) {
                    repository.addCombinedResource(currentLocal);
                }
            }

            repository.addResourceGroup(ci.getName(), group);
            put(cacheKey, new DependencyCacheEntry(lastread, requires, provides, optionals, ci));

            log.info(String.format("Resource group %s (%s resources) rebuilt in %s ms.", ci.getName(), ci.getSize(),
                    stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        }

        optional = get(cacheKey);
        if (optional.isPresent()) {
            ci.replaceParsedRequires(optional.get().getRequires());
        }

    }

    private List<Function<String, String>> createLinePreprocessors(final ResourceType type, final ManagedResource mr) {
        if (ResourceType.js.equals(type)) {
            /* nothing to do for js */
            return null;
        }

        List<Function<String, String>> result = Lists.newArrayList();
        result.add(new AbsolutizeCssUrlFunction(mr.getRequestPath()));
        return result;
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
