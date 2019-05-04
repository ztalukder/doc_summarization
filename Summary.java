//package edu.nyu.bigdata.summary;

import java.io.*;
import java.util.*;
import java.text.BreakIterator;

public class Summary {

    public static void main(String[] args) throws IOException{
    
        String file;
        if (args.length == 0) {
            file = "doors.txt";
        }
        else {
            file = args[0];
        }
            
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder builder = new StringBuilder();
        String currentLine = reader.readLine();
        while(currentLine != null){
            builder.append(currentLine);
            currentLine = reader.readLine();
        }
        reader.close();

        String str = builder.toString();
        
        // container for starting index of sentences
        ArrayList<Integer> sentences = new ArrayList<Integer>();
        
        BreakIterator sentenceBreak = BreakIterator.getSentenceInstance();
        sentenceBreak.setText(str);
        int start = sentenceBreak.first();
        for (int end = sentenceBreak.next();
                end != sentenceBreak.DONE;
                start = end, end = sentenceBreak.next()) {
            
            sentences.add(start);
        }
        
        int numSentences = sentences.size();
        
        // Map<String, Integer> occurance = new HashMap<String, Integer>();
        Map<String, Integer> sentenceCount = new HashMap<String, Integer>();
        ArrayList<Sentence> theSentences = new ArrayList<Sentence>();
        double averageLength = 0;
        for(int i = 0; i < sentences.size(); i++){
            int begin = sentences.get(i);
            int end = (i == sentences.size() - 1) 
                        ? str.length() : sentences.get(i + 1);

            Sentence currSentence = new Sentence(str.substring(begin, end));
            averageLength += currSentence.getLength();

            for (String word : currSentence.getCurrentSentence()) {
                if (sentenceCount.containsKey(word)) {
                    int count = sentenceCount.get(word) + 1;
                    sentenceCount.put(word, count);
                } else {
                    sentenceCount.put(word, 1);
                }
            }
            theSentences.add(currSentence);
        }
        averageLength /= (double) theSentences.size();



        // List<Map.Entry<String, Integer>> list = new ArrayList<>(occurance.entrySet());
        // list.sort(Map.Entry.comparingByValue());

        // for(Map.Entry<String, Integer> entry : list){
        //     System.out.printf("key: %s; occurences: %d; sentences: %d\n",
        //         entry.getKey(), entry.getValue(),
        //         sentenceCount.get(entry.getKey()));
        // }
        // System.out.printf("Number of sentences: %d\n", numSentences);

        /*
        TODO - replace everything up there with the sentence class and test it
        */
        
        /*
        Call the calculateBM25() function to get sentence rank
        */
        //BM-25 Array
        ArrayList<Double> bm25Array = new ArrayList<Double>();
        for (Sentence s : theSentences){
            bm25Array.add(s.calculateBM25(averageLength, sentenceCount, sentenceCount.size()));
        }
        System.out.println(bm25Array);
        /*
        TODO - create an n x n container where n is the number of sentences
        
        Sentence has a getSimilarity() function
        you are comparing every setence with every other sentence 
        so double for loop and call Sentences[i].similiarFunction(Setnences[j]) and save it in a n by n container
        */
        
        /*
        TODO - apply the page rank algorithm on the sentences
        */
    }

}
