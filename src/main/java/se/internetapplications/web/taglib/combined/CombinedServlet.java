package se.internetapplications.web.taglib.combined;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("serial")
public class CombinedServlet extends HttpServlet {

    /** Logger for this class. */
    private static final Log log = LogFactory.getLog(CombinedServlet.class);

    private static final String CHARSET_UTF8 = ";charset=utf-8";

    @Override
    protected void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {

        log.info(request.getRequestURI());

        CombinedResource resource = CombinedResourceRepository
                .getCombinedResource(request.getRequestURI());
        response.setContentType(resource.getContentType() + CHARSET_UTF8);
        resource.writeMinifiedResource(response.getWriter());
        response.getWriter().flush();
        response.getWriter().close();

    }
}
