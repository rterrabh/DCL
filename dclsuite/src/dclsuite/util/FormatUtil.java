package dclsuite.util;

import java.text.DecimalFormat;

public class FormatUtil {

	private FormatUtil(){}
	
	public static String formatDouble(double d){
        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(d);
	} 
	
	public static String formatInt(int i){
        DecimalFormat df = new DecimalFormat("##00");
        return df.format(i);
	} 
	
	
	
}
