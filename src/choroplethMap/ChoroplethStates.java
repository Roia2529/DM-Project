package choroplethMap;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.MapUtils;

/**
 * Visualizes population density of the world as a choropleth map. Countries are shaded in proportion to the population
 * density.
 * 
 * It loads the country shapes from a GeoJSON file via a data reader, and loads the population density values from
 * another CSV file (provided by the World Bank). The data value is encoded to transparency via a simplistic linear
 * mapping.
 */
public class ChoroplethStates extends PApplet {

	UnfoldingMap map;

	HashMap<String, DataEntry> dataEntriesMap;
	List<Marker> stateMarkers;
	public static final String[] se = new String[]{"WV","FL","NM","TX","LA",
			"TN","CA","NV","AL","AR","GA","AZ","ID","OK","MS","KY","OR"};
	public static final String[] mw = new String[]{"WA","DE","WI","HI","WY",
			"ND","NH","NE","NY","PA","VA","CO","AK","VT","IL","IN","IA",
			"MA","CT","ME","MD","OH","UT","MO","MN","MI","RI","KS","MT","SD"};
	
	HashSet<String> set_se = new HashSet<String>(Arrays.asList(se));
	HashSet<String> set_mw = new HashSet<String>(Arrays.asList(mw));

	public void settings() {
		size(800, 600, P2D);
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { ChoroplethStates.class.getName() });
	}

	public void setup() {
		map = new UnfoldingMap(this, 200, 20, 730, 600);
		map.zoomAndPanTo(4, new Location(40.7570769f,-93.8660f));
		map.setBackgroundColor(240);
		MapUtils.createDefaultEventDispatcher(this, map);

		// Load country polygons and adds them as markers
		List<Feature> states = GeoJSONReader.loadData(this, "states.geo.json");
		stateMarkers = MapUtils.createSimpleMarkers(states);
		map.addMarkers(stateMarkers);

		shadeCountries();
	}
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyChar()=='l')
			map.zoomOut();
		else if (e.getKeyChar()=='r')
			map.zoomAndPanTo(4, new Location(40.7570769f,-93.8660f));
	}

	public void draw() {
		background(240);

		// Draw map tiles and country markers
		map.draw();
	}

	public void shadeCountries() {
		for (Marker marker : stateMarkers) {
			// Find data for country of the current marker
			String countryId = marker.getId();
			//System.out.println(countryId);
			String state = marker.getStringProperty("LSAD");
//			DataEntry dataEntry = dataEntriesMap.get(countryId);
//
			if (set_se.contains(state) ) {
				float transparency = map(500, 0, 700, 10, 255);
				marker.setColor(color(255, 0, 0, transparency));
				//marker.setColor(color(255, 0, 0));
			}
			else if(set_mw.contains(state) )
			{
				float transparency = map(200, 0, 700, 10, 255);
				marker.setColor(color(255, 0, 0, transparency));
			}
			else {
				marker.setColor(color(100, 120));
			}
		}
	}

	public HashMap<String, DataEntry> loadPopulationDensityFromCSV(String fileName) {
		HashMap<String, DataEntry> dataEntriesMap = new HashMap<String, DataEntry>();

		String[] rows = loadStrings(fileName);
		for (String row : rows) {
			// Reads country name and population density value from CSV row
			String[] columns = row.split(";");
			if (columns.length >= 3) {
				DataEntry dataEntry = new DataEntry();
				dataEntry.countryName = columns[0];
				dataEntry.id = columns[1];
				dataEntry.value = Float.parseFloat(columns[2]);
				dataEntriesMap.put(dataEntry.id, dataEntry);
			}
		}

		return dataEntriesMap;
	}

	class DataEntry {
		String countryName;
		String id;
		Integer year;
		Float value;
	}

}