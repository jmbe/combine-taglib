package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import se.internetapplications.web.taglib.combined.CombineResourceStrategy;
import se.internetapplications.web.taglib.combined.CombinedResourceRepository;

public abstract class CombinedTagSupport extends BodyTagSupport implements CombineResourceStrategy {

    protected List<String> sources;

    private String name, path = "";
    private boolean enabled = true;
    private boolean reloadable = false;

    private boolean minify = false;

    private boolean combined;

    protected void init() {
        this.sources = Lists.newLinkedList();
        setCombined(false);
    }

    @Override
    public int doStartTag() throws JspException {
        // log.debug("start script");
        init();

        if (!isReloadable() && isEnabled() && CombinedResourceRepository.containsResourcePath(getPath(), getName())) {
            setCombined(true);
            return SKIP_BODY;
        }

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {

        if (!isEnabled()) {
            for (String source : sources) {
                writeOutputPath(source);
            }
        } else {
            if (!isCombined()) {
                addCombinedResources();
            }

            String scriptPath = CombinedResourceRepository.getResourcePath(getPath(), getName());
            writeOutputPath(scriptPath);
        }
        dispose();
        // log.debug("end script");
        return EVAL_PAGE;
    }

    private String addCombinedResources() {

        Function<String, String> serverPathToRealPath = new Function<String, String>() {
            public String apply(final String element) {
                return pageContext.getServletContext().getRealPath(element);
            }
        };
        List<String> realPaths = FluentIterable.from(sources).transform(serverPathToRealPath).toImmutableList();

        return CombinedResourceRepository.addCombinedResource(getPath(), getName(), realPaths, this);
    }

    protected void writeOutputPath(final String path) throws JspException {
        try {
            pageContext.getOut().write(format(path));
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    /* Format path for output in jsp, e.g. as script or link tag. */
    protected abstract String format(String path);

    protected void addSource(final String source) {
        this.sources.add(source);
    }

    protected void dispose() {
        this.sources.clear();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setReloadable(final boolean reloadable) {
        this.reloadable = reloadable;
    }

    public boolean isMinify() {
        return minify;
    }

    public void setMinify(final boolean minify) {
        this.minify = minify;
    }

    protected boolean isCombined() {
        return combined;
    }

    protected void setCombined(final boolean combined) {
        this.combined = combined;
    }

}
