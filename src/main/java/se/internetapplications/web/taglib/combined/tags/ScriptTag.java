package se.internetapplications.web.taglib.combined.tags;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import se.internetapplications.web.taglib.combined.CombinedResource;
import se.internetapplications.web.taglib.combined.CombinedResourceRepository;
import se.internetapplications.web.taglib.combined.ScriptCombinedResource;

public class ScriptTag extends CombinedTagSupport {

    protected String format(final String path) {
        return String.format("<script type=\"text/javascript\" charset=\"UTF-8\" src=\"%s\"></script>\r\n", path);
    }

    public long combineFiles(final PrintWriter pw, final List<ManagedResource> realPaths) throws IOException {
        // return minify ? yuiCompressPaths(pw, realPaths) : joinPaths(pw, realPaths);
        return CombinedResourceRepository.joinPaths(pw, realPaths);
    }

    public CombinedResource stringToCombinedResource(final String s, final long timestamp,
            final List<ManagedResource> realPaths) {
        return new ScriptCombinedResource(s, timestamp, realPaths);
    }
}
