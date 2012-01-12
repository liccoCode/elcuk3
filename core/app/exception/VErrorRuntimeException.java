package exception;

import play.data.validation.Error;
import play.utils.FastRuntimeException;

/**
 * View 使用的从业务代码中抛出到 Controller 的 Error 异常
 * User: wyattpan
 * Date: 1/12/12
 * Time: 1:22 PM
 */
public class VErrorRuntimeException extends FastRuntimeException {
    private String key;
    private String message;
    private String[] variables;
    private Error error;

    public VErrorRuntimeException(String key, String message) {
        this.key = key;
        this.message = message;
        this.variables = new String[]{};
    }

    public VErrorRuntimeException(String key, String message, String[] variables) {
        this.key = key;
        this.message = message;
        this.variables = variables;
    }

    public Error getError() {
        if(error == null) {
            error = new Error(this.key, this.message, this.variables);
        }
        return this.error;
    }
}
