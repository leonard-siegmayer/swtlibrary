package de.teamA.SWT.entities.reqres;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Map;

@JsonPropertyOrder({ "total", "distribution" })
public class Distribution {

    @JsonProperty("total")
    private Long total;

    @JsonProperty("distribution")
    private Map<String, Long> distribution;

    public Distribution(Long total, Map<String, Long> distribution) {
        this.total = total;
        this.distribution = distribution;
    }

    public Long getTotal() {
        return total;
    }

    public Map<String, Long> getDistribution() {
        return distribution;
    }

    @Override
    public String toString() {
        String string = "Total: " + total + "\n";
        for (Map.Entry<String, Long> entry : distribution.entrySet()) {
            string += "Key : " + entry.getKey() + " Count : " + entry.getValue();
        }

        return string;
    }
}
