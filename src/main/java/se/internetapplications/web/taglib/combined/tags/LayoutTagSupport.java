package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.CombineResourceStrategy;
import se.intem.web.taglib.combined.CombinedResourceRepository;
import se.intem.web.taglib.combined.ConcatCombineResourceStrategy;
import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.ResourceType;
import se.internetapplications.web.taglib.combined.node.ConfigurationItem;
import se.internetapplications.web.taglib.combined.node.TreeBuilder;

public abstract class LayoutTagSupport extends ConfigurationItemAwareTagSupport implements CombineResourceStrategy {

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

    protected RequestPath addCombinedResources(final String name, final ResourceType type,
            final List<RequestPath> sources) {

        List<ManagedResource> realPaths = FluentIterable.from(sources)
                .transform(new ServerPathToManagedResource(pageContext.getServletContext())).toList();

        return repository.addCombinedResource(name, type, realPaths, this);
    }

    public long combineFiles(final PrintWriter pw, final List<ManagedResource> realPaths) throws IOException {
        return new ConcatCombineResourceStrategy().joinPaths(pw, realPaths);
    }

    public abstract List<RequestPath> getResources(final ConfigurationItem configuration);

    protected abstract void outputInlineResources(ConfigurationItemsCollection configurationItems) throws JspException;

    protected abstract void beforeResolve(ConfigurationItemsCollection configurationItems);

    @Override
    public int doEndTag() throws JspException {

        Stopwatch stopwatch = Stopwatch.createStarted();

        ConfigurationItemsCollection configurationItems = getConfigurationItems();

        beforeResolve(configurationItems);

        List<ConfigurationItem> resolved = tb.resolve(configurationItems);

        for (ConfigurationItem ci : resolved) {
            List<RequestPath> resources = getResources(ci);
            if (resources.isEmpty()) {
                continue;
            }

            if (!ci.shouldBeCombined()) {
                /* Output resources as is */
                for (RequestPath path : resources) {
                    writeOutputPath(path);
                }
            } else {

                if (ci.isReloadable() || !repository.containsResourcePath(ci.getName(), getType())) {
                    log.info("Checking for changes in {}", ci.getName());
                    addCombinedResources(ci.getName(), getType(), resources);
                }

                RequestPath path = repository.getResourcePath(ci.getName(), getType());
                writeOutputPath(path);
            }

        }

        outputInlineResources(configurationItems);

        log.info("Handled {} resources in {} ms.", resolved.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return EVAL_PAGE;
    }

    protected abstract ResourceType getType();
}
