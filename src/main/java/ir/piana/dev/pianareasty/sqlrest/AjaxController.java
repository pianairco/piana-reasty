package ir.piana.dev.pianareasty.sqlrest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.piana.dev.pianareasty.uploadrest.StorageService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * !    => sequence
 * @    => multiple (
 *   * => parameter
 *   # => header
 *   $ => const
 * )
 * %    => like
 */
@Controller
@RequestMapping("api")
public class AjaxController {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SqlQueryService sqlService;

    @Autowired
    private ServiceProperties serviceProperties;

    @Autowired
    private ParameterParser parameterParser;

    @Autowired
    @Qualifier("databaseStorageService")
    private StorageService storageService;

    @Autowired
    @Qualifier("jdbcObjectMapper")
    private ObjectMapper objectMapper;

    /*
    https://www.baeldung.com/spring-programmatic-transaction-management
    https://ducmanhphan.github.io/2020-04-15-How-to-implement-programmatic-transaction-management-in-spring-data-jpa/
     */
    @Autowired
    private PlatformTransactionManager transactionManager;


    private static ObjectMapper jsonMapper = new ObjectMapper();

    private List<String> methods = Arrays.asList(new String[] {"insert", "update", "delete"});

    @PostConstruct
    public void init() {
        System.out.println("init");
//        List<String> dollorKeys = serviceProperties.resources.keySet().stream().collect(Collectors.toList());
//        Collections.sort(dollorKeys, Comparator.comparingInt(String::length).reversed());
    }

