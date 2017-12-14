/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ws_h4202;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.Text;


/**
 *
 * @author ASUS
 */
public abstract class AnnotationClient {
    class AnalyseResult {
        public String url;
        public double score;
        AnalyseResult(String s,double d){
            url = s;
            score = d;
        }
    }
    
    public Logger LOG = Logger.getLogger(this.getClass());
    
    // Create an instance of HttpClient.
    private static HttpClient client = new HttpClient();
    public String request(HttpMethod method) throws AnnotationException{
        String response = null;
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler(3, false));
        try{
            int statusCode = client.executeMethod(method);
            if(statusCode!= HttpStatus.SC_OK){
                LOG.error("Method failed: " + method.getStatusLine());
            }
            byte[] responseBody = method.getResponseBody();
            response = new String(responseBody);
        } catch(HttpException e){
            LOG.error("Fatal protocol violation: " + e.getMessage());
            throw new AnnotationException("Protocol error executing HTTP request.",e);
        } catch (IOException e){
            LOG.error("Fatal transport error: " + e.getMessage());
            LOG.error(method.getQueryString());
            throw new AnnotationException("Transport error executing HTTP request.",e);
        } finally{
            method.releaseConnection();
        }
        return response;
    }
    
    protected static String readFileAsString(String filePath) throws java.io.IOException{
        return readFileAsString(new File(filePath));
    }
    
    protected static String readFileAsString(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
        f.read(buffer);
        return new String(buffer);
    }
    
    static abstract class LineParser {

        public abstract String parse(String s) throws ParseException;

        static class ManualDatasetLineParser extends LineParser {
            public String parse(String s) throws ParseException {
                return s.trim();
            }
        }

        static class OccTSVLineParser extends LineParser {
            public String parse(String s) throws ParseException {
                String result = s;
                try {
                    result = s.trim().split("\t")[3];
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new ParseException(e.getMessage(), 3);
                }
                return result; 
            }
        }
    }
   
    public List<AnalyseResult> saveExtractedEntitiesSet(File inputFile, LineParser parser, int restartFrom) throws Exception{
        //PrintWriter out = new PrintWriter(outputFile);
        LOG.info("Opening input file "+inputFile.getAbsolutePath());
        String text = readFileAsString(inputFile);
        int i=0;
        int correct =0 ;
        int error = 0;
        int sum = 0;
        List<AnalyseResult> listUri = new ArrayList<>();
        for (String snippet: text.split("\n")) {
            String s = parser.parse(snippet);
            //System.out.println(s);
            if (s!= null && !s.equals("")) {
                i++;
                if (i<restartFrom) continue;
                List<DBpediaResource> entities = new ArrayList<DBpediaResource>();
                try {
                    final long startTime = System.nanoTime();
                    entities = extract(new Text(snippet.replaceAll("\\s+"," ")));
                    //System.out.println(entities);
                    final long endTime = System.nanoTime();
                    sum += endTime - startTime;
                    LOG.info(String.format("(%s) Extraction ran in %s ns.", i, endTime - startTime));
                    correct++;
                } catch (AnnotationException e) {
                    error++;
                    LOG.error(e);
                    e.printStackTrace();
                }
                for (DBpediaResource e: entities) {
                    //System.out.println(e);
                    //out.println("http://dbpedia.org/resource/"+e.uri());
                    //AnalyseResult r = new AnalyseResult("http://dbpedia.org/resource/"+e.uri(),e.prior());
                    AnalyseResult r = new AnalyseResult(e.uri(),e.prior());
                    listUri.add(r);
                }
                removeDuplication(listUri);
                //out.println();
                //out.flush();
            }
        }
        //out.close();
        LOG.info(String.format("Extracted entities from %s text items, with %s successes and %s errors.", i, correct, error));
        //LOG.info("Results saved to: "+outputFile.getAbsolutePath());
        double avg = (new Double(sum) / i);
        LOG.info(String.format("Average extraction time: %s ms", avg * 1000000));
        return listUri;
    }
    
    public static void removeDuplication(List<AnalyseResult> a){
        for (int i=0;i<a.size()-1;i++){
            for (int j=a.size()-1;j>i;j--){
                if (a.get(j).url.equals(a.get(i).url)){
                    a.remove(j);
                }
            }
        }
    }
    /*
    public List<AnalyseResult> saveExtractedEntitiesSet(String inputString, LineParser parser, int restartFrom) throws Exception{
        //LOG.info("Opening input file "+inputFile.getAbsolutePath());
        String text = inputString;
        int i=0;
        int correct =0 ;
        int error = 0;
        int sum = 0;
        List<AnalyseResult> listUri = new ArrayList<>();
        for (String snippet: text.split("\n")) {
            String s = parser.parse(snippet);
            if (s!= null && !s.equals("")) {
                i++;
                if (i<restartFrom) continue;
                List<DBpediaResource> entities = new ArrayList<DBpediaResource>();
                try {
                    final long startTime = System.nanoTime();
                    entities = extract(new Text(snippet.replaceAll("\\s+"," ")));
                    //System.out.println(entities);
                    final long endTime = System.nanoTime();
                    sum += endTime - startTime;
                    LOG.info(String.format("(%s) Extraction ran in %s ns.", i, endTime - startTime));
                    correct++;
                } catch (AnnotationException e) {
                    error++;
                    LOG.error(e);
                    e.printStackTrace();
                }
                for (DBpediaResource e: entities) {
                    //System.out.println(e);
                    //listUri.add("http://dbpedia.org/resource/"+e.uri().substring(31));
                    AnalyseResult r = new AnalyseResult("http://dbpedia.org/resource/"+e.uri(),e.prior());
                    listUri.add(r);
                }
                removeDuplication(listUri);
            }
        }
        
        LOG.info(String.format("Extracted entities from %s text items, with %s successes and %s errors.", i, correct, error));
        double avg = (new Double(sum) / i);
        LOG.info(String.format("Average extraction time: %s ms", avg * 1000000));
        return listUri;
    }
    */
    
    public List<AnalyseResult> evaluate(File input) throws Exception {
        return saveExtractedEntitiesSet(input,new LineParser.ManualDatasetLineParser(),0);
    }
    /*public void evaluateManual(File inputFile, File outputFile, int restartFrom) throws Exception {
         saveExtractedEntitiesSet(inputFile, outputFile, new LineParser.ManualDatasetLineParser(), restartFrom);
    }*/
    public abstract List<DBpediaResource> extract(Text text) throws AnnotationException;

    
}
