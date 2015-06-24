package se.intem.web.taglib.combined.io;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

import java.net.URL;

public class ClasspathResourceLoader {

    public Optional<URL> findInClasspath(final String resourceName) {
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

}
