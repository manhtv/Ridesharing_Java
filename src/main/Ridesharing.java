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

class DeepCopy{
	public static ArrayList<Integer> copy(ArrayList<Integer> list){
		ArrayList<Integer> clone=new ArrayList<Integer>();
		for(Integer id:list){
			clone.add(id);
		}
		return clone;
	}
}


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
	double benefit;
	ArrayList<Integer> children;
	public FatherTrip(FatherTrip f){
		benefit=f.benefit;
		this.children=DeepCopy.copy(f.children);
	}
	public FatherTrip(){
		benefit=0.0;
		this.children=new ArrayList<Integer>();
	}
}

class TripMeta {
	long taxi_id;
	GPSPoint start_point;
	GPSPoint end_point;
	int tt;
	double td;

	public TripMeta(long taxi_id, double lat1, double lon1, int time1,
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
		String dirName=Constants.DATE;
		
		//int[] delay={1500,1800,2100,2400};//, Constants.INF};
		int[] delay={300,600,900,1200};
		ArrayList<Integer> delayArray=new ArrayList<Integer>();
		for(int i=0;i<delay.length;i++){
			delayArray.add(delay[i]);
		}
		
		ArrayList<Double> upper_bound=new ArrayList<Double>();
		ArrayList<TripMeta> trip_meta=produceMergeableRelation(dirName, delayArray, true, upper_bound);
		//ArrayList<TripMeta> trip_meta=loadTripMetaFile(absolutePath(dirName,"trip_meta"));
		
		bounded_delay(dirName, upper_bound, delayArray, trip_meta, "percentage_of_saved_distance", Constants.NO_LONG_WALK);		
				
		/*
		HashMap<Integer, ArrayList<Integer>> child_trips=new HashMap<Integer, ArrayList<Integer>>();
		HashMap<Integer, FatherTrip> father_trips=new HashMap<Integer, FatherTrip>();
		HashMap<String, Double> mergeable_relation=new HashMap<String, Double>();
		int idx=3;
		String fileName="od_merge";
		loadMergeableRelation(dirName, fileName, delay[idx],false, child_trips, father_trips, mergeable_relation);
		*/
	}
	
