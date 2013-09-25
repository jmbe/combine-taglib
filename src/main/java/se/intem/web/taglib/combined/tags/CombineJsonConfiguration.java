package se.intem.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.node.ConfigurationItem;
import se.intem.web.taglib.combined.node.TreeBuilder;

public class CombineJsonConfiguration {

    private static final String JSON_CONFIGURATION = "/combine.json";

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombineJsonConfiguration.class);

    private long lastRead = 0;
    private TreeBuilder tb;

    /* Assume reloadable */
    private boolean reloadable = true;

    private Optional<ConfigurationItemsCollection> configuration = Optional.absent();

    private Optional<ServletContext> servletContext = Optional.absent();

    public CombineJsonConfiguration() {
        this.tb = new TreeBuilder();
        this.lastRead = 0;
    }

    public CombineJsonConfiguration(final ServletContext servletContext) {
        this();
        this.servletContext = Optional.fromNullable(servletContext);
    }

    public Optional<ConfigurationItemsCollection> readConfiguration() {
        if (!reloadable && configuration.isPresent()) {
            log.debug("Not reloadable, re-using last configuration");
            return configuration;
        }

        Optional<ManagedResource> optional = findWebInfConfiguration().or(findClasspathConfiguration());

        if (!optional.isPresent()) {
            log.info("Could not find " + JSON_CONFIGURATION + " in either classpath or WEB-INF");
            this.configuration = Optional.absent();
            return this.configuration;
        }

        ManagedResource managedResource = optional.get();
        log.debug("Using configuration {}", managedResource);

        long lastModified = 0;
        if (managedResource.isTimestampSupported()) {
            lastModified = managedResource.lastModified();
        } else {
            /* expected for war files */
            reloadable = false;
        }

        if (lastModified < lastRead) {
            log.debug("No changes, re-using last " + JSON_CONFIGURATION);
            return configuration;
        }

        log.info("Refreshing " + JSON_CONFIGURATION + ", last modified {}", lastModified);

        try {
            this.lastRead = new Date().getTime();
            this.configuration = Optional.of(tb.parse(managedResource.getInput()));

            /* Items read from file are by default library items. */
            if (configuration.isPresent()) {
                for (ConfigurationItem item : configuration.get()) {
                    item.setLibrary(true);
                }
            }

        } catch (IOException e) {
            log.error("Could not parse " + JSON_CONFIGURATION, e);
        }

        return this.configuration;
    }

    private Optional<ManagedResource> findWebInfConfiguration() {
        if (!servletContext.isPresent()) {
            return Optional.absent();
        }

        ManagedResource webinf = new ServerPathToManagedResource(servletContext.get()).apply(new RequestPath("/WEB-INF"
                + JSON_CONFIGURATION));
        if (webinf.exists()) {
            return Optional.of(webinf);
        }

        return Optional.absent();

    }

    /**
     * Try to find configuration file in classpath.
     */
    private Optional<ManagedResource> findClasspathConfiguration() {
        Optional<URL> url = getClassPathConfigurationUrl();

        if (!url.isPresent()) {
            return Optional.absent();
        }

        try {

            try {
                File file = new File(url.get().toURI());
                if (!file.exists()) {
                    throw new IllegalArgumentException("Could not find file " + file);
                }

                return Optional.of(new ManagedResource(JSON_CONFIGURATION, file.getPath(), url.get().openStream()));

            } catch (URISyntaxException e) {
                return Optional.of(new ManagedResource(JSON_CONFIGURATION, null, url.get().openStream()));
            }
        } catch (IOException e) {
            log.error("Could not open url " + url, e);
            return Optional.absent();
        }
    }

    private Optional<URL> getClassPathConfigurationUrl() {
        try {
            return Optional.of(Resources.getResource(JSON_CONFIGURATION));
        } catch (IllegalArgumentException e) {
            /* fall-through to next */
        }

        try {
            return Optional.of(Resources.getResource(Resources.class, JSON_CONFIGURATION));
        } catch (IllegalArgumentException e) {
            return Optional.absent();
        }
    }
}
