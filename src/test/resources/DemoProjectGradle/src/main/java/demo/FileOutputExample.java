package demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutputExample {
	public static void main(String...args) throws IOException  {
		FileOutputStream is = new FileOutputStream(new File("output.txt"));
		is.write(10);

		is.close();
	}
}
