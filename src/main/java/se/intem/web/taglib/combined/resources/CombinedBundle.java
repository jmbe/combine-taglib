package se.intem.web.taglib.combined.resources;

import com.google.common.hash.Hashing;

import java.io.PrintWriter;

import se.intem.web.taglib.combined.ResourceType;

public class CombinedBundle {

    private long lastread;

    private StringBuilder contents = new StringBuilder();
    private ResourceType type;

    public CombinedBundle(final ResourceType type, final long lastread) {
        this.type = type;
        this.lastread = lastread;
    }

    public String getContents() {
        return contents.toString();
    }

    public long getLastread() {
        return lastread;
    }

    public ResourceType getType() {
        return type;
    }

    public String getChecksum() {
        return Hashing.md5().hashUnencodedChars(getContents()).toString();
    }

    public void addContents(final String contents) {
        this.contents.append(contents + "\r\n");
    }

    public String getContentType() {
        return type.getContentType();
    }

    public void write(final PrintWriter writer) {
        writer.write(getContents());
    }

}
