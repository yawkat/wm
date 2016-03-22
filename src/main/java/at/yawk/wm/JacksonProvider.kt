package at.yawk.wm

import at.yawk.wm.style.FontDescriptor
import at.yawk.wm.style.NamedFontDescriptor
import at.yawk.wm.x.icon.IconDescriptor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.awt.Color
import java.io.IOException

/**
 * @author yawkat
 */
internal object JacksonProvider {
    private val ZEROES = charArrayOf('0', '0', '0', '0', '0', '0')

    fun createYamlObjectMapper(): ObjectMapper {
        val yaml = ObjectMapper(YAMLFactory())
        val module = SimpleModule()
        module.addSerializer(Color::class.java, object : StdSerializer<Color>(Color::class.java) {
            @Throws(IOException::class)
            override fun serialize(value: Color, jgen: JsonGenerator, provider: SerializerProvider) {
                val builder = StringBuilder(7)
                // insert hex representation
                builder.append(Integer.toHexString(value.rgb and 0xffffff))
                // fill up with zeroes
                builder.insert(0, ZEROES, 0, 6 - builder.length)
                // prepend #
                builder.insert(0, '#')

                jgen.writeString(builder.toString())
            }
        })
        module.addDeserializer(Color::class.java, object : StdDeserializer<Color>(Color::class.java) {
            @Throws(IOException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Color {
                val str = p.valueAsString
                if (str.isEmpty()) {
                    throw ctxt.weirdStringException(str, Color::class.java, "Expected non-empty string")
                }
                val rgb: Int
                val numberSignPrefix = str[0] == '#'
                if (numberSignPrefix) {
                    if (str.length != 7) {
                        throw ctxt.weirdStringException(str, Color::class.java, "Expected 7 characters")
                    }
                    rgb = Integer.parseUnsignedInt(str.substring(1), 16)
                } else {
                    if (str.length != 6) {
                        throw ctxt.weirdStringException(str, Color::class.java, "Expected 6 characters")
                    }
                    rgb = Integer.parseUnsignedInt(str, 16)
                }
                return Color(rgb)
            }
        })
        module.addKeyDeserializer(FontDescriptor::class.java, object : KeyDeserializer() {
            override fun deserializeKey(key: String, ctxt: DeserializationContext): Any {
                return NamedFontDescriptor(key)
            }
        })
        module.addKeyDeserializer(IconDescriptor::class.java, object : KeyDeserializer() {
            override fun deserializeKey(key: String, ctxt: DeserializationContext): Any {
                return IconDescriptor(key)
            }
        })
        yaml.registerModule(module)
        yaml.registerModule(JavaTimeModule())
        yaml.registerModule(KotlinModule())
        yaml.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return yaml
    }
}
