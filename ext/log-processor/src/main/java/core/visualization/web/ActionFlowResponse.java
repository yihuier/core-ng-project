package core.visualization.web;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author allison
 */
public class ActionFlowResponse {
    @NotNull
    @Property(name = "graph")
    public String graph;

    @NotNull
    @Property(name = "edges")
    public List<EdgeInfo> edges = new ArrayList<>();

    public static class EdgeInfo {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "actionName")
        public String actionName;

        @NotNull
        @Property(name = "elapsed")
        public Long elapsed;

        @NotNull
        @Property(name = "cpuTime")
        public Long cpuTime;

        @Property(name = "httpElapsed")
        public Long httpElapsed;

        @Property(name = "dbElapsed")
        public Long dbElapsed;

        @Property(name = "redisElapsed")
        public Long redisElapsed;

        @Property(name = "esElapsed")
        public Long esElapsed;

        @Property(name = "kafkaElapsed")
        public Long kafkaElapsed;

        @Property(name = "cacheHits")
        public Long cacheHits;

        @Property(name = "errorCode")
        public String errorCode;

        @Property(name = "errorMessage")
        public String errorMessage;
    }
}
