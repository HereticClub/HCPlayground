package org.hcmc.hcplayground.deserializer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.hcmc.hcplayground.model.player.CrazyBlockRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapObjectDeserializer extends TypeAdapter<Map<?, ?>> {
    @Override
    public void write(JsonWriter jsonWriter, Map<?, ?> map) throws IOException {

    }

    @Override
    public Map<?, ?> read(JsonReader jsonReader) throws IOException {
        Map<?, ?> map = new HashMap<>();

        jsonReader.beginObject();

        while(jsonReader.hasNext()){
            var name = jsonReader.nextName();
            var value = jsonReader.nextString();
            System.out.println(name + ": " + value);

            //map.put(name, value);
        }

        jsonReader.endObject();

        return map;
    }
}
