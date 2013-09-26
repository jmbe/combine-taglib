package se.intem.web.taglib.combined.tags;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import javax.servlet.ServletContext;

import se.intem.web.taglib.combined.RequestPath;

public class ServerPathToManagedResource implements Function<RequestPath, ManagedResource> {

    private ServletContext servletContext;

    public ServerPathToManagedResource(final ServletContext servletContext) {
        Preconditions.checkNotNull(servletContext);
        this.servletContext = servletContext;
    }

    public ManagedResource apply(final RequestPath element) {
        if (element.isRemote()) {
            return new ManagedResource(element.getPath(), null, null);
        }
        return new ManagedResource(element.getPath(), servletContext.getRealPath(element.getPath()),
                servletContext.getResourceAsStream(element.getPath()));
    }

}
