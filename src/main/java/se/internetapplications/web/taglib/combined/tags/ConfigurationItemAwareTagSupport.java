package se.internetapplications.web.taglib.combined.tags;

import com.google.common.collect.Lists;

import java.util.List;

import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.internetapplications.web.taglib.combined.node.ConfigurationItem;

public abstract class ConfigurationItemAwareTagSupport extends BodyTagSupport {

    public static final String REQUEST_CONFIGURATION_ITEMS_KEY = "combine_configuration_items";

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(ConfigurationItemAwareTagSupport.class);

    public List<ConfigurationItem> getConfigurationItems() {
        @SuppressWarnings("unchecked")
        List<ConfigurationItem> scripts = (List<ConfigurationItem>) pageContext.getRequest().getAttribute(
                REQUEST_CONFIGURATION_ITEMS_KEY);
        log.debug("Configuration items: {}", scripts);

        if (scripts == null) {
            log.debug("Creating new list");
            return Lists.newArrayList();
        }

        return scripts;
    }
}
