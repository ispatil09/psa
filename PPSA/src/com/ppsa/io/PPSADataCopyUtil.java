package com.ppsa.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.ppsa.exception.PPSAIOException;
import com.psa.entity.impl.Structure;
import com.psa.io.EnvFileIOUtil;

public class PPSADataCopyUtil {
	public static void savePostProcessedStructure(Structure structure) throws PPSAIOException
			 {

		String problemName = structure.getProblemName();
		String environmentPath = EnvFileIOUtil.getEnvironmentPath();
		try {
		FileOutputStream fos = new FileOutputStream(environmentPath + "\\"
				+ problemName + "\\" + problemName + ".ppsa");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(structure);
		oos.flush();
		oos.close();
		} catch (IOException e) {
			throw new PPSAIOException(e.getMessage());
		}
	}
	
	public static Structure getPostProcessedStructureFromFile(String problemName)
			 {

		String environmentPath = EnvFileIOUtil.getEnvironmentPath();
		FileInputStream fis;
		ObjectInputStream ois;
		Object object = null;
		Structure structure = null;
		try {
			fis = new FileInputStream(environmentPath + "\\"
					+ problemName + "\\" + problemName + ".ppsa");
		
		ois = new ObjectInputStream(fis);
		
		object = null;
		
			object = ois.readObject();
			ois.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		structure = (Structure)object;
		return structure;
	}
}
