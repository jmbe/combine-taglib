package se.intem.web.taglib.combined.io;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple resource loader to avoid dependencies such as net.sf.corn:corn-cps (which is exhaustive, but too slow (200+ ms
 * scanning)) or org.springframework:spring-core (PathMatchingResourcePatternResolver is fast (less than 1 ms scanning
 * time)).
 */
public class ClasspathResourceLoader {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(ClasspathResourceLoader.class);

    public Optional<URL> findOneInClasspath(final String resourceName) {
        try {
            return Optional.of(Resources.getResource(resourceName));
        } catch (IllegalArgumentException e) {
            /* fall-through to next */
        }

        try {
            return Optional.of(Resources.getResource(Resources.class, resourceName));
        } catch (IllegalArgumentException e) {
            return Optional.absent();
        }
    }

    public List<URL> findManyInClasspath(final String resourceName) {
        String path = resourceName;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return tryMany(path);
    }

    /**
     * @param path
     *            path from root, but excluding leading slash.
     */
    private List<URL> tryMany(final String path) {

        try {
            ClassLoader cl = getDefaultClassLoader();
            Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(path) : ClassLoader.getSystemResources(path));
            List<URL> result = Collections.list(resourceUrls);

            if (result.isEmpty()) {

                Optional<URL> one = findOneInClasspath(path);
                if (one.isPresent()) {
                    log.warn(
                            "Could not find any results using 'many' strategy, but found single {}. Many strategy needs updating.",
                            one.get());
                    result.add(one.get());
                }
            }

            return result;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /* Adapted from Spring ClassUtils. Also see PathMatchingResourcePatternResolver. */
    private static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClasspathResourceLoader.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }
}
