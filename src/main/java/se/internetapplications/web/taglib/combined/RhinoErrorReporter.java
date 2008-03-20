package se.internetapplications.web.taglib.combined;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

public class RhinoErrorReporter implements ErrorReporter {

    public void error(String arg0, String arg1, int arg2, String arg3, int arg4) {
        // TODO Auto-generated method stub

    }

    public EvaluatorException runtimeError(String arg0, String arg1, int arg2,
            String arg3, int arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    public void warning(String arg0, String arg1, int arg2, String arg3,
            int arg4) {
        // TODO Auto-generated method stub

    }

}
