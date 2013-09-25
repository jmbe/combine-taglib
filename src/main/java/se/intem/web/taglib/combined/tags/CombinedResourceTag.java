package se.intem.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.ResourceType;
import se.intem.web.taglib.combined.node.CombineCommentParser;
import se.intem.web.taglib.combined.node.ConfigurationItem;
import se.intem.web.taglib.combined.node.ResourceParent;

public class CombinedResourceTag extends ConfigurationItemAwareTagSupport implements ResourceParent {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombinedResourceTag.class);

    private ConfigurationItem ci = new ConfigurationItem();

    private CombineCommentParser jsParser;

    private DependencyCache cache;

    public CombinedResourceTag() {
        this.jsParser = new CombineCommentParser();
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

        readDependenciesFromResources();

        cleanup();

        return EVAL_PAGE;
    }

    private void readDependenciesFromResources() {
        if (ci.isRemote()) {
            return;
        }

        String cacheKey = ci.getName();

        boolean hasChanges = false;

        Optional<DependencyCacheEntry> optional = cache.get(cacheKey);
        if (optional.isPresent()) {
            DependencyCacheEntry cached = optional.get();
            if (cached.requiresRefresh(ci, pageContext.getServletContext())) {
                hasChanges = true;
            }
        } else {
            hasChanges = true;
        }

        if (hasChanges) {
            /* Rebuild cache */
            if (!ci.shouldBeCombined()) {
                log.info("Reading dependencies for uncombined resource {}.", ci.getName());
            } else {
                log.info("Changes detected for {}. Rebuilding dependency cache...", ci.getName());
            }

            long lastread = new Date().getTime();
            Map<ResourceType, List<ManagedResource>> realPaths = ci.getRealPaths(pageContext.getServletContext());
            Set<Entry<ResourceType, List<ManagedResource>>> entrySet = realPaths.entrySet();

            LinkedHashSet<String> requires = Sets.newLinkedHashSet();

            for (Entry<ResourceType, List<ManagedResource>> entry : entrySet) {

                for (ManagedResource mr : entry.getValue()) {
                    log.debug("Parsing {}", mr.getName());
                    try {
                        List<String> found = jsParser.findRequires(mr.getInput());
                        requires.addAll(found);
                    } catch (IOException e) {
                        log.error("Could not parse js", e);
                    }
                }
            }

            cache.put(cacheKey, new DependencyCacheEntry(lastread, requires, ci));

        }

        optional = cache.get(cacheKey);
        if (optional.isPresent()) {
            ci.addRequires(optional.get().getRequires());
        }

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
