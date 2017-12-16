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
import static com.mycompany.ws_h4202.textExtractor.ExtractTextUrl;
import java.net.URI;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class googleQuery {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0 Chrome/61.0.3163.100 Safari/537.36";

    public static void save(String txt) throws IOException {
        try (FileWriter file = new FileWriter("Liens.txt")) {
			file.write(txt);
			System.out.println("Successfully Copied to File");
		}
    }

    public static List<String> getLinks(String query, int offset) throws Exception {
        String API_KEY = "AIzaSyDB21zsab_XJO8Vb8XZFGJYl8kuak-TGa4";
        String charset = "UTF-8";

        StringBuilder result = new StringBuilder();
        URL url;

        if (offset > 0) {
            url = new URL("https://www.googleapis.com/customsearch/v1?key=" + API_KEY + "&cx=013036536707430787589:_pqjad5hr1a&q=" + URLEncoder.encode(query, charset) + "&hl=en&alt=json&start=" + offset);
        } else {
            url = new URL("https://www.googleapis.com/customsearch/v1?key=" + API_KEY + "&cx=013036536707430787589:_pqjad5hr1a&q=" + URLEncoder.encode(query, charset) + "&hl=en&alt=json");
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result.toString());
        JSONArray jArray = (JSONArray) json.get("items");

        List<String> urlList = new ArrayList<>();

        Iterator i = jArray.iterator();
        String s = "";
        while (i.hasNext()) {
            JSONObject res = (JSONObject) i.next();
            String link = (String) res.get("link");
            
            s += "\n" + link;
            
            System.out.println(link);
            urlList.add(link);
        }
        save(s);
        return urlList;
    }
    
    public static String getDBpedia(String query, double confidence, int support) throws Exception {

        StringBuilder result = new StringBuilder(); 
        URL url;
        URI uri = new URI(
        "http", 
        "model.dbpedia-spotlight.org", 
        "/en/annotate",
        "text=" + query + "&confidence=" + confidence + "&support=" + support,
        null);
        String request = uri.toASCIIString();
        
        url = new URL(request);
        
        System.out.println(url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();

        System.out.println(result.toString());
        
        return result.toString();
    }

    public static List<String> getUrls(String userSearch, int offset) throws IOException {

        String query = userSearch.replaceAll("\\s+", "+");
        List<String> urlList = new ArrayList<>();
        int i = 1;
        String URL;
        if (offset > 0) {
            URL = "https://google.com/search?q=" + query + "&start=" + offset;
        } else {
            URL = "https://google.com/search?q=" + query;
        }
        Document doc = Jsoup.connect(URL).userAgent(USER_AGENT).get();
        for (Element result : doc.select("h3.r  a")) {

            urlList.add(result.attr("href"));

            System.out.println(i + " " + urlList.get(i - 1));
            //ExtractTextUrl(urlList.get(i-1));
            i = i + 1;

        }
        return urlList;
    }

    public static void main(String[] args) throws Exception {

        List<String> urlList = new ArrayList<>();

        /* getLinks et getUrls font la même chose, mais de deux manières différentes */
        urlList = getLinks("hunger games", 10);
        //urlList = getUrls("hunger games", 10);
        System.out.println(">>>-------------------------------------<<<");
//        for (int i = 0; i < urlList.size(); i++) {
//            ExtractTextUrl(urlList.get(i));
//            System.out.println(">---------------------------<");
//        }
        String res =ExtractTextUrl(urlList.get(2));
        
        getDBpedia(res, 0.2, 20);

    }

}
