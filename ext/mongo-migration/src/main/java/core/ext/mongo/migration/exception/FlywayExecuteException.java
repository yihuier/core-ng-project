package core.ext.mongo.migration.exception;

import core.framework.log.ErrorCode;
import core.framework.log.Severity;

import java.io.Serial;

/**
 * @author Neal
 */
public class FlywayExecuteException extends RuntimeException implements ErrorCode {
    @Serial
    private static final long serialVersionUID = -1649290517142934034L;

    public FlywayExecuteException(String message) {
        super(message);
    }

    public FlywayExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
    }

    @Override
    public String errorCode() {
        return "FLY_WAY_EXECUTE_EXCEPTION";
    }
}
