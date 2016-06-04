package se.intem.web.taglib.combined.tags;

import java.util.List;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.ResourceType;
import se.intem.web.taglib.combined.configuration.ConfigurationItemsCollection;
import se.intem.web.taglib.combined.configuration.InlineContent;
import se.intem.web.taglib.combined.node.ConfigurationItem;

public class LayoutScriptTag extends LayoutTagSupport {

    /** Logger for this class. */
    static final Logger log = LoggerFactory.getLogger(LayoutScriptTag.class);

    public static final String SCRIPT_PASS_COMPLETE = "combine_script_pass_complete";

    public List<RequestPath> getResources(final ConfigurationItem configuration) {
        return configuration.getJs();
    }

    @Override
    protected String format(final RequestPath path, final String elementId) {
        return String.format("<script type=\"text/javascript\" charset=\"UTF-8\" src=\"%s\"></script>", path);
    }

    @Override
    protected ResourceType getType() {
        return ResourceType.js;
    }

    @Override
    protected void outputInlineResources(final ConfigurationItemsCollection cic) throws JspException {
        List<InlineContent> inlineScripts = cic.getInlineScripts();
        if (inlineScripts.isEmpty()) {
            return;
        }

        addInline(inlineScripts);

    }

    private void addInline(final List<InlineContent> inlineScripts) throws JspException {
        for (InlineContent inline : inlineScripts) {
            writeConditionalStart(inline);

            String output = String.format("<script type=\"text/javascript\">%s</script>", inline.getContents());
            println(output);

            writeConditionalEnd(inline);
        }
    }

    @Override
    protected void outputInlineResourcesBefore(final ConfigurationItemsCollection cic) throws JspException {
        List<InlineContent> scripts = cic.getInlineScriptEarlies();
        if (scripts.isEmpty()) {
            return;
        }
        addInline(scripts);
    }

    @Override
    protected boolean beforeResolve(final ConfigurationItemsCollection configurationItems) {
        Boolean complete = (Boolean) pageContext.getRequest().getAttribute(SCRIPT_PASS_COMPLETE);

        if (Boolean.TRUE.equals(complete)) {
            log.warn("Javascript has already been laid out for this request. Will not run again.");
            return false;
        }

        return true;
    }

    @Override
    protected void afterResolve() {
        pageContext.getRequest().setAttribute(SCRIPT_PASS_COMPLETE, Boolean.TRUE);
    }
}