    @RequestMapping(value = "resources/serve/{resource-path}/{resource-name}", method = RequestMethod.GET)
    public ResponseEntity handleResource(HttpServletRequest request,
                                         @PathVariable("resource-path") String resourcePath,
                                         @PathVariable("resource-name") String resourceName) {
        if (!serviceProperties.resources.containsKey(resourcePath))
            return notFound.apply(request);
        ServiceProperties.ActivityStage activityStage = serviceProperties.resources.get(resourcePath);

        if (activityStage.getFunction() != null) {
            return notImplement.apply(request);
        } else if (activityStage.getSql() != null) {
            Map<String, Object> containerMap = new LinkedHashMap();
            Object[] params = null;
            if (activityStage.getSql().getParams() != null && !activityStage.getSql().getParams().isEmpty()) {
                String[] split = activityStage.getSql().getParams().split(",");
                params = new Object[split.length];
                for (String s : split) {
                    String[] split1 = s.split("=");
                    if (split1[1].startsWith("~")) {//! => sequence
                        params[Integer.valueOf(split1[0]) - 1] = resourceName;
                    }
                }
            } else {
                params = new Object[0];
            }
            Object result = sqlService.execute(activityStage.getSql(), params);
            if (result instanceof AjaxReplaceType && result == AjaxReplaceType.NO_RESULT)
                return notFound.apply(request);
            else {
                HttpHeaders responseHeaders = new HttpHeaders();
                if (activityStage.getSql().getResult() != null) {
                    Object resource = new LinkedHashMap<>();
                    String[] split = activityStage.getSql().getResult().split(",");
                    for (int i = 0; i < split.length; i++) {
                        String[] split1 = split[i].split("=");
                        Object value = null;
                        if (split1[1].startsWith("@")) {
                            value = containerMap.get(split1[1].substring(1));
                        } else {
                            value = ((Map<String, Object>) result).get(split1[1]);
                        }
                        if (split1[0].startsWith("#")) {
                            responseHeaders.set(split1[0].substring(1), String.valueOf(value));
                        } else if (split1[0].startsWith("$")) {
                            resource = value;
                        }
                    }
                    return ResponseEntity.ok(resource);
                } else {
                    //ToDo: return id as { 'id': id }
                }
                String s = null;
                try {
                    s = objectMapper.writeValueAsString(result);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return ResponseEntity.ok(s);
            }

        }

        return notFound.apply(request);
    }

    @RequestMapping(value = "/ajax/serve", method = RequestMethod.POST,
            consumes = "application/json; charset=utf8",
            produces = "application/json; charset=utf8")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public @ResponseBody
    ResponseEntity handleAjax(HttpServletRequest request,
                              @RequestBody Map<String, Object> body) {
        String actionHeader = (String) request.getHeader("action");
        String activityHeader = (String) request.getHeader("activity");

        Map<String, Object> resultMap = new LinkedHashMap<>();

        if (serviceProperties.getActions().containsKey(actionHeader)
                && serviceProperties.getActions().get(actionHeader).containsKey(activityHeader)) {
            ServiceProperties.Activity activity = serviceProperties.getActions().get(actionHeader).get(activityHeader);

            for (ServiceProperties.ActivityStage stage : activity.getStages()) {
                if(stage.getThrows() != null) {
                  if(stage.getThrows().getType() != null && !stage.getThrows().getType().isEmpty()
                          && stage.getThrows().getType().equalsIgnoreCase("runtime")) {
                      throw new RuntimeException(stage.getThrows().getName());
                  }
                } else if (stage.getFunction() != null && !stage.getFunction().isEmpty()) {
                    try {
                        Action bean = (Action) applicationContext.getBean(actionHeader);
                        if (activityHeader != null && !activityHeader.isEmpty()) {
                            BiFunction<HttpServletRequest, Map<String, Object>, ResponseEntity> field = bean.getField(activityHeader);
                            return field.apply(request, body);
                        }
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        return internalServerError.apply(request);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return internalServerError.apply(request);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return internalServerError.apply(request);
                    }
                } else if (stage.getSql() != null) {
                    Map<String, Object> containerMap = new LinkedHashMap();
                    Object[] params = null;
                    if (stage.getSql().getParams() != null && !stage.getSql().getParams().isEmpty()) {
                        String[] split = stage.getSql().getParams().split(",");
                        params = new Object[split.length];
                        for (String s : split) {
                            String[] split1 = s.split("=");
                            if (split1[1].startsWith("!")) {//! => sequence
                                String[] split2 = split1[1].substring(1).split(":");
                                Long aLong = sqlService.selectSequenceValue(split2[1]);
                                containerMap.put(split2[0], aLong);
                                params[Integer.valueOf(split1[0]) - 1] = aLong;
                            } else if (split1[1].startsWith("%") || split1[1].endsWith("%")) {//%    => like
                                String begin = split1[1].startsWith("%") ? "%" : "";
                                String end = split1[1].endsWith("%") ? "%" : "";
                                String key = split1[1];
                                if (!begin.isEmpty())
                                    key = split1[1].substring(1);
                                if (!end.isEmpty())
                                    key = key.substring(0, key.length() - 1);
                                params[Integer.valueOf(split1[0]) - 1] = begin + body.get(key) + end;
                            } else if (split1[1].startsWith("@")) {// @  => multiple (* => parameter, # => header, $ => const)
                                String[] substring = split1[1].substring(1).split(":");
                                String index = substring[0];
                                String[] split2 = substring[1].split("&");
                                String base64 = parameterParser.parse(split2[0], request, body);
                                Integer rotate = parameterParser.parse(split2[1], request, body);
                                String group = parameterParser.parse(split2[2], request, body);
                                String imageSrc = storageService.store(base64, group, rotate);
                                params[Integer.valueOf(split1[0]) - 1] = imageSrc;
                                containerMap.put(index, imageSrc);
                            } else if (split1[1].startsWith("^")) {// @  => multiple (* => parameter, # => header, $ => const)
                                String[] substring = split1[1].substring(1).split(":");
                                String index = substring[0];
                                String type = substring[1];
                                Object val = null;
                                if(type.equalsIgnoreCase("i")) {
                                    val = resultMap.get(index);
                                }
                                params[Integer.valueOf(split1[0]) - 1] = val;
                            } else {
                                params[Integer.valueOf(split1[0]) - 1] = parameterParser.parse(split1[1], request, body);
                            }
                        }
                    } else {
                        params = new Object[0];
                    }
                    Object result = sqlService.execute(stage.getSql(), params);
                    if (result instanceof AjaxReplaceType && result == AjaxReplaceType.NO_RESULT)
                        return notFound.apply(request);
                    else {
//                        if (stage.getSql() != null && methods.contains(stage.getSql().getType())) {

                        if (stage.getSql() != null && stage.getSql().getResult() != null) {
                            String[] split = stage.getSql().getResult().split(",");
                            if (stage.getSql().getResultType() == null || stage.getSql().getResultType().isEmpty()) {
                                Map<String, Object> evaluateMap = new LinkedHashMap<>();
                                for (int i = 0; i < split.length; i++) {
                                    String[] split1 = split[i].split("=");
                                    if (split1[1].startsWith("@")) {
                                        evaluateMap.put(split1[0], containerMap.get(split1[1].substring(1)));
                                    } else {
                                        evaluateMap.put(split1[0], body.get(split1[1]));
                                    }
                                }
                                resultMap.put(stage.getName(), evaluateMap);
                            } else if (stage.getSql().getResultType().equalsIgnoreCase("object")) {
                                Map<String, Object> evaluateMap = new LinkedHashMap<>();
                                for (int i = 0; i < split.length; i++) {
                                    String[] split1 = split[i].split("=");
                                    if (split1[1].startsWith("@")) {
                                        evaluateMap.put(split1[0], containerMap.get(split1[1].substring(1)));
                                    } else if (split1[1].startsWith("~")) {
                                        evaluateMap.put(split1[0], ((Map)result).get(split1[1].substring(1)));
                                    } else {
                                        evaluateMap.put(split1[0], body.get(split1[1]));
                                    }
                                }
                                resultMap.put(stage.getName(), evaluateMap);
                            } else if (stage.getSql().getResultType().equalsIgnoreCase("list")) {
                                List<Map> list = new ArrayList<>();
                                for(Map<String, Object> res : ((List<Map>)result)) {
                                    Map<String, Object> evaluateMap = new LinkedHashMap<>();
                                    for (int i = 0; i < split.length; i++) {
                                        String[] split1 = split[i].split("=");
                                        if (split1[1].startsWith("@")) {
                                            evaluateMap.put(split1[0], containerMap.get(split1[1].substring(1)));
                                        } else if (split1[1].startsWith("~")) {
                                            evaluateMap.put(split1[0], res.get(split1[1].substring(1)));
                                        } else {
                                            evaluateMap.put(split1[0], body.get(split1[1]));
                                        }
                                    }
                                    list.add(evaluateMap);
                                }
                                resultMap.put(stage.getName(), list);
                            }
                        } else {
                            resultMap.put(stage.getName(), result);
                        }
                    }
                }
            }
            Object result = null;
            if(activity.getResult() == null) {
                result = null;
            } else {
                result = resultMap.get(activity.getResult());
            }
            String s = null;
            try {
                s = objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return ResponseEntity.ok(s);
        } else {
            return notFound.apply(request);
        }
    }



    Function<HttpServletRequest, ResponseEntity> notFound = (r) -> {
        HttpHeaders responseHeaders = new HttpHeaders();
        return new ResponseEntity<String>("Not Fund", responseHeaders, HttpStatus.NOT_FOUND);
    };

    Function<HttpServletRequest, ResponseEntity> notImplement = (r) -> {
        HttpHeaders responseHeaders = new HttpHeaders();
        return new ResponseEntity<String>("Not Implemented", responseHeaders, HttpStatus.NOT_IMPLEMENTED);
    };

    Function<HttpServletRequest, ResponseEntity> internalServerError = (r) -> {
        HttpHeaders responseHeaders = new HttpHeaders();
        return new ResponseEntity<String>("Internal Server Error", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    };

    public static String getTemplate(String vueTemplate) {
        vueTemplate = Arrays.stream(vueTemplate.split("\r\n")).map(line -> "\"".concat(line).concat("\" +\r\n "))
                .collect(Collectors.joining( ));
        return vueTemplate.substring(0, vueTemplate.lastIndexOf("+"));
    }

    public static <T> T getDto(InputStream inputStream, Class<T> dtoType)
            throws IOException {
        String s = IOUtils.toString(inputStream);
        T t = jsonMapper.readValue(s, dtoType);
        return t;
    }

    public static abstract class Action {
        public BiFunction<HttpServletRequest, Map<String, Object>, ResponseEntity> getField(String fieldName)
                throws NoSuchFieldException, IllegalAccessException {
            Field field = this.getClass().getField(fieldName);
            return (BiFunction<HttpServletRequest, Map<String, Object>, ResponseEntity>) field.get(this);
        }
    }

    public static enum AjaxReplaceType {
        NO_RESULT,
        INSERTED,
        UPDATED,
        ITS_ID
    }
}
