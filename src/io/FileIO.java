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

public class FileIO {
	public static void main(String[] args){
		//FileIO.test();
	}
	
	public static void test(){
		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateTest="2012-05-20 13:50:00";
		try {
			Date dt = fmt.parse(dateTest);
			Calendar cal=Calendar.getInstance();
			cal.setTime(dt);
			
			int sec_of_day=cal.get(Calendar.HOUR_OF_DAY)*3600+cal.get(Calendar.MINUTE)*60+cal.get(Calendar.SECOND);
			System.out.println(cal.get(Calendar.DAY_OF_WEEK)+" "+cal.get(Calendar.HOUR_OF_DAY)+" "+sec_of_day);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * @return load trajectories from a single day
	 *  the file format is the old format
	 */
	public static void ReadSingleDayFile(String file, String destDir, String format){
		try {
			Scanner sc=new Scanner(new File(file));
			BufferedWriter bw=null;
			String line;
			int trip_id=0;
			String taxi_id;
			StringBuilder sb=null;
			DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar cal=Calendar.getInstance();
			int sec_of_day=0;
			if(format.equals("old")){
				while(sc.hasNextLine()){
					line=sc.nextLine();
					if(line.startsWith("#")){
						trip_id+=1;
						//TODO extract taxi_id from the head line
						taxi_id=line.substring(2).split(",")[0];
						bw=new BufferedWriter(new FileWriter(destDir+String.valueOf(trip_id)+".txt"));
						sb=new StringBuilder();
						sb.append(taxi_id+",");
					}else{
						//destination format: id,lat,lon,day_of_week,hour_of_day,sec_of_day
						String[] fields=line.substring(0, line.length()-1).split(",");
						Date dt = fmt.parse(fields[1]); //fields[1] is dates
						cal.setTime(dt);
						sec_of_day=cal.get(Calendar.HOUR_OF_DAY)*3600+cal.get(Calendar.MINUTE)*60+cal.get(Calendar.SECOND);
						sb.append(fields[2]+","+fields[3]+",");//fields[2] is lat and fields[3] is lon
						sb.append(String.valueOf(cal.get(Calendar.DAY_OF_WEEK))+","+String.valueOf(cal.get(Calendar.HOUR_OF_DAY))+","+sec_of_day+"\n");
						bw.write(sb.toString());
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void ReadSingleDayFile(String file, String destDir){
		ReadSingleDayFile(file, destDir, "old");
	}
}
