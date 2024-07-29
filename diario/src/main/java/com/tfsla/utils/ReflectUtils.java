package com.tfsla.utils;

import java.lang.reflect.Field;

public class ReflectUtils {

    public static String inspect(Object obj) {
        try {
            if (obj != null) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("{Clase: ");
                buffer.append(obj.getClass());
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    buffer.append("[");
                    buffer.append(field.getName());
                    buffer.append(":");
                    buffer.append(field.get(obj));
                    buffer.append("]");
                }
                buffer.append("}");
                return buffer.toString();
            }
            else {
                return "null";
            }
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
