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
import se.internetapplications.web.taglib.combined.node.ConfigurationItem;
import se.internetapplications.web.taglib.combined.node.ResourceLink;
import se.internetapplications.web.taglib.combined.node.TreeBuilder;

public abstract class LayoutTagSupport extends ConfigurationItemAwareTagSupport implements CombineResourceStrategy {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(LayoutTagSupport.class);

    protected void writeOutputPath(final String path) throws JspException {
        try {
            pageContext.getOut().write(format(path));
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    /* Format path for output in jsp, e.g. as script or link tag. */
    protected abstract String format(String path);

    protected String addCombinedResources(final String name, final List<ResourceLink> sources) {

        Function<ResourceLink, ManagedResource> serverPathManaged = new Function<ResourceLink, ManagedResource>() {
            public ManagedResource apply(final ResourceLink element) {
                return new ManagedResource(element.getLink(), pageContext.getServletContext().getRealPath(
                        element.getLink()), pageContext.getServletContext().getResourceAsStream(element.getLink()));
            }
        };
        List<ManagedResource> realPaths = FluentIterable.from(sources).transform(serverPathManaged).toList();

        return CombinedResourceRepository.addCombinedResource(name, realPaths, this);
    }

    public long combineFiles(final PrintWriter pw, final List<ManagedResource> realPaths) throws IOException {
        return CombinedResourceRepository.joinPaths(pw, realPaths);
    }

    public abstract List<ResourceLink> getResources(final ConfigurationItem configuration);

    @Override
    public int doEndTag() throws JspException {

        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();

        List<ConfigurationItem> resolved = new TreeBuilder().resolve(getConfigurationItems());

        for (ConfigurationItem ci : resolved) {

            if (!ci.isEnabled() || ci.isRemote()) {
                /* Output resources as is */
                List<ResourceLink> resources = getResources(ci);
                for (ResourceLink resourceLink : resources) {
                    writeOutputPath(resourceLink.getLink());
                }
            } else {

                if (ci.isReloadable() || !CombinedResourceRepository.containsResourcePath(ci.getName())) {
                    log.info("Reloading {}", ci.getName());
                    List<ResourceLink> resources = getResources(ci);
                    addCombinedResources(ci.getName(), resources);
                }

                String path = CombinedResourceRepository.getResourcePath(ci.getName());
                writeOutputPath(path);
            }

        }

        log.info("Handled {} resources in {} ms.", resolved.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return EVAL_PAGE;
    }
}
