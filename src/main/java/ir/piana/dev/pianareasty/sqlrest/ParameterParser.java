package ir.piana.dev.pianareasty.sqlrest;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class ParameterParser {
    public <T> T parse(String key, HttpServletRequest request, Map<String, Object> body) {
        T value = null;
        if (key.contains(".")) {
            String[] split = key.split("\\.");
            value = (T) ((Map<String, Object>)body.get(split[0])).get(split[1]);
        } else if (key.startsWith("*")) {
            value = (T) request.getParameter(key.substring(1));
        } else if (key.startsWith("#")) {
            value = (T) request.getHeader(key.substring(1));
        } else if (key.startsWith("$")) {
            value = (T) key.substring(1);
        } else {
            value = (T) body.get(key);
        }
        return value;
    }
}
