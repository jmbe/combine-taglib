package se.internetapplications.web.taglib.combined.node;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Limitation: ConfigurationItem can contain only either remote or local resources, not both. If they contain both, then
 * no combining will be made on any resources.
 */
public class ConfigurationItem implements ResourceParent {

    private String name;
    private boolean reloadable = true;
    private boolean library = false;
    private boolean combine = true;

    private List<String> requires = Lists.newArrayList();
    private List<ResourceLink> js = Lists.newArrayList();
    private List<ResourceLink> css = Lists.newArrayList();
    private boolean supportsDevMode;

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(final List<String> requires) {
        this.requires = requires;
    }

    public List<ResourceLink> getJs() {
        return js;
    }

    public void setJs(final List<ResourceLink> js) {
        this.js = js;
    }

    public List<ResourceLink> getCss() {
        return css;
    }

    public void setCss(final List<ResourceLink> css) {
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
        Optional<ResourceLink> optional = FluentIterable.from(js).firstMatch(ResourceLink.isRemote);
        return optional.isPresent() || FluentIterable.from(css).firstMatch(ResourceLink.isRemote).isPresent();
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
        this.js.add(new ResourceLink(js));
    }

    @Override
    public void addCss(final String css) {
        this.css.add(new ResourceLink(css));
    }

    public void addRequires(final String requires) {
        Iterable<String> split = Splitter.on(",").trimResults().omitEmptyStrings().split(Strings.nullToEmpty(requires));
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

}
