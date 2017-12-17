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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;



public class googleQuery {
    

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0 Chrome/61.0.3163.100 Safari/537.36";
    
   
    public static List<String> getUrls(String userSearch,int offset) throws IOException{
        
        String query =userSearch.replaceAll("\\s+","+");
        List<String> urlList  = new ArrayList<>();
        int i=1;
        String URL;
        if(offset>0){
            URL = "https://google.fr/search?q="+query+"&start="+offset;
        }else{
            URL = "https://google.fr/search?q="+query;
        }
        Document doc = Jsoup.connect(URL).userAgent(USER_AGENT).get();
         for (Element result : doc.select("h3.r  a") ){ 
            
            urlList.add(result.attr("href"));
            
            System.out.println(i+" "+ urlList.get(i-1));
            //ExtractTextUrl(urlList.get(i-1));
            i =i+1;
        
        }
        return urlList;
    }
    
      public static void main(String[] args) throws Exception {
       
        List<String> urlList =new ArrayList<>();
        urlList=getUrls("hunger games",10);
        ExtractTextUrl(urlList.get(0));
        
     }
    
}

