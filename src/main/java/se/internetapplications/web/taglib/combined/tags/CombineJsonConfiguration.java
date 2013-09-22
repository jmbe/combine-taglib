package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import se.internetapplications.web.taglib.combined.node.ConfigurationItem;
import se.internetapplications.web.taglib.combined.node.TreeBuilder;

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

        ClassPathResource resource = new ClassPathResource(JSON_CONFIGURATION);

        if (!resource.exists()) {
            log.info("Could not find " + JSON_CONFIGURATION + " in classpath");
            this.configuration = Optional.absent();
            return this.configuration;
        }

        long lastModified = 0;
        try {
            lastModified = resource.lastModified();
        } catch (IOException e) {
            /* expected */
            reloadable = false;
        }

        if (lastModified < lastRead) {
            log.info("No changes, re-using last " + JSON_CONFIGURATION);
            return configuration;
        }

        log.info("Refreshing " + JSON_CONFIGURATION + ", last modified {}", lastModified);

        try {
            this.lastRead = new Date().getTime();
            this.configuration = Optional.of(tb.parse(resource.getInputStream()));

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
}
