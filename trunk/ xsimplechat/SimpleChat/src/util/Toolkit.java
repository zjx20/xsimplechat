package util;

import java.lang.reflect.Array;

public class Toolkit {
	/**
	 * <p>���������С��ָ�����ȣ������������ڵ����ݡ� </p>
	 * <p>����µĳ��ȱ�ԭ���ȴ����������ֵ�ֵ������</p>
	 * <p>����µĳ��ȱ�ԭ����С����ֻ����0..newLength-1�����ݡ�</p>
	 * 
	 * @param array ԭ�������
	 * @param newLength ָ�����³���
	 * @return ���ص�����С�������Object����Ҫ������ת�� ;
	 * 			��������array����һ����������򷵻�null
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
	 * </p>���ַ���������ӿո�ʹ֮�ﵽָ�����ȡ�</p>
	 * @param str Դ�ַ���
	 * @param length Ŀ�곤��
	 * @return ����ָ�����ȵ��ַ�����
	 * 			���Դ�ַ����ĳ��ȱ�ָ�����ȳ����򷵻�Դ�ַ���
	 */
	public static String padString(String str, int length) {
		String temp = "";
		for (int i = length - str.length(); i > 0; i--)
			temp += " ";
		return str + temp;
	}

	/**
	 * <p>���ɷ������ݡ�</p>
	 * <p>�ϲ�ͷ�������ģ�����һ��byte[]����</p>
	 * @param header ����ͷ��
	 * @param data ��������
	 * @return ���ط�������
	 */
	public static byte[] generateSendData(String header, byte[] data) {
		int hSize = header.getBytes().length, dSize = data.length;
		byte[] result = new byte[hSize + dSize];
		System.arraycopy(header.getBytes(), 0, result, 0, hSize);
		System.arraycopy(data, 0, result, hSize, dSize);
		return result;
	}

	/**
	 * <p>�ϲ��������飬�õ��������鳤��Ϊa1.length+a2.length����������Ϊa2�ӵ�a1����</p>
	 * <p>���������ڷ��������ͻ�������������Ͳ�ͬ���򷵻�null</p>
	 * @param a1 ����1
	 * @param a2 ����2
	 * @return �ϲ����������
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
	 * <p>��ָ�������н�ȡһ�Σ������ؽ�ȡ�����������</p>
	 * @param array Դ�������
	 * @param pos ��ʼλ��
	 * @param len ��ȡ����
	 * @return ��ȡ������Ķ���
	 * @throws NullPointerException ���arrayΪ��
	 * @throws IndexOutOfBoundsException ���<code>pos+len-1>array.length</code>
	 * @throws NegativeArraySizeException ���<code>pos>array.length</code>
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
	 * <p>��intת����4��Ԫ�ص�byte����</p>
	 * <p>���8λ����byte[0]���ε�8λ����byte[1]���Դ�����<p>
	 * @param x ��Ҫת��������
	 * @return byte����
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
	 * <p>��4��Ԫ�ص�byte����ת����int</p>
	 * <p>byte[0]������������8λ��byte[1]��Ŵε�8λ���Դ�����</p>
	 * @param a byte����
	 * @return ����
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
	 * <p>��longת����8��Ԫ�ص�byte����</p>
	 * <p>���8λ����byte[0]���ε�8λ����byte[1]���Դ�����<p>
	 * @param x ��Ҫת��������
	 * @return byte����
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
	 * <p>��8��Ԫ�ص�byte����ת����long</p>
	 * <p>byte[0]������������8λ��byte[1]��Ŵε�8λ���Դ�����</p>
	 * @param a byte����
	 * @return ����
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
