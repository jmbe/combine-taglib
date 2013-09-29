package se.intem.web.taglib.combined.tags;

import javax.servlet.jsp.JspException;

import se.intem.web.taglib.combined.node.ConfigurationItem;
import se.intem.web.taglib.combined.node.ResourceParent;

public class GroupTag extends ConfigurationItemAwareTagSupport implements ResourceParent {

    private ConfigurationItem ci = new ConfigurationItem();

    private DependencyCache cache;

    public GroupTag() {
        this.cache = DependencyCache.get();
    }

    /* Note: setters will be called BEFORE doStartTag, so cleanup must be done after tag is complete. */
    private void cleanup() {
        this.ci = new ConfigurationItem();
    }

    @Override
    public int doEndTag() throws JspException {

        ConfigurationItemsCollection configurations = getConfigurationItems();
        configurations.add(this.ci);

        cache.readDependenciesFromResources(pageContext.getServletContext(), ci);

        cleanup();

        return EVAL_PAGE;
    }

    public void addJavascript(final String js) {
        this.ci.addJavascript(js);
    }

    public void addCss(final String css) {
        this.ci.addCss(css);
    }

    public void setName(final String name) {
        this.ci.setName(name);
    }

    public void setReloadable(final boolean reloadable) {
        this.ci.setReloadable(reloadable);
    }

    public void setRequires(final String requires) {
        this.ci.addRequires(requires);
    }

    public void setLibrary(final boolean library) {
        this.ci.setLibrary(library);
    }

    public void setCombine(final boolean combine) {
        this.ci.setCombine(combine);
    }

    public void setSupportsDevMode(final boolean supportsDevMode) {
        this.ci.setSupportsDevMode(supportsDevMode);
    }

}
