package org.hcmc.hcplayground.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapObjectSerialization extends TypeAdapter<Map<?, ?>> {
    @Override
    public void write(JsonWriter jsonWriter, Map<?, ?> map) throws IOException {

    }

    @Override
    public Map<?, ?> read(JsonReader jsonReader) throws IOException {
        Map<?, ?> map = new HashMap<>();

        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            var name = jsonReader.nextName();
            var value = jsonReader.nextString();
            System.out.println(name + ": " + value);

            //map.put(name, value);
        }

        jsonReader.endObject();

        return map;
    }
}
