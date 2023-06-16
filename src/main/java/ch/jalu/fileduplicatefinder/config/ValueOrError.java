package ch.jalu.fileduplicatefinder.config;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

/**
 * Encapsulates a value or an error. Exactly one field is always non-null.
 *
 * @param <T> the value type
 */
public class ValueOrError<T> {

    private final @Nullable T value;
    private final @Nullable String errorReason;

    private ValueOrError(@Nullable T value, @Nullable String errorReason) {
        Preconditions.checkArgument(value != null ^ errorReason != null,
            "Value xor errorReason must be not null");
        this.value = value;
        this.errorReason = errorReason;
    }

    /**
     * Creates a new instance with the given value.
     *
     * @param value the result (not null)
     * @return instance with the value
     * @param <T> the value's type
     */
    public static <T> ValueOrError<T> forValue(T value) {
        return new ValueOrError<>(value, null);
    }

    /**
     * Creates a new instance with the given error reason.
     *
     * @param errorReason error detailing the issue with the conversion (not null)
     * @return instance with the error
     * @param <T> value or error type
     */
    public static <T> ValueOrError<T> forError(String errorReason) {
        return new ValueOrError<>(null, errorReason);
    }

    /**
     * @return the value; null if there is an error, otherwise guaranteed not-null
     */
    @Nullable
    public T getValue() {
        return value;
    }

    /**
     * @return the error; null if there is a value, otherwise guaranteed not-null
     */
    @Nullable
    public String getErrorReason() {
        return errorReason;
    }
}
