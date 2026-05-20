package com.alerts;

public class PriorityAlertDecorator extends AlertDecorator {
    private final String priorityLevel;

    public PriorityAlertDecorator(AlertInterface alert, String priorityLevel) {
        super(alert);
        this.priorityLevel = priorityLevel;
    }

    @Override
    public String getCondition() {
        return "[PRIORITY] " + wrappedAlert.getCondition();
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }
}
