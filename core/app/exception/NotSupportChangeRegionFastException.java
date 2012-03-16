package exception;

import play.utils.FastRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/16/12
 * Time: 4:05 PM
 */
public class NotSupportChangeRegionFastException extends FastRuntimeException {
    public NotSupportChangeRegionFastException() {
        super("Not Support Change Region Function.");
    }

    public NotSupportChangeRegionFastException(String desc) {
        super(desc);
    }

    public NotSupportChangeRegionFastException(String desc, Throwable cause) {
        super(desc, cause);
    }

    public NotSupportChangeRegionFastException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return super.fillInStackTrace();
    }
}
