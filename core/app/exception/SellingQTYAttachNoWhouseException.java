package exception;

import play.utils.FastRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/11/12
 * Time: 9:59 AM
 */
public class SellingQTYAttachNoWhouseException extends FastRuntimeException {
    public SellingQTYAttachNoWhouseException() {
        super("When SellingQTY Attch Selling, Did not find Whouse!");
    }

    public SellingQTYAttachNoWhouseException(String desc) {
        super(desc);
    }

    public SellingQTYAttachNoWhouseException(String desc, Throwable cause) {
        super(desc, cause);
    }

    public SellingQTYAttachNoWhouseException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return super.fillInStackTrace();
    }
}
