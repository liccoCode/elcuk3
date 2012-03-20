package exception;

import play.utils.FastRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/20/12
 * Time: 3:49 PM
 */
public class DBFastException extends FastRuntimeException {
    public DBFastException() {
        super();
    }

    public DBFastException(String desc) {
        super(desc);
    }

    public DBFastException(String desc, Throwable cause) {
        super(desc, cause);
    }

    public DBFastException(Throwable cause) {
        super(cause);
    }
}
