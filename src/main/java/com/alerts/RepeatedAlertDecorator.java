package com.alerts;

public class RepeatedAlertDecorator extends AlertDecorator {
    private final int repeatCount;

    public RepeatedAlertDecorator(AlertInterface alert, int repeatCount) {
        super(alert);
        this.repeatCount = repeatCount;
    }

    @Override
    public String getCondition() {
        return wrappedAlert.getCondition() + " [Repeated x" + repeatCount + "]";
    }

    public int getRepeatCount() {
        return repeatCount;
    }
}
