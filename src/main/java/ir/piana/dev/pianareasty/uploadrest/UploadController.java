package ir.piana.dev.pianareasty.uploadrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.piana.dev.pianareasty.sqlrest.SqlQueryService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("api/upload")
public class UploadController {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SqlQueryService sqlService;

    @Autowired
    @Qualifier("databaseStorageService")
    private StorageService storageService;

    @Autowired
    StorageProperties storageProperties;

    private static ObjectMapper jsonMapper = new ObjectMapper();

    @RequestMapping(value = "/serve", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "application/json; charset=utf8")
    public @ResponseBody
    ResponseEntity upload(HttpServletRequest request,
                          @RequestHeader("image_upload_group") String group,
                          @RequestParam("file") MultipartFile file,
                          RedirectAttributes redirectAttributes) {
        String rotation = request.getHeader("image-upload-rotation");
        String path = storageService.store(file, group, rotation);
        GroupProperties groupProperties = storageService.getGroupProperties(group);
        try {
            String beanName = storageProperties.getBean();
            AfterSaveImageAction bean = (AfterSaveImageAction) applicationContext.getBean(beanName);
            BiFunction<HttpServletRequest, String, ResponseEntity> field = bean.getField(
                    groupProperties.getAfterSaveImageActivity());
            return field.apply(request, path);
        } catch (NoSuchFieldException e){
            e.printStackTrace();
            return internalServerError.apply(request);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return internalServerError.apply(request);
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError.apply(request);
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

    public static abstract class AfterSaveImageAction {
        public BiFunction<HttpServletRequest, String, ResponseEntity> getField(String fieldName)
                throws NoSuchFieldException, IllegalAccessException {
            Field field = this.getClass().getField(fieldName);
            return (BiFunction<HttpServletRequest, String, ResponseEntity>) field.get(this);
        }

        public Object getValueObject(String val) {
            if(val.startsWith("i:")) {
                return Integer.parseInt(val.substring(2));
            } else if(val.startsWith("l:")) {
                return Long.parseLong(val.substring(2));
            } else if(val.startsWith("f:")) {
                return Float.parseFloat(val.substring(2));
            } else if(val.startsWith("d:")) {
                return Double.parseDouble(val.substring(2));
            } else if(val.startsWith("b:")) {
                return Boolean.valueOf(val.substring(2));
            } else {
                return val;
            }
        }
    }

    public static enum AjaxReplaceType {
        NO_RESULT,
        ITS_ID
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
