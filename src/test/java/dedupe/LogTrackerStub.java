package dedupe;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public class LogTrackerStub  implements BeforeTestExecutionCallback, AfterTestExecutionCallback
{
    private LogTracker logTracker = LogTracker.create();

    private LogTrackerStub() {
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        logTracker.prepareLoggingFramework();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        logTracker.resetLoggingFramework();
    }

    public static LogTrackerStub create() {
        return new LogTrackerStub();
    }

    public LogTrackerStub recordForLevel(LogTracker.LogLevel level) {
        logTracker.recordForLevel(level);
        return this;
    }

    public LogTrackerStub recordForType(Class<?> type) {
        logTracker.recordForType(type);
        return this;
    }

    public List<ILoggingEvent> getLogEvents() {
        return logTracker.getLogs();
    }

    public int size() {
        return logTracker.size();
    }

}