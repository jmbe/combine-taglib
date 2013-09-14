package se.internetapplications.web.taglib.combined.tags;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.internetapplications.web.taglib.combined.CombinedResource;
import se.internetapplications.web.taglib.combined.RequestPath;
import se.internetapplications.web.taglib.combined.ResourceType;
import se.internetapplications.web.taglib.combined.ScriptCombinedResource;
import se.internetapplications.web.taglib.combined.node.ConfigurationItem;
import se.internetapplications.web.taglib.combined.node.ResourceLink;

public class LayoutScriptTag extends LayoutTagSupport {

    /** Logger for this class. */
    static final Logger log = LoggerFactory.getLogger(LayoutScriptTag.class);

    public List<ResourceLink> getResources(final ConfigurationItem configuration) {
        return configuration.getJs();
    }

    @Override
    protected String format(final RequestPath path) {
        return String.format("<script type=\"text/javascript\" charset=\"UTF-8\" src=\"%s\"></script>", path);
    }

    public CombinedResource stringToCombinedResource(final String contents, final long timestamp,
            final String checksum, final List<ManagedResource> realPaths) {
        return new ScriptCombinedResource(contents, timestamp, checksum, realPaths);
    }

    @Override
    protected ResourceType getType() {
        return ResourceType.js;
    }

}
