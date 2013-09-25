# Combine-taglib #
A simple JSP taglib to concatenate and serve fingerprinted css or js resources, with support for dependency graphs.

Supports dev mode for use with Tincr or similar tools.

## Setup ##

Add maven dependency

    <dependency>
        <groupId>se.intem</groupId>
        <artifactId>combine-taglib</artifactId>
        <version>1.2.0-SNAPSHOT</version>
    </dependency>

Add the following method and call it from onStartup(:ServletContext) in WebApplicationInitializer

    private void addCombinedTagServlet(final ServletContext servletContext) {
        servletContext.addServlet("combinedtag", new CombinedServlet()).addMapping("*.combined");
    }


Or add servlet to web.xml

    <servlet>
        <servlet-name>CombinedServlet</servlet-name>
        <servlet-class>se.intem.web.taglib.combined.servlet.CombinedServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>CombinedServlet</servlet-name>
        <url-pattern>*.combined</url-pattern>
    </servlet-mapping>
    

    
    
## Usage ##

### Create combine.json (optional) ###
Define libraries in combine.json in root of classpath. A library will be loaded only if some other resource depends on it.

Name must be given. The css and js attributes can either have a single string or an array of strings. Add dependencies in requires attribute, either as comma or space separated string or as array of strings.

Optional dependencies are only included if some other resource actually requires it, but if it is included then it will be loaded before resources that optionally depends on it. For example: Angular optionally requires jquery. Angular will use jquery if included, but jquery is not required. However if jquery is included, then it must be loaded before angular.
    

    [
        {
            name : "jquery",
            js : [ "//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js" ]
        },

        {
            name : "angular",
            optional : "jquery",
            js : [ "//ajax.googleapis.com/ajax/libs/angularjs/1.1.5/angular.min.js",
                    "//ajax.googleapis.com/ajax/libs/angularjs/1.1.5/angular-resource.min.js" ]
        },

        {
            name : "bootstrap",
            requires : "jquery",
            css : "//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css",
            js : "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js"
        }

    ]


### Specify dependencies ###
Add a comment near the top of js or css files to pull in dependencies.
    
    /* combine @requires atmosphere angular */

### Use in JSP ###

Add taglib to jsp (required)

    <%@ taglib uri="http://combine.intem.se" prefix="combine" %>
    
Combine javascript resources. Local files will be scanned for dependencies and added to dependency graph.


    <combine:group name="combined-javascript">
        <combine:js path="/js/AngularAtmosphere.js" />
        <combine:js path="/js/Humanized.js" />
        ...
    </combine:group>
    
Combine css resources

    <combine:group name="combined-css" reloadable="true" requires="bootstrap">
        <combine:css path="/css/tpa.css"/>
        ...
    </combine:group>

Add inline javascript. Inline javascript will be added last, after all other scripts.

    <combine:script requires="angular">
        ...
    </combine:script>
    
Add inline css style. Will be added after all other css links.

    <combine:style>
       ...
    </combine:style>

Force pulling in some libraries on a page

    <combine:requires requires="bootstrap,angular" />

Output queued resources (required)

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


