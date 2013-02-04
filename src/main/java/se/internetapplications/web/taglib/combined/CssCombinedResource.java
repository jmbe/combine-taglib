package se.internetapplications.web.taglib.combined;

import java.util.List;

import se.internetapplications.web.taglib.combined.tags.ManagedResource;

public class CssCombinedResource extends CombinedResource {

    public CssCombinedResource(final String contents, final long timestamp, final List<ManagedResource> realPaths) {
        super("text/css", contents, timestamp, realPaths);
    }

}
