/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ws_h4202;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 *
 * @author DELL
 */
public class textExtractor {

    private static NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
            NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
            "1dfadf0e-f20e-41f2-a58a-225c2fc682ae",
            "J8jyS4y20zyq");
    public static int number =0;

    private static EntitiesOptions entities = new EntitiesOptions.Builder().limit(1).sentiment(true).build();
    private static ConceptsOptions concepts = new ConceptsOptions.Builder().limit(5).build();

    public static String ExtractTextUrl(String url) {
        System.out.println("Request to watson on " + url);
        Features features = new Features.Builder().concepts(concepts).build();
        AnalyzeOptions parameters
                = new AnalyzeOptions.Builder()
                        .url(url)
                        .features(features)
                        .returnAnalyzedText(true)
                        .language("ENGLISH")
                        .build();

        AnalysisResults results;
        results = service.analyze(parameters).execute();
        System.out.println(results.getAnalyzedText());
        
        try{ 
          enregistrerResultat(results.getAnalyzedText());
        }catch(IOException e){
         e.printStackTrace();
        }
        return results.getAnalyzedText();

    }
    public static int numb (){
       number++;
       return number;
    }
    public static String chargerResultat(int i) throws IOException, ClassNotFoundException {
         FileInputStream fileInput = new FileInputStream("Result "+i);
         ObjectInputStream objectInput = new ObjectInputStream(fileInput);
         String contenu = (String) objectInput.readObject();
         objectInput.close();
         return contenu;
     }
       public static void enregistrerResultat(String text) throws IOException{
         int i= numb();
         BufferedWriter bufferedWriter = new BufferedWriter (new FileWriter( "Result "+i ));
         FileOutputStream fileOutput = new FileOutputStream("Result "+i);
         ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);
         objectOutput.writeObject(text);
         objectOutput.close();
      }

}
