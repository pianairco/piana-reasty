package ir.piana.dev.pianareasty.util;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;

public class LowerCaseKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext deserializationContext) throws IOException {
        return key.toLowerCase();
    }
}
