package se.intem.web.taglib.combined.configuration;

import javax.servlet.ServletContext;

import se.intem.web.taglib.combined.node.ConfigurationItem;

public class DependencyCacheEntry {

    long lastread = 0;

    private Iterable<String> requires;

    private Iterable<String> provides;

    private Iterable<String> optionals;

    private ConfigurationItem ci;

    public DependencyCacheEntry(final long lastread, final Iterable<String> requires, final Iterable<String> provides,
            final Iterable<String> optionals, final ConfigurationItem ci) {
        this.lastread = lastread;
        this.requires = requires;
        this.provides = provides;
        this.optionals = optionals;
        this.ci = ci;
    }

    public long getLastread() {
        return lastread;
    }

    public Iterable<String> getRequires() {
        return requires;
    };

    public Iterable<String> getProvides() {
        return provides;
    }

    public Iterable<String> getOptionals() {
        return optionals;
    }

    public boolean requiresRefresh(final ConfigurationItem updated, final ServletContext servletContext) {

        boolean changed = !updated.getCss().equals(this.ci.getCss()) || !updated.getJs().equals(this.ci.getJs());
        if (changed) {
            return true;
        }

        long lastChange = ci.getLastChange(servletContext);
        return lastread < lastChange;

    }

}
