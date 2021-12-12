import scala.annotation.tailrec

object D11 extends App {

  val input: Array[Array[Int]] = io.Source.fromResource("D11_input.txt").mkString.split("\n").collect {
    case s if s.nonEmpty => s.split("").map(_.toInt)
  }

  private val maxSteps = 100
  private val buildFlashedState: () => Array[Array[Boolean]] = () => Array.fill(input.length)(Array.fill(input.head.length)(false))

  private def checkOctos(octos: Array[Array[Int]]): (Array[Array[Boolean]], Array[Array[Int]]) = {
    val flashed = buildFlashedState()

    def energize(x: Int, y: Int): Unit = {
      if (!flashed(x)(y)) {
        val energized = octos(x)(y) + 1
        octos(x)(y) = energized
        if (energized > 9) {
          octos(x)(y) = 0
          flashed(x)(y) = true
          walkNeighbors(x, y)
        }
      }
    }

    def walkNeighbors(x: Int, y: Int): Seq[Unit] = {
      for {
        newX <- x - 1 to x + 1
        newY <- y - 1 to y + 1
        (nX, nY) = (newX, newY) if (newX != x || newY != y) && ((newX >= 0 && newX <= octos.length - 1) && (newY >= 0 && newY <= octos.head.length - 1))
        _ = energize(nX, nY)
      } yield ()
    }

    for {
      (row, rowIdx) <- octos.zipWithIndex
      (_, colIdx) <- row.zipWithIndex
      _ = energize(rowIdx, colIdx)
    } yield ()

    (flashed, octos)
  }

  @tailrec
  def steps(step: Int, maxSteps: Int, input: Array[Array[Int]], accumlatedResult: Seq[(Array[Array[Int]], Array[Array[Boolean]])]): Seq[(Array[Array[Int]], Array[Array[Boolean]])] = {
    val (finalFlashed, octosUpdated) = checkOctos(input)
    val finalAcc = accumlatedResult ++ Seq((octosUpdated, finalFlashed))
    /*
    println()
    octosUpdated.foreach(o => {
      o.foreach(x => print(s" $x "))
      println()
    })
    println()
    finalFlashed.foreach(o => {
      o.foreach(x => print(s" $x "))
      println()
    })
    */
    if (step == maxSteps) finalAcc
    else steps(step + 1, maxSteps, finalAcc.last._1, finalAcc)
  }

  private val (octosResult, flashesResult) = steps(1, maxSteps, input, Seq.empty).unzip
  val totalFlashes = flashesResult.flatMap(_.map(_.count(_ == true))).sum
  println(s"c1: $totalFlashes")

  @tailrec
  private def continue(step: Int, maxSteps: Int, octos: Array[Array[Int]]): Int= {
    val (o, f) = steps(step, maxSteps, octos, Seq.empty).unzip
    val isAllFlashed: Array[Array[Boolean]] => Boolean = f => f.nonEmpty && f.forall(_.forall(_ == true))
    val allFlashedStep = f.zipWithIndex.collectFirst{
      case (flashes, currentStep) if isAllFlashed(flashes) => currentStep + maxSteps
    }

    if(allFlashedStep.isDefined) allFlashedStep.get
    else continue(maxSteps + 1, maxSteps + 1, o.last)
  }

  val allFlashesStep = continue(maxSteps + 1, maxSteps + 2, octosResult.last)
  println(s"c2: $allFlashesStep")

}
