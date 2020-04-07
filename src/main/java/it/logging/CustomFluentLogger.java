package it.logging;

import org.fluentd.logger.FluentLogger;
import org.fluentd.logger.FluentLoggerFactory;

public class CustomFluentLogger extends FluentLogger
{


    public static FluentLogger getLogger(String tagPrefix)
    {
        return FluentLogger.getLogger(tagPrefix,"fluentd.kube-logging.svc.cluster.local",24224);
    }
}
