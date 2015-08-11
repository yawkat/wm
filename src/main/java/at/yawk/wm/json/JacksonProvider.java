package at.yawk.wm.json;

import at.yawk.wm.style.FontDescriptor;
import at.yawk.wm.style.NamedFontDescriptor;
import at.yawk.wm.x.icon.IconDescriptor;
import at.yawk.yarn.Component;
import at.yawk.yarn.Provides;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import java.awt.*;
import java.io.IOException;

/**
 * @author yawkat
 */
@Component
public class JacksonProvider {
    private static final char[] ZEROES = { '0', '0', '0', '0', '0', '0' };

    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Color.class, new StdSerializer<Color>(Color.class) {
            @Override
            public void serialize(Color value, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException {
                StringBuilder builder = new StringBuilder(7);
                // insert hex representation
                builder.append(Integer.toHexString(value.getRGB() & 0xffffff));
                // fill up with zeroes
                builder.insert(0, ZEROES, 0, 6 - builder.length());
                // prepend #
                builder.insert(0, '#');

                jgen.writeString(builder.toString());
            }
        });
        module.addDeserializer(Color.class, new StdDeserializer<Color>(Color.class) {
            @Override
            public Color deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                String str = p.getValueAsString();
                if (str.isEmpty()) {
                    throw ctxt.weirdStringException(str, Color.class, "Expected non-empty string");
                }
                int rgb;
                boolean numberSignPrefix = str.charAt(0) == '#';
                if (numberSignPrefix) {
                    if (str.length() != 7) {
                        throw ctxt.weirdStringException(str, Color.class, "Expected 7 characters");
                    }
                    rgb = Integer.parseUnsignedInt(str.substring(1), 16);
                } else {
                    if (str.length() != 6) {
                        throw ctxt.weirdStringException(str, Color.class, "Expected 6 characters");
                    }
                    rgb = Integer.parseUnsignedInt(str, 16);
                }
                return new Color(rgb);
            }
        });
        module.addKeyDeserializer(FontDescriptor.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return new NamedFontDescriptor(key);
            }
        });
        module.addKeyDeserializer(IconDescriptor.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return new IconDescriptor(key);
            }
        });
        yaml.registerModule(module);
        yaml.registerModule(new Jdk7Module());
        yaml.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Provides
    public ObjectMapper yaml() {
        return yaml;
    }

}
