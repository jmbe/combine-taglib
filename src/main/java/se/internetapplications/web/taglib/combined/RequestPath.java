package se.internetapplications.web.taglib.combined;

import com.google.common.base.Objects;

public class RequestPath {
    private String path;

    public RequestPath(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof RequestPath)) {
            return false;
        }

        RequestPath that = (RequestPath) obj;

        return Objects.equal(this.path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }

    @Override
    public String toString() {
        return path;
    }
}
