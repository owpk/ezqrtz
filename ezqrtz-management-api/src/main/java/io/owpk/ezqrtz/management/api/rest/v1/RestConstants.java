package io.owpk.ezqrtz.management.api.rest.v1;

public final class RestConstants {
    private RestConstants() {}

    //@formatter:off
    // URL mapping constants
    public static final String BASE_PATH =                  "/v1/scheduling/management";
    public static final String GET_JOB_PATH =               "/job";
    public static final String GET_JOB_DEF_PATH =           "/job/definition";
    public static final String LIST_JOBS_PATH =             "/job/definitions";
    public static final String GET_TRIGGER_PATH =           "/trigger";
    public static final String LIST_TRIGGER_GROUPS_PATH =   "/triggers/groups";
    public static final String LIST_TRIGGERS_PATH =         "/triggers";
    public static final String CREATE_TRIGGER_PATH =        "/trigger";
    public static final String START_TRIGGER_PATH =         "/trigger/start";
    public static final String STOP_TRIGGER_PATH =          "/trigger/stop";
    public static final String UPDATE_TRIGGER_PATH =        "/trigger";
    public static final String GET_INFO_PATH =              "/info";
    //@formatter:on

}
