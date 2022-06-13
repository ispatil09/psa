package test.io.somthng;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.psa.exception.PSAFileIOException;
import com.psa.io.DataCopyUtil;

public class TestDataCopy {

	/**
	 * @param args
	 * @throws PSAFileIOException 
	 */
	public static void main(String[] args) throws PSAFileIOException {
		/*String string = "C:\\Users\\SONY\\Desktop\\PSA net upload\\del.txt";
		Path path = Paths.get(string);
		new DataCopyUtil().ceateFileAndSaveText("temp","csa",path, "txt");*/
		new DataCopyUtil().copyContentTo("C:\\Users\\SONY\\Desktop\\PSA net upload\\del.std", "std");
		//System.out.println("Copied");
	}

}
