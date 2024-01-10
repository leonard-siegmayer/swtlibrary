package de.teamA.SWT.entities.reqres;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.teamA.SWT.entities.serializers.JsonDateSerializer;
import de.teamA.SWT.entities.serializers.JsonTimeLineDateSerializer;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class PlotData {

    @JsonProperty("timeline")
    @JsonSerialize(contentUsing = JsonTimeLineDateSerializer.class)
    private List<Date> timeLine;

    @JsonProperty("data")
    @JsonSerialize(keyUsing = JsonDateSerializer.class)
    private Map<Date, Distribution> data;

    public PlotData(List<Date> timeLine, Map<Date, Distribution> data) {
        this.timeLine = timeLine;
        this.data = data;
    }

    public Map<Date, Distribution> getData() {
        return data;
    }

    public void setData(Map<Date, Distribution> data) {
        this.data = data;
    }

    public List<Date> getTimeLine() {
        return timeLine;
    }

    public void setTimeLine(List<Date> timeLine) {
        this.timeLine = timeLine;
    }

}
