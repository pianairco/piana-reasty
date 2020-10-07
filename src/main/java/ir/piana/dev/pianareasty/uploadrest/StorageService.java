package ir.piana.dev.pianareasty.uploadrest;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    void init();

    GroupProperties getGroupProperties(String group);

    String store(MultipartFile file, String group);

    default String store(MultipartFile file, String group, String rotation) {
        throw new NotImplementedException();
    }

    default String store(MultipartFile file, String group, String rotation, Integer width, Integer height) {
        throw new NotImplementedException();
    }

    default String store(String file, String group, int rotation) {
        throw new NotImplementedException();
    }

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}
