package se.internetapplications.web.taglib.combined.tags;

import java.util.List;

import javax.servlet.jsp.JspException;

import se.internetapplications.web.taglib.combined.node.ConfigurationItem;
import se.internetapplications.web.taglib.combined.node.ResourceParent;

public class CombinedResourceTag extends ConfigurationItemAwareTagSupport implements ResourceParent {

    private ConfigurationItem configurationItem = new ConfigurationItem();

    /* Note: setters will be called BEFORE doStartTag, so cleanup must be done after tag is complete. */
    private void cleanup() {
        this.configurationItem = new ConfigurationItem();
    }

    @Override
    public int doEndTag() throws JspException {

        List<ConfigurationItem> configurations = getConfigurationItems();
        configurations.add(this.configurationItem);
        pageContext.getRequest().setAttribute(ConfigurationItemAwareTagSupport.REQUEST_CONFIGURATION_ITEMS_KEY,
                configurations);

        cleanup();

        return EVAL_PAGE;
    }

    public void addJavascript(final String js) {
        this.configurationItem.addJavascript(js);
    }

    public void addCss(final String css) {
        this.configurationItem.addCss(css);
    }

    public void setName(final String name) {
        this.configurationItem.setName(name);
    }

    public void setReloadable(final boolean reloadable) {
        this.configurationItem.setReloadable(reloadable);
    }

    public void setRequires(final String requires) {
        this.configurationItem.addRequires(requires);
    }

    public void setLibrary(final boolean library) {
        this.configurationItem.setLibrary(library);
    }

    public void setEnabled(final boolean enabled) {
        this.configurationItem.setEnabled(enabled);
    }

}
