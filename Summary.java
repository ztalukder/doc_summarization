import java.io.*;
import java.util.*;
import java.text.BreakIterator;

public class Summary {

    public static void main(String[] args) throws IOException{
    
        String file = "doors.txt";
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

        String[] words;
        for(int i = 0; i < sentences.size(); i++){
            int begin = sentences.get(i);
            int end = (i == sentences.size() - 1) 
                        ? str.length() : sentences.get(i + 1);
            String singleSentence = str.substring(begin, end);
            
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
            }
        }
        
        // for (Map.Entry entry : occurance.entrySet()){
        //     System.out.println("key: " + entry.getKey() + "; value: " + entry.getValue());
        // }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(occurance.entrySet());
        list.sort(Map.Entry.comparingByValue());

        for(Map.Entry<String, Integer> entry : list){
            System.out.println("key: " + entry.getKey() + "; value: " + entry.getValue());
        }

        System.out.println(numSentences);
    }

}
