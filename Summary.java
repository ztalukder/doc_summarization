//package edu.nyu.bigdata.summary;

import java.io.*;
import java.util.*;
import java.text.BreakIterator;

class Option {
     String flag, opt;
     public Option(String flag, String opt) { this.flag = flag; this.opt = opt; }
}

public class Summary {

    public static void main(String[] args) throws IOException{
        List<Option> optList = new ArrayList<Option>();
        boolean useBM25 = true;
        String file = "001.txt";
        boolean hasIdealSummary = false;
        String idealSummaryFile = "";

        int numSentencesToShow = 5;
        
        for(int i = 0; i < args.length; i++){
            switch (args[i].charAt(0)){
            case '-':
                if(args[i].length()  < 2){
                    throw new IllegalArgumentException("Not a valid arguement: " + args[i]);
                }
                if(args[i].charAt(1) == '-'){
                    if(args[i].length() < 3){
                        throw new IllegalArgumentException("Not a valid arguement: " + args[i]);
                    }
                    optList.add(new Option(args[i], args[i].substring(2, args[i].length())));
                }
                else{
                    if(args.length-1 == i){
                        throw new IllegalArgumentException("Expected arg after: "+args[i]);
                    }
                    optList.add(new Option(args[i], args[i+1]));
                    i++;
                }
                break;
            }
        }
        for(int i = 0; i < optList.size(); i++){
            String flag = optList.get(i).flag; 
            String option = optList.get(i).opt;
            if(flag.equals("-d") || flag.equals("--d") ){
                file = option;
            }
            if (flag.equals("-b")  || flag.equals("--b") ){
                useBM25 = Boolean.valueOf(option);
            }
            if(flag.equals("-s") || flag.equals("--s")){
            	hasIdealSummary = true;
            	idealSummaryFile = option;
            }
            if(flag.equals("-n") || flag.equals("--n")){
                numSentencesToShow =Integer.valueOf(option);
            }
        }
        
        System.out.printf("File: %s\t\tAlgorithm: %s\n\n", file,
                            ((useBM25) ? "BM25" : "PageRank")); 
        
        String str = getFileString(file);
        
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
        
        ArrayList<Sentence> theSentences = makeSentences(sentences, str);

        setSentencesBM25(theSentences);
        
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
                if(i != j){
                    total += similarityContainer[i][j];
                }
            }
            theSentences.get(i).setSumSimilarity(total);
        }

        double dampingFactor = .05;
        int iterations = 20;
        double initialValue = 100/theSentences.size();

        for(int i = 0; i < theSentences.size(); i++){
            theSentences.get(i).setPageRank(initialValue);
        }

        double newPageRank;
        double[] updatedRanks = new double[theSentences.size()];
        for(int iter = 0; iter < iterations; iter++){
            for(int i = 0; i < theSentences.size(); i++){
                newPageRank = 0;
                for(int j = 0; j < similarityContainer[i].length; j++){
                    if( i != j){
                        newPageRank += theSentences.get(j).getPageRank()*similarityContainer[i][j]/theSentences.get(j).getSumSimilarity();
                    }
                }
                updatedRanks[i] = newPageRank;

            }
            for(int index = 0; index < theSentences.size(); index++){
                theSentences.get(index).setPageRank(updatedRanks[index]);
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
            
        ArrayList<Sentence> topSentences = new ArrayList<Sentence>();
        if(theSentences.size() < numSentencesToShow){
            for (int i = 0; i < theSentences.size(); i++) {
                topSentences.add(new Sentence(theSentences.get(i)));
            }
        }
        else{
            for (int i = 0; i < numSentencesToShow; i++) {
                topSentences.add(new Sentence(theSentences.get(i)));
            }
        }
        
        Comparator<Sentence> compareBySentenceNumber = 
            (Sentence s1, Sentence s2) -> ((Integer)(s1.getSentenceNumber())).compareTo((Integer)(s2.getSentenceNumber()));
        Collections.sort(topSentences, compareBySentenceNumber);
        
        for (int i = 0; i < topSentences.size(); i++) {
            System.out.printf("Sentence #%d\tBM25: %.2f\tSentence Rank: %.3f%%\n>>>\t%s\n\n",
                                topSentences.get(i).getSentenceNumber(),
                                topSentences.get(i).getBM25Val(),
                                topSentences.get(i).getPageRank(),
                                topSentences.get(i).getSentence());
        
            System.out.printf("==================================\n==================================\n");
        }

        if(hasIdealSummary){
        	evaluateResults(topSentences, idealSummaryFile);	
        }
        

    }

    public static String getFileString(String fileName){
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(fileName));
        }
        catch(Exception e){
            System.out.println("Unable to open file.");
            System.exit(0);
        }
        StringBuilder builder = new StringBuilder();
        try{
            String currentLine = reader.readLine();
            while(currentLine != null){
                builder.append(currentLine);
                currentLine = reader.readLine();
            }
            reader.close();
        }
        catch(IOException e){
            System.out.println("Error reading line.");
            System.exit(0);
        }

        return builder.toString();

    }

    //sentences is a list of starting indices for each sentence, str is the entire text in a string
    public static ArrayList<Sentence> makeSentences(ArrayList<Integer> sentences, String str){
        ArrayList<Sentence> theSentences = new ArrayList<Sentence>();
        for(int i = 0; i < sentences.size(); i++){
            int begin = sentences.get(i);
            int end = (i == sentences.size() - 1) 
                        ? str.length() : sentences.get(i + 1);
            Sentence currSentence = new Sentence(i, str.substring(begin, end));
            theSentences.add(currSentence);
        }

        return theSentences;
    }

    public static void setSentencesBM25(ArrayList<Sentence> theSentences){
        Map<String, Integer> sentenceCount = new HashMap<String, Integer>();
        double averageLength = 0;
        for(int i = 0; i < theSentences.size(); i++){
            averageLength += theSentences.get(i).getLength();

            for (String word : theSentences.get(i).getCurrentSentence()) {
                if (sentenceCount.containsKey(word)) {
                    int count = sentenceCount.get(word) + 1;
                    sentenceCount.put(word, count);
                } else {
                    sentenceCount.put(word, 1);
                }
            }
        }
        averageLength /= (double) theSentences.size();
        
        for (int i = 0; i < theSentences.size(); i++) {
            theSentences.get(i).calculateBM25(averageLength, sentenceCount, theSentences.size());
        }
    }


    public static void evaluateResults(ArrayList<Sentence> result, String idealSummaryFile){
        Sentence idealSummary = new Sentence(1, getFileString(idealSummaryFile));

        String resultString = "";
        for(int i = 0; i < result.size(); i++){
            resultString += result.get(i);
        }

        Sentence generatedSummary = new Sentence(1, resultString);

        System.out.printf("Evaluation: %f\n", generatedSummary.getSimilarity(idealSummary));


    }

}
