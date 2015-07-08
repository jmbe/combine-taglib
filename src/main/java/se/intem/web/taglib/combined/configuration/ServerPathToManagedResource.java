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

        /* Prefer classpath since Eclipse wtp will sometimes fail to update file in servlet context path. */
        Optional<ManagedResource> result = tryRemote(requestPath).or(
                tryClassPath(requestPath).or(tryServletContextPath(requestPath)));

        if (required && !result.isPresent()) {
            throw new RuntimeException("Could not find local file '" + requestPath.getPath()
                    + "'. Check spelling or path.");
        }

        return result.get();
    }

    private Optional<ManagedResource> tryRemote(final RequestPath requestPath) {
        if (requestPath.isRemote()) {
            return Optional.of(new ManagedResource(requestPath.getPath(), requestPath, null, null));
        }

        return Optional.absent();
    }

    private Optional<ManagedResource> tryServletContextPath(final RequestPath requestPath) {

        String realPath = servletContext.getRealPath(requestPath.getPath());
        InputStream input = servletContext.getResourceAsStream(requestPath.getPath());

        if (input == null) {
            return Optional.absent();
        }

        log.trace("Found file in context path {}", realPath);
        return Optional.of(new ManagedResource(requestPath.getPath(), requestPath, realPath, input));
    }

    private Optional<ManagedResource> tryClassPath(final RequestPath requestPath) {
        String path = requestPath.getPath();
        return tryClassPath(requestPath, path).or(tryClassPath(requestPath, "/META-INF/resources" + path));
    }

    private Optional<ManagedResource> tryClassPath(final RequestPath requestPath, final String path) {
        Optional<URL> url = classpathResourceLoader.findInClasspath(path);

        if (!url.isPresent()) {
            return Optional.absent();
        }

        try {
            URL resource = url.get();
            String file = resource.getFile();
            log.trace("Found file in classpath {}", file);
            return Optional.of(new ManagedResource(path, requestPath, file, resource.openStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
