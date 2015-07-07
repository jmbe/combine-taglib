# Combine-taglib #
Combine-taglib is a JSP taglib to concatenate and serve combined CSS and Javascript resources, similar to tools such as [Grails Resources plugin](http://grails-plugins.github.io/grails-resources/), [wro4j](http://alexo.github.io/wro4j/),
[JAWR](https://jawr.java.net/) and [pack:tag](https://github.com/ajkovar/packtag).


 * Bundles changed resources on the fly
 * Reloads changed configuration on the fly
 * Creates cache-friendly links which will survive server restarts, redeploys or deploys to different servers. Links will change only if content changes.
 * Fully declare resource relationships with attributes @requires, @provides and @optional
 * Configure dependencies either directly in js file (recommended), as JSP tags or in json configuration file
 * Will transitively add any required dependencies and load them in the correct order. Declare dependencies on what you directly use and let the dependency graph figure out what is needed.
 * Declare dependencies as granular or coarsely as fits the way you work
 * Supports development mode for use with live reload tools such as [Tincr](http://tin.cr/).
 * Supports IE conditionals.
 * Compatible with dynamic stylesheet libraries such as [YUI Stylesheet](http://yuilibrary.com/yui/docs/stylesheet/)
 * Supports replacing placeholders in paths, such as versions


## Setup ##

Add maven dependency

    <dependency>
        <groupId>se.intem</groupId>
        <artifactId>combine-taglib</artifactId>
        <version>1.10.0</version>
    </dependency>

#### Add servlet mapping

Add the following method and call it from onStartup(:ServletContext) in WebApplicationInitializer

    private void addCombinedTagServlet(final ServletContext servletContext) {
        servletContext.addServlet("combinedtag", new CombinedServlet()).addMapping("*.combined");
    }

#### Add servlet mapping (legacy)

Add servlet to web.xml

    <servlet>
        <servlet-name>CombinedServlet</servlet-name>
        <servlet-class>se.intem.web.taglib.combined.servlet.CombinedServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>CombinedServlet</servlet-name>
        <url-pattern>*.combined</url-pattern>
    </servlet-mapping>
    

#### Configure logging 

Sample for logback:

    <!-- For development you might want to use INFO -->
    <logger name="se.intem.web.taglib.combined" level="WARN" />
    
    
## Usage ##

### Create combine.json (optional) ###
Define *libraries* in a file named combine.json. Put it either in WEB-INF/ or in root of classpath. A *library* will be loaded only if some other resource depends on it.

Local files will be scanned for dependencies and added to dependency graph.

 * Name must be given
 * The **css** and **js** attributes can either have a single string or an array of strings.
 * Add dependencies in **requires** or **optional** attribute, either as comma or space separated string or as an array of strings.
 * If several related files have a common version in the path, you can move it to a **replace** map so that you only need to change it in one place when upgrading (see Angular sample below)

*Optional* dependencies are included only if some other resource actually requires it, but if it is included then it will be loaded before resources that optionally depends on it. For example: Angular optionally requires jquery. Angular will use jquery if included, but jquery is not required. However if jquery is included, then it must be loaded before angular.
    

    [
        {
            name : "jquery",
            js : [ "//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js" ]
        },

        {
            name : "angular",
            optional : "jquery",
            replace : {
                # : "1.2.0"
            },
            js : [ "//ajax.googleapis.com/ajax/libs/angularjs/#/angular.min.js",
                   "//ajax.googleapis.com/ajax/libs/angularjs/#/angular-resource.min.js" ]
        },

        {
            name : "bootstrap",
            requires : "jquery",
            css : "//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css",
            js : "//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"
        }

    ]


### Specify dependencies directly in resource files ###
Optionally add a specially formatted comment to js or css files to declare relationships. You can have several of these 
comments per file, for example if you are declaring several components in the same file.

    /* combine @requires atmosphere angular */

Using **@provides** allows other files to pull in a given resource without knowing the name of the bundle it belongs to.

    /* 
     * combine
     * @requires atmosphere angular
     * @provides MessageMultiplexer
     */
    ...
    // In another file (will pull in the bundle that MessageMultiplexer currently belongs to):
    /* combine @requires MessageMultiplexer */


### Use in JSP ###

Add taglib to jsp

    <%@ taglib uri="http://combine.intem.se" prefix="combine" %>
    
Require some libraries on a page

    <combine:requires requires="bootstrap angular site-start-page" />

Output queued resources

    <html>
        <body>
        <head>
            ...
            <combine:layout-css />
        </head>
        <body>
            ...
            <combine:layout-javascript />
        </body>
    </html>

#### Defining groups in JSP

Normally you would define resource groups in combine.json but if preferred you can define them directly in jsp. When you
define the group in jsp it will always be included, so it does not need to be explicitly required (set attribute 
library=true, to turn off automatic inclusion).

    <combine:group name="combined" requires="bootstrap">
        <combine:js path="/js/AngularAtmosphere.js" />
        <combine:js path="/js/Humanized.js" />
        <combine:css path="/css/tpa.css"/>        
        ...
    </combine:group>

#### Inline

Add inline javascript. Inline javascript will be added last, after all other scripts.

    <combine:script requires="angular">
        ...
    </combine:script>
    
To trigger proper display and formatting in IDE editors you can wrap a plain script tag:

    <combine:script requires="angular">
    <script>
        ...
    </script>
    </combine:script>
    
Add inline css style. Will be added after all other css links.

    <combine:style>
       ...
    </combine:style>

To trigger proper display and formatting in IDE editors you can wrap a plain style tag:

    <combine:style>
    <style>
       ...
    </style>
    </combine:style>

## Development mode
Resources will be bundled and links will change based on content whether you run an unpacked (typically in an IDE) or 
a packed war file. If you would rather output the individual file links to support live reload tools, you can enable
development mode.

Start server with **-DcombineDevMode=true**.

Add **supportsDevMode: true** to the bundle either in json configuration or while defining group as JSP tag.

    {
        name : "time-css",
        requires : "bootstrap-css select2 bootstrap-datepicker",
        supportsDevMode : true,
        css : "/css/timereport.css"
    }


## IE Conditionals
Use **conditional** to control which IE versions some bundles will be loaded in.

    {
        name : "ie8-support",
        conditional : "lte IE 8",
        css : "/css/ie8.css"
    }

## Dynamic stylesheet

Set the attribute **supportsDynamicCss** for bundles which you would like to edit at runtime using Javascript. This will add an id the combined stylesheet so that you can easily access it using e.g. [YUI Stylesheet](http://yuilibrary.com/yui/docs/stylesheet/). 

    {
        name : "dynamic",
        supportsDynamicCss: true,
        css : [ ... ]
    }

## Current limitations

 * No support for media attribute for css (workaround: put media query in css file)
 * No minification of files
 * No support for transcompiling LESS or SASS files
 
