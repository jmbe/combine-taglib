package se.intem.web.taglib.combined.configuration;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.io.ClasspathResourceLoader;
import se.intem.web.taglib.combined.node.ConfigurationItem;
import se.intem.web.taglib.combined.node.TreeBuilder;

public class CombineJsonConfiguration {

    private static final String JSON_CONFIGURATION = "/combine.json";

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombineJsonConfiguration.class);

    private long lastRead = 0;
    private TreeBuilder tb;

    private String configurationPath = JSON_CONFIGURATION;

    /* Assume reloadable */
    private boolean reloadable = true;

    private Optional<ConfigurationItemsCollection> configuration = Optional.absent();

    private Optional<ServletContext> servletContext = Optional.absent();

    private ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader();

    CombineJsonConfiguration() {
        this.tb = new TreeBuilder();
        this.lastRead = 0;
    }

    @VisibleForTesting
    CombineJsonConfiguration(final String configurationPath) {
        this();
        this.configurationPath = configurationPath;
    }

    public CombineJsonConfiguration withServletContext(final ServletContext servletContext) {
        this.servletContext = Optional.fromNullable(servletContext);
        return this;
    }

    public Optional<ConfigurationItemsCollection> readConfiguration() {
        if (!reloadable && configuration.isPresent()) {
            log.debug("Not reloadable, re-using last configuration");
            return configuration;
        }

        Optional<ManagedResource> optional = findWebInfConfiguration().or(findClasspathConfiguration());

        if (!optional.isPresent()) {
            log.info("Could not find " + configurationPath + " in either classpath or WEB-INF");
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
            log.debug("No changes, re-using last " + configurationPath);
            return configuration;
        }

        log.info("Refreshing " + configurationPath + ", last modified {} > {}", lastModified, lastRead);

        long lastRead = new Date().getTime();
        try {
            this.configuration = Optional.of(tb.parse(managedResource.getInput()));

            /* Items read from file are by default library items. */
            if (configuration.isPresent()) {
                for (ConfigurationItem item : configuration.get()) {
                    item.setLibrary(true);
                }
            }

            /* Update timestamp only if file was successfully read. */
            this.lastRead = lastRead;
        } catch (JsonParseException e) {
            throw new RuntimeException("Syntax error in " + configurationPath, e);
        } catch (IOException e) {
            log.error("Could not parse " + configurationPath, e);
        }

        return this.configuration;
    }

    private Optional<ManagedResource> findWebInfConfiguration() {
        if (!servletContext.isPresent()) {
            return Optional.absent();
        }

        ManagedResource webinf = new ServerPathToManagedResource(servletContext.get(), false).apply(new RequestPath(
                "/WEB-INF" + configurationPath));
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

                return Optional
                        .of(new ManagedResource(configurationPath, null, file.getPath(), url.get().openStream()));

            } catch (URISyntaxException e) {
                return Optional.of(new ManagedResource(configurationPath, null, null, url.get().openStream()));
            }
        } catch (IOException e) {
            log.error("Could not open url " + url, e);
            return Optional.absent();
        }
    }

    private Optional<URL> getClassPathConfigurationUrl() {
        return classpathResourceLoader.findInClasspath(configurationPath);
    }

    public static CombineJsonConfiguration get() {
        return InstanceHolder.instance;
    }

    /**
     * http://en.wikipedia.org/wiki/Singleton_pattern#The_solution_of_Bill_Pugh
     */
    private static class InstanceHolder {
        private static final CombineJsonConfiguration instance = new CombineJsonConfiguration();
    }

}
