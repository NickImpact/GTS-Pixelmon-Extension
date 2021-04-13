package net.impactdev.gts.reforged.legacy;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.List;
import java.util.Map;

public class GsonUtils {

    public static NBTTagCompound deserialize(JsonObject json) {
        Map<String, Object> map = new GsonBuilder().setPrettyPrinting().create().fromJson(json.get("element").getAsString(), Map.class);
        return nbtFromMap(map);
    }

    private static NBTTagCompound nbtFromMap(Map<String, Object> map) {
        NBTTagCompound nbt = new NBTTagCompound();

        for (String key : map.keySet()) {
            try {
                apply(nbt, key, map.get(key));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return nbt;
    }

    private static NBTTagList nbtFromList(List<Object> list) {
        NBTTagList nList = new NBTTagList();
        list.forEach(entry -> nList.appendTag(read(entry)));
        return nList;
    }

    private static NBTBase read(Object in) {
        if(in instanceof String) {
            return new NBTTagString((String) in);
        } else if(in instanceof Map) {
            return nbtFromMap((Map<String, Object>) in);
        } else if(in instanceof List) {
            return nbtFromList((List<Object>) in);
        } else {
            return new NBTTagDouble((Double) in);
        }
    }

    private static void apply(NBTTagCompound nbt, String key, Object obj) throws Exception {
        if (obj instanceof String)
            nbt.setString(key, (String) obj);
        else if (obj instanceof Map)
            nbt.setTag(key, nbtFromMap((Map<String, Object>) obj));
        else if (obj instanceof List)
            nbt.setTag(key, nbtFromList((List<Object>) obj));
        else
            nbt.setDouble(key, (Double) obj);
    }

}
