package exception;

import play.utils.FastRuntimeException;

/**
 * 对于需要登陆网站做的操作没有登陆, 则抛出此异常
 * User: wyattpan
 * Date: 3/15/12
 * Time: 3:54 PM
 */
public class NotLoginFastException extends FastRuntimeException {
    public NotLoginFastException() {
        super("Not Login, Please login first!");
    }

    public NotLoginFastException(String desc) {
        super(desc);
    }

    public NotLoginFastException(String desc, Throwable cause) {
        super(desc, cause);
    }

    public NotLoginFastException(Throwable cause) {
        super(cause);
    }
}
