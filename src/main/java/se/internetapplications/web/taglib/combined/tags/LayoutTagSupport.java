package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.internetapplications.web.taglib.combined.CombineResourceStrategy;
import se.internetapplications.web.taglib.combined.CombinedResourceRepository;
import se.internetapplications.web.taglib.combined.ConcatCombineResourceStrategy;
import se.internetapplications.web.taglib.combined.RequestPath;
import se.internetapplications.web.taglib.combined.ResourceType;
import se.internetapplications.web.taglib.combined.node.ConfigurationItem;
import se.internetapplications.web.taglib.combined.node.TreeBuilder;
import se.internetapplications.web.taglib.combined.servlet.CombinedConfigurationHolder;

public abstract class LayoutTagSupport extends ConfigurationItemAwareTagSupport implements CombineResourceStrategy {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(LayoutTagSupport.class);

    private CombinedResourceRepository repository;

    private TreeBuilder tb;

    public LayoutTagSupport() {
        this.repository = CombinedResourceRepository.get();
        this.tb = new TreeBuilder();
    }

    protected void writeOutputPath(final RequestPath path) throws JspException {
        try {
            pageContext.getOut().println(format(path));
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    /* Format path for output in jsp, e.g. as script or link tag. */
    protected abstract String format(RequestPath path);

    protected RequestPath addCombinedResources(final String name, final ResourceType type,
            final List<RequestPath> sources) {

        Function<RequestPath, ManagedResource> serverPathManaged = new Function<RequestPath, ManagedResource>() {
            public ManagedResource apply(final RequestPath element) {
                if (element.isRemote()) {
                    return new ManagedResource(element.getPath(), null, null);
                }
                return new ManagedResource(element.getPath(), pageContext.getServletContext().getRealPath(
                        element.getPath()), pageContext.getServletContext().getResourceAsStream(element.getPath()));
            }
        };
        List<ManagedResource> realPaths = FluentIterable.from(sources).transform(serverPathManaged).toList();

        return repository.addCombinedResource(name, type, realPaths, this);
    }

    public long combineFiles(final PrintWriter pw, final List<ManagedResource> realPaths) throws IOException {
        return new ConcatCombineResourceStrategy().joinPaths(pw, realPaths);
    }

    public abstract List<RequestPath> getResources(final ConfigurationItem configuration);

    @Override
    public int doEndTag() throws JspException {

        Stopwatch stopwatch = Stopwatch.createStarted();

        List<ConfigurationItem> resolved = tb.resolve(getConfigurationItems());

        for (ConfigurationItem ci : resolved) {
            List<RequestPath> resources = getResources(ci);
            if (resources.isEmpty()) {
                continue;
            }

            if ((CombinedConfigurationHolder.isDevMode() && ci.isSupportsDevMode()) || !ci.isCombine() || ci.isRemote()) {
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

        log.info("Handled {} resources in {} ms.", resolved.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return EVAL_PAGE;
    }

    protected abstract ResourceType getType();
}
