/*                     __                                               *\
**     ________ ___   / /  ___     Scala Ant Tasks                      **
**    / __/ __// _ | / /  / _ |    (c) 2005-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala.tools.ant;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * An Ant task to determine given the invoked targets which one is currently
 * executed (see http://ant.apache.org/manual/properties.html#built-in-props).
 *
 * @author Stephane Micheloud
 * @version 1.0
 */
public class InvokedTask extends Task {
    private String property = null;
    private String[] names = null;
    private static int i = 0;

    public void setProperty(String property) {
        this.property = property;
    }

    private static final String PROP_NAME = "ant.project.invoked-targets";

    public void execute() {
        Project p = getProject();

        if (property == null)
            throw new BuildException("attribute 'property' must be set");
        if (names == null) {
            String value = p.getProperty(PROP_NAME); 
            if (value == null)
                throw new BuildException("built-in property '"+PROP_NAME+"' is undefied");
            names = value.split(",");
        }
        if (names.length < i)
            throw new BuildException("built-in property '"+PROP_NAME+"' is incomplete");

        p.setProperty(property, names[i++]);
    }

}
