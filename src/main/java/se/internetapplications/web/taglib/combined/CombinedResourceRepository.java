package se.internetapplications.web.taglib.combined;

import static com.google.common.base.Preconditions.*;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.internetapplications.web.taglib.combined.tags.ManagedResource;

public class CombinedResourceRepository {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombinedResourceRepository.class);

    /**
     * key(path, name) -> requestPath
     */
    private static Map<String, RequestPath> resourcePaths;
    /**
     * requestPath -> CombinedResource
     */
    private static Map<RequestPath, CombinedResource> combinedResourcePaths;

    static {
        resourcePaths = Maps.newHashMap();
        combinedResourcePaths = Maps.newHashMap();
    }

    public static boolean containsResourcePath(final String name, final ResourceType type) {
        return resourcePaths.containsKey(createResourcePathKey(name, type));
    }

    static String createResourcePathKey(final String name, final ResourceType type) {
        return String.format("/%s/%s", name, type);
    }

    public static RequestPath getResourcePath(final String name, final ResourceType type) {
        return resourcePaths.get(createResourcePathKey(name, type));
    }

    public static CombinedResource getCombinedResource(final RequestPath requestUri) {
        return combinedResourcePaths.get(requestUri);
    }

    public static RequestPath addCombinedResource(final String name, final ResourceType type,
            final List<ManagedResource> resources, final CombineResourceStrategy combinator) {

        checkNotNull(name, "Name cannot be null.");
        checkNotNull(resources, "Resources cannot be null.");

        RequestPath requestPath = null;

        CombinedResource resource = getCombinedResourceByKey(name, type);

        if (resource == null || resource.hasChangedFile(resources)) {
            if (resource == null) {
                log.info("Building resource for '{}'...", name);
            } else {
                log.info(String.format("Modified resource '%s' detected. Rebuilding...", name));
            }
            try {

                final StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                long startReadAt = new Date().getTime();
                long timestamp = combinator.combineFiles(pw, resources);
                if (timestamp == 0) {
                    timestamp = startReadAt;
                }

                String contents = sw.toString();
                String md5 = Hashing.md5().hashUnencodedChars(contents).toString();

                requestPath = createRequestPath(name, type, md5);

                log.debug("Adding combined resource" + requestPath);

                resourcePaths.put(createResourcePathKey(name, type), requestPath);
                combinedResourcePaths.put(requestPath,
                        combinator.stringToCombinedResource(contents, timestamp, md5, resources));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            requestPath = createRequestPath(name, type, resource.getChecksum());
        }

        return requestPath;
    }

    /**
     * Creates the path that will be used in the request from the browser.
     */
    private static RequestPath createRequestPath(final String name, final ResourceType type, final String checksum) {
        String path = String.format("%s-%s.combined", createResourcePathKey(name, type), checksum);
        return new RequestPath(path);
    }

    private static CombinedResource getCombinedResourceByKey(final String name, final ResourceType type) {
        if (containsResourcePath(name, type)) {
            RequestPath scriptPath = getResourcePath(name, type);
            return getCombinedResource(scriptPath);
        }
        return null;
    }

}
