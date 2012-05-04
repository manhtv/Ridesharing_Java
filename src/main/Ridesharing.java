package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import util.Geo;

class GPSPoint {
	double lat;
	double lon;
	int time;

	public GPSPoint(double lat, double lon, int time) {
		this.lat = lat;
		this.lon = lon;
		this.time = time;
	}
}

class FatherTrip{
	double benefit=0.0;
	ArrayList<Integer> children=new ArrayList<Integer>();
}

class TripMeta {
	int taxi_id;
	GPSPoint start_point;
	GPSPoint end_point;
	int tt;
	double td;

	public TripMeta(int taxi_id, double lat1, double lon1, int time1,
			double lat2, double lon2, int time2, int tt, double td) {
		this.taxi_id = taxi_id;
		this.start_point = new GPSPoint(lat1, lon1, time1);
		this.end_point = new GPSPoint(lat2, lon2, time2);
		this.tt = tt;
		this.td = td;
	}
}

public class Ridesharing {
	public static void main(String[] args) {
		new Ridesharing().main();
	}

	public void main() {
		int[] delay={900,1800,2700,3600};//, Constants.INF};
		String dirName="Taxi_Shanghai";//"small_test_copy";
		
		/*
		ArrayList<Integer> delayArray=new ArrayList<Integer>();
		for(int i=0;i<delay.length;i++){
			delayArray.add(delay[i]);
		}	
		produceMergeableRelation(dirName, delayArray, true);
		*/
		
		HashMap<Integer, ArrayList<Integer>> child_trips=new HashMap<Integer, ArrayList<Integer>>();
		child_trips.put(0, new ArrayList<Integer>());
		HashMap<Integer, FatherTrip> father_trips=new HashMap<Integer, FatherTrip>();
		father_trips.put(0, new FatherTrip());
		HashMap<String, Double> mergeable_relation=new HashMap<String, Double>();
		
		int idx=3;
		String fileName="od_merge";
		loadMergeableRelation(dirName, fileName, delay[idx],false, child_trips, father_trips, mergeable_relation);
	}
	
