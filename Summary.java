import java.io.*;
import java.util.*;

public class Summary {

    public static void main(String[] args) throws IOException{
        // Prints "Hello, World" to the terminal window.

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
        // System.out.println(str);
        // return;
        String[] sentences = str.split("\\.");
        int numSentences = sentences.length;


        Map<String, Integer> occurance = new HashMap<String, Integer>();

        String[] words;
        for(String singleSentence: sentences){
            words = singleSentence.split("\\s+");
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