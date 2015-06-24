package se.intem.web.taglib.combined.configuration;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.io.ByteSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import se.intem.web.taglib.combined.RequestPath;

public class ManagedResource {

    private String name;
    private String realPath;
    private InputStream input;
    private RequestPath requestPath;

    public ManagedResource(final String name, final RequestPath requestPath, final String realPath,
            final InputStream input) {
        this.name = name;
        this.realPath = realPath;
        this.input = input;
        this.requestPath = requestPath;
    }

    public boolean isTimestampSupported() {
        return this.realPath != null;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRealPath() {
        return realPath;
    }

    public void setRealPath(final String realPath) {
        this.realPath = realPath;
    }

    public InputStream getInput() {
        return input;
    }

    public void setInput(final InputStream input) {
        this.input = input;
    }

    public ByteSource getByteSource() {
        return new ByteSource() {
            public InputStream openStream() throws IOException {
                return input;
            }
        };
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ManagedResource)) {
            return false;
        }

        ManagedResource other = (ManagedResource) o;

        return this.name.equals(other.name);
    }

    public long lastModified() {
        File file = new File(realPath);
        return file.lastModified();
    }

    public boolean exists() {
        return this.input != null;
    }

    public RequestPath getRequestPath() {
        return requestPath;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("realPath", realPath)
                .add("input", exists() ? "provided" : "missing").toString();
    }

    public boolean isRemote() {
        return requestPath.isRemote();
    }

}
