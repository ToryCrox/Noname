package com.tory.library.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
	private static final String TAG = "AESUtils";
	public static final String AES_KEY = "mimikkouiaeskey2016";

	private static final String TYPE = "AES";
	private static final String TRANSFORMATION = TYPE + "/CBC/PKCS5Padding";
	private static final int KEY_LENGTH = 16;
	public static final Charset DEAFULT_CHARSET = Charset.forName("UTF-8");

	public static byte[] encrypt(byte[] data) {
		return cipherOperate(Cipher.ENCRYPT_MODE, data);
	}

	public static byte[] decrypt(byte[] enData) {
		return cipherOperate(Cipher.DECRYPT_MODE, enData);
	}

	@Nullable
	public static String encrypt(@NonNull String data){
		byte[] bytes = encrypt(data.getBytes(DEAFULT_CHARSET));
		if(bytes == null || bytes.length <= 0){
			return null;
		} else{
			return Base64.encodeToString(bytes, Base64.NO_WRAP);
		}
	}

	@Nullable
	public static String decrypt(@NonNull String enData){
		byte[] bytes = decrypt(Base64.decode(enData, Base64.NO_WRAP));
		if(bytes == null || bytes.length <= 0){
			return null;
		} else{
			return new String(bytes, DEAFULT_CHARSET);
		}
	}

	public static Bitmap decryptBitmapFrom(InputStream inputStream) {
		CipherInputStream cipherInputStream = decryptStreamFrom(inputStream);
		byte[] byteArray = getByteArrayFrom(cipherInputStream);
		if (byteArray == null) {
			return null;
		}
		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
	}

	public static String decryptStringFrom(InputStream inputStream) {
		CipherInputStream cipherInputStream = decryptStreamFrom(inputStream);
		byte[] byteArray = getByteArrayFrom(cipherInputStream);
		if (byteArray == null) {
			return null;
		}
		try {
			return new String(byteArray, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decryptBytesFrom(InputStream inputStream) {
		CipherInputStream cipherInputStream = decryptStreamFrom(inputStream);
		return getByteArrayFrom(cipherInputStream);
	}

	public static CipherInputStream decryptStreamFrom(InputStream inputStream) {
		Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
		if (cipher == null) {
			return null;
		}
		return new CipherInputStream(inputStream, cipher);
	}

	public static String decryptTmpFileFrom(Context context, InputStream inputStream, String suffix) {
		try {
			CipherInputStream cipherInputStream = decryptStreamFrom(inputStream);
			File file = new File(FileUtils.getAppPath(context), "tmp/decrypt." + suffix);
			file.delete();
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.createNewFile()) {
				FileOutputStream outputStream = new FileOutputStream(file);
				writeBytes(cipherInputStream, outputStream);
			}
			return file.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			IoUtils.closeSilently(inputStream);
		}
	}

	private static byte[] cipherOperate(int opmode, byte[] data) {
		Cipher cipher = getCipher(opmode);
		if (cipher == null) {
			return null;
		}
		try {
			return cipher.doFinal(data);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
            Log.e(TAG, "cipherOperate",e);
		}
		return null;
	}

	private static Cipher getCipher(int opmode) {
		try {
			byte[] keyByteArray = new byte[KEY_LENGTH];
			byte[] tmpByteArray = AES_KEY.getBytes("UTF-8");
			for (int i = 0; i < keyByteArray.length; i++) {
				if (i < tmpByteArray.length) {
					keyByteArray[i] = tmpByteArray[i];
				} else {
					keyByteArray[i] = 0;
				}
			}

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyByteArray, TYPE);
			cipher.init(opmode, secretKeySpec, new IvParameterSpec(new byte[16]));
			return cipher;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] getByteArrayFrom(InputStream inputStream) {
		byte[] rtn = null;
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			byte[] b = new byte[4096];
			int n;
			while ((n = inputStream.read(b)) != -1) {
				out.write(b, 0, n);
			}
			rtn = out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rtn;
	}

	private static void writeBytes(InputStream inputStream, OutputStream outputStream) {
		try {
			byte[] b = new byte[4096];
			int n;
			while ((n = inputStream.read(b)) != -1) {
				outputStream.write(b, 0, n);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
