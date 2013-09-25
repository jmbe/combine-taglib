package se.intem.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public CombineJsonConfiguration() {
        this.tb = new TreeBuilder();
        this.lastRead = 0;
    }

    public Optional<ConfigurationItemsCollection> readConfiguration() {
        if (!reloadable && configuration.isPresent()) {
            log.info("Not reloadable, re-using last configuration");
            return configuration;
        }

        Optional<URL> url = getConfigurationUrl();

        if (!url.isPresent()) {
            log.debug("Could not find " + JSON_CONFIGURATION + " in classpath");
            this.configuration = Optional.absent();
            return this.configuration;
        }

        long lastModified = 0;
        try {
            lastModified = getLastModified(url.get());
        } catch (IllegalArgumentException e) {
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
            this.configuration = Optional.of(tb.parse(url.get().openStream()));

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

    /**
     * @throws IllegalArgumentException
     *             if url does not support timestamp
     */
    private long getLastModified(final URL url) throws IllegalArgumentException {
        try {
            File file = new File(url.toURI());
            if (!file.exists()) {
                throw new IllegalArgumentException("Could not find file " + file);
            }

            return file.lastModified();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public Optional<URL> getConfigurationUrl() {
        try {
            return Optional.of(Resources.getResource(JSON_CONFIGURATION));
        } catch (IllegalArgumentException e) {
            /* fall-through to next */
        }

        try {
            return Optional.of(Resources.getResource(Resources.class, JSON_CONFIGURATION));
        } catch (IllegalArgumentException e2) {
            return null;
        }
    }
}
