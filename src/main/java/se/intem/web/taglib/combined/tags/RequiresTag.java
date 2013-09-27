package se.intem.web.taglib.combined.tags;

import java.util.UUID;

import javax.servlet.jsp.JspException;

import se.intem.web.taglib.combined.node.ConfigurationItem;

public class RequiresTag extends ConfigurationItemAwareTagSupport {

    private ConfigurationItem configurationItem = new ConfigurationItem();

    @Override
    public int doEndTag() throws JspException {

        if (configurationItem.hasDependencies()) {
            configurationItem.setName(UUID.randomUUID().toString());
            getConfigurationItems().add(configurationItem);
        }

        cleanup();
        return EVAL_PAGE;
    }

    /* Note: setters will be called BEFORE doStartTag, so cleanup must be done after tag is complete. */
    private void cleanup() {
        this.configurationItem = new ConfigurationItem();
    }

    public void setRequires(final String requires) {
        this.configurationItem.addRequires(requires);
    }

}
