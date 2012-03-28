package dclsuite.util;

import java.text.DecimalFormat;

public class FormatUtil {

	private FormatUtil(){}
	
	public static String formatDouble(double d){
        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(d);
	} 
	
	
	
}
