package se.intem.web.taglib.combined.resources;

import com.google.common.hash.Hashing;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.ResourceType;

public class CombinedBundle implements RequestPathBundle {

    private long lastread;

    private StringBuilder contents = new StringBuilder();
    private ResourceType type;

    private ResourceName name;

    private RequestPath requestPath;

    public CombinedBundle(final ResourceName name, final ResourceType type, final long lastread) {
        this.name = name;
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

    public RequestPath getRequestPath() {
        return this.requestPath;
    }

    public void setRequestPath(final RequestPath requestPath) {
        this.requestPath = requestPath;
    }

    public ResourceName getName() {
        return name;
    }

    @Override
    public List<RequestPath> getPaths() {
        return Collections.singletonList(requestPath);
    }
}
