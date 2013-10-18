package se.intem.web.taglib.combined.resources;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.ResourceType;

public class ResourceGroup implements Iterable<RequestPathBundle> {

    private List<RequestPathBundle> js = Lists.newArrayList();
    private List<RequestPathBundle> css = Lists.newArrayList();

    private static final Function<RequestPathBundle, Iterable<RequestPath>> bundleToPaths = new Function<RequestPathBundle, Iterable<RequestPath>>() {
        public Iterable<RequestPath> apply(final RequestPathBundle input) {
            return input.getPaths();
        };
    };

    public void addBundle(final RequestPathBundle bundle) {
        if (ResourceType.js.equals(bundle.getType())) {
            js.add(bundle);
        } else {
            css.add(bundle);
        }
    }

    public List<RequestPathBundle> getJs() {
        return js;
    }

    public List<RequestPathBundle> getCss() {
        return css;
    }

    @Override
    public Iterator<RequestPathBundle> iterator() {
        return Iterators.concat(css.iterator(), js.iterator());
    }

    public Iterable<RequestPath> getRequestPaths(final ResourceType type) {
        List<RequestPathBundle> iterable = ResourceType.js.equals(type) ? js : css;
        return FluentIterable.from(iterable).transformAndConcat(bundleToPaths);
    }

}
