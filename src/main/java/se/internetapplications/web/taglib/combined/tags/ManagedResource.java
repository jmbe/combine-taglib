package se.internetapplications.web.taglib.combined.tags;

import com.google.common.io.InputSupplier;

import java.io.File;
import java.io.InputStream;

public class ManagedResource {

    private String name;
    private String realPath;
    private InputStream input;

    public ManagedResource(final String name, final String realPath, final InputStream input) {
        this.name = name;
        this.realPath = realPath;
        this.input = input;
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

    public InputSupplier<InputStream> getInputSupplicer() {
        return new InputSupplier<InputStream>() {
            public InputStream getInput() {
                return input;
            }
        };
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ManagedResource)) {
            return false;
        }

        ManagedResource other = (ManagedResource) o;

        return this.name.equals(other.name);
    }

    public long getTimestamp() {
        File file = new File(realPath);
        return file.lastModified();
    }

}
