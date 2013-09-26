package se.intem.web.taglib.combined;

import com.google.common.collect.Lists;

import java.io.PrintWriter;
import java.util.List;

import se.intem.web.taglib.combined.tags.ManagedResource;

public class CombinedResource {

    private String contentType;
    protected String contents;
    protected long timestamp;
    private List<ManagedResource> filePaths;
    private String checksum;

    public CombinedResource(final String contentType, final String contents, final long timestamp,
            final String checksum, final List<ManagedResource> realPaths) {
        this.contentType = contentType;
        this.contents = contents;
        this.timestamp = timestamp;
        this.checksum = checksum;
        this.filePaths = realPaths;

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

    public String getChecksum() {
        return checksum;
    }

    public boolean hasChangedFile(final List<ManagedResource> resources) {

        if (!areListsEqual(this.filePaths, resources)) {
            return true;
        }

        for (ManagedResource realPath : resources) {

            if (realPath.isTimestampSupported()) {

                if (timestamp < realPath.lastModified()) {
                    return true;
                }
            }

        }
        return false;
    }

    private boolean areListsEqual(final List<ManagedResource> a, final List<ManagedResource> b) {

        if (a.size() != b.size()) {
            return false;
        }

        List<ManagedResource> list1 = Lists.newArrayList(a);
        List<ManagedResource> list2 = Lists.newArrayList(b);

        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }

        return true;
    }

}
