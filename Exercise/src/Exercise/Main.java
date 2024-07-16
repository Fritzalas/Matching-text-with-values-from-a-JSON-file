package Exercise;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

    // Function to remove symbols between letters
    public static String removeSymbolsBetweenLetters(String text) {
        return text.replaceAll("(?<=\\w)-(?=\\w)", "").replaceAll("\\s+", " ");
    }

    // Function to reinsert symbols into original text
    public static String reinsertSymbols(String originalText, int matchStart, int matchEnd) {
        return originalText.substring(matchStart, matchEnd);
    }

    // Function to split text into parts based on provided values
    public static List<String> splitText(String text, List<String> values) {
        List<String> parts = new ArrayList<>();
        int currentIndex = 0;
        String normalizedText = removeSymbolsBetweenLetters(text).toLowerCase();
        int similarityThreshold = 94;

        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            String normalizedValue = removeSymbolsBetweenLetters(value).toLowerCase();

            int bestMatchIndex = -1;
            int bestMatchScore = -1;

            for (int start = currentIndex; start < normalizedText.length(); start++) {
                int end = start + normalizedValue.length();
                if (end <= normalizedText.length()) {
                    String textPart = normalizedText.substring(start, end);
                    int similarityScore = FuzzySearch.partialRatio(normalizedValue, textPart);

                    if (similarityScore > bestMatchScore) {
                        bestMatchIndex = start;
                        bestMatchScore = similarityScore;
                    }
                }
            }

            if (bestMatchScore >= similarityThreshold) {
                int matchEnd = bestMatchIndex + normalizedValue.length();

                if (bestMatchIndex > currentIndex) {
                    String part = text.substring(currentIndex, bestMatchIndex).trim();
                    if (!parts.isEmpty() && parts.getLast().startsWith("matched_value" + i + ":")) {
                        parts.set(parts.size() - 1, parts.getLast() + part);
                    } else {
                        parts.add("matched_value" + (i + 1) + ":" + part);
                    }
                }

                String matchedPart = reinsertSymbols(text, bestMatchIndex, matchEnd);
                parts.add("matched_value" + (i + 1) + ":" + matchedPart);

                currentIndex = matchEnd;
            }
        }

        if (currentIndex < text.length()) {
            String remainingPart = text.substring(currentIndex).trim();
            if (!remainingPart.isEmpty()) {
                if (!parts.isEmpty() && parts.getLast().startsWith("matched_value" + values.size() + ":")) {
                    parts.set(parts.size() - 1, parts.getLast() + remainingPart);
                } else {
                    parts.add("matched_value" + (values.size() + 1) + ":" + remainingPart);
                }
            }
        }

        List<String> updatedParts = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            String originalTextWithoutMatchedValue = part.split(":", 2)[1];
            updatedParts.add("matched_value" + (i + 1) + ":" + originalTextWithoutMatchedValue);
        }

        List<String> cleanedList = new ArrayList<>();
        for (String string : updatedParts) {
            String cleanedString = string.split(":", 2)[1].trim();
            cleanedList.add(cleanedString);
        }

        return cleanedList;
    }

    // Function to read text from file
    public static String processFile(String filePath) throws IOException {
        String text = Files.readString(Paths.get(filePath)).trim();
        text = text.replaceAll("\\s+", " ");
        return text;
    }

    public static List<String> concatenateStrings(List<String> stringList) {
        List<String> result = new ArrayList<>();
        int i = 0;

        while (i < stringList.size()) {
            String currentString = stringList.get(i);

            // Check if the string ends with '.', '!', or '?'
            if (!currentString.endsWith(".") && !currentString.endsWith("!") && !currentString.endsWith("?")) {
                // If the next string exists
                if (i + 1 < stringList.size()) {
                    String nextString = stringList.get(i + 1);
                    // Find the first word in the next string
                    String[] nextStringWords = nextString.split("\\s+", 2);
                    if (nextStringWords.length > 0) {
                        String firstWordNextString = nextStringWords[0];
                        // Concatenate the first word of the next string to the current string
                        currentString += firstWordNextString;
                        // Remove the first word from the next string
                        if (nextStringWords.length > 1) {
                            stringList.set(i + 1, nextStringWords[1].trim());
                        } else {
                            stringList.set(i + 1, "");
                        }
                    }
                }
            }

            result.add(currentString);
            i++;
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        String text = processFile("Sample.txt");

        List<String> values = new ArrayList<>();
        List<Integer>  indexed=new ArrayList<>();

        // Read values from JSON file
        String jsonContent = Files.readString(Paths.get("Sample.json"));
        JSONArray jsonArray = new JSONArray(jsonContent);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String value = jsonObject.getString("value");
            int indexing=jsonObject.getInt("index");
            values.add(value);
            indexed.add(indexing);
        }

        List<String> result = splitText(text, values);
        result=concatenateStrings(result);
        List<DataEntry> dataEntries = new ArrayList<>();
        for (int i = 0; i < indexed.size(); i++) {
            dataEntries.add(new DataEntry(indexed.get(i), values.get(i), result.get(i)));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File("output.json"), dataEntries);
            System.out.println("JSON file created successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}