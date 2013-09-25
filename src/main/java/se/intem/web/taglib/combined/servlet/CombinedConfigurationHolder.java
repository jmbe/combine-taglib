package se.intem.web.taglib.combined.servlet;

public class CombinedConfigurationHolder {

    public static final String COMBINE_DEV_MODE = "combineDevMode";
    private static Boolean devMode = null;

    public static boolean isDevMode() {
        if (devMode == null) {
            String combineDevMode = System.getProperties().getProperty(COMBINE_DEV_MODE);
            devMode = "true".equalsIgnoreCase(combineDevMode);
        }

        return devMode;
    }

}
