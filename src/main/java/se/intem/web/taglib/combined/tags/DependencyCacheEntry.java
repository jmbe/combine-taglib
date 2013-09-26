package se.intem.web.taglib.combined.tags;

import javax.servlet.ServletContext;

import se.intem.web.taglib.combined.node.ConfigurationItem;

public class DependencyCacheEntry {

    long lastread = 0;

    Iterable<String> requires;

    private ConfigurationItem ci;

    public DependencyCacheEntry(final long lastread, final Iterable<String> requires, final ConfigurationItem ci) {
        this.lastread = lastread;
        this.requires = requires;
        this.ci = ci;
    }

    public long getLastread() {
        return lastread;
    }

    public Iterable<String> getRequires() {
        return requires;
    };

    public boolean requiresRefresh(final ConfigurationItem updated, final ServletContext servletContext) {

        boolean changed = !updated.getCss().equals(this.ci.getCss()) || !updated.getJs().equals(this.ci.getJs());
        if (changed) {
            return true;
        }

        long lastChange = ci.getLastChange(servletContext);
        return lastread < lastChange;

    }

}
