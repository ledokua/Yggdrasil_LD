package net.ledok.util;

import java.util.List;

public interface AttributeProvider {
    List<AttributeData> getAttributes();
    void setAttributes(List<AttributeData> attributes);
}
