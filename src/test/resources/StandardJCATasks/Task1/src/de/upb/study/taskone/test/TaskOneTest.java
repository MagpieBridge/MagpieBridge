package de.upb.study.taskone.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.upb.study.taskone.TaskOneMain;

public class TaskOneTest {

	private static String outputPath = ".\\resources\\output.txt";
	private static String plainTextPath = ".\\bin\\input.txt";
	private static boolean result = false;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException, GeneralSecurityException {
		result = TaskOneMain.encryptFile(plainTextPath, outputPath, new char[] {'p', 'w', 'd'});
	}

	@AfterClass
	public static void tearDownAfterClass() throws IOException {
		if (Files.exists(Paths.get(outputPath))) {
			Files.delete(Paths.get(outputPath));
		}
	}

	@Test
	public final void testEncryptFileFileExists() {	
		Assert.assertTrue(new File(outputPath).exists());
	}

	@Test
	public final void testEncryptFileFileNotEmpty() throws IOException {	
		String outputContent = String.join("\n", Files.readAllLines(Paths.get(outputPath)));
		assertNotEquals(outputContent.trim().length(), 0);
	}
	
	@Test
	public final void testWriteSuccesful() {
		assertTrue(result);
	}
	
	@Test
	public final void testEncryptFileFileNotEqual() throws IOException {	
		String outputContent = String.join("\n", Files.readAllLines(Paths.get(outputPath)));
		String inputContent = String.join("\n", Files.readAllLines(Paths.get(plainTextPath)));
		
		assertNotEquals(inputContent, outputContent);
	}
	
}
