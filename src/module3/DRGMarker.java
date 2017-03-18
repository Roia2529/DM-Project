package module3;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PConstants;
import processing.core.PGraphics;

/** Implements a visual marker for cities on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
// TODO: Change SimplePointMarker to CommonMarker as the very first thing you do 
// in module 5 (i.e. CityMarker extends CommonMarker).  It will cause an error.
// That's what's expected.
public class DRGMarker extends CommonMarker {
	public static int TRI_SIZE = 10;  // The size of the triangle marker
	public static final float THRESHOLD_MODERATE = 18000;
	public static final float THRESHOLD_LIGHT = 11000;
	public static final float THRESHOLD_INTERMEDIATE = 10;
	public static final float THRESHOLD_DEEP = 19;
	
	protected float radius;
	public DRGMarker(Location location) {
		super(location);
	}
	
	
	public DRGMarker(Feature HSP) {
		super(((PointFeature)HSP).getLocation(), HSP.getProperties());
		// Cities have properties: "name" (city name), "country" (country name)
		// and "population" (population, in millions)
	}

	
	/**
	 * Implementation of method to draw marker on the map.
	 */
	public void drawMarker(PGraphics pg, float x, float y) {
		// Save previous drawing style
		float payment=getPaymentNumber();
    	float Poverty = this.getPovertyRateNumber();
		pg.pushStyle();
		//Poverty level
		if (Poverty < THRESHOLD_INTERMEDIATE) {
			pg.fill(0, 255, 0);
		}
		else if (Poverty > THRESHOLD_DEEP) {
			pg.fill(255, 0, 0);
		}
		else {
			pg.fill(0, 0, 255);
		}
		
		//Charge Level
		if (payment < THRESHOLD_LIGHT) {
			radius=9;
		}
		else if (payment > THRESHOLD_MODERATE) {
			radius=19;
		}
		else {
			radius=14;
		}
		pg.ellipse(x, y, radius, radius);
		// Restore previous drawing style
		pg.popStyle();
	}
	
	/** Show the title of the city if this marker is selected */
	public void showTitle(PGraphics pg, float x, float y)
	{	
		// TODO: Implement this method
		String Hospital = this.getHospital();
		
		pg.pushStyle();
		pg.fill(255);
		pg.rect(x-3, y-55,pg.textWidth(Hospital)+6,50);
		pg.fill(0);
		pg.textSize(12);
		pg.text(Hospital, x,y-40);
		
		pg.popStyle();
	
	}
	
	
	
	/* Local getters for some city properties.  
	 */
	private String getHospital()
	{
		return getStringProperty("Provider Name");
	}
	
//	private String getCountry()
//	{
//		return getStringProperty("country");
//	}
	private String getPovertyRate()
	{
		return getStringProperty("Poverty Rate");
	} 
	private float getPovertyRateNumber()
	{
		return Float.parseFloat(getStringProperty("Poverty Rate"));
	} 
	private float getPaymentNumber()
	{
		Object magObj = getProperty(" Average Total Payments ");
    	float Payment = Float.parseFloat(magObj.toString());
    	return Payment;
		//return Float.parseFloat(getStringProperty(" Average Total Payments "));
	}

}
