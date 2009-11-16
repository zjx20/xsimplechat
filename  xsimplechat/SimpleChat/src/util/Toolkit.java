package util;

import java.lang.reflect.Array;

public class Toolkit {
	/**
	 * <p>调整数组大小到指定长度，并保留数组内的内容。 </p>
	 * <p>如果新的长度比原长度大，则新增部分的值不定；</p>
	 * <p>如果新的长度比原长度小，则只保留0..newLength-1的内容。</p>
	 * 
	 * @param array 原数组对象
	 * @param newLength 指定的新长度
	 * @return 返回调整大小后的数组Object，需要做类型转换 ;
	 * 			如果传入的array不是一个数组对象，则返回null
	 */
	public static Object fixArraySize(Object array, int newLength) {
		Class<?> c = array.getClass();
		if (!c.isArray())
			return null;
		Object newArray = Array.newInstance(c.getComponentType(), newLength);
		System.arraycopy(array, 0, newArray, 0, Math.min(Array.getLength(array), newLength));
		return newArray;
	}

	/**
	 * </p>在字符串后面添加空格，使之达到指定长度。</p>
	 * @param str 源字符串
	 * @param length 目标长度
	 * @return 返回指定长度的字符串；
	 * 			如果源字符串的长度比指定长度长，则返回源字符串
	 */
	public static String padString(String str, int length) {
		String temp = "";
		for (int i = length - str.length(); i > 0; i--)
			temp += " ";
		return str + temp;
	}

	/**
	 * <p>生成发送数据。</p>
	 * <p>合并头部和正文，返回一个byte[]对象。</p>
	 * @param header 发送头部
	 * @param data 发送正文
	 * @return 返回发送数据
	 */
	public static byte[] generateSendData(String header, byte[] data) {
		int hSize = header.getBytes().length, dSize = data.length;
		byte[] result = new byte[hSize + dSize];
		System.arraycopy(header.getBytes(), 0, result, 0, hSize);
		System.arraycopy(data, 0, result, hSize, dSize);
		return result;
	}
}
