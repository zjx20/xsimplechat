package util;

import java.lang.reflect.Array;

public class Toolkit {
	public static Object fixArraySize(Object array, int newLength) {
		Class<?> c = array.getClass();
		if (!c.isArray())
			return null;
		Object newArray = Array.newInstance(c.getComponentType(), newLength);
		System.arraycopy(array, 0, newArray, 0, Math.min(Array.getLength(array),
				newLength));
		return newArray;
	}
}
