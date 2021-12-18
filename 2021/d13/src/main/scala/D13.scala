import scala.annotation.tailrec

/*
--- Day 13: Transparent Origami ---
You reach another volcanically active part of the cave. It would be nice if you could do some kind of thermal imaging so you could tell ahead of time which caves are too hot to safely enter.

Fortunately, the submarine seems to be equipped with a thermal camera! When you activate it, you are greeted with:

Congratulations on your purchase! To activate this infrared thermal imaging
camera system, please enter the code found on page 1 of the manual.
Apparently, the Elves have never used this feature. To your surprise, you manage to find the manual; as you go to open it, page 1 falls out. It's a large sheet of transparent paper! The transparent paper is marked with random dots and includes instructions on how to fold it up (your puzzle input). For example:

6,10
0,14
9,10
0,3
10,4
4,11
6,0
6,12
4,1
0,13
10,12
3,4
3,0
8,4
1,10
2,14
8,10
9,0

fold along y=7
fold along x=5
The first section is a list of dots on the transparent paper. 0,0 represents the top-left coordinate. The first value, x, increases to the right. The second value, y, increases downward. So, the coordinate 3,0 is to the right of 0,0, and the coordinate 0,7 is below 0,0. The coordinates in this example form the following pattern, where # is a dot on the paper and . is an empty, unmarked position:

...#..#..#.
....#......
...........
#..........
...#....#.#
...........
...........
...........
...........
...........
.#....#.##.
....#......
......#...#
#..........
#.#........
Then, there is a list of fold instructions. Each instruction indicates a line on the transparent paper and wants you to fold the paper up (for horizontal y=... lines) or left (for vertical x=... lines). In this example, the first fold instruction is fold along y=7, which designates the line formed by all of the positions where y is 7 (marked here with -):

...#..#..#.
....#......
...........
#..........
...#....#.#
...........
...........
-----------
...........
...........
.#....#.##.
....#......
......#...#
#..........
#.#........
Because this is a horizontal line, fold the bottom half up. Some of the dots might end up overlapping after the fold is complete, but dots will never appear exactly on a fold line. The result of doing this fold looks like this:

#.##..#..#.
#...#......
......#...#
#...#......
.#.#..#.###
...........
...........
Now, only 17 dots are visible.

Notice, for example, the two dots in the bottom left corner before the transparent paper is folded; after the fold is complete, those dots appear in the top left corner (at 0,0 and 0,1). Because the paper is transparent, the dot just below them in the result (at 0,3) remains visible, as it can be seen through the transparent paper.

Also notice that some dots can end up overlapping; in this case, the dots merge together and become a single dot.

The second fold instruction is fold along x=5, which indicates this line:

#.##.|#..#.
#...#|.....
.....|#...#
#...#|.....
.#.#.|#.###
.....|.....
.....|.....
Because this is a vertical line, fold left:

#####
#...#
#...#
#...#
#####
.....
.....
The instructions made a square!

The transparent paper is pretty big, so for now, focus on just completing the first fold. After the first fold in the example above, 17 dots are visible - dots that end up overlapping after the fold is completed count as a single dot.

How many dots are visible after completing just the first fold instruction on your transparent paper?

Your puzzle answer was 724.

--- Part Two ---
Finish folding the transparent paper according to the instructions. The manual says the code is always eight capital letters.

What code do you use to activate the infrared thermal imaging camera system?

Your puzzle answer was CPJBERUL.

Both parts of this puzzle are complete! They provide two gold stars: **

 */
object D13 extends App {

  type ColRow = (Int, Int)
  type FoldInstruction = (String, Int)
  val (inputPoints: Array[ColRow], inputDirections: List[FoldInstruction]) = {
    val Array(p, dir) = io.Source.fromResource("D13_input.txt").mkString.split("\n\n")
    val points = p.split("\n")
      .map(_.split(","))
      .map{ case Array(col, row) => (col.toInt, row.toInt) }

    val directions = dir.split("\n")
      .map(_.split(" ").last)
      .map(_.split("="))
      .map{ case Array(dir, n) => dir -> n.toInt }

    (points, directions.toList)
  }

  private def markPaper(): Array[Array[String]] = {
    val maxX = inputPoints.map(_._2).max + 1
    val maxY = inputPoints.map(_._1).max + 1
    val paper = Array.fill(maxX, maxY)(".")
    inputPoints.foreach{ case (y, x) => paper(x)(y) = "#" }
    paper
  }

  private def foldPaper(paper: Array[Array[String]], foldInstruction: List[FoldInstruction]): Array[Array[String]] ={
    @tailrec
    def fold(toFold: Array[Array[String]], instructions: List[FoldInstruction]): Array[Array[String]] = {
      instructions match {
        case Nil => toFold
        case (direction, number) :: tail =>
          val foldedPaper = direction match {
            case "x" =>
              val rowLength: Int = toFold.head.length - 1
              val foldFrom: Int = rowLength - number
              val colsToFold = foldFrom + 1  to rowLength
              val colsToKeep = 0 until  foldFrom

              val mergedIdxs = colsToKeep.map {
                case i if colsToKeep.length - i <= colsToFold.length => (i, colsToFold((colsToKeep.length - i) - 1))
                case i => (i, -1)
              }

              toFold.map{ row =>
                mergedIdxs.map {
                  case (keep, fold) if fold == -1 => row(keep)
                  case (keep, fold) if keep == -1 => row(fold)
                  case (keep, fold) =>
                    if (row(keep) == "#") row(keep)
                    else row(fold)
                }.toArray
              }
            case "y" =>
              val folded = toFold.slice(toFold.length - number, toFold.length).reverse.zipWithIndex.map {
                case (row, x) => row.zipWithIndex.map {
                  case (col, y) => if (toFold(x)(y) == "#") "#" else col
                }
              }
              toFold.slice(0, number - toFold.length) ++ folded
          }
          fold(foldedPaper, tail)
      }
    }

    fold(paper, foldInstruction)
  }

  def printPaper(paper: Array[Array[String]]): Unit = paper.map(_.mkString).foreach(println)

  private val c1 = foldPaper(markPaper(), inputDirections.slice(0, 1))
  println(s"c1: ${c1.map(_.count(_ == "#")).sum}")

  private val c2 = foldPaper(markPaper(), inputDirections)
  println(s"c2:")
  printPaper(c2)

}
