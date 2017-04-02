package hospital2poverty;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.GeoUtils;

public class AvgPoverty {
	private static List<PointFeature> Hsp_Lct;
	private static List<PointFeature> Conty_pov;
	
	private final double radius=80.4672;
	private final String poverty="SAEPOVRTALL_RATE";
	public AvgPoverty(String s1,String s2) {
		// TODO Auto-generated constructor stub
		Hsp_Lct = this.hospitaldata(s1);
		Conty_pov = this.povertydata(s2);
	}
	public List<PointFeature> hospitaldata(String path)
	{
		String line;
		String[] result = null;
		int item=0;
		int Label = 0;
		List<PointFeature> features = new ArrayList<PointFeature>();
		int nofattribute=8;
		HashMap<Integer,String> attributes= new HashMap<Integer,String>();
		try{
			ClassLoader classLoader = getClass().getClassLoader();
		    InputStream fis = classLoader.getResourceAsStream(path);
		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);
		    while ((line = br.readLine()) != null)
			{
		    	result = line.split(","); //split by tab
		    	if(Label==0)//first line
		    	{
		    		int i;
		    		for(i=0;i<4;i++)
			    	{
		    			attributes.put(i, result[i]);
			    	}
		    		attributes.put(i, result[i].substring(1, result[i].length()-1));
		    		i++;
		    		attributes.put(i, result[i].substring(0, result[i].length()-2));
		    		Label=1;
		    	}
		    	else
		    	{
		    		//char[] temp;
		    		PointFeature point= new PointFeature();
		    		
			    	for(int i=0;i<4;i++)
			    	{
			    		String at = attributes.get(i);
			    		point.putProperty(at, result[i]);
			    	}
			    	//Location
		    		float lat = Float.valueOf(result[5].substring(1, result[5].length()-1));
		    		float lon = Float.valueOf(result[6].substring(0, result[6].length()-2));
		    		point.setLocation(new Location(lat,lon));
		    		item=5;
		    		//
		    		String at = attributes.get(item++);
			    	point.putProperty(at, result[7]);
		    		
			    	features.add(point);
					if(features.size()==2)
					{
						PointFeature p1=features.get(0);
						PointFeature p2=features.get(1);
						System.out.println(GeoUtils.getDistance(p1.getLocation(), p2.getLocation()));
					}
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
	public List<PointFeature> povertydata(String path){
		String line;
		String[] result = null;
		int item=0;
		int Label = 0;
		List<PointFeature> features = new ArrayList<PointFeature>();
		HashMap<Integer,String> attributes= new HashMap<Integer,String>();
		try{
			ClassLoader classLoader = getClass().getClassLoader();
		    InputStream fis = classLoader.getResourceAsStream(path);
		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);
		    while ((line = br.readLine()) != null)
			{
		    	result = line.split(","); //split by tab
		    	if(Label==0)//first line
		    	{
		    		for(int i=0;i<5;i++)
		    			attributes.put(i, result[i]);
		    		Label=1;
		    	}
		    	else
		    	{
		    		//char[] temp;
		    		PointFeature point= new PointFeature();
		    		
			    	for(int i=0;i<3;i++)
			    	{
			    		String at = attributes.get(i);
			    		point.putProperty(at, result[i]);
			    	}
			    	//Location
		    		float lat = Float.valueOf(result[4]);
		    		float lon = Float.valueOf(result[5]);
		    		point.setLocation(new Location(lat,lon));
		    		
			    	features.add(point);
					if(features.size()==2)
					{
						PointFeature p1=features.get(0);
						PointFeature p2=features.get(1);
						System.out.println(GeoUtils.getDistance(p1.getLocation(), p2.getLocation()));
					}
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
	public void weighted_Poverty()
	{
		//for(int i=0;i<10;i++)
		for(PointFeature p_hospital:AvgPoverty.Hsp_Lct)
		{
			//PointFeature p_hospital=AvgPoverty.Hsp_Lct.get(i);
			HashMap<String, Double> c_p = new HashMap<String,Double>();
			HashMap<String, Double> c_d = new HashMap<String,Double>();
			double sum_d=0.0;
			double close_d=Double.MAX_VALUE,close_rate=0.0;
			String close_county;
			for(PointFeature p_county:AvgPoverty.Conty_pov)
			{
				double d=GeoUtils.getDistance(p_hospital.getLocation(), p_county.getLocation());
				if(d<close_d)
				{
					close_d=d;
					close_county=p_county.getStringProperty("NAME");
					close_rate = Double.parseDouble(p_county.getStringProperty(poverty));
				}
				if(d<this.radius)
				{
					sum_d+=d;
					String county = p_county.getStringProperty("NAME");
					
					Double povertyrate = Double.parseDouble(p_county.getStringProperty(poverty));
					c_p.put(county, povertyrate);
					c_d.put(county, d);
				}
			}
			
			double avg_pov=0.0;
			//System.out.print(p_hospital.getStringProperty("Provider Id")+",");
			if(sum_d>0)
			{
				int amount_county = c_p.size();
				//System.out.print( amount_county+",");
				if(amount_county!=1)
				{	
					HashMap<String, Double> c_weight = new HashMap<String,Double>();
					double normalize=0.0;
					for(String c:c_p.keySet())
					{
						double w=1-c_d.get(c)/sum_d;
						normalize+=w;
						c_weight.put(c, w);
					}
					
					for(String c:c_p.keySet())
						avg_pov+=c_p.get(c)*c_weight.get(c)/normalize;
				}
				else
					avg_pov=close_rate;
			}
			else
			{
				//System.out.print( "0,");
				avg_pov = close_rate;
			}
			System.out.println(avg_pov);
		}
	}
	private float getPovertyRateNumber(int index)
	{
		return Float.parseFloat(this.Conty_pov.get(index).getStringProperty(poverty));
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s2="AllCounty_povr_loc.csv";
		String s1="Hospitals_location.csv";
		
		AvgPoverty ap=new AvgPoverty(s1,s2);
		ap.weighted_Poverty();
	}

}
