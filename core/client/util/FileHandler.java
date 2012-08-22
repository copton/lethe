package util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileHandler {
	public static boolean deleteFile(String fileName) {
		return deleteFile(new File(fileName));
	}
	
	public static boolean deleteFile(File f) {
		if(f.isFile()) return f.delete();
		else {
			boolean deletedAll = true;
			File[] fileList = f.listFiles();
			for(int i=0; i<fileList.length && deletedAll; i++)
				deletedAll = deletedAll & deleteFile(fileList[i]);
			
			if(deletedAll) deletedAll = deletedAll & f.delete();
			return deletedAll;
		}
	}
	
	public static void copyFile(String from, String to) throws Exception {
		copyFile(new File(from), new File(to));
	}
	
	public static void copyFile(File from, File to) throws Exception {	
		if(from.isFile()) {
			FileReader reader = new FileReader(from);
			FileWriter writer = new FileWriter(to);
		
			char[] bytes = new char[1024];
			while(reader.ready() && 
				reader.read(bytes) > 0) {
				writer.write(bytes);
			}
			reader.close();
			writer.close();
		} else {
			to.mkdirs();
			File[] files = from.listFiles();
			for(int i=0; i<files.length; i++) 
				copyFile(files[i], new File(to, files[i].getName()));
		}
	}
}
