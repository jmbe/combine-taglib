package se.intem.web.taglib.combined.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.CombinedResourceRepository;
import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.configuration.DependencyCache;
import se.intem.web.taglib.combined.resources.CombinedBundle;

public class CombinedServlet extends HttpServlet {

    private transient CombinedResourceRepository repository;

    private DependencyCache dependencies;

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CombinedServlet.class);

    private static final String CHARSET_UTF8 = ";charset=utf-8";
    private static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * Should be formatted as RFC1123 in GMT timezone.
     */
    private static final String expiresDateFormat = "EEE, dd MMM yyyy HH:mm:ss zzz";

    @Override
    public void init() throws ServletException {
        log.debug("Init " + this.getClass().getSimpleName());
        this.repository = CombinedResourceRepository.get();
        this.dependencies = DependencyCache.get();
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException {

        log.info("Handling {}", request.getRequestURI());

        RequestPath path = new RequestPath(request.getRequestURI());

        if ("/systemjs.combined".equals(request.getRequestURI())) {
            createSystemJsConfig(response);
            return;
        }

        CombinedBundle resource = repository.getCombinedResource(path);

        if (resource != null) {
            response.setContentType(resource.getContentType() + CHARSET_UTF8);
            cacheResource(response, 365);
            resource.write(response.getWriter());
        } else {
            log.debug("Could not find resource for {}", request.getRequestURI());
        }

        response.getWriter().flush();
        response.getWriter().close();
    }

    private void createSystemJsConfig(final HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write("System.config({})");

        dependencies.createSystemJsConfiguration();

        writer.flush();
        writer.close();
    }

    private void cacheResource(final HttpServletResponse response, final int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        SimpleDateFormat formatter = new SimpleDateFormat(expiresDateFormat, Locale.ENGLISH);
        formatter.setTimeZone(GMT_ZONE);

        String formatted = formatter.format(calendar.getTime());
        response.setHeader("Expires", formatted);
        response.setHeader("Cache-Control", "max-age=" + days * 24 * 60 * 60);
        response.setHeader("Pragma", "cache");
    }
}
