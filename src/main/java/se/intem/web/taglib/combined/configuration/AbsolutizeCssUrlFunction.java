package se.intem.web.taglib.combined.configuration;

import com.google.common.base.Function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.intem.web.taglib.combined.RequestPath;

public class AbsolutizeCssUrlFunction implements Function<String, String> {

    Pattern pattern = Pattern.compile("url\\s*\\(\\s*['\"]?(.+?)['\"]?\\s*\\)");

    private RequestPath css;

    public AbsolutizeCssUrlFunction(final RequestPath css) {
        this.css = css;
    }

    public String apply(final String input) {

        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String filename = matcher.group(1);

            RequestPath url = new RequestPath(filename);

            if (url.isRelative()) {
                return matcher.replaceFirst(String.format("url('%s')", css.dirname() + "/" + filename));
            }

        }

        return input;
    }

}
