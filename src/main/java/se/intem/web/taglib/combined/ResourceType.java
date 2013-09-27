package se.intem.web.taglib.combined;

public enum ResourceType {
    css, js;

    public String getContentType() {
        switch (this) {
        case css:
            return "text/css";
        case js:
            return "text/javascript";
        }

        throw new RuntimeException();
    }
}
