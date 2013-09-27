package se.intem.web.taglib.combined;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.resources.CombinedBundle;

public class CombinedResourceRepository {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombinedResourceRepository.class);

    /**
     * key(path, name) -> requestPath
     */
    private Map<String, RequestPath> resourcePaths;
    /**
     * requestPath -> CombinedResource
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

    public Optional<RequestPath> getResourcePath(final String name, final ResourceType type) {
        return Optional.fromNullable(resourcePaths.get(createResourcePathKey(name, type)));
    }

    public CombinedBundle getCombinedResource(final RequestPath requestUri) {
        return combinedResourcePaths.get(requestUri);
    }

    public RequestPath addCombinedResource(final String name, final CombinedBundle bundle) {
        RequestPath requestPath = createRequestPath(name, bundle.getType(), bundle.getChecksum());

        log.debug("Adding combined resource {}.", requestPath);

        resourcePaths.put(createResourcePathKey(name, bundle.getType()), requestPath);
        combinedResourcePaths.put(requestPath, bundle);
        return requestPath;
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
