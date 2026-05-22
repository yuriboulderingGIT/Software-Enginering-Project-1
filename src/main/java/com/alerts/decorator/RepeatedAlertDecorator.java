package com.alerts.decorator;

import com.alerts.AlertInterface;

/**
 * This decorator adds a repeat count to an alert's condition message.
 * For example if the same alert fired 3 times, this will add "[Repeated x3]" at the end.
 *
 * <p>This uses the Decorator design pattern - it wraps an existing alert and changes the condition text.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    // how many times this alert has been repeated
    private int repeatCount;

    /**
     * Creates a new RepeatedAlertDecorator that wraps the given alert.
     *
     * @param alert the alert we want to mark as repeated
     * @param repeatCount how many times the alert was repeated
     */
    public RepeatedAlertDecorator(AlertInterface alert, int repeatCount) {
        super(alert);
        this.repeatCount = repeatCount;
    }

    /**
     * Returns the condition message with the repeat count added at the end.
     * For example: "Low blood saturation [Repeated x3]"
     *
     * @return the condition string with "[Repeated xN]" appended
     */
    @Override
    public String getCondition() {
        return wrappedAlert.getCondition() + " [Repeated x" + repeatCount + "]";
    }

    /**
     * Returns how many times this alert was repeated.
     *
     * @return the repeat count number
     */
    public int getRepeatCount() {
        return repeatCount;
    }
}
