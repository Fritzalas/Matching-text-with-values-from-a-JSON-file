package Exercise;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataEntry {
    private int index;
    private String value;
    private String matchedValue;

    public DataEntry(int index, String value, String matchedValue) {
        this.index = index;
        this.value = value;
        this.matchedValue = matchedValue;
    }

    @JsonProperty("index")
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @JsonProperty("matchedValue")
    public String getMatchedValue() {
        return matchedValue;
    }

    public void setMatchedValue(String matchedValue) {
        this.matchedValue = matchedValue;
    }
}
