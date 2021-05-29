package com.neural.datasync.utils;

import java.util.Calendar;
import java.util.Date;

public class TestParserUtil {

	public void runTest() {
		
		Date d = Calendar.getInstance().getTime();
		
		String reply = ParserUtil.getDateAsRFC822String(null);
		
		System.out.println("Date: " + reply);
	}
	
	public static void main(String[] args) {
		TestParserUtil util = new TestParserUtil();
		
		util.runTest();
	}
}
