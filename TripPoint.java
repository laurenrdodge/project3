import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TripPoint {

	private double lat;	// latitude
	private double lon;	// longitude
	private int time;	// time in minutes
	
	private static ArrayList<TripPoint> trip;	// ArrayList of every point in a trip
	private static ArrayList<TripPoint> movingTrip; 

	// default constructor
	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}
	
	// constructor given time, latitude, and longitude
	public TripPoint(int time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}
	
	// returns time
	public int getTime() {
		return time;
	}
	
	// returns latitude
	public double getLat() {
		return lat;
	}
	
	// returns longitude
	public double getLon() {
		return lon;
	}
	
	// returns a copy of trip ArrayList
	public static ArrayList<TripPoint> getTrip() {
		return new ArrayList<>(trip);
	}
	
	// uses the haversine formula for great sphere distance between two points
	public static double haversineDistance(TripPoint first, TripPoint second) {
		// distance between latitudes and longitudes
		double lat1 = first.getLat();
		double lat2 = second.getLat();
		double lon1 = first.getLon();
		double lon2 = second.getLon();
		
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
	}
	
	// finds the average speed between two TripPoints in km/hr
	public static double avgSpeed(TripPoint a, TripPoint b) {
		
		int timeInMin = Math.abs(a.getTime() - b.getTime());
		
		double dis = haversineDistance(a, b);
		
		double kmpmin = dis / timeInMin;
		
		return kmpmin*60;
	}
	
	// returns the total time of trip in hours
	public static double totalTime() {
		int minutes = trip.get(trip.size()-1).getTime();
		double hours = minutes / 60.0;
		return hours;
	}
	
	// finds the total distance traveled over the trip
	public static double totalDistance() throws FileNotFoundException, IOException {
		
		double distance = 0.0;
		
		if (trip.isEmpty()) {
			readFile("triplog.csv");
		}
		
		for (int i = 1; i < trip.size(); ++i) {
			distance += haversineDistance(trip.get(i-1), trip.get(i));
		}
		
		return distance;
	}
	
	public String toString() {
		
		return null;
	}

	public static void readFile(String filename) throws FileNotFoundException, IOException {

		// construct a file object for the file with the given name.
		File file = new File(filename);

		// construct a scanner to read the file.
		Scanner fileScanner = new Scanner(file);
		
		// initiliaze trip
		trip = new ArrayList<TripPoint>();

		// create the Array that will store each lines data so we can grab the time, lat, and lon
		String[] fileData = null;

		// grab the next line
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			// split each line along the commas
			fileData = line.split(",");

			// only write relevant lines
			if (!line.contains("Time")) {
				// fileData[0] corresponds to time, fileData[1] to lat, fileData[2] to lon
				trip.add(new TripPoint(Integer.parseInt(fileData[0]), Double.parseDouble(fileData[1]), Double.parseDouble(fileData[2])));
			}
		}

		// close scanner
		fileScanner.close();
	}
	
	/**
	 * Returns the number of stops using heuristic 1 and initializes and fills the movingTrip ArrayList with the actual tripPoints
	 * 
	 * The first heuristic for detecting stop zones is to check if a point is within a certain distance from the previous point. We will call this distance the "displacement threshold". If a point falls within the displacement threshold of the previous point, it should be considered a stop.
	 * 
	 * @return the number of stops using heuristic 1
	 */
	public static int h1StopDetection() {
		double min = 0.6; //km
		movingTrip = new ArrayList<TripPoint>();
		int stops = 0;
		
		if(trip.size() > 1){
			movingTrip.add(trip.get(0)); //add first trip point to moving trip
			
			for(int i = 1; i < trip.size(); ++i) { //start from the second point
				if(haversineDistance(trip.get(i-1), trip.get(i)) <= min) {
					++stops; //add a stop
				}
				else {
					movingTrip.add(trip.get(i)); //add new trip point to moving trip
				}
			}
		}
		return stops;
	}
	
	/**
	 * Returns the number of stops using heuristic 2 and initializes and fills the movingTrip ArrayList with the actual tripPoints.
	 * 
	 * The second heuristic for detecting stop zones is to check for groups of three or more points that are all within a certain distance of each other. We will call this distance the "stop radius". If there are three or more points within the stop radius of each other, we consider them to be part of the same stop zone. This way the points do not have to be directly consecutive. For example, a point could be outside the stop radius of the point directly before it, but inside the radius of another point in the same stop zone, so that point is still considered a stop.
	 * 
	 * @return the number of stops using heuristic 1
	 */
	public static int h2StopDetection() {
		double min = 0.5; //km
		movingTrip = new ArrayList<TripPoint>();
		int stops = 0;
		
		if(trip.size() > 1){
			ArrayList<TripPoint> stopZone = new ArrayList<TripPoint>();
			boolean inStopZone = false; //in the beginning we are not yet inside a stop zone
			
			//iterate through the entire trip list
			for(int i = 0; i < trip.size(); ++i) { 
				
				if(!inStopZone) {
					
					int count = 0;
					//if, for the edge case at the end of the list
					if(i < trip.size()-2) {
						//check how many of the distances between points are within the stop radius , min
						double[] distances = {haversineDistance(trip.get(i), trip.get(i+1)), haversineDistance(trip.get(i+1), trip.get(i+2)), haversineDistance(trip.get(i), trip.get(i+2))};
						for(double d: distances) {
							if(d <= min) {
								++count;
							}
						}
					}
					
					//if 2 or more are within, then add them all to the stop zone
					//count is always 0 for the last two points, so no index out of bound exception possible
					//so if for the last two points they are not in a already existing stop zone, they get added to moving trip automatically
					if(count >= 2) {
						stopZone = new ArrayList<TripPoint>(); //reset stop zone
						stopZone.add(trip.get(i));
						stopZone.add(trip.get(i+1));
						stopZone.add(trip.get(i+2));
						i += 2; //increase index by two to skip the two point that are already in the stop zone
						stops += 3;
						inStopZone = true;
					}
					else {
						movingTrip.add(trip.get(i)); //if the point is not in a stop zone add it to moving trip
					}
				}
				else {
					//see if next point is in stop zone and if yes add it to the stop zone list and increase stops by one
					if(isWithinStopZone(stopZone, trip.get(i), min)) {
						stopZone.add(trip.get(i));
						++stops;
					}
					else {
						inStopZone = false; //not in stop zone anymore, so reset for next iteration of the loop
						--i; //to check the same point again for being in a different stop zone
					}
				}
			}		
		}
		return stops;
	}
	
	//does not need a javadoc comment as the method is private
	private static boolean isWithinStopZone(ArrayList<TripPoint> stopZone, TripPoint point, double min) {
		for(int i = 0; i < stopZone.size(); ++i) {
			if(haversineDistance(stopZone.get(i), point) <= min) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a copy of the movingTrip ArrayList.
	 * 
	 * @return a copy of the movingTrip ArrayList
	 */
	public static ArrayList<TripPoint> getMovingTrip() {
		return new ArrayList<TripPoint>(movingTrip); //to prevent the movingTrip ArrayList to get changed
	}
	
	/**
	 * Returns the amount of time spent moving in hours.
	 * 
	 * @return the amount of time spent moving in hours
	 */
	public static double movingTime() {
		return (movingTrip.size()-1)* 5.0 /60.0; //-1 because e.g. 2 points would mean a time of 5min not 10min
	}
	
	/**
	 * Returns the amount of time spent stopped in hours.
	 * 
	 * @return the amount of time spent stopped in hours
	 */
	public static double stoppedTime() {
		return (trip.size()-movingTrip.size())* 5.0 /60.0; //why not -1 though
	}
	
	/**
	 * Returns the total distance traveled over the trip that was not during a stop in km.
	 * 
	 * @return the total distance traveled over the trip that was not during a stop in km
	 */
	public static double totalMovingDistance() {
			
		double distance = 0.0;
		
		for (int i = 1; i < movingTrip.size(); ++i) {
			distance += haversineDistance(movingTrip.get(i-1), movingTrip.get(i));
		}
		
		return distance;
	}

	/**
	 * Returns the average speed over the course of the trip only considering the moving points in km/h.
	 * 
	 * @return the average speed over the course of the trip only considering the moving points in km/h
	 */
	public static double avgMovingSpeed() {
		return totalMovingDistance()/movingTime();
	}
}
