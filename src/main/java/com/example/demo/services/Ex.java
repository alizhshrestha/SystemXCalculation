package com.example.demo.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class Ex {
	public static void main(String[] args) {
//		LocalDate today = LocalDate.now();
////        System.out.println("Current date: " + today);
//
//        //minus 52 week to the current date
//        LocalDate last52weeks = today.minus(50, ChronoUnit.WEEKS);
//        
//        LocalDate addWeeks = today.plus(5, ChronoUnit.WEEKS);
//        
//        LocalDate last60weeks = today.minus(52, ChronoUnit.WEEKS);
////        System.out.println("last 52 week: " + last52weeks);
//        System.out.println(last52weeks.compareTo(addWeeks));
//        
//        
//        
        System.out.println(today()); 
        
	}
	
	public static Date today() {
		final Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.DATE, -1);
		return cal.getTime();
	}
}
