import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class D10 {

    private static final Map<String, String> closeOpenMatchers = Map.of(
    ")", "(" ,
    "]", "[" ,
    "}", "{" ,
    ">", "<"
    );

    private static final Map<String, Integer> corruptedCharScore = Map.of(
    ")", 3,
    "]", 57,
    "}", 1197,
    ">", 25137
    );

    private static final Map<String, Integer> incompleteCharScore = Map.of(
    ")", 1,
    "]", 2,
    "}", 3,
    ">", 4
    );

    private static final boolean notCorrupted = true;
    private static final boolean corrupted = false;

    public static void main(String[] args) throws IOException {
        Input input = Input.create("D10_input.txt");
        var partitions = partition(input.chunks);
        System.out.println("c1: " + scoreCorruptedChunks(partitions.get(corrupted)));
        System.out.println("c2: " + scoreIncompleteChunks(partitions.get(notCorrupted)));
    }

    private static Long scoreIncompleteChunks(List<String> chunks){
         var costs = chunks.stream()
             .map(D10::getIncompleteChars)
             .map(D10::calculateIncomplete)
             .collect(Collectors.toList());

        Collections.sort(costs);
        int size = costs.size();
        return size % 2 == 0
                 ? costs.get(size / 2 - 1)
                 : costs.get(size / 2);
    }

    private static Long calculateIncomplete(List<String> incompletes){
        return incompletes.stream().reduce(
                0L,
                (accum, c) -> {
                    Integer charScore = incompleteCharScore.get(c);
                    System.out.println(accum + " " + c + " " + charScore);
                    long total = (accum * 5L) + charScore;
                    System.out.println("total -> " + total);
                    return total;
                },
                Long::sum
        );
    }

    private static List<String> getIncompleteChars(String chunk){
        final Stack<String> control = new Stack<>();
        var reversedMatchers = closeOpenMatchers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        Supplier<List<String>> incompletChars = () -> {
            for(Character c : chunk.toCharArray()){
                var chunkKey = c.toString();
                boolean isOpening = closeOpenMatchers.containsValue(chunkKey);
                if(isOpening){
                    control.push(chunkKey);
                } else {
                    control.pop(); // assume they match
                }
            }
            return IntStream.iterate(control.size() - 1, step -> step >= 0, step -> step - 1)
                    .mapToObj(idx -> reversedMatchers.get(control.get(idx)))
                    .collect(Collectors.toList());
        };
        return incompletChars.get();
    }

    private static long scoreCorruptedChunks(List<String> chunks){
        return chunks.stream()
                .map(D10::getFirstCorrupted)
                .map(i -> corruptedCharScore.getOrDefault(i,0))
                .collect(Collectors.summarizingInt(s  -> s)).getSum();
    }

    private static Map<Boolean, List<String>> partition(List<String> chunks){
        return chunks.stream()
                .collect(Collectors.groupingBy(c -> getFirstCorrupted(c).isEmpty(), Collectors.mapping(Function.identity(), Collectors.toList())));
    }

    private static String getFirstCorrupted(String chunk){
        final Stack<String> control = new Stack<>();
        Function<String, Boolean> notMatchingOpener = openerMatch -> !control.pop().equals(openerMatch);

        Supplier<String> corrupted = () -> {
            for(Character c : chunk.toCharArray()){
                var chunkKey = c.toString();
                boolean isOpening = closeOpenMatchers.containsValue(chunkKey);
                if(isOpening){
                    control.push(chunkKey);
                } else {
                    String openerMatch = closeOpenMatchers.get(chunkKey);
                    boolean notValid = control.isEmpty() || notMatchingOpener.apply(openerMatch);
                    if(notValid){
                        return chunkKey;
                    }
                }
            }
            return "";
        };
        return corrupted.get();
    }

    record Input(List<String> chunks) {
        static Input create(String filePath) throws IOException {
            var input = Objects.requireNonNull(D10.class.getResource(filePath)).getPath();
            var chunks = Files.readAllLines(Path.of(input));
            return new Input(chunks);
        }
    }

}
