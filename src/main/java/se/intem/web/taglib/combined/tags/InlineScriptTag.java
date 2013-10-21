package se.intem.web.taglib.combined.tags;

import se.intem.web.taglib.combined.configuration.InlineContent;

public class InlineScriptTag extends InlineTagSupport {

    @Override
    protected void addContents(final InlineContent contents) {

        if (hasLayoutBeenCalled()) {
            throw new IllegalStateException("Adding inline script, but layout has already been called. "
                    + "All configuration must be completed before any layout tag is called.");
        }

        getConfigurationItems().addInlineScript(contents);
    }

}
