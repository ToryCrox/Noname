package com.tory.library.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {
	private static final char HEXS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static final String DIGITAL = "0123456789abcdef";
	private static final String MD5 = "MD5";

	public static String digest(String str) {
		try {
			MessageDigest alga = MessageDigest.getInstance(MD5);
			alga.update(str.getBytes());

			byte[] digesta = alga.digest();
			String ls_str = byte2hex(digesta);

			return ls_str;
		} catch (NoSuchAlgorithmException ex) {
			System.out.println("没有这个加密算法请检查JDK版本");
		}
		return null;
	}

	public static String digest(File file) {
		if (file == null || !file.isFile()) {
			System.err.println("文件不存在!" + file.getAbsolutePath());
			return null;
		}
		FileInputStream fileInputStream = null;
		try {
			MessageDigest m = MessageDigest.getInstance(MD5);
			fileInputStream = new FileInputStream(file);
			byte[] buffer = new byte[1024 * 4];
			int len;
			while ((len = fileInputStream.read(buffer)) != -1) {
				m.update(buffer, 0, len);
			}
			byte s[] = m.digest();
			return byte2hex(s);
		} catch (FileNotFoundException e) {
			System.err.println("找不到文件");
			return null;
		} catch (IOException e) {
			System.err.println("IO异常");
			return null;
		} catch (NoSuchAlgorithmException e) {
			System.err.println("无" + MD5 + "加密方法");
			return null;
		} finally {
			if (fileInputStream != null)
				try {
					fileInputStream.close();
				} catch (IOException e) {
					System.err.println("IO异常");
					return null;
				}
		}
	}

	/**
	 * 字节数组转换成16进制字符串
	 * 
	 * @param bytes
	 *            输入字节数组
	 * @return 16进制字符串（小写）
	 */
	private static String byte2hex(byte[] bytes) {
		StringBuffer sBuffer = new StringBuffer();
		int temp;
		for (int i = 0; i < bytes.length; i++) {
			temp = (bytes[i] >>> 4) & 0x0F;
			sBuffer.append(HEXS[temp]);
			temp = bytes[i] & 0x0F;
			sBuffer.append(HEXS[temp]);
		}
		return sBuffer.toString();
	}

	/**
	 * 16进制字符串转换成byte数组
	 * 
	 * @param hexString
	 *            16进制字符串
	 * @return 转换后的byte数组
	 */
	private static byte[] hex2Byte(String hexString) {
		if (isEmpty(hexString))
			throw new IllegalArgumentException("this hexString must not be empty");
		hexString = hexString.toLowerCase();
		byte[] bytes = new byte[hexString.length() / 2];// 用于存储最终结果的byte数组
		char[] hex2char = hexString.toCharArray();// 将字符串转换成字符数组
		int temp;
		for (int i = 0; i < bytes.length; i++) {
			temp = DIGITAL.indexOf(hex2char[2 * i]) << 4;
			temp += DIGITAL.indexOf(hex2char[2 * i + 1]);
			bytes[i] = (byte) (temp & 0xff);
		}
		return bytes;
	}
	/**
	 * 检查字符串是否为空，null或""
	 * 
	 * @param str
	 *            输入字符串，可以为null
	 * @return
	 */
	private static boolean isEmpty(String str)
	{
		return str == null || str.length() == 0;
	}
}
