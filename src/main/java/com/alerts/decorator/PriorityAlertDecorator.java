package com.alerts.decorator;

import com.alerts.AlertInterface;

/**
 * This decorator marks an alert as high priority by adding "[PRIORITY] " at the start of the condition.
 * You can also store a priority level string like "HIGH" or "MEDIUM".
 *
 * <p>This uses the Decorator design pattern - it wraps an existing alert and changes the condition text.
 */
public class PriorityAlertDecorator extends AlertDecorator {

    // the priority level, for example "HIGH" or "MEDIUM"
    private String priorityLevel;

    /**
     * Creates a new PriorityAlertDecorator that wraps the given alert.
     *
     * @param alert the alert we want to mark as priority
     * @param priorityLevel a string like "HIGH" or "MEDIUM" describing the priority
     */
    public PriorityAlertDecorator(AlertInterface alert, String priorityLevel) {
        super(alert);
        this.priorityLevel = priorityLevel;
    }

    /**
     * Returns the condition message with "[PRIORITY] " added at the beginning.
     * For example: "[PRIORITY] Low blood saturation"
     *
     * @return the condition string with "[PRIORITY] " prepended
     */
    @Override
    public String getCondition() {
        return "[PRIORITY] " + wrappedAlert.getCondition();
    }

    /**
     * Returns the priority level that was set when this decorator was created.
     *
     * @return the priority level string
     */
    public String getPriorityLevel() {
        return priorityLevel;
    }
}
