package se.internetapplications.web.taglib.combined;

import java.util.List;

public class ScriptCombinedResource extends CombinedResource {

    public ScriptCombinedResource(final String contents, final long timestamp,
            final List<String> filePaths) {
        super("text/javascript", contents, timestamp, filePaths);
    }
}
