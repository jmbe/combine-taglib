Simple taglib to concatenate and serve fingerprinted css or js resources.

Setup
-----

Add maven dependency

    <dependency>
        <groupId>se.intem</groupId>
        <artifactId>combine-taglib</artifactId>
        <version>1.2.0-SNAPSHOT</version>
    </dependency>

Add method and call it from onStartup(:ServletContext) in WebApplicationInitializer

    private void addCombinedTagServlet(final ServletContext servletContext) {
        servletContext.addServlet("combinedtag", new CombinedServlet()).addMapping("*.combined");
    }


Or add servlet to web.xml

    <servlet>
        <servlet-name>CombinedServlet</servlet-name>
        <servlet-class>se.internetapplications.web.taglib.combined.servlet.CombinedServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>CombinedServlet</servlet-name>
        <url-pattern>*.combined</url-pattern>
    </servlet-mapping>
    

    
    
Usage
-----
Add taglib to jsp

    <%@ taglib uri="http://combine.intem.se" prefix="combine" %>
    
Combine javascript resources

    <combine:resource name="jquery" library="true">
      <combine:script path="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js" />
    </combine:resource>

    <combine:resource name="angular" library="true" requires="jquery">
      <combine:script path="//ajax.googleapis.com/ajax/libs/angularjs/1.1.5/angular.min.js" />
      <combine:script path="//ajax.googleapis.com/ajax/libs/angularjs/1.1.5/angular-resource.min.js" />
    </combine:resource>

    <combine:resource name="combined-javascript" requires="angular">
        <combine:script path="/js/AngularAtmosphere.js" />
        <combine:script path="/js/Humanized.js" />
        ...
    </combine:resource>
    
Combine css resources

    <combine:resource name="combined-css" reloadable="true">
        <combine:css path="/css/tpa.css"/>
        ...
    </combine:resource>
    

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


