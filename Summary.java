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
        
        Map<String, Integer> occurance = new HashMap<String, Integer>();
        Map<String, Integer> sentenceCount = new HashMap<String, Integer>();
        
        String[] words;
        for(int i = 0; i < sentences.size(); i++){
            int begin = sentences.get(i);
            int end = (i == sentences.size() - 1) 
                        ? str.length() : sentences.get(i + 1);
            String singleSentence = str.substring(begin, end);
            
            // TODO: there's a better way to do this right?
            ArrayList<String> currentSentence = new ArrayList<String>();
                
            // remove punctuation and convert to lowercase and then split
            // on white spaces
            words = singleSentence.replaceAll("[^a-zA-Z ]", "")
                    .toLowerCase().split("\\s+");
            for(String singleWord: words){
                if(occurance.containsKey(singleWord)){
                    int count = occurance.get(singleWord) + 1;
                    occurance.put(singleWord, count);
                } else {
                    occurance.put(singleWord, 1);
                }
                
                if (!currentSentence.contains(singleWord)) {
                    currentSentence.add(singleWord);
                }
            }
            
            for (String word : currentSentence) {
                if (sentenceCount.containsKey(word)) {
                    int count = sentenceCount.get(word) + 1;
                    sentenceCount.put(word, count);
                } else {
                    sentenceCount.put(word, 1);
                }
            }
        }
        
        List<Map.Entry<String, Integer>> list = new ArrayList<>(occurance.entrySet());
        list.sort(Map.Entry.comparingByValue());

        for(Map.Entry<String, Integer> entry : list){
            System.out.printf("key: %s; occurences: %d; sentences: %d\n",
                entry.getKey(), entry.getValue(),
                sentenceCount.get(entry.getKey()));
        }

        System.out.printf("Number of sentences: %d\n", numSentences);
        /*
        TODO - replace everything up there with the sentence class and test it
        */
        
        /*
        TODO - call the calculateBM25() function to get sentence rank
        */
        
        /*
        TODO - create an n x n container where n is the number of sentences
        
        Sentence has a getSimilarity() function
        */
        
        /*
        TODO - apply the page rank algorithm on the sentences
        */
    }

}
