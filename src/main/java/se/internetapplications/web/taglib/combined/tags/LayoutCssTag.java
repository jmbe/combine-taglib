package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Strings;

import java.util.List;

import se.internetapplications.web.taglib.combined.CombinedResource;
import se.internetapplications.web.taglib.combined.CssCombinedResource;
import se.internetapplications.web.taglib.combined.ResourceType;
import se.internetapplications.web.taglib.combined.node.ConfigurationItem;
import se.internetapplications.web.taglib.combined.node.ResourceLink;

public class LayoutCssTag extends LayoutTagSupport {

    private String media = null; // TODO support media

    public List<ResourceLink> getResources(final ConfigurationItem configuration) {
        return configuration.getCss();
    }

    @Override
    protected String format(final String path) {
        String mediaAttribute = "";
        if (!Strings.isNullOrEmpty(media)) {
            mediaAttribute = "media=\"" + media + "\" ";
        }

        return String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\" %s/>", path, mediaAttribute);
    }

    public CombinedResource stringToCombinedResource(final String contents, final long timestamp,
            final String checksum, final List<ManagedResource> realPaths) {
        return new CssCombinedResource(contents, timestamp, checksum, realPaths);
    }

    public void setMedia(final String media) {
        this.media = media;
    }

    public String getMedia() {
        return media;
    }

    @Override
    protected ResourceType getType() {
        return ResourceType.css;
    }

}
