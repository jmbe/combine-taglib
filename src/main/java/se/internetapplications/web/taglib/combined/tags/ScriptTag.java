package se.internetapplications.web.taglib.combined.tags;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import java.util.List;

import javax.servlet.jsp.JspException;

import se.internetapplications.web.taglib.combined.CombinedResourceRepository;

public class ScriptTag extends CombinedTagSupport {

    @Override
    public int doStartTag() throws JspException {
        // log.debug("start script");
        init();

        if (!isReloadable() && isEnabled() && CombinedResourceRepository.containsScriptPath(getPath(), getName())) {
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
                addCombinedScripts();
            }

            String scriptPath = CombinedResourceRepository.getScriptPath(getPath(), getName());
            writeOutputPath(scriptPath);
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

        return CombinedResourceRepository.addCombinedScripts(getPath(), getName(), realPaths, isMinify());
    }

    protected String format(final String path) {
        return String.format("<script type=\"text/javascript\" charset=\"UTF-8\" src=\"%s\"></script>\r\n", path);
    }

}
