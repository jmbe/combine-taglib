package se.internetapplications.web.taglib.combined.node;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import se.internetapplications.web.taglib.combined.RequestPath;
import se.internetapplications.web.taglib.combined.ResourceType;
import se.internetapplications.web.taglib.combined.servlet.CombinedConfigurationHolder;
import se.internetapplications.web.taglib.combined.tags.ManagedResource;
import se.internetapplications.web.taglib.combined.tags.ServerPathToManagedResource;

/**
 * Limitation: ConfigurationItem can contain only either remote or local resources, not both. If they contain both, then
 * no combining will be made on any resources.
 */
public class ConfigurationItem implements ResourceParent {

    private String name;
    private boolean reloadable = true;
    private boolean library = false;
    private boolean combine = true;

    private LinkedHashSet<String> requires = Sets.newLinkedHashSet();
    private List<RequestPath> js = Lists.newArrayList();
    private List<RequestPath> css = Lists.newArrayList();
    private boolean supportsDevMode;

    public List<String> getRequires() {
        return Lists.newArrayList(requires);
    }

    public void setRequires(final List<String> requires) {
        for (String require : requires) {
            addRequires(require);
        }
    }

    public List<RequestPath> getJs() {
        return js;
    }

    public void setJs(final List<RequestPath> js) {
        this.js = js;
    }

    public List<RequestPath> getCss() {
        return css;
    }

    public void setCss(final List<RequestPath> css) {
        this.css = css;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isReloadable() {
        return reloadable && !isRemote();
    }

    public boolean isRemote() {
        Optional<RequestPath> remoteJs = FluentIterable.from(js).firstMatch(RequestPath.isRemote);
        Optional<RequestPath> remoteCss = FluentIterable.from(css).firstMatch(RequestPath.isRemote);
        return remoteJs.isPresent() || remoteCss.isPresent();
    }

    public void setReloadable(final boolean reloadable) {
        this.reloadable = reloadable;
    }

    public boolean isLibrary() {
        return library;
    }

    public void setLibrary(final boolean library) {
        this.library = library;
    }

    @Override
    public void addJavascript(final String js) {
        this.js.add(new RequestPath(js));
    }

    @Override
    public void addCss(final String css) {
        this.css.add(new RequestPath(css));
    }

    public void addRequires(final String requires) {
        Iterable<String> split = Splitter.on(CharMatcher.anyOf(" ,")).trimResults().omitEmptyStrings()
                .split(Strings.nullToEmpty(requires));
        this.requires.addAll(FluentIterable.from(split).toList());
    }

    public boolean isCombine() {
        return combine;
    }

    public void setCombine(final boolean combine) {
        this.combine = combine;
    }

    public void setSupportsDevMode(final boolean supportsDevMode) {
        this.supportsDevMode = supportsDevMode;
    }

    public boolean isSupportsDevMode() {
        return supportsDevMode;
    }

    @Override
    public String toString() {
        return Strings.nullToEmpty(name);
    }

    public void addRequires(final Iterable<String> requires) {
        for (String require : requires) {
            addRequires(require);
        }
    }

    public List<RequestPath> getPaths(final ResourceType type) {
        if (ResourceType.js.equals(type)) {
            return getJs();
        }

        return getCss();
    }

    public boolean hasResources(final ResourceType type) {
        return !getPaths(type).isEmpty();
    }

    public Map<ResourceType, List<ManagedResource>> getRealPaths(final ServletContext servletContext) {
        Map<ResourceType, List<ManagedResource>> result = Maps.newHashMap();

        ResourceType[] values = ResourceType.values();
        for (ResourceType resourceType : values) {
            List<ManagedResource> realPaths = FluentIterable.from(getPaths(resourceType))
                    .transform(new ServerPathToManagedResource(servletContext)).toList();
            if (!realPaths.isEmpty()) {
                result.put(resourceType, realPaths);
            }
        }

        return result;
    }

    public boolean shouldBeCombined() {
        return !((CombinedConfigurationHolder.isDevMode() && isSupportsDevMode()) || !isCombine() || isRemote());
    }

    public long getLastChange(final ServletContext servletContext) {
        Map<ResourceType, List<ManagedResource>> map = getRealPaths(servletContext);
        Iterable<ManagedResource> realPaths = Iterables.concat(map.values());

        long result = 0;
        for (ManagedResource managedResource : realPaths) {
            if (!managedResource.isTimestampSupported()) {
                continue;
            }

            result = Math.max(result, managedResource.getTimestamp());
        }

        return result;
    }

}
