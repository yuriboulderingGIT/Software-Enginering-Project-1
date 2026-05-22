package com.alerts;

// Represents an alert
public class Alert implements AlertInterface {
    private String patientId;
    private String condition;
    private long timestamp;

    public Alert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    /**
     * Returns the unique identifier of the patient associated with this alert.
     *
     * @return the patient ID as a String
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Returns the condition description that triggered this alert.
     *
     * @return the condition string
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Returns the time at which this alert was generated.
     *
     * @return the timestamp in milliseconds since the Unix epoch
     */
    public long getTimestamp() {
        return timestamp;
    }
}
