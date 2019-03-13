package de.upb.study.taskone.filehandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHandler {

	public static String readFile(String path) {
		try {
			return String.join("\n", Files.readAllLines(Paths.get(path)));
		} catch (IOException e) {
			return null;
		}
	}

	public static boolean writeContent(String path, String content) {
		try {
			Files.write(Paths.get(path), content.getBytes("UTF-8"));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}