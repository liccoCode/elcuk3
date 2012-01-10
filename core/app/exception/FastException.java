package exception;

/**
 * 快速异常, 没有堆栈信息的
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午6:38
 */
public class FastException extends RuntimeException {
    public FastException() {
    }

    public FastException(String message) {
        super(message);
    }

    public FastException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }
}
