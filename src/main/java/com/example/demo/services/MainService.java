/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.services;

import com.example.demo.LiveTradingCalcApplication;
import com.example.demo.common.Entities;
import com.example.demo.entities.Tearsheetderivedtable;
import com.example.demo.repositories.TearsheetderivedtableRepository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MainService {

	// Filtered map list of latestpricelive table
	List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();

	@Autowired
	TearsheetderivedtableRepository repo;

	// All data from database to be computed
	public List<Entities> getData() {
		return LiveTradingCalcApplication.allDataMap;
	}

	public List<String> getTckLstString() {
		List<Entities> allDataList = getData();
		List<String> tcklst = new ArrayList<String>();
		int size = 0;

		for (Entities e : allDataList) {
			if (e.getTablename().equals("stocksymbolsforsearchbox")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					tcklst.add(e.getRows().get(i).get("StockSymbol"));
				}
				size++;
			}

		}

//		System.out.println("Ticker List size: " + size);

		return tcklst;
	}

	public List<Tearsheetderivedtable> getTckLst() {
		List<Entities> allDataList = getData();
		List<String> tcklst = getTckLstString();

		List<Tearsheetderivedtable> trshtlst = new ArrayList<Tearsheetderivedtable>();
		int size = 0;

		for (String s : tcklst) {
			size++;
			Tearsheetderivedtable trshttbl = new Tearsheetderivedtable();
			trshttbl.setTicker(s);
			trshtlst.add(trshttbl);
		}

//		System.out.println("Ticker List size: " + size);

		return trshtlst;
	}

	public List<Tearsheetderivedtable> fillData() throws ParseException {
		Tearsheetderivedtable tearsheet = new Tearsheetderivedtable();
		List<Tearsheetderivedtable> tearsheetList = getTckLst();

		String tradingDate = "";

		List<Entities> allDataList = getData();

		Map<String, String> result = new HashMap<String, String>();

		Date latest_date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// Boolean Flags:
		boolean hasVolume = false;
		boolean hasAmount = false;
		boolean hasWeightedAvePrice = false;
		boolean hasTicker = false;
		boolean hasTimestamp = false;

		double amount = 0.0;
		double volume = 0.0;
		Double WeightedAvePrice = 0.0;

		boolean isSSFIL = false;
		int tkcount = 0;

		for (Entities e : allDataList) {
			if (e.getTablename().equals("latestprice")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("TICKER"))) {
							tkcount++;
							amount = Double.valueOf(e.getRows().get(i).get("amount"));
							volume = Double.valueOf(e.getRows().get(i).get("volume"));
//							tearsht.setSector(e.getRows().get(i).get("SmtmSector"));
							tearsht.setVolume(volume);
							tradingDate = e.getRows().get(i).get("TRADING_DATE");
							Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(tradingDate);
//							System.out.println(tearsht.getTicker() +" Trading date: " + e.getRows().get(i).get("TRADING_DATE"));
							tearsht.setTradingDate(date1);

							WeightedAvePrice = amount / volume;
							if (WeightedAvePrice.isNaN()) {
								tearsht.setWeightedAvePrice(null);
							} else {
								tearsht.setWeightedAvePrice(WeightedAvePrice);
							}
						}

					}
				}

			}
		}
		System.out.println("Ticker total count: " + tkcount);
		return tearsheetList;
	}

	public List<Tearsheetderivedtable> fullFill() throws ParseException {
		List<Tearsheetderivedtable> tearsheetList = fillData();

		List<Entities> allDataList = getData();

		// For 180 days trading date calculattion
		int dtcount = 1; // for previous dates calculation
		int averagecount = 0; // for average calculation
		Double averageClosingPrice = 0.0;

		// date of now
		Date latest_date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		boolean isLatest = false;

		// filtering data according to table names
		for (Entities e : allDataList) {
			if (e.getTablename().equals("stocksymbolsforsearchbox")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("StockSymbol"))) {
							tearsht.setSector(e.getRows().get(i).get("SmtmSector"));
						}

					}
				}

			}

			if (e.getTablename().equals("tearsheet_eps")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("ticker"))) {
							if (e.getRows().get(i).get("eps_f") != null) {
								tearsht.setEps(Double.valueOf(e.getRows().get(i).get("eps_f")));
							} else {
								tearsht.setEps(null);
							}

						}
					}
				}
			}

			if (e.getTablename().equals("tearsheet_pe")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("ticker"))) {
							if (e.getRows().get(i).get("pe_f") != null) {
								tearsht.setPe(Double.valueOf(e.getRows().get(i).get("pe_f")));
							} else {
								tearsht.setPe(null);
							}

						}
					}
				}
			}
