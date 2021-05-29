package com.neural.datasync.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.openadaptor.util.DateTimeHolder;

public class ParserUtil {
	
	private ParserUtil() { }
	
	public static String getISO8601Date(Date date) {
		if(date == null) {
			return "";
		}
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String d = parser.format(date);
		return d.replace(" ", "T");
	}
	

	public static String getDateAsRFC822String(Date date) {
		if(date == null) {
			return "";
		}
		SimpleDateFormat parser = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z");
		return parser.format(date);
	}

	public static Date getDateFromBO(Object dat) {
	 	if(dat != null) {
			if(dat instanceof Date) {
				 return (Date)dat;
				
			} else if ( dat instanceof DateTimeHolder ) {
				DateTimeHolder expiresDate = (DateTimeHolder)dat;
				if(expiresDate != null) {
					Calendar cal = Calendar.getInstance();
					cal.set(expiresDate.getTrueYear(), expiresDate.getMonth(), 
							expiresDate.getDate(), expiresDate.getHours(), 
							expiresDate.getMinutes(), expiresDate.getSeconds());
					
					return cal.getTime();
				}
			}	   	 		
	 	}
	 	
	 	return null;
	}
}