	public void loadMergeableRelation(String dirName, String fileName, int maxDelay, boolean graphViz, HashMap<Integer,ArrayList<Integer>> child_trips, HashMap<Integer, FatherTrip> father_trips, HashMap<String, Double> mergeable_relation){
		long start=System.currentTimeMillis();
		String[] names={"trip_meta", fileName};
		String[] fileNames=new String[names.length];
		int i,j;
		for(i=0;i<fileNames.length;i++){
			fileNames[i]=Constants.PROCESSED_DIR+dirName+"/"+names[i]+".txt";
		}
		ArrayList<TripMeta> trip_meta=loadTripMetaFile(fileNames[0]);
		
		BufferedWriter relation_graph=null;
		if(graphViz){
			try {
				relation_graph=new BufferedWriter(new FileWriter(Constants.PROCESSED_DIR+dirName+"/relation_graph.gv"));
				relation_graph.write("digraph graphname {\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		int line_no=0, maximum_no_of_father=Constants.INF;
		int c_id, f_id; 
		String pair_id;
		double delay;
		try {
			Scanner sc=new Scanner(new File(fileNames[1]));
			while(sc.hasNext()){
				String line=sc.nextLine();
				String[] fields=line.substring(0, line.length()-1).split(",");
				c_id=Integer.parseInt(fields[0]);
				f_id=Integer.parseInt(fields[1]);
				delay=Double.parseDouble(fields[2]);
				if(delay<=maxDelay){
					if(graphViz){
						relation_graph.write(fields[0]+" -> "+fields[1]+";\n");
					}
					pair_id=fields[0]+"_"+fields[1];
					line_no+=1;
					
					mergeable_relation.put(pair_id, delay);
					//update child_trips
					if(!child_trips.containsKey(c_id)){
						child_trips.put(c_id, new ArrayList<Integer>());
					}
					if (!(  fileName.equals("od_merge")  && child_trips.get(c_id).size()>maximum_no_of_father)){
						child_trips.get(c_id).add(f_id);
					}else{
						//case that is not used here
					}
					
					//update father_trips
					if (!father_trips.containsKey(f_id)){
						father_trips.put(f_id, new FatherTrip());
					}
					father_trips.get(f_id).children.add(c_id);
					father_trips.get(f_id).benefit+=trip_meta.get(c_id).td;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(graphViz){
			try {
				relation_graph.write("}");
				relation_graph.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//sort children list in descending order of the travel distance of trips
		ArrayList<Double> distance;
		ArrayList<Integer> sorted;
		for(Integer father_id: father_trips.keySet()){
			distance=new ArrayList<Double>();
			for(Integer cid: father_trips.get(father_id).children){
				distance.add(trip_meta.get(cid).td);
			}
			sorted=CustomSort.sort(father_trips.get(father_id).children, distance, Collections.reverseOrder(new CustomizedSort()));
			father_trips.get(father_id).children=sorted;
		}		
		System.out.println("load mergeable relation : "+(System.currentTimeMillis() - start)/1000/60 + " minutes elapsed");
	}

	public double od_merge_mergeable(TripMeta child_trip, TripMeta father_trip) {
		if (child_trip.start_point.time >= father_trip.start_point.time) {
			return -Constants.INF;
		}
		double t_walkLeg1 = Geo.distBetween(child_trip.start_point.lat,
				child_trip.start_point.lon, father_trip.start_point.lat,
				father_trip.start_point.lon)
				/ Constants.PACE;
		if (child_trip.start_point.time + t_walkLeg1 > father_trip.start_point.time) {
			return -Constants.INF;
		}
		double t_walkLeg2 = Geo.distBetween(child_trip.end_point.lat,
				child_trip.end_point.lon, father_trip.end_point.lat,
				father_trip.end_point.lon)
				/ Constants.PACE;
		double delay = (father_trip.end_point.time + t_walkLeg2 - child_trip.end_point.time)
				- child_trip.tt;
		return delay;
	}

	public ArrayList<TripMeta> loadTripMetaFile(String file_name) {
		ArrayList<TripMeta> trip_meta = new ArrayList<TripMeta>();
		trip_meta.add(null);
		try {
			Scanner sc = new Scanner(new File(file_name));
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] fields = line.substring(0, line.length() - 1).split(
						",");
				int taxi_id = Integer.parseInt(fields[1]);
				double lat1 = Double.parseDouble(fields[2]);
				double lon1 = Double.parseDouble(fields[3]);
				int time1 = Integer.parseInt(fields[4]);
				double lat2 = Double.parseDouble(fields[5]);
				double lon2 = Double.parseDouble(fields[6]);
				int time2 = Integer.parseInt(fields[7]);
				int tt = Integer.parseInt(fields[8]);
				double td = Double.parseDouble(fields[9]);
				trip_meta.add(new TripMeta(taxi_id, lat1, lon1, time1, lat2,
						lon2, time2, tt, td));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return trip_meta;
	}

	public ArrayList<Double> produceMergeableRelation(String dirName,
			ArrayList<Integer> delayArray, boolean output) {
		long start = System.currentTimeMillis();
		ArrayList<Double> upper_bound = new ArrayList<Double>();

		ArrayList<String> names = new ArrayList<String>();
		names.add("trip_meta");
		int i, j;
		for (i = 0; i < delayArray.size() - 1; i++) {
			names.add("od_merge_" + String.valueOf(delayArray.get(i)));
		}
		names.add("od_merge");

		ArrayList<String> file_names = new ArrayList<String>();
		for (String name : names) {
			file_names.add(Constants.PROCESSED_DIR + dirName + "/" + name
					+ ".txt");
		}
		ArrayList<TripMeta> trip_meta = loadTripMetaFile(file_names.get(0));
		// System.out.println(trip_meta.size());

		ArrayList<ArrayList<Integer>> child_sets = new ArrayList<ArrayList<Integer>>();
		if (delayArray != null) {
			for (i = 0; i < delayArray.size(); i++) {
				child_sets.add(new ArrayList<Integer>());
				upper_bound.add(0.0);
			}
		}

		ArrayList<BufferedWriter> pair_files = new ArrayList<BufferedWriter>();
		if (output) {
			for (i = 1; i < file_names.size(); i++) {
				try {
					pair_files.add(new BufferedWriter(new FileWriter(file_names
							.get(i))));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		boolean mergeable;
		double delay;
		int c_id = 0, f_id = 0, idx;
		for (i = 1; i < trip_meta.size(); i++) {
			for (j = i + 1; j < trip_meta.size(); j++) {
				mergeable = false;
				delay = od_merge_mergeable(trip_meta.get(i), trip_meta.get(j));
				if (delay != -Constants.INF ) {
					c_id = i;
					f_id = j;
					mergeable = true;
				} else {
					delay = od_merge_mergeable(trip_meta.get(j),
							trip_meta.get(i));
					if (delay != -Constants.INF) {
						c_id = j;
						f_id = i;
						mergeable = true;
					}
					if (mergeable) {
						if (delayArray.size() > 0) {
							for (idx = 0; idx < child_sets.size(); idx++) {
								if (delay <= delayArray.get(idx)
										&& !child_sets.get(idx).contains(c_id)) {
									child_sets.get(idx).add(c_id);
								}
							}
						}
						if (output) {
							for (idx = 0; idx < delayArray.size(); idx++) {
								if (delay < delayArray.get(idx)) {
									try {
										pair_files.get(idx).write(
												String.valueOf(c_id) + ","
														+ String.valueOf(f_id)
														+ ","
														+ String.valueOf(delay)
														+ "\n");
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
		}
		
		double benefit;
		if (delayArray.size()>0){
			for(idx=0;idx<child_sets.size();idx++){
				benefit=0.0;
				for(Integer child: child_sets.get(idx)){
					benefit+=trip_meta.get(child).td;
				}
				upper_bound.add(benefit);
			}
		}
		
		if (output){
			for(BufferedWriter pair_file: pair_files){
				try {
					pair_file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("produce mergeable relation : "+(System.currentTimeMillis() - start)/1000/60 + " minutes elapsed");
		return upper_bound;
	}

	public ArrayList<Double> produceMergeableRelation(String dirName,
			ArrayList<Integer> delayArray) {
		return produceMergeableRelation(dirName, delayArray, false);
	}

	public ArrayList<Double> produceMergeableRelation(String dirName,
			boolean output) {
		return produceMergeableRelation(dirName, new ArrayList<Integer>(),
				output);
	}

	public ArrayList<Double> produceMergeableRelation(String dirName) {
		return produceMergeableRelation(dirName, new ArrayList<Integer>(),
				false);
	}
}
