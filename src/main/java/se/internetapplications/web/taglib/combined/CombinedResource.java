package se.internetapplications.web.taglib.combined;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CombinedResource {

    private String contentType;
    protected String contents;
    protected long timestamp;
    private List<String> filePaths;

    public CombinedResource(final String contentType, final String contents,
            final long timestamp, final List<String> filePaths) {
        this.contentType = contentType;
        this.contents = contents;
        this.timestamp = timestamp;
        this.filePaths = filePaths;

    }

    public String getContentType() {
        return contentType;
    }

    public void writeMinifiedResource(final PrintWriter writer) {
        writer.write(getContents());

    }

    public String getContents() {
        return contents;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean hasChangedFile(final List<String> realPaths) {

        if (!areListsEqual(this.filePaths, realPaths)) {
            return true;
        }

        for (String realPath : realPaths) {
            File file = new File(realPath);

            if (timestamp < file.lastModified()) {
                return true;
            }
        }
        return false;
    }

    private boolean areListsEqual(final List<String> a, final List<String> b) {

        if (a.size() != b.size()) {
            return false;
        }

        List<String> list1 = new ArrayList<String>(a);
        List<String> list2 = new ArrayList<String>(b);

        Collections.sort(list1);
        Collections.sort(list2);

        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }

        return true;
    }

}
