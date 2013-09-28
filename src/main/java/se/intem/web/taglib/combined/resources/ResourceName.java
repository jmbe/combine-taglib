package se.intem.web.taglib.combined.resources;

import com.google.common.base.Objects;

/**
 * A symbolic name for a resource or resource group.
 */
public class ResourceName {

    private String name;

    public ResourceName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ResourceName derive(final int i) {
        return new ResourceName(name + i);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ResourceName)) {
            return false;
        }

        ResourceName that = (ResourceName) obj;

        return Objects.equal(this.name, that.name);
    }
}
