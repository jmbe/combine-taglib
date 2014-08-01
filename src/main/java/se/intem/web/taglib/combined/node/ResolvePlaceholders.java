package se.intem.web.taglib.combined.node;

import com.google.common.base.Function;

import java.util.Map;

import se.intem.web.taglib.combined.RequestPath;

public class ResolvePlaceholders implements Function<RequestPath, RequestPath> {

    private Map<String, String> replace;

    public ResolvePlaceholders(final Map<String, String> replace) {
        this.replace = replace;
    }

    @Override
    public RequestPath apply(final RequestPath input) {
        return input.resolvePlaceholders(replace);
    }

}
