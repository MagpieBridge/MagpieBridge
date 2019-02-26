package de.upb.study.taskone;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

import de.cognicrypt.crypto.KeyManagment;
import de.cognicrypt.crypto.SymmetricEnc;
import de.upb.study.taskone.filehandler.FileHandler;

public class TaskOneMain {

	public static boolean encryptFile(String plainTextPath, String cipherTextPath, char[] encryptionPassword)
			throws IOException, GeneralSecurityException {
		String fileContent = FileHandler.readFile(plainTextPath);
		fileContent = templateUsage(fileContent, encryptionPassword);
		return FileHandler.writeContent(cipherTextPath, fileContent);
	}

	public static String templateUsage(String data, char[] pwd)
			throws GeneralSecurityException, UnsupportedEncodingException {
		KeyManagment km = new KeyManagment();

		SecretKey encryptionKey = km.getKey(pwd);
		SymmetricEnc symEnc = new SymmetricEnc();

		return symEnc.encrypt(data, encryptionKey);
	}
}
