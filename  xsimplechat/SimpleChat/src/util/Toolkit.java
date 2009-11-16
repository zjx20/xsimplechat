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
}
