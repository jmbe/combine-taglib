package se.intem.web.taglib.combined.tags;

public class InlineScriptEarlyTag extends InlineTagSupport {

    @Override
    protected void addContents(final String contents) {

        if (hasLayoutBeenCalled()) {
            throw new IllegalStateException("Adding inline script, but layout has already been called. "
                    + "All configuration must be completed before any layout tag is called.");
        }

        getConfigurationItems().addInlineScriptEarly(contents);
    }

}
