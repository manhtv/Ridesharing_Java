package main;

import java.util.Random;

public class Constants {
	public static final boolean debug=true;
	
	//directory configuration
	public static final String BASE_DIR = "E:/Users/v-shuoma/Desktop/workspace/taxi/";
	public static final String RAW_DIR = BASE_DIR + "raw/";
	public static final String PROCESSED_DIR = BASE_DIR + "processed/";
	public static final String SAVEDIR="C:/Users/v-shuoma/Desktop/workspace/data/results/";

	public static final Random rand=new Random();	
	public static final int INF = 9999999;

	//Mergeable Relation
	public static final double DELTA = 15*60;
	public static final double PACE = 1.3;
	
	//measurements
	public static final String[] MEASUREMENTS={"saved_distance", "no_of_saved_trip","avg_delay","max_no_of_passenger","avg_no_of_passenger","max_delay"};
	public static final String[] HEURISTICS={"upper_bound","optimal_filter","benefit", "avg_benefit", "children_no", "random"};
}

