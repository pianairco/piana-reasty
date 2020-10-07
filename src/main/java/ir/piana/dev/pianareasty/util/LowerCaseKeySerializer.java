package ir.piana.dev.pianareasty.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import org.apache.commons.text.CaseUtils;

import java.io.IOException;

public class LowerCaseKeySerializer extends StdKeySerializers.StringKeySerializer {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeFieldName(CaseUtils.toCamelCase(((String)o).toLowerCase(), false, new char[]{'_'}));
    }
}
