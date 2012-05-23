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
		//ProduceTrajectoriesIndexedByDate();
		ProduceSingleDateFile(Constants.DATE);
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

	/**
	 * @function load the whole trajectory data set(filtered) and partition them into dates. Trajectories of each day is contained in a folder named by the date and 
	 * there is a file containing all trajectories in that day for each taxi.
	 */
	public static void ProduceTrajectoriesIndexedByDate() {
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
	
	/**
	 * @function a folder in which there is a file containing all trajectories of each taxi in the given date
	 * input file format, i.e. jiafeng hu's format DateTrackData
	 */
	public static void ProduceSingleDateFile(String date){
		Scanner sc;
		String taxi_id="", line;
		File dateDir, date_taxi;
		BufferedWriter bw=null;
		String[] fields;
		try {
			dateDir=new File(Constants.RAW_DIR+date);
			if(!dateDir.exists()){
				dateDir.mkdir();
			}
			sc = new Scanner(new File(Constants.RAW_DIR+date+".txt"));
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				line=line.replace('/', '-');
				if (line.startsWith("#")) { //separated by time. if time gap is large enough, GPS points are partitioned.
					fields=line.split(",");
					taxi_id=fields[2];
					date_taxi=new File(Constants.RAW_DIR+date+"/"+taxi_id+".txt");
					if(bw!=null){
						bw.write(taxi_id+","+"2001-01-01 01:01:01,39.87417,116.31596,0,0,0"+"\n"); //insert a line in which the occupied field is 0 as the separator line 
						bw.close();
					}
					if(date_taxi.exists()){
						bw=new BufferedWriter(new FileWriter(date_taxi, true));
					}else{
						bw=new BufferedWriter(new FileWriter(date_taxi));
					} 
				}else{
					bw.write(taxi_id + "," + line+"\n");
				}
			}
			// System.out.println(line_no);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
