/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.controller;

import com.example.demo.common.Entities;
import com.example.demo.entities.Tearsheetderivedtable;
import com.example.demo.services.MainService;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author alizh
 */
@RestController
public class MainController {
    
    @Autowired
    MainService service;
    
    @GetMapping("/hello")
    public String hello(){
        return "Hello";
    }
    
    
    @GetMapping("/all")
    public List<Entities> getData(){
        return service.getData();
    }
    
    @GetMapping("/fillData")
    public List<Tearsheetderivedtable> fillData() throws ParseException {
    	return service.fillData();
    }
    
    @GetMapping("/fullFill")
    public List<Tearsheetderivedtable> fullFill() throws ParseException {
    	return service.fullFill();
    }
    
//    @GetMapping("/getEpsAnnualizedFullFill")
//    public List<Tearsheetderivedtable> getEpsAnnualizedFullFill() throws ParseException {
//    	return service.getEpsAnnualizedFullFill();
//    }
//    
    @GetMapping("/getEpsAnnualizedFullFill")
    public  List<Map<String, Map<String, String>>> getEpsAnnualizedFullFill() throws ParseException {
    	return service.getEpsAnnualizedFullFill();
    }
    
    @GetMapping("/calculateProfitabilityChange")
    public List<Tearsheetderivedtable> calculateProfitabilityChange() throws ParseException {
    	return service.calculateProfitabilityChange();
    }
    
    
    @GetMapping("/saveTearsheet")
    public String saveTearsheet() throws ParseException {
    	return service.saveTearsheetDerivedTable();
    }
    
    @GetMapping("/getTickers")
    public List<String> getTickers() {
    	return service.getTckLstString();
    }
//    
    
    @GetMapping("/getTckLst")
    public List<Tearsheetderivedtable> getTckLst() {
    	return service.getTckLst();
    }
    
//    @GetMapping("/allTables")
//    public List<String> getAllTables(){
//        return service.getTables();
//    }
    
}
