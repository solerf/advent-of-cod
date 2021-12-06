import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
--- Day 4: Giant Squid ---
You're already almost 1.5km (almost a mile) below the surface of the ocean, already so deep that you can't see any sunlight. What you can see, however, is a giant squid that has attached itself to the outside of your submarine.

Maybe it wants to play bingo?

Bingo is played on a set of boards each consisting of a 5x5 grid of numbers. Numbers are chosen at random, and the chosen number is marked on all boards on which it appears. (Numbers may not appear on all boards.) If all numbers in any row or any column of a board are marked, that board wins. (Diagonals don't count.)

The submarine has a bingo subsystem to help passengers (currently, you and the giant squid) pass the time. It automatically generates a random order in which to draw numbers and a random set of boards (your puzzle input). For example:

7,4,9,5,11,17,23,2,0,14,21,24,10,16,13,6,15,25,12,22,18,20,8,19,3,26,1

22 13 17 11  0
 8  2 23  4 24
21  9 14 16  7
 6 10  3 18  5
 1 12 20 15 19

 3 15  0  2 22
 9 18 13 17  5
19  8  7 25 23
20 11 10 24  4
14 21 16 12  6

14 21 17 24  4
10 16 15  9 19
18  8 23 26 20
22 11 13  6  5
 2  0 12  3  7
After the first five numbers are drawn (7, 4, 9, 5, and 11), there are no winners, but the boards are marked as follows (shown here adjacent to each other to save space):

22 13 17 11  0         3 15  0  2 22        14 21 17 24  4
 8  2 23  4 24         9 18 13 17  5        10 16 15  9 19
21  9 14 16  7        19  8  7 25 23        18  8 23 26 20
 6 10  3 18  5        20 11 10 24  4        22 11 13  6  5
 1 12 20 15 19        14 21 16 12  6         2  0 12  3  7
After the next six numbers are drawn (17, 23, 2, 0, 14, and 21), there are still no winners:

22 13 17 11  0         3 15  0  2 22        14 21 17 24  4
 8  2 23  4 24         9 18 13 17  5        10 16 15  9 19
21  9 14 16  7        19  8  7 25 23        18  8 23 26 20
 6 10  3 18  5        20 11 10 24  4        22 11 13  6  5
 1 12 20 15 19        14 21 16 12  6         2  0 12  3  7
Finally, 24 is drawn:

22 13 17 11  0         3 15  0  2 22        14 21 17 24  4
 8  2 23  4 24         9 18 13 17  5        10 16 15  9 19
21  9 14 16  7        19  8  7 25 23        18  8 23 26 20
 6 10  3 18  5        20 11 10 24  4        22 11 13  6  5
 1 12 20 15 19        14 21 16 12  6         2  0 12  3  7
At this point, the third board wins because it has at least one complete row or column of marked numbers (in this case, the entire top row is marked: 14 21 17 24 4).

The score of the winning board can now be calculated. Start by finding the sum of all unmarked numbers on that board; in this case, the sum is 188. Then, multiply that sum by the number that was just called when the board won, 24, to get the final score, 188 * 24 = 4512.

To guarantee victory against the giant squid, figure out which board will win first. What will your final score be if you choose that board?

Your puzzle answer was 10374.

--- Part Two ---
On the other hand, it might be wise to try a different strategy: let the giant squid win.

You aren't sure how many bingo boards a giant squid could play at once, so rather than waste time counting its arms, the safe thing to do is to figure out which board will win last and choose that one. That way, no matter which boards it picks, it will win for sure.

In the above example, the second board is the last to win, which happens after 13 is eventually called and its middle column is completely marked. If you were to keep playing until this point, the second board would have a sum of unmarked numbers equal to 148 for a final score of 148 * 13 = 1924.

Figure out which board will win last. Once it wins, what would its final score be?

Your puzzle answer was 24742.
 */

public class D04 {

  public static void main(String[] args) throws IOException {
    var input = Input.create("./D04_input.txt");
    var boards = input.boards;
    var numbersCalled = input.numbersCalled;

    var firstWinnerScore = checkNumbersInBoards(numbersCalled, boards)
        .filter(Board::isBingo)
        .findFirst()
        .map(Board::finalScore)
        .orElseThrow();
    System.out.println("c1: " + firstWinnerScore);

    var lastWinnerScore = checkNumbersInBoards(numbersCalled, boards)
        .collect(Collectors.toSet())
        .stream().reduce((b1, b2) -> (b1.calledNumbers.size() > b2.calledNumbers.size()) ? b1 : b2)
        .map(Board::finalScore)
        .orElseThrow();
    System.out.println("c2: " + lastWinnerScore);
  }

  private static Stream<Board> checkNumbersInBoards(List<Integer> numbersCalled,
      Set<Board> boards) {
    return IntStream.range(0, numbersCalled.size())
        .mapToObj(idx -> {
          var called = numbersCalled.get(idx);
          return boards.stream().peek(b -> b.markNumber(called));
        })
        .flatMap(s -> s);
  }

  private static final Integer boardNumRows = 5;

  record Board(List<List<Integer>> numbers, List<Integer> calledNumbers) {

    Board(List<List<Integer>> numbers) {
      this(numbers, new ArrayList<>());
    }

    Long score() {
      return numbers.stream()
          .flatMap(Collection::stream)
          .filter(bn -> bn > -1)
          .collect(Collectors.summarizingInt(bn -> bn))
          .getSum();
    }

    Long finalScore() {
      var lastCalled = calledNumbers.get(calledNumbers.size() - 1);
      return this.score() * lastCalled;
    }

    void markNumber(Integer calledNumber) {
      if (this.isBingo()) {
        return;
      }
      for (List<Integer> number : numbers) {
        for (var col = 0; col < number.size(); col++) {
          if (number.get(col).equals(calledNumber)) {
            number.set(col, -1);
            calledNumbers.add(calledNumber);
            break;
          }
        }
      }
    }

    boolean isBingo() {
      var rowBingo = numbers.stream().map(Collection::stream)
          .anyMatch(s -> s.allMatch(bn -> bn == -1));
      return rowBingo || transpose(numbers).stream().map(Collection::stream)
          .anyMatch(s -> s.allMatch(bn -> bn == -1));
    }

    private static List<List<Integer>> transpose(List<List<Integer>> board) {
      List<List<Integer>> transposed = List.of(
          new ArrayList<>(5),
          new ArrayList<>(5),
          new ArrayList<>(5),
          new ArrayList<>(5),
          new ArrayList<>(5)
      );

      for (var row = 0; row < board.size(); row++) {
        for (var col = 0; col < board.get(row).size(); col++) {
          transposed.get(row).add(col, board.get(col).get(row));
        }
      }
      return transposed;
    }
  }

  record Input(Set<Board> boards, List<Integer> numbersCalled) {

    static Input create(String filePath) throws IOException {
      var input = Objects.requireNonNull(D04.class.getResource(filePath)).getPath();
      var lines = Files.readAllLines(Path.of(input));
      var numbersCalled = readNumbersCalled(lines);
      var boards = readBoards(lines.subList(1, lines.size()));
      return new Input(boards, numbersCalled);
    }

    private static Set<Board> readBoards(List<String> rawBoard) {
      var boardsRows = rawBoard.stream()
          .filter(s -> !s.isEmpty())
          .map(Input::readRow)
          .toList();

      return IntStream.range(0, boardsRows.size() / boardNumRows).mapToObj(boardIdx -> {
        var startIdx = boardIdx * boardNumRows;
        return new Board(boardsRows.subList(startIdx, startIdx + boardNumRows));
      }).collect(Collectors.toSet());
    }


    private static List<Integer> readRow(String row) {
      return Arrays.stream(row.split("\s+"))
          .filter(s -> !s.isEmpty())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
    }

    private static List<Integer> readNumbersCalled(List<String> lines) {
      return Arrays.stream(lines.get(0).split(",")).map(Integer::parseInt).toList();
    }
  }

}
