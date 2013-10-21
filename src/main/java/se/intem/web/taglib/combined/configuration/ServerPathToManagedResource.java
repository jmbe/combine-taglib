package se.intem.web.taglib.combined.configuration;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import java.io.InputStream;

import javax.servlet.ServletContext;

import se.intem.web.taglib.combined.RequestPath;

public class ServerPathToManagedResource implements Function<RequestPath, ManagedResource> {

    private ServletContext servletContext;
    private boolean required = true;

    public ServerPathToManagedResource(final ServletContext servletContext, final boolean required) {
        Preconditions.checkNotNull(servletContext);
        this.servletContext = servletContext;
        this.required = required;
    }

    public ManagedResource apply(final RequestPath requestPath) {
        if (requestPath.isRemote()) {
            return new ManagedResource(requestPath.getPath(), requestPath, null, null);
        }

        String realPath = servletContext.getRealPath(requestPath.getPath());
        InputStream input = servletContext.getResourceAsStream(requestPath.getPath());

        if (required && input == null) {
            throw new RuntimeException("Could not find local file '" + requestPath.getPath() + "'. Check spelling or path.");
        }

        return new ManagedResource(requestPath.getPath(), requestPath, realPath, input);
    }

}
