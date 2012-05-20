package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileIO {
	/*
	 * @return load trajectories from a single day
	 *  the file format is the old format
	 */
	public static void ReadSingleDayFile(String file, String destDir, String format){
		try {
			Scanner sc=new Scanner(new File(file));
			String line;
			int trip_id;
			if(format.equals("old")){
				while(sc.hasNextLine()){
					line=sc.nextLine();
					if(line.startsWith("#")){
						
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static void ReadSingleDayFile(String file, String destDir){
		ReadSingleDayFile(file, destDir, "old");
	}
}
