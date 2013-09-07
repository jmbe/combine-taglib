package se.internetapplications.web.taglib.combined.servlet;

public class CombinedConfigurationHolder {

    private static Boolean devMode = null;

    public static boolean isDevMode() {
        if (devMode == null) {
            String combineDevMode = System.getProperties().getProperty("combineDevMode");
            devMode = "true".equalsIgnoreCase(combineDevMode);
        }

        return devMode;
    }

}
