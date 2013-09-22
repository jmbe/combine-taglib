package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.internetapplications.web.taglib.combined.CombinedResourceRepository;
import se.internetapplications.web.taglib.combined.ResourceType;
import se.internetapplications.web.taglib.combined.node.ConfigurationItem;
import se.internetapplications.web.taglib.combined.node.CombineCommentParser;
import se.internetapplications.web.taglib.combined.node.ResourceParent;

public class CombinedResourceTag extends ConfigurationItemAwareTagSupport implements ResourceParent {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombinedResourceTag.class);

    private ConfigurationItem configurationItem = new ConfigurationItem();
    private CombinedResourceRepository repository;

    private CombineCommentParser jsParser;

    private DependencyCache cache;

    public CombinedResourceTag() {
        this.repository = CombinedResourceRepository.get();
        this.jsParser = new CombineCommentParser();
        this.cache = DependencyCache.get();
    }

    /* Note: setters will be called BEFORE doStartTag, so cleanup must be done after tag is complete. */
    private void cleanup() {
        this.configurationItem = new ConfigurationItem();
    }

    @Override
    public int doEndTag() throws JspException {

        ConfigurationItemsCollection configurations = getConfigurationItems();
        configurations.add(this.configurationItem);

        readDependenciesFromResources();

        cleanup();

        return EVAL_PAGE;
    }

    private void readDependenciesFromResources() {
        if (this.configurationItem.isRemote()) {
            return;
        }

        ConfigurationItem ci = configurationItem;

        String cacheKey = ci.getName();

        boolean hasChanges = false;

        Map<ResourceType, List<ManagedResource>> realPaths = ci.getRealPaths(pageContext.getServletContext());

        Set<Entry<ResourceType, List<ManagedResource>>> entrySet = realPaths.entrySet();
        for (Entry<ResourceType, List<ManagedResource>> entry : entrySet) {
            if (hasChanges) {
                continue;
            }
            hasChanges = repository.hasChanges(ci.getName(), entry.getKey(), entry.getValue());
        }

        if (hasChanges) {
            /* Rebuild cache */
            log.info("Changes detected for {}. Rebuilding dependency cache...", ci.getName());
            LinkedHashSet<String> requires = Sets.newLinkedHashSet();

            for (Entry<ResourceType, List<ManagedResource>> entry : entrySet) {

                for (ManagedResource mr : entry.getValue()) {
                    log.info("Parsing {}", mr.getName());
                    try {
                        List<String> found = jsParser.findRequires(mr.getInput());
                        requires.addAll(found);
                    } catch (IOException e) {
                        log.error("Could not parse js", e);
                    }
                }
            }

            cache.put(cacheKey, requires);

        }

        Optional<Iterable<String>> optional = cache.get(cacheKey);
        if (optional.isPresent()) {
            ci.addRequires(optional.get());
        }

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

    public void setCombine(final boolean combine) {
        this.configurationItem.setCombine(combine);
    }

    public void setSupportsDevMode(final boolean supportsDevMode) {
        this.configurationItem.setSupportsDevMode(supportsDevMode);
    }

}
