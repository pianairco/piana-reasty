package ir.piana.dev.pianareasty.uploadrest;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface AfterSaveImage<T> {
    ResponseEntity<T> doWork(HttpServletRequest request, String path);

    static Object getValueObject(String val) {
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
