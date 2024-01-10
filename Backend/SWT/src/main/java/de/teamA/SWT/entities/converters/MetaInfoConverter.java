package de.teamA.SWT.entities.converters;

import javax.persistence.AttributeConverter;
import java.util.*;

public class MetaInfoConverter implements AttributeConverter<Map<String, List<String>>, String> {

    private final String entrySeparator = "¦";
    private final String keyValueSeparator = "ː";
    private final String listSeparator = "﹐";

    @Override
    public String convertToDatabaseColumn(Map<String, List<String>> map) {
        if (map == null || map.size() == 0) {
            return "";
        }

        String[] entryArray = new String[map.size()];
        int pos = 0;

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String entryString = entry.getKey() + keyValueSeparator + String.join(listSeparator, entry.getValue());
            entryArray[pos] = entryString;
            pos++;
        }

        return String.join(entrySeparator, entryArray);
    }

    @Override
    public Map<String, List<String>> convertToEntityAttribute(String serializedString) {
        Map<String, List<String>> map = new HashMap<>();
        if (serializedString != null && !serializedString.isEmpty()) {

            String[] entryArray = serializedString.split(entrySeparator);

            for (String entry : entryArray) {
                String[] keyValuePair = entry.split(keyValueSeparator);
                if (keyValuePair.length < 2) {
                    map.put(keyValuePair[0], new ArrayList<>());
                } else {
                    map.put(keyValuePair[0], new ArrayList<>(Arrays.asList(keyValuePair[1].split(listSeparator))));
                }
            }
        }

        return map;
    }

}