//			
			if (e.getTablename().equals("tearsheet_pbv")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("ticker"))) {

							if (e.getRows().get(i).get("pbv") != null) {
								tearsht.setPbv(Double.valueOf(e.getRows().get(i).get("pbv")));
							} else {
								tearsht.setPbv(null);
							}

						}
					}
				}
			}
//			
			if (e.getTablename().equals("tearsheet_roe")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("ticker"))) {
							if (e.getRows().get(i).get("roe_f") != null) {
								tearsht.setRoe(Double.valueOf(e.getRows().get(i).get("roe_f")));
							} else {
								tearsht.setRoe(null);
							}

						}
					}
				}
			}
//			
			if (e.getTablename().equals("tearsheet_roa")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("ticker"))) {
							if (e.getRows().get(i).get("roa_f") != null) {
								tearsht.setRoa(Double.valueOf(e.getRows().get(i).get("roa_f")));
							} else {
								tearsht.setRoa(null);
							}

						}
					}
				}
			}
			
			if (e.getTablename().equals("tearsheetpercentileindicatortable")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("TICKER"))) {
							if (e.getRows().get(i).get("DaysHighLow") != null) {
//								tearsht.setRoa(Double.valueOf(e.getRows().get(i).get("roa_f")));
								tearsht.setDaysHighLowPercentile(Double.valueOf(e.getRows().get(i).get("DaysHighLow")));
							} else {
								tearsht.setDaysHighLowPercentile(null);
							}
							
							if (e.getRows().get(i).get("FiftyTwoWeekHighLow") != null) {
//								tearsht.setRoa(Double.valueOf(e.getRows().get(i).get("roa_f")));
								tearsht.setFiftyTwoHighLowPercentile(Double.valueOf(e.getRows().get(i).get("FiftyTwoWeekHighLow")));
							} else {
								tearsht.setFiftyTwoHighLowPercentile(null);
							}

						}
					}
				}
			}
			
			
			
			
			
			
			if (e.getTablename().equals("stock_price_actions_derived")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("ticker"))) {
							if (e.getRows().get(i).get("fifty_two_week_high") != null) {
//								tearsht.setRoa(Double.valueOf(e.getRows().get(i).get("roa_f")));
								tearsht.setFiftyHigh(Double.valueOf(e.getRows().get(i).get("fifty_two_week_high")));
							} else {
								tearsht.setFiftyHigh(null);
							}
							
							if (e.getRows().get(i).get("fifty_two_week_low") != null) {
//								tearsht.setRoa(Double.valueOf(e.getRows().get(i).get("roa_f")));
								tearsht.setFiftyLow(Double.valueOf(e.getRows().get(i).get("fifty_two_week_low")));
							} else {
								tearsht.setFiftyLow(null);
							}

						}
					}
				}
			}

			if (e.getTablename().equals("latestprice")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("TICKER"))) {
							if (e.getRows().get(i).get("high") != null) {
								tearsht.setDaysHigh(Double.valueOf(e.getRows().get(i).get("high")));
							} else {
								tearsht.setDaysHigh(null);
							}

							if (e.getRows().get(i).get("low") != null) {
								tearsht.setDaysLow(Double.valueOf(e.getRows().get(i).get("low")));
							} else {
								tearsht.setDaysLow(null);
							}

						}

					}
				}
			}

			if (e.getTablename().equals("stock_data")) {

				for (Tearsheetderivedtable tearsht : tearsheetList) {
					double totalClosingPrice = 0.0;
					double closingPrice = 0.0;
					long days = 1;

					String currentTicker = "";

					// for getting out of loop
					outerloop: for (int i = 0; i < e.getRows().size(); i++) {
						if (e.getRows().get(i).get("ticker").equals(tearsht.getTicker())) {
							Date actual_date = dateFormat.parse(e.getRows().get(i).get("trading_date")); // parsing
																											// string to
																											// date

							// parsing string to date
							String tradeLatestDatestr = dateFormat.format(actual_date);
							Date tradeLatestDate = dateFormat.parse(tradeLatestDatestr);

							String LatestDatestr = dateFormat.format(latest_date);
							Date LatestDate = dateFormat.parse("2021-06-16");
							String strDate = dateFormat.format(latest_date);
							Date previousDate = getPreviousDate(strDate, dtcount);
							dtcount++;

							days = getDifferenceDays(tradeLatestDate, LatestDate);

							// checking if ticker has latest date
							if (tradeLatestDate.equals(LatestDate)) {
								isLatest = true; // setting flag
								currentTicker = e.getRows().get(i).get("ticker"); // current ticker from loop
							}

							if (isLatest && currentTicker.equals(e.getRows().get(i).get("ticker"))) {
								if (days < 180) {
									closingPrice = Double.valueOf(e.getRows().get(i).get("closingPrice"));
//											+ closingPrice);
									totalClosingPrice += closingPrice;
									averagecount++;

								} else {
									dtcount = 1;
									days = 0;
									break outerloop;
								}
							} else {
								averagecount = 0;
								totalClosingPrice = 0;
								isLatest = false;
							}

						}
					}
					averageClosingPrice = totalClosingPrice / averagecount;
					if (averageClosingPrice.isNaN()) {
						averageClosingPrice = 0.0;
					}

					tearsht.setOneEightyDayAverage(averageClosingPrice);

					totalClosingPrice = 0.0;

					averagecount = 0;

				}

			}

		}
		return tearsheetList;

	}

	public List<Map<String, Map<String, String>>> getEpsAnnualizedFullFill() throws ParseException {
		List<Tearsheetderivedtable> tearsheetList = fullFill();
		Map<String, String> epsdetailMap = new HashMap<String, String>();
		Map<String, Map<String, String>> epsMap = new HashMap<String, Map<String, String>>();
		List<Map<String, Map<String, String>>> epsMapList = new ArrayList<Map<String, Map<String, String>>>();

		int count = 0, size = 0, quater = 0, previousquater = 0;
		String year = null, latestyear = null;
		List<Entities> allDataList = getData();
		Double epsAnnualized = 0.0, previousepsAnnualized = 0.0, profitabilityChange = 0.0, reportedPeAnnualized = 0.0;

		String ticker = "", table = "";
		int position = 0;

		for (Tearsheetderivedtable tearsheet : tearsheetList) {
			ticker = tearsheet.getTicker();
			String sectorKeyStatsTableName = getSector(tearsheet.getSector());
			outerloop: for (Entities e : allDataList) {
				if (e.getTablename().equals(sectorKeyStatsTableName)) {
					table = e.getTablename();
					for (int i = 0; i < e.getRows().size(); i++) {
						if (tearsheet.getTicker().equals(e.getRows().get(i).get("Ticker"))) {
							if (e.getRows().get(i).get("EpsAnnualized") != null) {
								quater = Integer.valueOf(e.getRows().get(i).get("Quarter"));
								year = e.getRows().get(i).get("Year");
								epsAnnualized = Double.valueOf(e.getRows().get(i).get("EpsAnnualized"));
							}

							if (e.getRows().get(i).get("ReportedPeAnnualized") != null) {
								reportedPeAnnualized = Double.valueOf(e.getRows().get(i).get("ReportedPeAnnualized"));
							}

							if (count == 0) {
//								epsMap.put("ticker", ticker);
								epsdetailMap.put("quater", String.valueOf(quater));
								epsdetailMap.put("year", year);
								epsdetailMap.put("epsAnnualized", String.valueOf(epsAnnualized));
								epsdetailMap.put("reportedPeAnnualized", String.valueOf(reportedPeAnnualized));

								epsMap.put(ticker, epsdetailMap);

							} else if (count == 1) {
								epsdetailMap.put("previous_quater", String.valueOf(quater));
								epsdetailMap.put("previous_quater_eps", String.valueOf(epsAnnualized));
								epsMapList.add(epsMap);
							} else {
								epsdetailMap = new HashMap<String, String>();
								epsMap = new HashMap<String, Map<String, String>>();
								count = 0;
								break outerloop;
							}
							count++;
						}
					}

				}
			}

			System.out.println(table);
		}

		System.out.println("EpsMapList: " + epsMapList.size());
		return epsMapList;

	}

	public List<Tearsheetderivedtable> calculateProfitabilityChange() throws ParseException {
		List<Entities> allDataList = getData();
		List<Map<String, Map<String, String>>> epsMapList = getEpsAnnualizedFullFill();
		Map<String, String> epsMapDetails = new HashMap<String, String>();
		List<Tearsheetderivedtable> tearsheetList = fullFill();

		String ticker = "";
		Double epsAnnualized = 0.0, previousepsAnnualized = 0.0, profitabilityChange = 0.0, pe_d = 0.0, reportedPeAnnualized = 0.0, sentimentChange= 0.0;

		for (Tearsheetderivedtable tearsheet : tearsheetList) {
			for (Map<String, Map<String, String>> tm : epsMapList) {
				for (Map.Entry<String, Map<String, String>> entry : tm.entrySet()) {
					if (entry.getKey().equals(tearsheet.getTicker())) {
						epsMapDetails = entry.getValue();
						if (epsMapDetails.get("epsAnnualized") != null) {
							epsAnnualized = Double.valueOf(epsMapDetails.get("epsAnnualized"));
						}

						if (epsMapDetails.get("previous_quater_eps") != null) {
							previousepsAnnualized = Double.valueOf(epsMapDetails.get("previous_quater_eps"));
						}

						if(epsMapDetails.get("epsAnnualized") != null && epsMapDetails.get("previous_quater_eps") != null) {
							profitabilityChange = (epsAnnualized / previousepsAnnualized) - 1;
							tearsheet.setProfitabilityChange(profitabilityChange);
						}else if(epsMapDetails.get("epsAnnualized") == null || epsMapDetails.get("previous_quater_eps") == null) {
							profitabilityChange = null;
							tearsheet.setProfitabilityChange(profitabilityChange);
						}
						
						

					}

				}
			}
		}

		for (Entities e : allDataList) {
			if (e.getTablename().equals("tearsheet_pe")) {
				for (int i = 0; i < e.getRows().size(); i++) {
					for (Tearsheetderivedtable tearsht : tearsheetList) {
						if (tearsht.getTicker().equals(e.getRows().get(i).get("ticker"))) {
							if (e.getRows().get(i).get("pe_D") != null) {
								pe_d = Double.valueOf(e.getRows().get(i).get("pe_D"));

								for (Map<String, Map<String, String>> tm : epsMapList) {
									for (Map.Entry<String, Map<String, String>> entry : tm.entrySet()) {
										if (entry.getKey().equals(tearsht.getTicker())) {
											epsMapDetails = entry.getValue();
											
											if (epsMapDetails.get("reportedPeAnnualized") != null) {
												reportedPeAnnualized = Double
														.valueOf(epsMapDetails.get("reportedPeAnnualized"));
												sentimentChange = (pe_d / reportedPeAnnualized)-1;
												tearsht.setSentimentChange(sentimentChange);
											}else {
												sentimentChange = null;
												tearsht.setSentimentChange(sentimentChange);
											}
										}
									}
								}

							} else {
								sentimentChange = null;
								tearsht.setSentimentChange(sentimentChange);
							}

						}
					}
				}
			}
		}

		return tearsheetList;
	}

	public String getSector(String sector) throws ParseException {
		switch (sector) {
		case "Commercial Banks":
			return "cbkeystats";
		case "Hydro Power":
			return "hpkeystats";
		case "Life Insurance":
			return "lifekeystats";
		case "Manufacturing And Processing":
			return "mpkeystats";
		case "Finance":
			return "financekeystats";
		case "Microcredit":
			return "mfikeystats";
		case "Organized Fund":
			return "ofkeystats";
		case "Non Life Insurance":
			return "nonlifekeystats";
		case "Development Bank":
			return "dbkeystats";
		case "Telecom":
			return "telkeystats";
		case "Hotels":
			return "hotelkeystats";
		default:
			return null;
		}

	}

	// save data to TearsheetDerivedTable
	public String saveTearsheetDerivedTable() throws ParseException {
		List<Tearsheetderivedtable> trshtlst = new ArrayList<Tearsheetderivedtable>();
		if (trshtlst.size() == 0) {
			trshtlst = fullFill();
		}
		for (Tearsheetderivedtable t : trshtlst) {
			repo.save(t);
		}
		return "Saved Successfully";
	}

	// previous day calculation
	public Date getPreviousDate(String str, int k) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = sdf.parse(str);
		Calendar cal = Calendar.getInstance();
//	    subtracting a day
		cal.setTime(date);
		cal.add(cal.DATE, -k);

		return cal.getTime();
	}

	public Date yesterday() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -2);
		return cal.getTime();
	}

	public String getYesterdayDateString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		return dateFormat.format(yesterday());
	}

	// calculating difference between dates
	public static long getDifferenceDays(Date d1, Date d2) {
		long diff = d2.getTime() - d1.getTime();
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}
}
