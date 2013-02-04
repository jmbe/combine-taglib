package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import se.internetapplications.web.taglib.combined.CombinedResourceRepository;

/**
 * TODO add equivalent CSS tag with additional attribute media
 * 
 */
public class ScriptTag extends BodyTagSupport {

    private List<String> sources;

    private String name, path = "";
    private boolean enabled = true;
    private boolean reloadable = false;

    /**
     * TODO use option to minify files or not
     */
    private boolean minify = true;

    private boolean combined;

    @Override
    public int doStartTag() throws JspException {
        // log.debug("start script");
        init();

        if (!isReloadable() && isEnabled() && CombinedResourceRepository.containsScriptPath(getPath(), getName())) {
            this.combined = true;
            return SKIP_BODY;
        }

        return EVAL_BODY_INCLUDE;
    }

    private void init() {
        this.sources = new LinkedList<String>();
        this.combined = false;
    }

    @Override
    public int doEndTag() throws JspException {

        if (!isEnabled()) {
            for (String string : sources) {
                writeScriptPath(string);
            }
        } else {

            if (!this.combined) {
                addCombinedScripts();
            }

            String scriptPath = CombinedResourceRepository.getScriptPath(getPath(), getName());
            writeScriptPath(scriptPath);
        }
        dispose();
        // log.debug("end script");
        return EVAL_PAGE;
    }

    private String addCombinedScripts() {

        Function<String, String> serverPathToRealPath = new Function<String, String>() {
            public String apply(final String element) {
                return pageContext.getServletContext().getRealPath(element);
            }
        };
        List<String> realPaths = FluentIterable.from(sources).transform(serverPathToRealPath).toImmutableList();

        return CombinedResourceRepository.addCombinedScripts(getPath(), getName(), realPaths);
    }

    private void writeScriptPath(final String path) throws JspException {
        try {
            pageContext.getOut().write(format(path));
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    private void dispose() {
        this.sources.clear();
    }

    private String format(final String path) {
        return String.format("<script type=\"text/javascript\" charset=\"UTF-8\" src=\"%s\"></script>\r\n", path);

    }

    public void addSource(final String source) {
        this.sources.add(source);
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

}
