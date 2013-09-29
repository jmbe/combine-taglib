package se.intem.web.taglib.combined.resources;

import java.util.List;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.ResourceType;

public interface RequestPathBundle {
    List<RequestPath> getPaths();

    ResourceType getType();
}
