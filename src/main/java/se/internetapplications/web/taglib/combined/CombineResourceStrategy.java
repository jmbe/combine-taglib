package se.internetapplications.web.taglib.combined;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import se.internetapplications.web.taglib.combined.tags.ManagedResource;

public interface CombineResourceStrategy {

    /**
     * @return timestamp of newest file
     */
    long combineFiles(PrintWriter pw, List<ManagedResource> realPaths) throws IOException;

    CombinedResource stringToCombinedResource(String contents, long timestamp, List<ManagedResource> realPaths);

}
