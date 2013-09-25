package se.intem.web.taglib.combined;

import java.util.List;

import se.internetapplications.web.taglib.combined.tags.ManagedResource;

public class ScriptCombinedResource extends CombinedResource {

    public ScriptCombinedResource(final String contents, final long timestamp, final String checksum,
            final List<ManagedResource> realPaths) {
        super("text/javascript", contents, timestamp, checksum, realPaths);
    }
}
