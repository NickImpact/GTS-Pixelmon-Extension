package net.impactdev.gts.reforged.legacy;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

import java.util.List;
import java.util.Map;

public class GsonUtils {

    public static CompoundNBT deserialize(JsonObject json) {
        Map<String, Object> map = new GsonBuilder().setPrettyPrinting().create().fromJson(json.get("element").getAsString(), Map.class);
        return nbtFromMap(map);
    }

    private static CompoundNBT nbtFromMap(Map<String, Object> map) {
        CompoundNBT nbt = new CompoundNBT();

        for (String key : map.keySet()) {
            try {
                apply(nbt, key, map.get(key));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return nbt;
    }

    private static ListNBT nbtFromList(List<Object> list) {
        ListNBT nList = new ListNBT();
        list.forEach(entry -> nList.add(read(entry)));
        return nList;
    }

    private static INBT read(Object in) {
        if(in instanceof String) {
            return StringNBT.valueOf((String) in);
        } else if(in instanceof Map) {
            return nbtFromMap((Map<String, Object>) in);
        } else if(in instanceof List) {
            return nbtFromList((List<Object>) in);
        } else {
            return DoubleNBT.valueOf((double) in);
        }
    }

    private static void apply(CompoundNBT nbt, String key, Object obj) throws Exception {
        if (obj instanceof String)
            nbt.putString(key, (String) obj);
        else if (obj instanceof Map)
            nbt.put(key, nbtFromMap((Map<String, Object>) obj));
        else if (obj instanceof List)
            nbt.put(key, nbtFromList((List<Object>) obj));
        else
            nbt.putDouble(key, (Double) obj);
    }

}
