package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.internetapplications.web.taglib.combined.CombinedResource;
import se.internetapplications.web.taglib.combined.RequestPath;
import se.internetapplications.web.taglib.combined.ResourceType;
import se.internetapplications.web.taglib.combined.ScriptCombinedResource;
import se.internetapplications.web.taglib.combined.node.ConfigurationItem;
import se.internetapplications.web.taglib.combined.node.JavaScriptParser;

public class LayoutScriptTag extends LayoutTagSupport {

    /** Logger for this class. */
    static final Logger log = LoggerFactory.getLogger(LayoutScriptTag.class);

    private static final Predicate<ConfigurationItem> keepLocalJsOnly = new Predicate<ConfigurationItem>() {
        public boolean apply(final ConfigurationItem input) {
            return !input.isRemote() && !input.getJs().isEmpty();
        }
    };

    private JavaScriptParser jsParser;
    private DependencyCache cache;

    public LayoutScriptTag() {
        this.jsParser = new JavaScriptParser();
        this.cache = DependencyCache.get();
    }

    public List<RequestPath> getResources(final ConfigurationItem configuration) {
        return configuration.getJs();
    }

    @Override
    protected String format(final RequestPath path) {
        return String.format("<script type=\"text/javascript\" charset=\"UTF-8\" src=\"%s\"></script>", path);
    }

    public CombinedResource stringToCombinedResource(final String contents, final long timestamp,
            final String checksum, final List<ManagedResource> realPaths) {
        return new ScriptCombinedResource(contents, timestamp, checksum, realPaths);
    }

    @Override
    protected ResourceType getType() {
        return ResourceType.js;
    }

    @Override
    protected void outputInlineResources(final ConfigurationItemsCollection cic) throws JspException {
        List<String> inlineScripts = cic.getInlineScripts();
        if (inlineScripts.isEmpty()) {
            return;
        }

        for (String inline : inlineScripts) {
            String output = String.format("<script type=\"text/javascript\" charset=\"UTF-8\">%s</script>", inline);
            println(output);
        }

    }

    @Override
    protected void beforeResolve(final ConfigurationItemsCollection configurationItems) {

        List<ConfigurationItem> js = FluentIterable.from(configurationItems).filter(keepLocalJsOnly).toList();

        for (ConfigurationItem ci : js) {

            List<ManagedResource> realPaths = FluentIterable.from(ci.getJs())
                    .transform(new ServerPathToManagedResource(pageContext.getServletContext())).toList();

            boolean hasChanges = repository.hasChanges(ci.getName(), ResourceType.js, realPaths);

            String cacheKey = repository.createResourcePathKey(ci.getName(), ResourceType.js);

            if (hasChanges) {
                /* Rebuild cache */
                log.info("Changes detected for {}. Rebuilding dependency cache...", ci.getName());

                LinkedHashSet<String> requires = Sets.newLinkedHashSet();

                for (ManagedResource mr : realPaths) {
                    log.info("Parsing {}", mr.getName());
                    try {
                        List<String> found = jsParser.findRequires(mr.getInput());
                        requires.addAll(found);
                    } catch (IOException e) {
                        log.error("Could not parse js", e);
                    }
                }
                cache.put(cacheKey, requires);

            }

            Optional<Iterable<String>> optional = cache.get(cacheKey);
            if (optional.isPresent()) {
                ci.addRequires(optional.get());
            }

        }

    }
}
