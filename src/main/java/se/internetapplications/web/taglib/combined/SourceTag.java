package se.internetapplications.web.taglib.combined;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("serial")
public class SourceTag extends TagSupport {

    /** Logger for this class. */
    private static final Log log = LogFactory.getLog(SourceTag.class);

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public int doStartTag() throws JspException {
        // log.debug("Start source");
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {

        ((ScriptTag) getParent()).addSource(getPath());
        // log.debug("end SourceTag");
        return EVAL_PAGE;
    }

}
