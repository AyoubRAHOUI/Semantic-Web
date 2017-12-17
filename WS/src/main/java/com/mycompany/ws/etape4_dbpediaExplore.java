/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ws;

/**
 *
 * @author DELL
 */


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;



public class etape4_dbpediaExplore {
    
    /// vérifie s'il s'agit d'un film puis retourne tous les informations d'un film graçe à infoFilm ///
    public static List<String> getInfoFilm(String uri) {
        try {
            if(isFilm(uri)) {
                System.out.println("Film : Yes");
                return infoFilm(uri);
            } else {
               return null;
            }
        } catch(ParseException p){
            return null;
        }
    }
    
    /// vérifie si un URI répresente un film ///
    public static boolean isFilm(String uri){
        String result = "";
        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> select ?type where { ?i rdfs:label \"" + getName(uri) +"\"@pt;a ?type. }";
        System.out.println(query);
        try{
            ParameterizedSparqlString queryString = new ParameterizedSparqlString(query);
            QueryExecution exec = QueryExecutionFactory.sparqlService( "http://dbpedia.org/sparql", queryString.asQuery() );
            ResultSet results = ResultSetFactory.copyResults(exec.execSelect());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            //Turn result into a String
            result = new String(outputStream.toByteArray());
            //System.out.println(result);
        } catch(QueryException q) {
            System.out.println("Erreur" + q);
        }
        
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(result);
            JSONArray array = ((JSONArray)((JSONObject)json.get("results")).get("bindings"));
            List<String> valueList = new ArrayList<>();		
            for(int i=0; i<array.size();i++){
                JSONObject jsonObject = (JSONObject) array.get(i); 
                JSONObject typeObj = (JSONObject)jsonObject.get("type");
		String value = typeObj.get("value").toString();
		valueList.add(value); 
            }   
            for (String temp : valueList) {
		if (temp.equals("http://dbpedia.org/ontology/Film")) {
                    return true;
		}
	    }
            return false; 
        } catch (Exception e) {
            return false;
        }
    }
    public static List<String> infoFilm(String uri) throws ParseException {
        List<String> infoFilm = new ArrayList<>();
        String nameFilm = getName(uri);
        nameFilm = nameFilm.replace(' ','_');
        String queryAbstract = "PREFIX db: <http://dbpedia.org/resource/>\n" +
                               "PREFIX dbo: <http://dbpedia.org/ontology/>\n" + 
                               "SELECT ?value WHERE {db:"+nameFilm+" dbo:abstract ?value FILTER (lang(?value) = 'en')}";
        String queryDirector = "PREFIX db: <http://dbpedia.org/resource/>\n" +
                               "PREFIX dbo: <http://dbpedia.org/ontology/>\n" + 
                               "SELECT ?value WHERE {db:"+nameFilm+" dbo:director ?value}";
        String queryCountry = "PREFIX db: <http://dbpedia.org/resource/>\n" +
                              "PREFIX dbp: <http://dbpedia.org/property/>\n" + 
                              "SELECT ?value WHERE {db:"+nameFilm+" dbp:country ?value}";
        String queryLanguage = "PREFIX db: <http://dbpedia.org/resource/>\n" +
                               "PREFIX dbp: <http://dbpedia.org/property/>\n" + 
                               "SELECT ?value WHERE {db:"+nameFilm+" dbp:language ?value}";
        String queryStudio = "PREFIX db: <http://dbpedia.org/resource/>\n" +
                             "PREFIX dbp: <http://dbpedia.org/property/>\n" + 
                             "SELECT ?value WHERE {db:"+nameFilm+" dbp:studio ?value}";
        
        infoFilm.add(requestResult(queryAbstract));
        infoFilm.add(requestResult(queryDirector));
        infoFilm.add(requestResult(queryCountry));
        infoFilm.add(requestResult(queryLanguage));
        infoFilm.add(requestResult(queryStudio));
        return infoFilm;
    }
    public static String requestResult(String query) throws ParseException {
        String result = "";
        String r ="";
        try{
        ParameterizedSparqlString queryString = new ParameterizedSparqlString(query);
        QueryExecution exec = QueryExecutionFactory.sparqlService( "http://dbpedia.org/sparql", queryString.asQuery() );
        ResultSet results = ResultSetFactory.copyResults(exec.execSelect());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        //Turn result into a String
        result = new String(outputStream.toByteArray());
        } catch(QueryException q) {
            System.out.println("Erreur" + q);
        }
        
        //Select the value URI into the result json
        if(result.equals("")) {
            return result;
        } else {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result);
        JSONArray array = ((JSONArray)((JSONObject)json.get("results")).get("bindings"));
        for(int i=0; i<array.size();i++){
            JSONObject jo = (JSONObject)((JSONObject)array.get(i));
            r = r + jo.toString() + "\n";
        }
        return r;  
        }
    }
    
    public static String getName(String uri){
        //System.out.println("uri = "+uri);
        if (uri.length()>=28){
            String subStr = uri.substring(28);
            String resourceName = subStr.replace('_',' ');
            return resourceName;
        }else{
            String resourceName = uri.replace('_',' ');
            return resourceName;
        }
        
    }
    
    
    public static void main(String [] args) throws MalformedURLException, IOException, Exception {
        
        String s = "http://dbpedia.org/resource/The_Hunger_Games";
        System.out.println(getName(s));
        List<String> s1 = getInfoFilm(s);
        System.out.println(s1);

    }
}
