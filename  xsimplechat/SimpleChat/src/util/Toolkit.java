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

	/**
	 * <p>合并两个数组，得到的新数组长度为a1.length+a2.length，数据内容为a2接到a1后面</p>
	 * <p>若参数存在非数组类型或两个数组的类型不同，则返回null</p>
	 * @param a1 数组1
	 * @param a2 数组2
	 * @return 合并后的新数组
	 */
	public static Object mergeArray(Object a1, Object a2) {
		Class<?> c1 = a1.getClass(), c2 = a2.getClass();
		if (!c1.isArray() || !c2.isArray() || !c1.equals(c2))
			return null;
		int length1 = Array.getLength(a1), length2 = Array.getLength(a2);
		Object result = Array.newInstance(c1.getComponentType(), length1 + length2);
		System.arraycopy(a1, 0, result, 0, length1);
		System.arraycopy(a2, 0, result, length1, length2);
		return result;
	}

	/**
	 * <p>从指定数组中截取一段，并返回截取出的数组对象</p>
	 * @param array 源数组对象
	 * @param pos 开始位置
	 * @param len 截取长度
	 * @return 截取的数组的对象
	 * @throws NullPointerException 如果array为空
	 * @throws IndexOutOfBoundsException 如果<code>pos+len-1>array.length</code>
	 * @throws NegativeArraySizeException 如果<code>pos>array.length</code>
	 */
	public static Object cutArray(Object array, int pos, int len) {
		Class<?> c = array.getClass();
		if (!c.isArray())
			return null;
		int length = Math.min(len, Array.getLength(array) - pos);
		Object result = Array.newInstance(c.getComponentType(), length);
		System.arraycopy(array, pos, result, 0, length);
		return result;
	}

	/**
	 * <p>将int转换成4个元素的byte数组</p>
	 * <p>最低8位放在byte[0]，次低8位放在byte[1]，以此类推<p>
	 * @param x 需要转换的整数
	 * @return byte数组
	 */
	public static byte[] intToByteArray(int x) {
		byte[] result = new byte[4];
		int i, mask = 0xff;
		for (i = 0; i < 4; i++) {
			result[i] = (byte) (x & mask);
			x >>>= 8;
		}
		return result;
	}

	/**
	 * <p>将4个元素的byte数组转换成int</p>
	 * <p>byte[0]存放整数的最低8位，byte[1]存放次低8位，以此类推</p>
	 * @param a byte数组
	 * @return 整数
	 */
	public static int byteArrayToInt(byte[] a) {
		int result = 0;
		int i, mask = 0xff;
		for (i = 3; i >= 0; i--) {
			result <<= 8;
			result |= (a[i] & mask);
		}
		return result;
	}
	
	/**
	 * <p>将long转换成8个元素的byte数组</p>
	 * <p>最低8位放在byte[0]，次低8位放在byte[1]，以此类推<p>
	 * @param x 需要转换的整数
	 * @return byte数组
	 */
	public static byte[] longToByteArray(long x) {
		byte[] result = new byte[8];
		int i, mask = 0xff;
		for (i = 0; i < 8; i++) {
			result[i] = (byte) (x & mask);
			x >>>= 8;
		}
		return result;
	}

	/**
	 * <p>将8个元素的byte数组转换成long</p>
	 * <p>byte[0]存放整数的最低8位，byte[1]存放次低8位，以此类推</p>
	 * @param a byte数组
	 * @return 整数
	 */
	public static long byteArrayToLong(byte[] a) {
		long result = 0;
		int i, mask = 0xff;
		for (i = 7; i >= 0; i--) {
			result <<= 8;
			result |= (a[i] & mask);
		}
		return result;
	}
}
