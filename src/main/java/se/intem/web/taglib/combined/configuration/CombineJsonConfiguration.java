package se.intem.web.taglib.combined.configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.io.ClasspathResourceLoader;
import se.intem.web.taglib.combined.node.ConfigurationItem;
import se.intem.web.taglib.combined.node.TreeBuilder;

public class CombineJsonConfiguration {

    private static final String JSON_CONFIGURATION = "/combine.json";

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombineJsonConfiguration.class);

    private long lastRead = 0;
    private TreeBuilder tb;

    private String configurationPath = JSON_CONFIGURATION;

    /* Assume reloadable */
    private boolean reloadable = true;

    private Optional<ConfigurationItemsCollection> configuration = Optional.absent();

    private Optional<ServletContext> servletContext = Optional.absent();

    private ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader();

    CombineJsonConfiguration() {
        this.tb = new TreeBuilder();
        this.lastRead = 0;
    }

    @VisibleForTesting
    CombineJsonConfiguration(final String configurationPath) {
        this();
        this.configurationPath = configurationPath;
    }

    public CombineJsonConfiguration withServletContext(final ServletContext servletContext) {
        this.servletContext = Optional.fromNullable(servletContext);
        return this;
    }

    public Optional<ConfigurationItemsCollection> readConfiguration() {
        if (!reloadable && configuration.isPresent()) {
            log.debug("Not reloadable, re-using last configuration");
            return configuration;
        }

        List<ManagedResource> configs = collectConfiguration();

        if (configs.isEmpty()) {
            log.info("Could not find {} in either classpath or WEB-INF", configurationPath);
            this.configuration = Optional.absent();
            return this.configuration;
        }

        long lastModified = checkLastModified(configs);
        if (lastModified < lastRead) {
            log.debug("No changes, re-using last {}", configurationPath);
            return configuration;
        }

        long lastRead = new Date().getTime();

        Optional<ConfigurationItemsCollection> parsed = Optional.absent();

        Stopwatch stopwatch = Stopwatch.createStarted();

        log.info("Refreshing ({}) combine.json [ {} ]", configs.size(), configsForLogging(configs));
        for (ManagedResource config : configs) {

            log.debug("Reading configuration {}", config.getDisplayName());
            try {
                parsed = Optional.of(tb.parse(config.getInput(), parsed));
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse " + config.getDisplayName(), e);
            }
        }

        log.debug("Parsed {} combine.json in {}", configs.size(), stopwatch);

        this.configuration = parsed;
        /* Items read from file are by default library items. */
        if (configuration.isPresent()) {
            for (ConfigurationItem item : configuration.get()) {
                item.setLibrary(true);
            }
        }

        /* Update timestamp only if files were successfully read. */
        this.lastRead = lastRead;
        return this.configuration;
    }

    private String configsForLogging(final List<ManagedResource> configs) {

        if (configs.isEmpty()) {
            return "";
        }

        if (configs.size() == 1) {
            return configs.get(0).getDisplayName();
        }

        String prefix = configs.get(0).getDisplayName();

        for (ManagedResource resource : configs) {
            prefix = Strings.commonPrefix(prefix, resource.getDisplayName());
        }

        final String remove = prefix;

        String joined = FluentIterable.from(configs).transform(new Function<ManagedResource, String>() {

            public String apply(final ManagedResource input) {
                return input.getDisplayName().replace(remove, "");
            }
        }).join(Joiner.on(", "));

        return joined;
    }

    private long checkLastModified(final List<ManagedResource> configs) {

        long lastModified = 0;

        for (ManagedResource managedResource : configs) {
            if (managedResource.isTimestampSupported()) {
                lastModified = Math.max(lastModified, managedResource.lastModified());
            } else {
                /* expected for deployed war files */
                reloadable = false;
            }
        }

        return lastModified;
    }

    private List<ManagedResource> collectConfiguration() {
        Iterable<ManagedResource> presentInstances = Iterables.concat(
                Optional.presentInstances(findClasspathConfiguration()),
                Optional.presentInstances(Arrays.asList(findWebInfConfiguration())));

        List<ManagedResource> list = Lists.newArrayList(presentInstances);

        if (list.isEmpty()) {
            /* Workaround for Grails */
            log.debug("No configuration found. Trying Grails workaround.");
            Optional<ManagedResource> workaround = findInClasspathUsingServletContext();
            if (workaround.isPresent()) {
                list.add(workaround.get());
            }
        }

        log.debug("Found configurations {}", list);
        return list;
    }

    private Optional<ManagedResource> findWebInfConfiguration() {
        return findUsingServletContext("/WEB-INF" + configurationPath);
    }

    /*
     * Try using servlet context to resolve outside WEB-INF, to fix issues finding files when running Grails 2.4.5 app
     * as war.
     */
    private Optional<ManagedResource> findInClasspathUsingServletContext() {
        return findUsingServletContext(configurationPath);
    }

    private Optional<ManagedResource> findUsingServletContext(final String path) {
        if (!servletContext.isPresent()) {
            return Optional.absent();
        }

        ServerPathToManagedResource toManagedResource = new ServerPathToManagedResource(servletContext.get(), false);
        ManagedResource config = toManagedResource.apply(new RequestPath(path));

        if (config.exists()) {
            return Optional.of(config);
        }

        return Optional.absent();

    }

    /**
     * Try to find configuration file in classpath.
     */
    private List<Optional<ManagedResource>> findClasspathConfiguration() {
        List<URL> urls = getClassPathConfigurationUrl();
        List<Optional<ManagedResource>> resources = Lists.newArrayList();
        for (URL url : urls) {
            resources.add(urlToManagedResource(url));
        }
        return resources;
    }

    private Optional<ManagedResource> urlToManagedResource(final URL url) {
        try {
            try {
                File file = new File(url.toURI());
                if (!file.exists()) {
                    throw new IllegalArgumentException("Could not find file " + file);
                }

                return Optional.of(new ManagedResource(configurationPath, null, file.getPath(), url.openStream()));

            } catch (URISyntaxException e) {
                return Optional.of(new ManagedResource(configurationPath, null, null, url.openStream()));
            } catch (IllegalArgumentException e) {
                return Optional.absent();
            }
        } catch (IOException e) {
            log.error("Could not open url " + url, e);
            return Optional.absent();
        }
    }

    private List<URL> getClassPathConfigurationUrl() {
        return classpathResourceLoader.findManyInClasspath(configurationPath);
    }

    public static CombineJsonConfiguration get() {
        return InstanceHolder.instance;
    }

    /**
     * http://en.wikipedia.org/wiki/Singleton_pattern#The_solution_of_Bill_Pugh
     */
    private static class InstanceHolder {
        private static final CombineJsonConfiguration instance = new CombineJsonConfiguration();
    }

}
