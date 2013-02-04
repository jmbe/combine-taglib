package se.internetapplications.web.taglib.combined.tags;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import se.internetapplications.web.taglib.combined.CombinedResource;
import se.internetapplications.web.taglib.combined.CombinedResourceRepository;
import se.internetapplications.web.taglib.combined.CssCombinedResource;

public class CssTag extends CombinedTagSupport {

    private String media;

    public long combineFiles(final PrintWriter pw, final List<String> realPaths) throws IOException {
        return CombinedResourceRepository.joinPaths(pw, realPaths);
    }

    public CombinedResource stringToCombinedResource(final String contents, final long timestamp,
            final List<String> realPaths) {
        return new CssCombinedResource(contents, timestamp, realPaths);
    }

    @Override
    protected String format(final String path) {
        // TODO use media
        return String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\" />", path);
    }

}