	public void bounded_delay(String dirName, ArrayList<Double> upper_bound, ArrayList<Integer> delayArray, ArrayList<TripMeta> trip_meta, String ylabel, boolean no_long_walk){
		int i,j;
		String pair_file;
		int delay;
		
		ArrayList<ArrayList<Double>> data=new ArrayList<ArrayList<Double>>();
		for(i=0;i<Constants.HEURISTICS.length;i++){
			data.add(new ArrayList<Double>());
		}
		
		HashMap<Integer, ArrayList<Integer>> child_trips=new HashMap<Integer, ArrayList<Integer>>();
		HashMap<Integer, FatherTrip> father_trips=new HashMap<Integer, FatherTrip>();
		HashMap<String, Double> mergeable_relation=new HashMap<String, Double>();
		
		HashMap<Integer, ArrayList<Integer>> child_trips_copy;
		HashMap<Integer, FatherTrip> father_trips_copy;
	
		// calculate the totoal distance of all trips
		double totalDist=0;
		for(i=1;i<trip_meta.size();i++){
			totalDist+=trip_meta.get(i).td;
		}
		totalDist/=1000.0;
		
		HashMap<Integer,ArrayList<Integer>> rp=new HashMap<Integer,ArrayList<Integer>>();
		for(i=0;i<delayArray.size();i++){
			child_trips.clear();
			father_trips.clear();
			mergeable_relation.clear();
			
			pair_file="od_merge";
			delay=delayArray.get(i);
			
			if(delay<Constants.INF || Constants.HEURISTICS.length>2){
				if(delay<Constants.INF){
					pair_file+="_"+String.valueOf(delay);
				}

				loadMergeableRelation(dirName, pair_file, delay,false, child_trips, father_trips, mergeable_relation);
			}
			if(Constants.debug){
				System.out.println(String.valueOf(delay)+" : "+child_trips.size()+"  "+father_trips.size()+"  "+mergeable_relation.size());
			}
			
			double measure;
			for(j=0;j<Constants.HEURISTICS.length;j++){
				child_trips_copy=copyC(child_trips);
				father_trips_copy=copyF(father_trips);
				if(Constants.debug){
					System.out.print(Constants.HEURISTICS[j]+" : "+child_trips_copy.size()+"  "+father_trips_copy.size()+"  "+mergeable_relation.size()+" ");
				}
				if(j==0){
					if(delay<Constants.INF){
						measure=Analyze.upper_bound(child_trips_copy, father_trips_copy, trip_meta, Constants.INF)/1000;
					}else{
						measure=upper_bound.get(j);
					}
					if(ylabel.startsWith("percentage")){
						measure/=totalDist;
					}
					data.get(j).add(measure);
				}else{
					if(j==1 && delay<Constants.INF){
						rp=Analyze.optimal_filter(child_trips_copy, father_trips_copy, mergeable_relation, trip_meta, Constants.INF);
					}else{
						if(j>1){
							rp=Analyze.greedy_strategy(child_trips_copy, father_trips_copy, trip_meta, Constants.INF, Constants.HEURISTICS[j]);
							//System.out.println(rp.size());
						}
					}
					if(! (j==1&&delay==Constants.INF)){
						measure=Analyze.profileRP(rp, mergeable_relation, trip_meta).get("saved_distance")/1000.0;
					}else{
						measure=upper_bound.get(i)/1000.0;
					}
					if(ylabel.startsWith("percentage")){
						measure/=totalDist;
					}
					data.get(j).add(measure);
				}
				if(Constants.debug){
					System.out.println(data.get(j).get(i));
				}
			}
		}
		

		
		BufferedWriter bounded_delay;
		String saveFile=dirName+"_";
		for(Integer d: delayArray){
			saveFile+=String.valueOf(d)+"_";
		}
		saveFile+=ylabel;
		try {
			bounded_delay = new BufferedWriter(new FileWriter(absolutePath(dirName,saveFile)));	
			for(i=0;i<data.size();i++){
				for(j=0;j<data.get(i).size();j++){
					bounded_delay.write(String.valueOf(data.get(i).get(j)));
					if(j<data.get(i).size()-1){
						bounded_delay.write(",");
					}
				}
				bounded_delay.write("\n");
			}
			bounded_delay.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadMergeableRelation(String dirName, String fileName, int maxDelay, boolean graphViz, HashMap<Integer,ArrayList<Integer>> child_trips, HashMap<Integer, FatherTrip> father_trips, HashMap<String, Double> mergeable_relation){
		long start=System.currentTimeMillis();
		String[] names={"trip_meta", fileName};
		String[] fileNames=new String[names.length];
		int i,j;
		for(i=0;i<fileNames.length;i++){
			fileNames[i]=absolutePath(dirName, names[i]);
		}
		ArrayList<TripMeta> trip_meta=loadTripMetaFile(fileNames[0]);
		
		BufferedWriter relation_graph=null;
		if(graphViz){
			try {
				relation_graph=new BufferedWriter(new FileWriter(absolutePath(dirName, "relation_graph")));
				relation_graph.write("digraph graphname {\n");
			}catch(IOException e){
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

	public double od_merge_mergeable(TripMeta child_trip, TripMeta father_trip, boolean walkDistanceConstraint) {
		if (child_trip.start_point.time >= father_trip.start_point.time) {
			return -Constants.INF;
		}
		double t_walkLeg1 = Geo.distBetween(child_trip.start_point.lat,
				child_trip.start_point.lon, father_trip.start_point.lat,
				father_trip.start_point.lon)
				/ Constants.PACE;
		if(t_walkLeg1*Constants.PACE>Constants.MAXIMUM_WALKING_DISTANCE){
			return -Constants.INF;
		}
		if (child_trip.start_point.time + t_walkLeg1 > father_trip.start_point.time) {
			return -Constants.INF;
		}
		double t_walkLeg2 = Geo.distBetween(child_trip.end_point.lat,
				child_trip.end_point.lon, father_trip.end_point.lat,
				father_trip.end_point.lon)
				/ Constants.PACE;
		if(t_walkLeg2*Constants.PACE>Constants.MAXIMUM_WALKING_DISTANCE){
			return -Constants.INF;
		}
		double delay = (father_trip.end_point.time + t_walkLeg2 - child_trip.start_point.time)
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
				long taxi_id = Long.parseLong(fields[1]);
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

	public ArrayList<TripMeta> produceMergeableRelation(String dirName,
			ArrayList<Integer> delayArray, boolean output, ArrayList<Double> upper_bound) {
		long start = System.currentTimeMillis();

		ArrayList<String> names = new ArrayList<String>();
		names.add("trip_meta");
		int i, j;
		for (i = 0; i < delayArray.size(); i++) {
			if(delayArray.get(i)<Constants.INF){
				names.add("od_merge_" + String.valueOf(delayArray.get(i)));
			}else{
				names.add("od_merge");
			}
		}
		
		ArrayList<String> file_names = new ArrayList<String>();
		for (String name : names) {
			file_names.add(absolutePath(dirName,name));
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
				delay = od_merge_mergeable(trip_meta.get(i), trip_meta.get(j), Constants.NO_LONG_WALK);
				if (delay != -Constants.INF ) {
					c_id = i;
					f_id = j;
					mergeable = true;
				} else {
					delay = od_merge_mergeable(trip_meta.get(j),
							trip_meta.get(i), Constants.NO_LONG_WALK);
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
		return trip_meta;
	}

	public ArrayList<TripMeta> produceMergeableRelation(String dirName,
			ArrayList<Integer> delayArray, ArrayList<Double> upper_bound) {
		return produceMergeableRelation(dirName, delayArray, false, upper_bound);
	}

	public ArrayList<TripMeta> produceMergeableRelation(String dirName,
			boolean output, ArrayList<Double> upper_bound) {
		return produceMergeableRelation(dirName, new ArrayList<Integer>(),
				output,upper_bound);
	}

	public ArrayList<TripMeta> produceMergeableRelation(String dirName, ArrayList<Double> upper_bound) {
		return produceMergeableRelation(dirName, new ArrayList<Integer>(),
				false,upper_bound);
	}
	
	HashMap<Integer,ArrayList<Integer>> copyC(HashMap<Integer,ArrayList<Integer>> child_trips){
		HashMap<Integer,ArrayList<Integer>> copy=new HashMap<Integer,ArrayList<Integer>>();
		ArrayList<Integer> clone=new ArrayList<Integer>();
		for(Integer c_id:child_trips.keySet()){
			//copy.put(c_id, DeepCopy.copy(child_trips.get(c_id)));
			copy.put(c_id, new ArrayList<Integer>(child_trips.get(c_id)));
		}
		return copy;
	}
	
	HashMap<Integer,FatherTrip> copyF(HashMap<Integer,FatherTrip> father_trips){
		HashMap<Integer,FatherTrip> copy=new HashMap<Integer,FatherTrip>();
		for(Integer f_id:father_trips.keySet()){
			copy.put(f_id, new FatherTrip(father_trips.get(f_id)));
		}
		return copy;
	}
	
	String absolutePath(String dirName, String fileName){
		String ret=Constants.PROCESSED_DIR+dirName+"/";
		if(Constants.NO_LONG_WALK){
			ret+="no_long_walk/";
		}
		ret+=fileName+".txt";
		return ret;
	}
}
