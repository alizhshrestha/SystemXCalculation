package com.example.demo.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class TimeDemo {

	public static Date yesterday() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return cal.getTime();
	}

	public static String getYesterdayDateString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		return dateFormat.format(yesterday());
	}
	
	public static void main(String[] args) {
//		System.out.println(getYesterdayDateString());
		for(int i = 0; i < 5; i++) {
			outerloop:
			for(int j = 5; j < 10; j++) {
				if(j==8) {
					break outerloop;
				}else {
					System.out.println(j);
				}
			}
		}
	}
}
