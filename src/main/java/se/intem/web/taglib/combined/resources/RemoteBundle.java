package se.intem.web.taglib.combined.resources;

import com.google.common.collect.Lists;

import java.util.List;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.ResourceType;

public class RemoteBundle implements RequestPathBundle {

    private List<RequestPath> paths;
    private ResourceType type;

    public RemoteBundle(final ResourceType type) {
        this.type = type;
        this.paths = Lists.newArrayList();
    }

    @Override
    public List<RequestPath> getPaths() {

        return this.paths;
    }

    @Override
    public ResourceType getType() {
        return this.type;
    }

    public void addPath(final RequestPath requestPath) {
        this.paths.add(requestPath);
    }

}
