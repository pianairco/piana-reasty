package ir.piana.dev.pianareasty.sqlrest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

//@Profile({ "production"})
@ConfigurationProperties(prefix = "app.service")
public class ServiceProperties {
    Map<String, ActivityStage> resources;
    Map<String, Map<String, Activity>> actions;
//    List<Activity> actions;

    public Map<String, Map<String, Activity>> getActions() {
        return actions;
    }

    public void setActions(Map<String, Map<String, Activity>> actions) {
        this.actions = actions;
    }

    public Map<String, ActivityStage> getResources() {
        return resources;
    }

    public void setResources(Map<String, ActivityStage> resources) {
        this.resources = resources;
    }

    public static class Activity {
        private String method;
        private String result;
        private List<ActivityStage> stages;

        public Activity() {
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<ActivityStage> getStages() {
            return stages;
        }

        public void setStages(List<ActivityStage> stages) {
            this.stages = stages;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    public static class ActivityStage {
        private String name;
        private Trx trx;
        @JsonProperty("throws")
        private Throws exception;
        private String function;
        private SQL sql;

        public ActivityStage() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFunction() {
            return function;
        }

        public void setFunction(String function) {
            this.function = function;
        }

        public SQL getSql() {
            return sql;
        }

        public void setSql(SQL sql) {
            this.sql = sql;
        }

        public Throws getThrows() {
            return exception;
        }

        public void setThrows(Throws exception) {
            this.exception = exception;
        }

        public Trx getTrx() {
            return trx;
        }

        public void setTrx(Trx trx) {
            this.trx = trx;
        }
    }

    public static class Trx {
        private Propagation propagation;

        public Propagation getPropagation() {
            return propagation;
        }

        public void setPropagation(Propagation propagation) {
            this.propagation = propagation;
        }
    }

    public static class SQL {
        private String type;
        private String query;
        private String resultType;
        private String sequenceName;
        private String params;
        private String result;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getResultType() {
            return resultType;
        }

        public void setResultType(String resultType) {
            this.resultType = resultType;
        }

        public String getSequenceName() {
            return sequenceName;
        }

        public void setSequenceName(String sequenceName) {
            this.sequenceName = sequenceName;
        }

        public String getParams() {
            return params;
        }

        public void setParams(String params) {
            this.params = params;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    public static class Throws {
        private String name;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }
}
