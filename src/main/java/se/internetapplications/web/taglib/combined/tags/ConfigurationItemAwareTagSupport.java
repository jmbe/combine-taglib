package se.internetapplications.web.taglib.combined.tags;

import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfigurationItemAwareTagSupport extends BodyTagSupport {

    public static final String REQUEST_CONFIGURATION_ITEMS_KEY = "combine_configuration_items";

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(ConfigurationItemAwareTagSupport.class);

    public ConfigurationItemsCollection getConfigurationItems() {
        ConfigurationItemsCollection collection = (ConfigurationItemsCollection) pageContext.getRequest().getAttribute(
                REQUEST_CONFIGURATION_ITEMS_KEY);
        log.debug("Configuration items: {}", collection);

        if (collection == null) {
            log.debug("Creating new list");
            return new ConfigurationItemsCollection();
        }

        return collection;
    }
}
