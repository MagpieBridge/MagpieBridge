package magpiebridge.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;

public class Logger {

	public static boolean debug = false;

	public static InputStream logStream(InputStream is, String logFileName) {
		if (!false)
			return is;
		File log;
		try {
			log = File.createTempFile(logFileName,".log");
			return new TeeInputStream(is, new FileOutputStream(log));
		} catch (IOException e) {
			return is;
		}
	}

	public static OutputStream logStream(OutputStream os, String logFileName) {
		if (!debug)
			return os;
		File log;
		try {
			log = File.createTempFile(logFileName, ".log");
			return new TeeOutputStream(os, new FileOutputStream(log));
		} catch (IOException e) {
			return os;
		}
	}

}
