package se.internetapplications.web.taglib.combined.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.internetapplications.web.taglib.combined.CombinedResource;
import se.internetapplications.web.taglib.combined.CombinedResourceRepository;

public class CombinedServlet extends HttpServlet {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombinedServlet.class);

    private static final String CHARSET_UTF8 = ";charset=utf-8";

    private static final String expiresDateFormat = "EEE, dd MMM yyyy HH:mm:ss zzz";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException {

        log.debug("Handling {}", request.getRequestURI());

        CombinedResource resource = CombinedResourceRepository.getCombinedResource(request.getRequestURI());
        response.setContentType(resource.getContentType() + CHARSET_UTF8);
        cacheResource(response, 365);
        resource.writeMinifiedResource(response.getWriter());
        response.getWriter().flush();
        response.getWriter().close();
    }

    private void cacheResource(final HttpServletResponse response, final int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        SimpleDateFormat formatter = new SimpleDateFormat(expiresDateFormat);
        response.setHeader("Expires", formatter.format(calendar.getTime()));
    }
}
