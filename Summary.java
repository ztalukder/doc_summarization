//package edu.nyu.bigdata.summary;

import java.io.*;
import java.util.*;
import java.text.BreakIterator;

public class Summary {

    public static void main(String[] args) throws IOException{
        boolean useBM25 = true;
        String file;
        if (args.length == 0) {
            file = "doors.txt";
        }
        else {
            file = args[0];
            
            if (args.length > 1 && args[1].equals("pr")) {
                System.out.printf("==========USING PAGERANK==========\n");
                useBM25 = false;
            }
            else {
                System.out.printf("============USING BM25============\n");
            }
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

            Sentence currSentence = new Sentence(i, str.substring(begin, end));
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
        
        for (int i = 0; i < theSentences.size(); i++) {
            theSentences.get(i).calculateBM25(averageLength, sentenceCount, theSentences.size());
        }
        
        double[][] similarityContainer = new double[theSentences.size()][theSentences.size()];
        for(int i = 0; i < similarityContainer.length; i++){
            for(int j = 0; j < similarityContainer[i].length; j++){
                similarityContainer[i][j] = theSentences.get(i).getSimilarity(theSentences.get(j));
            }
        }

        double total;
        for(int i = 0; i < similarityContainer.length; i++){
            total = 0;
            for(int j = 0; j < similarityContainer[i].length; j++){
                total += similarityContainer[i][j];
            }
            theSentences.get(i).setSumSimilarity(total);
        }

        double dampingFactor = .05;
        int iterations = 20;
        double initialValue = 1.0/theSentences.size();

        for(int i = 0; i < theSentences.size(); i++){
            theSentences.get(i).setPageRank(initialValue);
        }

        // TODO - might need to fix this
        double newPageRank;
        for(int iter = 0; iter < iterations; iter++){
            for(int i = 0; i < theSentences.size(); i++){
                newPageRank = (1-dampingFactor);
                for(int j = 0; j < similarityContainer[i].length; j++){
                    if( i != j){
                        newPageRank += dampingFactor*theSentences.get(j).getPageRank()*similarityContainer[i][j]/theSentences.get(j).getSumSimilarity();
                    }
                }
                theSentences.get(i).setPageRank(newPageRank);
            }
        }
        
        if (useBM25) {
            Comparator<Sentence> compareByBM25 = 
                (Sentence s1, Sentence s2) -> ((Double)(s2.getBM25Val())).compareTo( (Double)(s1.getBM25Val()) );
            Collections.sort(theSentences, compareByBM25);
        }
        else {
            Comparator<Sentence> compareByPageRank = 
                (Sentence s1, Sentence s2) -> ((Double)(s2.getPageRank())).compareTo( (Double)(s1.getPageRank()) );
            Collections.sort(theSentences, compareByPageRank);
        }
        
        // TODO - remove after we fix pagerank
        for (int i = 0; i < theSentences.size(); i++) {
            System.out.printf("Sentence #%s\tPageRank: %f\n", theSentences.get(i).getSentenceNumber(), theSentences.get(i).getPageRank());
        }
            
        ArrayList<Sentence> topSentences = new ArrayList<Sentence>();
        for (int i = 0; i < 5; i++) {
            topSentences.add(new Sentence(theSentences.get(i)));
        }
        
        Comparator<Sentence> compareBySentenceNumber = 
            (Sentence s1, Sentence s2) -> ((Integer)(s1.getSentenceNumber())).compareTo((Integer)(s2.getSentenceNumber()));
        Collections.sort(topSentences, compareBySentenceNumber);
        
        for (int i = 0; i < topSentences.size(); i++) {
            System.out.printf("Sentence #%d\tBM25: %f\tSentence Rank: %f\n>>>\t%s\n\n",
                                topSentences.get(i).getSentenceNumber(),
                                topSentences.get(i).getBM25Val(),
                                topSentences.get(i).getPageRank(),
                                topSentences.get(i).getSentence());
        }
        System.out.printf("==================================\n==================================\n");
    }

}

// TODO - REMOVE ALL THE TODO comments after we are done
