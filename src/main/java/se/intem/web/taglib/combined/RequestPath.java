package se.intem.web.taglib.combined;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

public class RequestPath {
    private String path;

    public static final Predicate<RequestPath> isRemote = new Predicate<RequestPath>() {

        public boolean apply(final RequestPath item) {
            return item.isRemote();
        }
    };

    public RequestPath(final String path) {
        Preconditions.checkNotNull(path);
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

    public boolean isRemote() {
        return path.contains("://") || path.startsWith("//");
    }

    public String dirname() {

        int index = path.lastIndexOf("/");
        if (index > -1) {
            return path.substring(0, index);
        }

        return "";
    }

    private boolean isAbsolute() {
        return path.startsWith("/");
    }

    public boolean isRelative() {
        return !isRemote() && !isAbsolute() && !path.startsWith("data:");
    }

}
