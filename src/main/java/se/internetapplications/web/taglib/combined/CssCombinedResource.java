package se.internetapplications.web.taglib.combined;

import java.util.List;

public class CssCombinedResource extends CombinedResource {

    public CssCombinedResource(final String contents, final long timestamp, final List<String> filePaths) {
        super("text/css", contents, timestamp, filePaths);
    }

}
