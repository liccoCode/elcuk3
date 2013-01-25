package exception;

import play.utils.FastRuntimeException;

/**
 * 判断支付模块中的异常
 * User: wyatt
 * Date: 1/25/13
 * Time: 3:19 PM
 */
public class PaymentException extends FastRuntimeException {
    public PaymentException() {
        super();
    }

    public PaymentException(String desc) {
        super(desc);
    }

    public PaymentException(String desc, Throwable cause) {
        super(desc, cause);
    }

    public PaymentException(Throwable cause) {
        super(cause);
    }
}
