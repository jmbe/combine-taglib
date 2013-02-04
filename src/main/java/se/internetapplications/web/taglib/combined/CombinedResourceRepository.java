package se.internetapplications.web.taglib.combined;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
        scriptPaths = new HashMap<String, String>();
        combinedScripts = new HashMap<String, CombinedResource>();
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

    public static String addCombinedScripts(final String path, final String name, final List<String> realPaths) {

        if (path == null) {
            throw new NullPointerException("Path cannot be null.");
        }

        if (name == null) {
            throw new NullPointerException("Name cannot be null.");
        }

        if (realPaths == null) {
            throw new NullPointerException("Real paths cannot be null.");
        }

        String requestPath = null;

        CombinedResource resource = getCombinedResourceByKey(path, name);

        if (resource == null || resource.hasChangedFile(realPaths)) {
            log.info(String.format("Modified resource '%s' detected. Rebuilding...", name));
            try {

                final StringWriter sw = new StringWriter();

                YuiCompressorWriter yuiWriter = new YuiCompressorWriter(sw);

                log.info("Starting compressor thread");
                Thread compressorThread = new Thread(yuiWriter);
                compressorThread.start();

                log.info("Reading files");

                long timestamp = 0;
                for (String realPath : realPaths) {

                    try {
                        File file = new File(realPath);
                        timestamp = Math.max(timestamp, file.lastModified());
                        String contents = FileUtils.readFileToString(file, "UTF-8");
                        yuiWriter.write(contents);
                    } catch (IOException e) {
                        throw new RuntimeException("Could not read file " + realPath, e);
                    }
                }
                yuiWriter.flush();
                yuiWriter.close();

                compressorThread.join();

                requestPath = createRequestPath(path, name, timestamp);

                log.info("Adding combined script " + requestPath);

                scriptPaths.put(createScriptPathKey(path, name), requestPath);
                combinedScripts.put(requestPath, new ScriptCombinedResource(sw.toString(), timestamp, realPaths));
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            requestPath = createRequestPath(path, name, resource.getTimestamp());
        }

        return requestPath;
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
