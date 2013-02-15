Simple taglib to concatenate and serve fingerprinted css or js resources.

Setup
-----

Add maven dependency

    <dependency>
        <groupId>se.intem</groupId>
        <artifactId>combine-taglib</artifactId>
        <version>1.1.0-SNAPSHOT</version>
    </dependency>

Add method and call it from onStartup(:ServletContext) in WebApplicationInitializer

    private void addCombinedTagServlet(final ServletContext servletContext) {
        servletContext.addServlet("combinedtag", new CombinedServlet()).addMapping("*.combined");
    }


Or add servlet to web.xml

    <servlet>
        <servlet-name>CombinedServlet</servlet-name>
        <servlet-class>se.internetapplications.web.taglib.combined.CombinedServlet</servlet-class>
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

    <combine:script name="combined-javascript" reloadable="true">
        <combine:source path="/js/AngularAtmosphere.js" />
        <combine:source path="/js/Humanized.js" />
        ...
    </combine:script>
    
Combine css resources

    <combine:css name="combined-css" reloadable="true">
        <combine:source path="/css/tpa.css"/>
        ...
    </combine:css>
    

