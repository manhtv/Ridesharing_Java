package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Pair{
	int t_id;
	double attr;
	public Pair(int t_id, double attr){
		this.t_id=t_id;
		this.attr=attr;
	}
}

class CustomizedSort implements Comparator<Pair>{
	@Override
	public int compare(Pair p1, Pair p2) {
		return p1.attr>p2.attr?1:(p1.attr==p2.attr?0:-1);
	}
}

/*
 * @sort an array based on associative attribute and return the top n entries
 */
public class CustomSort{
	public static int max(ArrayList<Integer> array, ArrayList<Double> attr_array){
		int i,ret=0;
		double max=-Constants.INF;
		ArrayList<Pair> pairs=new ArrayList<Pair>();
		for(i=0;i<array.size();i++){
			if(attr_array.get(i)>max){
				max=attr_array.get(i);
				ret=i;
			}
		}
		return ret;
	}
	
	public static ArrayList<Integer> sort(ArrayList<Integer> array, ArrayList<Double> attr_array, Comparator<Pair> comp, int size){
		ArrayList<Integer> ret=new ArrayList<Integer>();
		if(array.size()!=attr_array.size()){
			return ret;
		}
		int i;
		ArrayList<Pair> pairs=new ArrayList<Pair>();
		for(i=0;i<array.size();i++){
			pairs.add(new Pair(array.get(i), attr_array.get(i)));
		}
		Collections.sort(pairs, comp);
		for(i=0;i<size;i++){
			ret.add(pairs.get(i).t_id);
		}
		return ret;
	}
	
	public static ArrayList<Integer> sort(ArrayList<Integer> array, ArrayList<Double> attr_array, Comparator<Pair> comp){
		return sort(array, attr_array, comp, array.size());
	}

}


