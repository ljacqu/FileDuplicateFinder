package ch.jalu.fileduplicatefinder.config;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;

public class JfuErrorRecorder extends ConvertErrorRecorder {

    private String errorReason;

    @Override
    public void setHasError(String reason) {
        super.setHasError(reason);
        this.errorReason = reason;
    }

    public String getErrorReason() {
        return errorReason;
    }
}
