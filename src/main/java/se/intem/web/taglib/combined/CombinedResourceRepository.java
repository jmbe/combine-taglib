package se.intem.web.taglib.combined;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.resources.CombinedBundle;
import se.intem.web.taglib.combined.resources.ResourceGroup;

public class CombinedResourceRepository {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombinedResourceRepository.class);

    /**
     * Resource group name -> ResourceGroup
     */
    private Map<String, ResourceGroup> resourcePaths;

    /**
     * requestPath (finger-printed) -> CombinedResource
     */
    private Map<RequestPath, CombinedBundle> combinedResourcePaths;

    private CombinedResourceRepository() {
        resourcePaths = Maps.newHashMap();
        combinedResourcePaths = Maps.newHashMap();
    }

    @VisibleForTesting
    String createResourcePathKey(final String name, final ResourceType type) {
        return String.format("/%s/%s", name, type);
    }

    public Iterable<RequestPath> getResourcePath(final String name, final ResourceType type) {

        Optional<ResourceGroup> optional = Optional.fromNullable(resourcePaths.get(name));
        if (optional.isPresent()) {
            return optional.get().getRequestPaths(type);
        }

        return Collections.emptyList();
    }

    /**
     * Used by servlet to get bundle contents.
     */
    public CombinedBundle getCombinedResource(final RequestPath requestUri) {
        return combinedResourcePaths.get(requestUri);
    }

    public RequestPath addCombinedResource(final CombinedBundle bundle) {
        Preconditions.checkNotNull(bundle);

        RequestPath requestPath = createRequestPath(bundle.getName().getName(), bundle.getType(), bundle.getChecksum());
        bundle.setRequestPath(requestPath);

        log.debug("Adding combined resource {}.", requestPath);
        combinedResourcePaths.put(requestPath, bundle);
        return requestPath;
    }

    public void addResourceGroup(final String name, final ResourceGroup group) {
        resourcePaths.put(name, group);
    }

    /**
     * Creates the path that will be used in the request from the browser.
     */
    private RequestPath createRequestPath(final String name, final ResourceType type, final String checksum) {
        String path = String.format("%s-%s.combined", createResourcePathKey(name, type), checksum);
        return new RequestPath(path);
    }

    public static CombinedResourceRepository get() {
        return InstanceHolder.instance;
    }

    /**
     * http://en.wikipedia.org/wiki/Singleton_pattern#The_solution_of_Bill_Pugh
     */
    private static class InstanceHolder {
        private static final CombinedResourceRepository instance = new CombinedResourceRepository();
    }

}
