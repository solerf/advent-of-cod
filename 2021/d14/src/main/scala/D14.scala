import scala.annotation.tailrec
import scala.collection.immutable.ListMap

/*
--- Day 14: Extended Polymerization ---
The incredible pressures at this depth are starting to put a strain on your submarine. The submarine has polymerization equipment that would produce suitable materials to reinforce the submarine, and the nearby volcanically-active caves should even have the necessary input elements in sufficient quantities.

The submarine manual contains instructions for finding the optimal polymer formula; specifically, it offers a polymer template and a list of pair insertion rules (your puzzle input). You just need to work out what polymer would result after repeating the pair insertion process a few times.

For example:

NNCB

CH -> B
HH -> N
CB -> H
NH -> C
HB -> C
HC -> B
HN -> C
NN -> C
BH -> H
NC -> B
NB -> B
BN -> B
BB -> N
BC -> B
CC -> N
CN -> C
The first line is the polymer template - this is the starting point of the process.

The following section defines the pair insertion rules. A rule like AB -> C means that when elements A and B are immediately adjacent, element C should be inserted between them. These insertions all happen simultaneously.

So, starting with the polymer template NNCB, the first step simultaneously considers all three pairs:

The first pair (NN) matches the rule NN -> C, so element C is inserted between the first N and the second N.
The second pair (NC) matches the rule NC -> B, so element B is inserted between the N and the C.
The third pair (CB) matches the rule CB -> H, so element H is inserted between the C and the B.
Note that these pairs overlap: the second element of one pair is the first element of the next pair. Also, because all pairs are considered simultaneously, inserted elements are not considered to be part of a pair until the next step.

After the first step of this process, the polymer becomes NCNBCHB.

Here are the results of a few steps using the above rules:

Template:     NNCB
After step 1: NCNBCHB
After step 2: NBCCNBBBCBHCB
After step 3: NBBBCNCCNBBNBNBBCHBHHBCHB
After step 4: NBBNBNBBCCNBCNCCNBBNBBNBBBNBBNBBCBHCBHHNHCBBCBHCB
This polymer grows quickly. After step 5, it has length 97; After step 10, it has length 3073. After step 10, B occurs 1749 times, C occurs 298 times, H occurs 161 times, and N occurs 865 times; taking the quantity of the most common element (B, 1749) and subtracting the quantity of the least common element (H, 161) produces 1749 - 161 = 1588.

Apply 10 steps of pair insertion to the polymer template and find the most and least common elements in the result. What do you get if you take the quantity of the most common element and subtract the quantity of the least common element?

Your puzzle answer was 3342.

--- Part Two ---
The resulting polymer isn't nearly strong enough to reinforce the submarine. You'll need to run more steps of the pair insertion process; a total of 40 steps should do it.

In the above example, the most common element is B (occurring 2192039569602 times) and the least common element is H (occurring 3849876073 times); subtracting these produces 2188189693529.

Apply 40 steps of pair insertion to the polymer template and find the most and least common elements in the result. What do you get if you take the quantity of the most common element and subtract the quantity of the least common element?

Your puzzle answer was 3776553567525.

Both parts of this puzzle are complete! They provide two gold stars: **

*/

object D14 extends App {

  val (basePolymer: String, rules: Map[String, String]) = {
    val Array(i, r) = io.Source.fromResource("D14_input.txt").mkString.split("\n\n")
    val inst = r.split("\n")
      .map(_.split(" -> "))
      .foldLeft(Map.empty[String, String]){
        case (acc, Array(a, b)) => acc ++ ListMap(a -> b)
      }

    (i, inst)
  }

  @tailrec
  private def run(start: String, step: Int, maxSteps: Int): String = {
    // this explodes
    def process(p: String): String = {
      p.split("").sliding(2).zipWithIndex
        .map { case (pair, idx) =>
          val ruleOutput = rules(pair.mkString)
          if(idx == 0) s"${pair(0)}$ruleOutput${pair(1)}"
          else s"$ruleOutput${pair(1)}"
        }.toSeq.mkString
    }

    if(step > maxSteps) start
    else run(process(start), step + 1, maxSteps)
  }

  private def maxAndMin(finalPolymer: String): (Int, Int) = {
    val values = finalPolymer.split("").groupBy(identity).view.mapValues(_.length).toMap
    (values.maxBy(_._2)._2, values.minBy(_._2)._2)
  }

  @tailrec
  private def counting(start: Map[String, Long], step: Int, maxSteps: Int): Map[String, Long] = {
    def process(accumulated: Map[String, Long]): Map[String, Long] = {
      // just count the findings
      val result = accumulated.toSeq.flatMap { case (pair, count) =>
        rules.get(pair) match {
          case Some(newChar) =>
            val Array(first, second) = pair.split("")
            Seq(pair -> count * -1, s"$first$newChar" -> count, s"$newChar$second" -> count)
          case None =>
            Seq.empty
        }
      }

      // aggregate on the existent
      val newOne = accumulated.toSeq ++ result
      newOne.groupBy(_._1).view.mapValues(_.map(_._2).sum).toMap.filter(_._2 > 0)
    }

    if(step > maxSteps) countLetters(start)
    else counting(process(start), step + 1, maxSteps)
  }

  def countLetters(accumulated: Map[String, Long]): Map[String, Long] =
    accumulated.toSeq
      .flatMap {
        case (k, v) => k.split("").map(_ -> v).toSeq
      }
      .groupBy(_._1).view.mapValues(_.map(_._2).sum).toMap
      .updatedWith(basePolymer.last.toString)(_.map(_ + 1))
      .view.mapValues(_ / 2).toMap

  private def maxAndMin(finalCount: Map[String, Long]): (Long, Long) = {
    val max = finalCount.maxBy(_._2)
    val min = finalCount.minBy(_._2)
    (max._2, min._2)
  }

  val (c1Max, c1Min) = maxAndMin(run(basePolymer, 1, 10))
  println(s"c1: ${c1Max - c1Min}")

  val initialCount = basePolymer.sliding(2).map(_.mkString).toSeq.groupBy(identity).view.mapValues(_.length.toLong).toMap
  val (c2Max, c2Min) = maxAndMin(counting(initialCount, 1, 40))
  println(s"c2: ${c2Max - c2Min}")
}
