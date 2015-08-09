package at.yawk.wm.style;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author yawkat
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.MINIMAL_CLASS,
        defaultImpl = NamedFontDescriptor.class
)
@JsonSubTypes({ @JsonSubTypes.Type(NamedFontDescriptor.class) })
public interface FontDescriptor {}
