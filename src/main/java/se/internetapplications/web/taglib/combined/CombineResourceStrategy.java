package se.internetapplications.web.taglib.combined;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public interface CombineResourceStrategy {

    /**
     * @return timestamp of newest file
     */
    long combineFiles(PrintWriter pw, List<String> realPaths) throws IOException;

    CombinedResource stringToCombinedResource(String contents, long timestamp, List<String> realPaths);

}
