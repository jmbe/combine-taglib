package se.intem.web.taglib.combined.tags;

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
import se.intem.web.taglib.combined.node.ConfigurationItem;
import se.intem.web.taglib.combined.node.TreeBuilder;

public abstract class LayoutTagSupport extends ConfigurationItemAwareTagSupport {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(LayoutTagSupport.class);

    protected CombinedResourceRepository repository;

    private TreeBuilder tb;

    public LayoutTagSupport() {
        this.repository = CombinedResourceRepository.get();
        this.tb = new TreeBuilder();
    }

    protected void writeOutputPath(final RequestPath path) throws JspException {
        String output = format(path);
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
    protected abstract String format(RequestPath path);

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

    @Override
    public int doEndTag() throws JspException {

        Stopwatch stopwatch = Stopwatch.createStarted();

        ConfigurationItemsCollection configurationItems = getConfigurationItems();

        if (!beforeResolve(configurationItems)) {
            return EVAL_PAGE;
        }

        List<ConfigurationItem> resolved = tb.resolve(configurationItems);

        outputInlineResourcesBefore(configurationItems);

        int count = 0;
        for (ConfigurationItem ci : resolved) {
            List<RequestPath> resources = getResources(ci);
            if (resources.isEmpty()) {
                continue;
            }

            count++;

            if (!ci.shouldBeCombined()) {
                /* Output resources as is */
                for (RequestPath path : resources) {
                    writeOutputPath(path);
                }
            } else {
                Iterable<RequestPath> paths = repository.getResourcePath(ci.getName(), getType());
                for (RequestPath path : paths) {
                    writeOutputPath(path);
                }
            }

        }

        outputInlineResources(configurationItems);
        log.info(String.format("Handled %s %s bundles in %s ms.", count, getType(),
                stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        return EVAL_PAGE;
    }

    protected abstract ResourceType getType();
}
