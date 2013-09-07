package se.internetapplications.web.taglib.combined;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import java.io.File;
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
    private static Map<String, String> resourcePaths;
    /**
     * requestPath -> CombinedResource
     */
    private static Map<String, CombinedResource> combinedResourcePaths;

    static {
        resourcePaths = Maps.newHashMap();
        combinedResourcePaths = Maps.newHashMap();
    }

    public static boolean containsResourcePath(final String name) {
        return resourcePaths.containsKey(createResourcePathKey(name));
    }

    static String createResourcePathKey(final String name) {
        return String.format("/%s", name);
    }

    public static String getResourcePath(final String name) {
        return resourcePaths.get(createResourcePathKey(name));
    }

    public static CombinedResource getCombinedResource(final String requestUri) {
        return combinedResourcePaths.get(requestUri);
    }

    public static String addCombinedResource(final String name, final List<ManagedResource> resources,
            final CombineResourceStrategy combinator) {

        checkNotNull(name, "Name cannot be null.");
        checkNotNull(resources, "Resources cannot be null.");

        String requestPath = null;

        CombinedResource resource = getCombinedResourceByKey(name);

        if (resource == null || resource.hasChangedFile(resources)) {
            log.info(String.format("Modified resource '%s' detected. Rebuilding...", name));
            try {

                final StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                long startReadAt = new Date().getTime();
                long timestamp = combinator.combineFiles(pw, resources);
                if (timestamp == 0) {
                    timestamp = startReadAt;
                }

                String contents = sw.toString();
                String md5 = Hashing.md5().hashString(contents).toString();

                requestPath = createRequestPath(name, md5);

                log.debug("Adding combined resource" + requestPath);

                resourcePaths.put(createResourcePathKey(name), requestPath);
                combinedResourcePaths.put(requestPath,
                        combinator.stringToCombinedResource(contents, timestamp, md5, resources));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            requestPath = createRequestPath(name, resource.getChecksum());
        }

        return requestPath;
    }

    public static long joinPaths(final PrintWriter writer, final List<ManagedResource> realPaths) {
        log.trace("Reading files");

        long timestamp = 0;
        for (ManagedResource realPath : realPaths) {

            try {
                if (realPath.isTimestampSupported()) {
                    File file = new File(realPath.getRealPath());
                    timestamp = Math.max(timestamp, file.lastModified());
                }

                String contents = CharStreams.toString(CharStreams.newReaderSupplier(realPath.getInputSupplicer(),
                        Charsets.UTF_8));

                writer.println(contents);
            } catch (IOException e) {
                throw new RuntimeException("Could not read file " + realPath, e);
            }
        }
        writer.flush();

        return timestamp;
    }

    @SuppressWarnings("unused")
    private static long yuiCompressPaths(final PrintWriter writer, final List<String> realPaths) throws IOException,
            InterruptedException {
        YuiCompressorWriter yuiWriter = new YuiCompressorWriter(writer);

        log.info("Starting compressor thread");
        Thread compressorThread = new Thread(yuiWriter);
        compressorThread.start();

        log.info("Reading files");

        long timestamp = 0;
        for (String realPath : realPaths) {

            try {
                File file = new File(realPath);
                timestamp = Math.max(timestamp, file.lastModified());
                String contents = Files.toString(file, Charsets.UTF_8);
                yuiWriter.write(contents);
            } catch (IOException e) {
                throw new RuntimeException("Could not read file " + realPath, e);
            }
        }
        yuiWriter.flush();
        yuiWriter.close();

        compressorThread.join();
        return timestamp;
    }

    /**
     * Creates the path that will be used in the request from the browser.
     */
    static String createRequestPath(final String id, final String checksum) {
        String path = String.format("%s-%s.combined", createResourcePathKey(id), checksum);
        return path;
    }

    private static CombinedResource getCombinedResourceByKey(final String name) {
        if (containsResourcePath(name)) {
            String scriptPath = getResourcePath(name);
            return getCombinedResource(scriptPath);
        }
        return null;
    }

}
