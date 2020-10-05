package net.impactdev.gts.reforged.sponge.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.impactor.api.json.factory.JObject;

import java.util.Map;

public class JObjectConverter {

    public static JObject convert(net.impactdev.pixelmonbridge.data.factory.JObject original) {
        JObject result = new JObject();
        JsonObject object = original.toJson();
        for(Map.Entry<String, JsonElement> element : object.entrySet()) {
            result.add(element.getKey(), element.getValue());
        }

        return result;
    }

}
