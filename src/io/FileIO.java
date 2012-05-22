package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import main.Constants;

public class FileIO {
	public static void main(String[] args) {
		ReadSingleDayFile();
	}

	public static void test() {
		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateTest = "2012-05-20 13:50:00";
		try {
			Date dt = fmt.parse(dateTest);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dt);

			int sec_of_day = cal.get(Calendar.HOUR_OF_DAY) * 3600
					+ cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);
			System.out.println(cal.get(Calendar.DAY_OF_WEEK) + " "
					+ cal.get(Calendar.HOUR_OF_DAY) + " " + sec_of_day);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/*
	 * @return load trajectories from a single day the file format is the old
	 * format, i.e. jiafeng hu's format
	 */
	public static void ReadSingleDayFile() {
		int line_no = 0;
		Scanner sc;
		String taxi_id, line, date;
		File dateDir, date_taxi;
		BufferedWriter bw;
		try {
			for (File f : new File(Constants.RAW_DIR
					+ "trip_trajectory_per_taxi/").listFiles()) {
				// System.out.println(f);
				line_no += 1;
				sc = new Scanner(f);
				taxi_id = f.getName().substring(0, f.getName().indexOf('.'));
				while (sc.hasNextLine()) {
					line = sc.nextLine();
					if (!line.startsWith("#")) {
						line=line.replace('/', '-');
						date=line.split(" ")[0];
						
						dateDir=new File(Constants.RAW_DIR+date);
						if(!dateDir.exists()){
							dateDir.mkdir();
						}
						
						date_taxi=new File(Constants.RAW_DIR+date+"/"+taxi_id+".txt");
						if(date_taxi.exists()){
							bw = new BufferedWriter(new FileWriter(date_taxi, true));
						}else{
							bw = new BufferedWriter(new FileWriter(date_taxi));
						}
						bw.write(taxi_id + "," + line+"\n");
						bw.close();
					}
				}
				
			}
			// System.out.println(line_no);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
