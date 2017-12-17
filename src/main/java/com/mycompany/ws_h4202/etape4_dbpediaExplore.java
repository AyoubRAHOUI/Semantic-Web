/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ws_h4202;

/**
 *
 * @author DELL
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;



public class etape4_dbpediaExplore {
    
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0 Chrome/61.0.3163.100 Safari/537.36";

    
    /// vérifie s'il s'agit d'un film puis retourne tous les informations d'un film graçe à infoFilm ///
    public static List<String> getInfoFilm(String uri) {
        String title = null;
        try {
            title = isFilm2(uri);
            System.out.println("Titre : " + title);
        } catch (IOException ex) {
            Logger.getLogger(etape4_dbpediaExplore.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(title != null) {
            try {
                return infoFilm(uri, title);
            } catch (ParseException ex) {
                Logger.getLogger(etape4_dbpediaExplore.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
           return null;
        }
        return null;
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
    
    //retourne le nom du filme si oui, null si non.
    public static String isFilm2(String query) throws IOException{

        String URL = query;
        boolean film = false;
        Document doc = Jsoup.connect(URL).userAgent(USER_AGENT).get();
        
        for (Element result : doc.select(":containsOwn(An Entity of Type :)")) {
            if (result.toString().contains("http://dbpedia.org/ontology/Film")){
                film = true;
            }
        }        
        if(film){
           for (Element span : doc.select("[property=\"foaf:name\"]") ){
               return span.text();
           }
        }
        return null;
    }
    
    public static List<String> infoFilm(String uri, String nameFilm) throws ParseException {
        List<String> infoFilm = new ArrayList<>();
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
        
        String s = "http://dbpedia.org/resource/The_Shawshank_Redemption";

        List<String> s1 = getInfoFilm(s);
        System.out.println(s1);

    }
}