package it.logging;

import org.fluentd.logger.FluentLogger;
import org.fluentd.logger.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


public class CustomFluentLogger extends FluentLogger
{

    public static FluentLogger getLogger(String tagPrefix)
    {
        return FluentLogger.getLogger(tagPrefix,"fluentd.kube-logging.svc.cluster.local",24224);
    }




}
