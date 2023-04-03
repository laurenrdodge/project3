import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;




/**

A class that represents a point in a trip.
Contains methods to read and analyze trip data.
*/
public class TripPoint {

	//instance variables
	private double lat;	// latitude
	private double lon;	// longitude
	private int time;	// time in minutes
	
	private static ArrayList<TripPoint> trip;	// ArrayList of every point in a trip
	private static ArrayList<TripPoint> movingTrip; //added
	// default constructor
	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}
	
	/**
	 * Constructor for TripPoint with time, latitude, and longitude.
	 * @param time an integer representing time in minutes.
	 * @param lat a double representing the latitude.
	 * @param lon a double representing the longitude.
	 */
	// constructor given time, latitude, and longitude
	public TripPoint(int time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}
	
	/**
	 * Returns time of this TripPoint.
	 * @return an integer representing time in minutes.
	 */
	// returns time
	public int getTime() {
		return time;
	}
	
	/**
	 * Returns latitude of this TripPoint.
	 * @return a double representing latitude.
	 */
	// returns latitude
	public double getLat() {
		return lat;
	}
	
	/**
	 * Returns longitude of this TripPoint.
	 * @return a double representing longitude.
	 */
	// returns longitude
	public double getLon() {
		return lon;
	}
	
	/**
	 * Returns a copy of ArrayList trip.
	 * @return an ArrayList containing TripPoints.
	 */
	// returns a copy of trip ArrayList
	public static ArrayList<TripPoint> getTrip() {
		return new ArrayList<>(trip);
	}
	
	/**
	 * Calculates the distance between two points on a sphere using the Haversine formula.
	 * @param first a TripPoint representing the first point.
	 * @param second a TripPoint representing the second point.
	 * @return a double representing the distance in kilometers.
	 */
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
        double rad = 6371; //radius of the Earth in kilometers
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
	}
	
	
	/**
	 * Calculates the average speed between two TripPoints in km/hr.
	 * @param a a TripPoint representing the first point.
	 * @param b a TripPoint representing the second point.
	 * @return a double representing the average speed in km/hr.
	 */
	// finds the average speed between two TripPoints in km/hr
	public static double avgSpeed(TripPoint a, TripPoint b) {
		
		int timeInMin = Math.abs(a.getTime() - b.getTime());
		
		double dis = haversineDistance(a, b);
		
		double kmpmin = dis / timeInMin;
		
		return kmpmin*60;
	}
	
	/**
	 * Calculates the total time of the trip in hours.
	 * @return the total time of the trip in hours
	 */
	
	// returns the total time of trip in hours
	public static double totalTime() {
		int minutes = trip.get(trip.size()-1).getTime();
		double hours = minutes / 60.0;
		return hours;
	}
	
	
	/**
	 * Calculates the total distance traveled over the trip.
	 * @return the total distance traveled over the trip
	 */
	
	// finds the total distance traveled over the trip
	public static double totalDistance() {
		
		double distance = 0.0;
		
		//if (trip.isEmpty()) {
		//	readFile("triplog.csv");
		//}
		
		for (int i = 1; i < trip.size(); ++i) {
			distance += haversineDistance(trip.get(i-1), trip.get(i));
		}
		
		return distance;
	}
	
	/**
	 * Returns a string representation of the trip.
	 * @return a string representation of the trip
	 */
	public String toString() {
		
		return null;
	}

	/**
	 * Reads trip data from a CSV file.
	 * @param filename the name of the CSV file to read from
	 * @throws FileNotFoundException if the file is not found
	 * @throws IOException if an error occurs while reading the file
	 */
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

	h1StopDetection is a static method that detects the number of stops in a trip based on a minimum distance between
	consecutive points. A stop is defined as a point where the distance between two consecutive points is less than or
	equal to the specified distance.
	@return an integer value indicating the number of stops detected in the trip.
	*/
	public static int h1StopDetection() {
		
		// Distance threshold for detecting stops in kilometers
		double minutes = 0.6; //km
		// List to store the moving points of the trip
		movingTrip = new ArrayList<TripPoint>();
		//Counter for the number of stops detected
		int stops = 0;
		// Check if the trip contains at least two points
		if(trip.size() > 1){
			// Add the first point to the moving trip list
			movingTrip.add(trip.get(0));
			// Iterate through the remaining points in the trip
			for(int i = 1; i < trip.size(); ++i) { // ++i not i++
				// Calculate the distance between the current point and the previous point and check if the distance is less than or equal to the specified distance threshold
				if(haversineDistance(trip.get(i-1), trip.get(i)) <= minutes) {
					// Increment the stop counter
					++stops; 
				}
				// If the distance is greater than the threshold, add the point to the moving trip list
				else {
					movingTrip.add(trip.get(i));
				}
			}
		}
		// Return the total number of stops detected in the trip
		return stops;
	}

	
	public static int h2StopDetection() { 
		
	    movingTrip = new ArrayList<TripPoint>();
    double minutes = 0.5; //km
	    int stops = 0;
//
//	    for (int i = 0; i < trip.size(); i++) {
//	        
//	        boolean isStop = false;
//	        if (i < trip.size() - 2) {
//	            
//	            double[] distances = {haversineDistance(trip.get(i), trip.get(i+1)),
//	                                  haversineDistance(trip.get(i+1), trip.get(i+2)),
//	                                  haversineDistance(trip.get(i), trip.get(i+2))};
//
//	            int count = 0;
//	            for (double d : distances) {
//	                if (d <= minutes) {
//	                    count++;
//	                }
//	            }
//
//	            if (count >= 2) {
//	                isStop = true;
//	                i += 2; // skip next two points
//	                stops += 3; // update total stops count
//	            }
//	        }
//
//	        if (!isStop) {
//	            movingTrip.add(trip.get(i));
//	        }
//	    }
//	    return stops;
//	}
		
	    /**

	    This method detects stops in a trip based on a threshold distance of 0.5 km between points.
	    A stop is considered to occur when there are two consecutive points within a 0.5 km radius
	    and a third point that is also within the 0.5 km radius of those two points.
	    If a stop is detected, the three points within the 0.5 km radius are added to an ArrayList called "radius".
	    The method then continues to check if any additional points are within the 0.5 km radius of the points in "radius".
	    Once a point outside the 0.5 km radius is detected, the method adds the points in "radius" to the final ArrayList
	    of moving points and resets the "radius" ArrayList. The method then continues to check for stops from the
	    current point.
	    @return the number of stops detected in the trip
	    */
		if(trip.size() > 1){
			ArrayList<TripPoint> radius = new ArrayList<TripPoint>();
			boolean inRadius = false; 
			
			for(int i = 0; i < trip.size(); ++i) { 
				
				if(!inRadius) {
					
					int count = 0;
					
					if(i < trip.size() - 2) {
						
						double[] distances = {haversineDistance(trip.get(i), trip.get(i+1)), 
								haversineDistance(trip.get(i+1), 
										trip.get(i+2)), haversineDistance(trip.get(i), 
												trip.get(i+2))};
						for(double distance: distances) {
							if(distance <= minutes) {
								++count;
							}
						}
					}
					
					if(count >= 2) {
						
						radius = new ArrayList<TripPoint>();
						
						radius.add(trip.get(i));
						radius.add(trip.get(i+1));
						radius.add(trip.get(i+2));
						
						i += 2; 
						stops += 3;
						
						inRadius = true;
					}
					
					else {
						movingTrip.add(trip.get(i)); 
					}
				}
				
				else {
					
					if(inRadius(radius, trip.get(i), minutes)) {
						radius.add(trip.get(i));
						++stops;
					}
					else {
						inRadius = false; 
						
						--i; 
					}
				}
			}		
		}
		return stops;
	}
	
	private static boolean inRadius(ArrayList<TripPoint> radius, TripPoint point, double minutes) {
		for(int i = 0; i < radius.size(); ++i) {
			if(haversineDistance(radius.get(i), point) <= minutes) {
				return true;
			}
		}
		return false;
	}
	
	/**

	Calculates the total time spent in motion during the trip based on the size of the movingTrip ArrayList.
	The moving time is calculated as (size - 1) * 5.0 / 60.0.
	@return A double value representing the total time spent in motion during the trip.
	*/
	public static double movingTime() {

		double movingTime = movingTrip.size();
		 movingTime = (movingTime - 1) * 5.0 /60.0;
		 
		 return movingTime;
		
	    
	}
	
	//complete stoppedTime, avgMovingSpeed, getMovingTrip
	/**

	Calculates the total time the vehicle was stopped during the trip.
	@return the total time the vehicle was stopped during the trip, in hours.
	*/
	public static double stoppedTime() {
		
		return totalTime() - movingTime();
	}
	
	/**

	Calculates the average speed of the vehicle while it was moving during the trip.
	@return the average speed of the vehicle while it was moving during the trip, in kilometers per hour.
	@throws IllegalStateException if no moving trip points are available.
	*/
	public static double avgMovingSpeed() { //only problem - blue x
	    double distance = 0.0;
	    //int timeInMin = 0;
	    //int count = 0;
	

		for (int i = 1; i < movingTrip.size(); ++i) {
			
			distance += haversineDistance(movingTrip.get(i - 1), movingTrip.get(i));
		}
		
		//distance divided by speed - average 
		
		return distance / movingTime();
	}

	/**

	Returns a copy of the ArrayList of TripPoints containing only the points that are considered
	to be in motion (i.e. not stopped).
	@return an ArrayList of TripPoints representing the moving trip
	*/
	public static ArrayList<TripPoint> getMovingTrip() {
		
		return new ArrayList<TripPoint>(movingTrip);
	}
	

	}


	
	
