package module3;

//Java utilities libraries
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;

//Processing library
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

public class HospitalMap extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = true;
	//private static final boolean offline = false;
	
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	final int yellow = color(255, 255, 0);
    final int red = color(255,0,0);
    final int green = color(0,255,0);
    private List<Marker> PFmarkers = new ArrayList<Marker>();
    private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {
		size(950, 650, OPENGL);

		if (offline) {
		    //map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    map = new UnfoldingMap(this, 200, 20, 730, 600);
		    //earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 20, 700, 580, new Google.GoogleMapProvider());
		}
		
		map.zoomAndPanTo(4, new Location(40.7570769f,-93.8660f));
	    MapUtils.createDefaultEventDispatcher(this, map);	
			
	    // The List you will populate with new SimplePointMarkers
	    //List<Marker> markers = new ArrayList<Marker>();
	    
	    List<PointFeature> DRG = ParseFeed.readdata(this, "Utah470DRG_2.csv");
	    
	    
	    //TODO: Add code here as appropriate
	    for(PointFeature tempPF: DRG)
	    {
	    	PFmarkers.add(new DRGMarker(tempPF));
	    }
	    map.addMarkers(PFmarkers);
	    
	}
	
	@Override
	public void mouseMoved()
	{
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(PFmarkers);
	}
	
	private void selectMarkerIfHover(List<Marker> markers)
	{
		if (lastSelected != null) {
			return;
		}
			for(Marker m:markers)
			{
				if(m.isInside(map, mouseX, mouseY))
				{
					CommonMarker common = (CommonMarker) m;
					lastSelected = common;
					lastSelected.setSelected(true);
					return;
				}
			}
	}
	
	public void draw() {
	    background(10);
	    map.draw();
	    addKey();
	}


	// helper method to draw key in GUI
	// TODO: Implement this method to draw the key
	private void addKey() 
	{	
		// Remember you can use Processing's graphics methods here
		fill(37,216,198);
		rect(15, 50, 160, 500);
		
		textSize(16);
		fill(0);
		text("Poverty Rate",30, 90);
		text("Charge Level",30, 280);
		
		//Color vs Poverty
		fill(green);
		ellipse(35, 130, 13, 13);
		fill(yellow);
		ellipse(35, 170, 13, 13);
		fill(red);
		ellipse(35, 210, 13, 13);
		
		//Radius vs Rank
		fill(color(255));
		ellipse(35, 310, 7, 7);
		ellipse(35, 340, 13, 13);
		ellipse(35, 380, 20, 20);
		
		fill(0);
		textSize(14);
		text("Low",50, 137); 
		text("Medium",50, 177);
		text("High ",50, 215);
		
		text("Cheap",50, 313); 
		text("Medium",50, 347);
		text("Expensive",50, 385);
	}
	
	
	
	
}
