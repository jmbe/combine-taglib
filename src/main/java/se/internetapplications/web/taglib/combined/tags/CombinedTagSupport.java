package se.internetapplications.web.taglib.combined.tags;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public abstract class CombinedTagSupport extends BodyTagSupport {

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
