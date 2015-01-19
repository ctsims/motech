package org.motechproject.mds.json.rest.swagger.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by pawel on 1/19/15.
 */
public class Schema implements Serializable {

    private static final long serialVersionUID = -6514177795552932633L;

    private String type;
    private Map<String, String> items;

    public Schema() {
    }

    public Schema(String type, Map<String, String> items) {
        this.type = type;
        this.items = items;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getItems() {
        return items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }
}