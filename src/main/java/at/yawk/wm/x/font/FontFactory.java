package at.yawk.wm.x.font;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.awt.*;

/**
 * @author yawkat
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = StandardFontFactory.class
)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = StandardFontFactory.class, name = "standard")
})
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface FontFactory {
    @JsonIgnore
    int getCellSize();

    @JsonIgnore
    String getDescriptor();

    Font createFont(FontStyle style);
}
