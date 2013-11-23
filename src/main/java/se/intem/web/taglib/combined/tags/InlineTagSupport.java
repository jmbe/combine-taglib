package se.intem.web.taglib.combined.tags;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.configuration.InlineContent;
import se.intem.web.taglib.combined.node.ConfigurationItem;

public abstract class InlineTagSupport extends ConfigurationItemAwareTagSupport {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(InlineTagSupport.class);

    private ConfigurationItem configurationItem = new ConfigurationItem();

    protected abstract void addContents(InlineContent contents);

    @Override
    public int doAfterBody() throws JspException {
        BodyContent b = getBodyContent();
        try {
            String contents = CharStreams.toString(b.getReader());

            configurationItem.setName("inline-" + UUID.randomUUID().toString());
            configurationItem.setRoot(true);

            if (!Strings.nullToEmpty(contents).trim().isEmpty()) {

                String md5 = Hashing.md5().hashUnencodedChars(contents).toString();
                configurationItem.setName("inline-" + md5);

                /* Add content */
                InlineContent inline = new InlineContent(contents, configurationItem.getConditional());

                addContents(inline);
            }

            if (configurationItem.hasDependencies()) {
                /* Add dependencies even if content is empty */
                log.debug("Adding inline resource {}", configurationItem.getName());
                addConfigurationItem(configurationItem);
            }

        } catch (IOException e) {
            log.error("Failed to read inline content", e);
        }

        cleanup();
        return SKIP_BODY;
    }

    /* Note: setters will be called BEFORE doStartTag, so cleanup must be done after tag is complete. */
    private void cleanup() {
        this.configurationItem = new ConfigurationItem();
    }

    public void setRequires(final String requires) {
        this.configurationItem.addRequires(requires);
    }

    public void setConditional(final String conditional) {
        this.configurationItem.setConditional(conditional);
    }

}
