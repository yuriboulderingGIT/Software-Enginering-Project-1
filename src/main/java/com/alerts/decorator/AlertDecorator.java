package com.alerts.decorator;

import com.alerts.AlertInterface;

/**
 * This is the base class for all alert decorators.
 * The Decorator design pattern lets us add extra information to an alert without changing the original Alert class.
 *
 * <p>This class holds a reference to another AlertInterface (which could be a plain Alert
 * or another decorator). Subclasses can then override methods to add their own behaviour on top.
 */
public abstract class AlertDecorator implements AlertInterface {

    // the alert we are wrapping - could be a plain Alert or another decorator
    protected AlertInterface wrappedAlert;

    /**
     * Creates a new decorator that wraps around the given alert.
     *
     * @param alert the alert we want to add extra behaviour to
     */
    public AlertDecorator(AlertInterface alert) {
        this.wrappedAlert = alert;
    }

    /**
     * Returns the patient id from the wrapped alert.
     *
     * @return the patient id string
     */
    @Override
    public String getPatientId() {
        return wrappedAlert.getPatientId();
    }

    /**
     * Returns the condition message from the wrapped alert.
     * Subclasses can override this to add extra text to the message.
     *
     * @return the condition string
     */
    @Override
    public String getCondition() {
        return wrappedAlert.getCondition();
    }

    /**
     * Returns the timestamp from the wrapped alert.
     *
     * @return the timestamp in milliseconds
     */
    @Override
    public long getTimestamp() {
        return wrappedAlert.getTimestamp();
    }
}
