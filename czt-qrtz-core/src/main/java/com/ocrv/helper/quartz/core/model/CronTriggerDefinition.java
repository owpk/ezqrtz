package com.ocrv.helper.quartz.core.model;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.time.ZoneId;
import java.util.TimeZone;

@Builder
public record CronTriggerDefinition(
        Cron cron, @Nullable TimeZone timeZone
) implements TriggerDefinition {
    public static final CronDefinition cronDefinition =
            CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
    public static final CronParser cronParser = new CronParser(cronDefinition);

    public CronTriggerDefinition(Cron cron) {
        this(cron, null);
    }

    public CronTriggerDefinition(String cron) {
        this(cronParser.parse(cron), null);
    }

    public CronTriggerDefinition {
        cron.validate();
    }

    public static CronTriggerDefinition of(String cron, @Nullable String zoneId) {
        if (zoneId != null && !zoneId.isBlank())
            return new CronTriggerDefinition(cronParser.parse(cron), TimeZone.getTimeZone(zoneId));
        else return new CronTriggerDefinition(cron);
    }

    public static CronTriggerDefinition of(String cron, @Nullable ZoneId zoneId) {
        if (zoneId != null)
            return new CronTriggerDefinition(cronParser.parse(cron), TimeZone.getTimeZone(zoneId));
        else return new CronTriggerDefinition(cron);
    }

    public static CronTriggerDefinition of(Cron cron, @Nullable ZoneId zoneId) {
        if (zoneId != null)
            return new CronTriggerDefinition(cron, TimeZone.getTimeZone(zoneId));
        else return new CronTriggerDefinition(cron);
    }

    public String cronString() {
        return cron.asString();
    }
}