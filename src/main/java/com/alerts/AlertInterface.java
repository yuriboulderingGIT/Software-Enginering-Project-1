package com.alerts;

public interface AlertInterface {

    /**
     * Returns the unique identifier of the patient associated with this alert.
     *
     * @return the patient ID as a String
     */
    String getPatientId();

    /**
     * Returns the condition description that triggered this alert.
     *
     * @return the condition string
     */
    String getCondition();

    /**
     * Returns the time at which this alert was generated.
     *
     * @return the timestamp in milliseconds since the Unix epoch
     */
    long getTimestamp();
}
