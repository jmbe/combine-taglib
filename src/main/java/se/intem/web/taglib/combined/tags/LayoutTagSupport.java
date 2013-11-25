package se.intem.web.taglib.combined.tags;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.CombinedResourceRepository;
import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.ResourceType;
import se.intem.web.taglib.combined.configuration.ConfigurationItemsCollection;
import se.intem.web.taglib.combined.configuration.SupportsConditional;
import se.intem.web.taglib.combined.node.ConfigurationItem;
import se.intem.web.taglib.combined.node.TreeBuilder;

public abstract class LayoutTagSupport extends ConfigurationItemAwareTagSupport {

    private static final String KEY_COMBINED_RESOLVED = "COMBINED_RESOLVED";

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(LayoutTagSupport.class);

    protected CombinedResourceRepository repository;

    private TreeBuilder tb;

    public LayoutTagSupport() {
        this.repository = CombinedResourceRepository.get();
        this.tb = new TreeBuilder();
    }

    protected void writeOutputPath(final RequestPath path, final String elementId) throws JspException {
        String output = format(path, elementId);
        println(output);
    }

    protected void println(final String output) throws JspException {
        try {
            pageContext.getOut().println(output);
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    /* Format path for output in jsp, e.g. as script or link tag. */
    protected abstract String format(RequestPath path, String elementId);

    public abstract List<RequestPath> getResources(final ConfigurationItem configuration);

    /**
     * Output inline resources after all other resources have loaded.
     */
    protected abstract void outputInlineResources(ConfigurationItemsCollection configurationItems) throws JspException;

    /**
     * Output inline resources before resource groups (such as configuration data or translations).
     */
    protected abstract void outputInlineResourcesBefore(ConfigurationItemsCollection configurationItems)
            throws JspException;

    /**
     * @return true if processing should continue or false to abort
     */
    protected abstract boolean beforeResolve(ConfigurationItemsCollection configurationItems);

    protected abstract void afterResolve();

    @Override
    public int doEndTag() throws JspException {

        Stopwatch stopwatch = Stopwatch.createStarted();

        ConfigurationItemsCollection configurationItems = getConfigurationItems();

        if (!beforeResolve(configurationItems)) {
            return EVAL_PAGE;
        }

        List<ConfigurationItem> resolved = null;
        Optional<List<ConfigurationItem>> cached = null;
        if (hasLayoutBeenCalled() && (cached = getResolved()).isPresent()) {
            resolved = cached.get();
        } else {
            resolved = tb.resolve(configurationItems);
            setResolved(resolved);
        }

        outputInlineResourcesBefore(configurationItems);

        int total = 0;
        for (ConfigurationItem ci : resolved) {
            List<RequestPath> resources = getResources(ci);
            if (resources.isEmpty()) {
                continue;
            }

            total++;

            writeConditionalStart(ci);
            if (!ci.shouldBeCombined()) {
                /* Output resources as is */
                for (RequestPath path : resources) {
                    writeOutputPath(path, null);
                }
            } else {
                Iterable<RequestPath> paths = repository.getResourcePath(ci.getName(), getType());
                for (RequestPath path : paths) {
                    writeOutputPath(path, generateElementId(ci));
                }
            }
            writeConditionalEnd(ci);

        }

        outputInlineResources(configurationItems);
        log.debug(String.format("Handled %s %s bundles in %s ms.", total, getType(),
                stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        afterResolve();

        return EVAL_PAGE;
    }

    private Optional<List<ConfigurationItem>> getResolved() {
        @SuppressWarnings("unchecked")
        List<ConfigurationItem> attribute = (List<ConfigurationItem>) pageContext.getRequest().getAttribute(
                KEY_COMBINED_RESOLVED);

        return Optional.fromNullable(attribute);
    }

    private void setResolved(final List<ConfigurationItem> resolved) {
        pageContext.getRequest().setAttribute(KEY_COMBINED_RESOLVED, resolved);
    }

    public String generateElementId(final ConfigurationItem ci) {
        if (!ci.isSupportsDynamicCss()) {
            return null;
        }

        if (ResourceType.js.equals(getType())) {
            return null;
        }

        return ci.getName() + "-" + getType();
    }

    protected void writeConditionalStart(final SupportsConditional ci) throws JspException {
        if (!ci.hasConditional()) {
            return;
        }

        println(String.format("<!--[if %s]>", ci.getConditional()));
    }

    protected void writeConditionalEnd(final SupportsConditional ci) throws JspException {
        if (!ci.hasConditional()) {
            return;
        }
        println("<![endif]-->");
    }

    protected abstract ResourceType getType();
}
