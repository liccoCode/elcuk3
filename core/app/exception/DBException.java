package exception;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/20/12
 * Time: 3:50 PM
 */
public class DBException extends RuntimeException {
    public DBException() {
        super();
    }

    public DBException(String message) {
        super(message);
    }

    public DBException(String message, Throwable cause) {
        super(message, cause);
    }

    public DBException(Throwable cause) {
        super(cause);
    }
}
