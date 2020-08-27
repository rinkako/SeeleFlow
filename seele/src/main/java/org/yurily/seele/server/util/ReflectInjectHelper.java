/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.yurily.seele.server.util;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Class : ReflectInjectHelper
 * Usage :
 */
public class ReflectInjectHelper {

    public static <TyLeft, TyRight> TyRight rightReflect(Class<TyLeft> classLeft,
                                                         Class<TyRight> classRight,
                                                         TyLeft instanceLeft) throws Exception {
        TyRight right = classRight.newInstance();
        Field[] fields = classRight.getDeclaredFields();
        for (Field field : fields) {
            Field leftField = classLeft.getDeclaredField(field.getName());
            leftField.setAccessible(true);
            Object fVal = leftField.get(instanceLeft);
            field.set(right, fVal);
        }
        return right;
    }

    public static <Ty> Ty mapReflect(Class<Ty> clazz, Map<String, Object> map) throws Exception {
        Ty result = clazz.newInstance();
        for (Map.Entry<String, Object> kvp : map.entrySet()) {
            Field keyedField = clazz.getDeclaredField(kvp.getKey());
            keyedField.setAccessible(true);
            keyedField.set(result, kvp.getValue());
        }
        return result;
    }

}
