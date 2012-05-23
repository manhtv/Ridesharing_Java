Ridesharing_Java
================
Note:
io.FileIO.main(): read filtered trajectories and partition them by date; create a folder for each day in which there is a file for each single taxi



Program:
1. (Python) io.ReadTrips: read trajectories from a day and generate trip_meta file
2. main.Ridesharing.main(): read the output from last step and output the measurements
3. (Python) plot.scatter: read the output from last step and draw the scatter picture
4. if use no_long_walk:  create a folder named no_long_walk in the date folder and copy the trip_meta file into no_long_walk then redo step 2 and 3  