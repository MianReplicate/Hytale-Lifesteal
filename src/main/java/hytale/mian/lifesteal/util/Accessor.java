package hytale.mian.lifesteal.util;

import java.lang.reflect.Field;

public class Accessor {
    public static <T> T getPublic(Object object, String fieldName){
        try{
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch(NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }
}
