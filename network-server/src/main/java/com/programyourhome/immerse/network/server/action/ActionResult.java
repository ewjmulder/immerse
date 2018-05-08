package com.programyourhome.immerse.network.server.action;

import java.io.Serializable;

import com.programyourhome.immerse.domain.Serialization;

/**
 * An action result represents the outcome of an action:
 * - success or not
 * - if success: a result value
 * - if no success: an error message
 */
public class ActionResult<T> implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    private final boolean success;
    private final T result;
    private final String errorMessage;

    private ActionResult(boolean success, T result, String errorMessage) {
        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public T getResult() {
        return this.result;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public String toString() {
        return this.success ? "[Success]: " + this.result : "[Error]: " + this.errorMessage;
    }

    public static <T> ActionResult<T> success(T result) {
        return new ActionResult<>(true, result, null);
    }

    public static <T> ActionResult<T> error(String errorMessage) {
        return new ActionResult<>(false, null, errorMessage);
    }

    public static <T> ActionResult<T> error(Exception exception) {
        return new ActionResult<>(false, null, exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }

}
