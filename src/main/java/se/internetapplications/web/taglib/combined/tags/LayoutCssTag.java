package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Strings;

import java.util.List;

import javax.servlet.jsp.JspException;

import se.intem.web.taglib.combined.CombinedResource;
import se.intem.web.taglib.combined.CssCombinedResource;
import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.ResourceType;
import se.intem.web.taglib.combined.node.ConfigurationItem;

public class LayoutCssTag extends LayoutTagSupport {

    private String media = null; // TODO support media

    public List<RequestPath> getResources(final ConfigurationItem configuration) {
        return configuration.getCss();
    }

    @Override
    protected String format(final RequestPath path) {
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

    @Override
    protected void outputInlineResources(final ConfigurationItemsCollection configurationItems) throws JspException {
        List<String> inlineStyles = configurationItems.getInlineStyles();

        if (inlineStyles.isEmpty()) {
            return;
        }

        for (String inline : inlineStyles) {
            String output = String.format("<style>%s</style>", inline);
            println(output);
        }
    }

    @Override
    protected void beforeResolve(final ConfigurationItemsCollection configurationItems) {
        /* nothing to do */
    }

}
