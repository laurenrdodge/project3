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
	private static ArrayList<TripPoint> movingTrip; //added
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
	
	public static int h1StopDetection() {
		
		double minutes = 0.6; //km
		movingTrip = new ArrayList<TripPoint>();
		int stops = 0;
		
		if(trip.size() > 1){
			movingTrip.add(trip.get(0));
			
			for(int i = 1; i < trip.size(); ++i) { // ++i not i++
				
				if(haversineDistance(trip.get(i-1), trip.get(i)) <= minutes) {
					++stops; 
				}
				
				else {
					movingTrip.add(trip.get(i));
				}
			}
		}
		
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
	
		
	public static double movingTime() {

		double movingTime = movingTrip.size();
		 movingTime = (movingTime - 1) * 5.0 /60.0;
		 
		 return movingTime;
		
	    
	}
	
	//complete stoppedTime, avgMovingSpeed, getMovingTrip
	
	public static double stoppedTime() {
		
		return totalTime() - movingTime();
	}
	
	//only problem - blue x
	public static double avgMovingSpeed() {
	    double distance = 0.0;
	    //int timeInMin = 0;
	    //int count = 0;
	

		for (int i = 1; i < movingTrip.size(); ++i) {
			
			distance += haversineDistance(movingTrip.get(i - 1), movingTrip.get(i));
		}
		
		//distance divided by speed - average 
		
		return distance / movingTime();
	}

	
	public static ArrayList<TripPoint> getMovingTrip() {
		
		return new ArrayList<TripPoint>(movingTrip);
	}
	

	}


	
	
