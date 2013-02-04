package se.internetapplications.web.taglib.combined;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombinedResourceRepository {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombinedResourceRepository.class);

    /**
     * key(path, name) -> requestPath
     */
    private static Map<String, String> scriptPaths;
    /**
     * requestPath -> CombinedResource
     */
    private static Map<String, CombinedResource> combinedScripts;

    static {
        scriptPaths = Maps.newHashMap();
        combinedScripts = Maps.newHashMap();
    }

    public static boolean containsScriptPath(final String path, final String name) {
        return scriptPaths.containsKey(createScriptPathKey(path, name));
    }

    static String createScriptPathKey(final String path, final String name) {
        String directory = path.replaceAll("^/+|/+$", "");
        return String.format("%s/%s", directory.trim().length() == 0 ? "" : "/" + directory, name);
    }

    public static String getScriptPath(final String path, final String name) {
        return scriptPaths.get(createScriptPathKey(path, name));
    }

    public static CombinedResource getCombinedResource(final String requestURI) {
        return combinedScripts.get(requestURI);
    }

    public static String addCombinedScripts(final String path, final String name, final List<String> realPaths,
            final boolean minify) {

        checkNotNull(path, "Path cannot be null.");
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(realPaths, "Real paths cannot be null.");

        String requestPath = null;

        CombinedResource resource = getCombinedResourceByKey(path, name);

        if (resource == null || resource.hasChangedFile(realPaths)) {
            log.info(String.format("Modified resource '%s' detected. Rebuilding...", name));
            try {

                final StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                long timestamp = minify ? yuiCompressPaths(pw, realPaths) : joinPaths(pw, realPaths);

                requestPath = createRequestPath(path, name, timestamp);

                log.info("Adding combined script " + requestPath);

                scriptPaths.put(createScriptPathKey(path, name), requestPath);
                combinedScripts.put(requestPath, new ScriptCombinedResource(sw.toString(), timestamp, realPaths));
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            requestPath = createRequestPath(path, name, resource.getTimestamp());
        }

        return requestPath;
    }

    private static long joinPaths(final PrintWriter writer, final List<String> realPaths) throws IOException {
        log.info("Reading files");

        long timestamp = 0;
        for (String realPath : realPaths) {

            try {
                File file = new File(realPath);
                timestamp = Math.max(timestamp, file.lastModified());
                String contents = Files.toString(file, Charsets.UTF_8);
                writer.println(contents);
            } catch (IOException e) {
                throw new RuntimeException("Could not read file " + realPath, e);
            }
        }
        writer.flush();

        return timestamp;
    }

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
    static String createRequestPath(final String directory, final String id, final long timestamp) {
        String path = String.format("%s-%s.combined", createScriptPathKey(directory, id), timestamp);
        return path;
    }

    private static CombinedResource getCombinedResourceByKey(final String path, final String name) {
        if (containsScriptPath(path, name)) {
            String scriptPath = getScriptPath(path, name);
            return getCombinedResource(scriptPath);
        }
        return null;
    }

}
