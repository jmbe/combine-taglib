package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Strings;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import se.internetapplications.web.taglib.combined.CombinedResource;
import se.internetapplications.web.taglib.combined.CombinedResourceRepository;
import se.internetapplications.web.taglib.combined.CssCombinedResource;

public class CssTag extends CombinedTagSupport {

    private String media;

    public long combineFiles(final PrintWriter pw, final List<ManagedResource> realPaths) throws IOException {
        return CombinedResourceRepository.joinPaths(pw, realPaths);
    }

    public CombinedResource stringToCombinedResource(final String contents, final long timestamp,
            final String checksum, final List<ManagedResource> realPaths) {
        return new CssCombinedResource(contents, timestamp, checksum, realPaths);
    }

    @Override
    protected String format(final String path) {
        String mediaAttribute = "";
        if (!Strings.isNullOrEmpty(media)) {
            mediaAttribute = "media=\"" + media + "\" ";
        }

        return String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\" %s/>", path, mediaAttribute);
    }

    public void setMedia(final String media) {
        this.media = media;
    }

}
