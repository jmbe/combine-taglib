package se.intem.web.taglib.combined.io;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ClasspathResourceLoader {

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
        try {
            Enumeration<URL> resources = this.getClass().getClassLoader().getResources(resourceName);
            return Collections.list(resources);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

}
