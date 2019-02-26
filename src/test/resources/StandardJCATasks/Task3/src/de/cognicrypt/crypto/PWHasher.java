
package de.cognicrypt.crypto;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

/** @author CogniCrypt */
public class PWHasher {
	// adopted code from https://github.com/defuse/password-hashing

	public static String createPWHash(char[] pwd) throws GeneralSecurityException {
		byte[] salt = new byte[224 / 8];
		SecureRandom.getInstanceStrong().nextBytes(salt);

		PBEKeySpec spec = new PBEKeySpec(pwd, salt, 65536, 224);
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA224");
		String pwdHash = toBase64(salt) + ":" + toBase64(f.generateSecret(spec).getEncoded());
		spec.clearPassword();
		return pwdHash;
	}

	public static boolean verifyPWHash(char[] pwd, String pwdhash) throws GeneralSecurityException {
		String[] parts = pwdhash.split(":");
		byte[] salt = fromBase64(parts[0]);

		PBEKeySpec spec = new PBEKeySpec(pwd, salt, 65536, 224);
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA224");
		Boolean areEqual = slowEquals(f.generateSecret(spec).getEncoded(), fromBase64(parts[1]));
		spec.clearPassword();
		return areEqual;
	}

	private static boolean slowEquals(byte[] a, byte[] b) {
		int diff = a.length ^ b.length;
		for (int i = 0; i < a.length && i < b.length; i++) {
			diff |= a[i] ^ b[i];
		}
		return diff == 0;
	}

	private static String toBase64(byte[] array) {
		return DatatypeConverter.printBase64Binary(array);
	}

	private static byte[] fromBase64(String hash) {
		return DatatypeConverter.parseBase64Binary(hash);
	}
}
