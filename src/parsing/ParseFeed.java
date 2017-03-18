package parsing;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;
import processing.data.XML;

public class ParseFeed {
	
	private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    private static final char DEFAULT_MS = '$';
    
	public static List<PointFeature> readdata(PApplet p,String path)
	{
		String line;
		String[] result = null;
		int item=0;
		int Label = 0;
		List<PointFeature> features = new ArrayList<PointFeature>();
		int nofattribute=9;
		HashMap<Integer,String> attributes= new HashMap<Integer,String>();
		try{
		    InputStream fis = new FileInputStream(path);
		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);
		    while ((line = br.readLine()) != null)
			{
		    	result = line.split(","); //split by tab
		    	if(Label==0)//first line
		    	{
		    		for(int i=0;i<result.length;i++)
			    	{
		    			attributes.put(i, result[i]);
			    	}
		    		Label=1;
		    	}
		    	else
		    	{
		    		char[] temp;
		    		PointFeature point= new PointFeature();
		    		item=9;
			    	for(int i=0;i<9;i++)
			    	{
			    		String at = attributes.get(i);
			    		point.putProperty(at, result[i]);
			    	}
			    	for(int i=9;i<14;i+=2)
			    	{
			    		String at = attributes.get(item);
			    		StringBuffer sb=new StringBuffer(result[i].substring(2));
			    		sb.append(result[i+1], 0, result[i+1].length()-1);
			    		point.putProperty(at, sb);
			    		item++;
			    	}
			    	//County
			    	String at = attributes.get(item);
			    	point.putProperty(at, result[15]);
			    	item++;
			    	//Poverty rate
			    	at = attributes.get(item);
			    	point.putProperty(at, result[16]);
			    	item++;
			    	//Location
		    		float lat = Float.valueOf(result[17].substring(1, result[17].length()-1));
		    		float lon = Float.valueOf(result[18].substring(0, result[18].length()-2));
		    		point.setLocation(new Location(lat,lon));
		    		
		    	//point = new PointFeature(location);
				features.add(point);
		    	}
			}
		    
		    br.close();
		    return features;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;	
	}

	/*
	 * Gets location from georss:point tag
	 * 
	 * @param XML Node which has point as child
	 * 
	 * @return Location object corresponding to point
	 */
	private static Location getLocationFromPoint(XML itemXML) {
		// set loc to null in case of failure
		Location loc = null;
		XML pointXML = itemXML.getChild("georss:point");
		
		// set location if existing
		if (pointXML != null && pointXML.getContent() != null) {
			String pointStr = pointXML.getContent();
			String[] latLon = pointStr.split(" ");
			float lat = Float.valueOf(latLon[0]);
			float lon = Float.valueOf(latLon[1]);

			loc = new Location(lat, lon);
		}
		
		return loc;
	}	
	
	/*
	 * This method is to parse a file containing airport information.  
	 * The file and its format can be found: 
	 * http://openflights.org/data.html#airport
	 * 
	 * It is also included with the UC San Diego MOOC package in the file airports.dat
	 * 
	 * @param p - PApplet being used
	 * @param fileName - file name or URL for data source
	 */
	public static List<PointFeature> parseAirports(PApplet p, String fileName) {
		List<PointFeature> features = new ArrayList<PointFeature>();

		String[] rows = p.loadStrings(fileName);
		for (String row : rows) {
			
			// hot-fix for altitude when lat lon out of place
			int i = 0;
			
			// split row by commas not in quotations
			String[] columns = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			
			// get location and create feature
			//System.out.println(columns[6]);
			float lat = Float.parseFloat(columns[6]);
			float lon = Float.parseFloat(columns[7]);
			
			Location loc = new Location(lat, lon);
			PointFeature point = new PointFeature(loc);
			
			// set ID to OpenFlights unique identifier
			point.setId(columns[0]);
			
			// get other fields from csv
			point.addProperty("name", columns[1]);
			point.putProperty("city", columns[2]);
			point.putProperty("country", columns[3]);
			
			// pretty sure IATA/FAA is used in routes.dat
			// get airport IATA/FAA code
			if(!columns[4].equals("")) {
				point.putProperty("code", columns[4]);
			}
			// get airport ICAO code if no IATA
			else if(!columns[5].equals("")) {
				point.putProperty("code", columns[5]);
			}
			
			point.putProperty("altitude", columns[8 + i]);
			
			features.add(point);
		}

		return features;
		
	}
	
	

	/*
	 * This method is to parse a file containing airport route information.  
	 * The file and its format can be found: 
	 * http://openflights.org/data.html#route
	 * 
	 * It is also included with the UC San Diego MOOC package in the file routes.dat
	 * 
	 * @param p - PApplet being used
	 * @param fileName - file name or URL for data source
	 */
	public static List<ShapeFeature> parseRoutes(PApplet p, String fileName) {
		List<ShapeFeature> routes = new ArrayList<ShapeFeature>();
		
		String[] rows = p.loadStrings(fileName);
		
		for(String row : rows) {
			String[] columns = row.split(",");
			
			ShapeFeature route = new ShapeFeature(Feature.FeatureType.LINES);
			
			// set id to be OpenFlights identifier for source airport
			
			// check that both airports on route have OpenFlights Identifier
			if(!columns[3].equals("\\N") && !columns[5].equals("\\N")){
				// set "source" property to be OpenFlights identifier for source airport
				route.putProperty("source", columns[3]);
				// "destination property" -- OpenFlights identifier
				route.putProperty("destination", columns[5]);
				
				routes.add(route);
			}
		}
		return routes;
		
	}
	
	

	/*
	 * This method is to parse a file containing life expectancy information from
	 * the world bank.  
	 * The file and its format can be found: 
	 * http://data.worldbank.org/indicator/SP.DYN.LE00.IN
	 * 
	 * It is also included with the UC San Diego MOOC package 
	 * in the file LifeExpectancyWorldBank.csv
	 * 
	 * @param p - PApplet being used
	 * @param fileName - file name or URL for data source
	 * @return A HashMap of country->average age of death
	 */
	public static HashMap<String, Float> loadLifeExpectancyFromCSV(PApplet p, String fileName) {
		// HashMap key: country ID and  data: lifeExp at birth
		HashMap<String, Float> lifeExpMap = new HashMap<String, Float>();

		// get lines of csv file
		String[] rows = p.loadStrings(fileName);
		
		// Reads country name and population density value from CSV row
		for (String row : rows) {
			// split row by commas not in quotations
			String[] columns = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			
			// check if there is any life expectancy data from any year, get most recent
			/*
			 * EXTENSION: Add code to also get the year the data is from.
			 * You may want to use a list of Floats as the  values for the HashMap
			 * and store the year as the second value. (There are many other ways to do this)
			 */
			//
			for(int i = columns.length - 1; i > 3; i--) {
				
				// check if value exists for year
				if(!columns[i].equals("..")) {
					lifeExpMap.put(columns[3], Float.parseFloat(columns[i]));
					
					// break once most recent data is found
					break;
				}
			}
			
		}

		return lifeExpMap;
	}
	
	

}