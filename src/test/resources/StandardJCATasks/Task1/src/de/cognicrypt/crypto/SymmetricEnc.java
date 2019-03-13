
package de.cognicrypt.crypto;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/** @author CogniCrypt */
public class SymmetricEnc {

	public String encrypt(String message, SecretKey key) throws GeneralSecurityException, UnsupportedEncodingException {
		byte[] data = message.getBytes("UTF-8");
		return Base64.getEncoder().encodeToString(encrypt(data, key));
	}

	public byte[] encrypt(byte[] data, SecretKey key) throws GeneralSecurityException {
		byte[] ivb = new byte[16];
		SecureRandom.getInstanceStrong().nextBytes(ivb);
		IvParameterSpec iv = new IvParameterSpec(ivb);

		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, key, iv);

		byte[] res = c.doFinal(data);

		byte[] ret = new byte[res.length + ivb.length];
		System.arraycopy(ivb, 0, ret, 0, ivb.length);
		System.arraycopy(res, 0, ret, ivb.length, res.length);
		return ret;
	}

	public String decrypt(String message, SecretKey key) throws GeneralSecurityException {
		byte[] ciphertext = Base64.getDecoder().decode(message);
		return new String(decrypt(ciphertext, key));
	}

	public byte[] decrypt(byte[] ciphertext, SecretKey key) throws GeneralSecurityException {
		byte[] ivb = new byte[16];
		System.arraycopy(ciphertext, 0, ivb, 0, ivb.length);
		IvParameterSpec iv = new IvParameterSpec(ivb);
		byte[] data = new byte[ciphertext.length - ivb.length];
		System.arraycopy(ciphertext, ivb.length, data, 0, data.length);

		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, key, iv);

		int conv_len = 0;
		byte[] res = new byte[c.getOutputSize(data.length)];
		for (int i = 0; i + 1024 <= ciphertext.length; i += 1024) {
			byte[] input = new byte[1024];
			System.arraycopy(data, i, input, 0, 1024);
			conv_len += c.update(input, 0, input.length, res, i);
		}
		conv_len += c.doFinal(data, conv_len, data.length - conv_len, res, conv_len);

		return res;
	}
}
