package se.intem.web.taglib.combined.configuration;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.io.ClasspathResourceLoader;

public class ServerPathToManagedResource implements Function<RequestPath, ManagedResource> {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(ServerPathToManagedResource.class);

    private ServletContext servletContext;
    private boolean required = true;

    private ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader();

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

        log.trace("Found file in context path {}", realPath);

        InputStream input = servletContext.getResourceAsStream(requestPath.getPath());

        if (input == null) {
            return tryClassPath(requestPath);
        }

        return new ManagedResource(requestPath.getPath(), requestPath, realPath, input);

    }

    private ManagedResource tryClassPath(final RequestPath requestPath) {
        Optional<URL> url = classpathResourceLoader.findInClasspath(requestPath.getPath());

        if (required && !url.isPresent()) {
            throw new RuntimeException("Could not find local file '" + requestPath.getPath()
                    + "'. Check spelling or path.");
        }

        try {
            URL resource = url.get();
            String file = resource.getFile();
            log.trace("Found file in classpath {}", file);
            return new ManagedResource(requestPath.getPath(), requestPath, file, resource.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
