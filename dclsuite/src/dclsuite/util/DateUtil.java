package dclsuite.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	private DateUtil(){}
	
	public static String fullPattern = "yyyy/MMM/dd HH:mm:ss.SSS";
	
	public static String dateToStr(Date date, String pattern){
		DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}
	
}