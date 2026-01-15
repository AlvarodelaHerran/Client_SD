package model;

import java.util.List;

public class AssignRequest {

    private String plantName;
    private List<Long> dumpsterIds;

    public AssignRequest(String plantName, List<Long> dumpsterIds) {
        this.plantName = plantName;
        this.dumpsterIds = dumpsterIds;
    }
}
