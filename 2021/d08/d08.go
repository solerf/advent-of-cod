package main

import (
  "fmt"
  "os"
  "strings"
)

type void struct{}

// length -> digit
var uniqueDigitsByLength = map[int]int{
  2: 1, 3: 7, 4: 4, 7: 8,
}

func main() {
  signalPatterns, digitOutputs := getInput("D08_input.txt")
  fmt.Printf("c1: %v\n", getUniqueDigitsCount(digitOutputs))
  fmt.Printf("c2: %v\n", getOutputDigitsSum(signalPatterns, digitOutputs))
}

func getOutputDigitsSum(signalPatterns [][]string, digitOutputs [][]string) int {
  getKnowDigitsMapping := func(signalPatterns []string) map[int]string {
    knowDigitsMap := make(map[int]string)

    for _, sp := range signalPatterns {
      // map only the know digits
      if len(sp) == 2 { // number 1
        knowDigitsMap[1] = sp
      }
      if len(sp) == 3 { // number 7
        knowDigitsMap[7] = sp
      }
      if len(sp) == 4 { // number 4
        knowDigitsMap[4] = sp
      }
    }
    return knowDigitsMap
  }

  // change this to maps
  containsAll := func(in string, contains string) bool {
    exists := func(a string, list []string) bool {
      for _, b := range list {
        if b == a {
          return true
        }
      }
      return false
    }

    inSplit := strings.Split(in, "")
    containsSplit := strings.Split(contains, "")
    for _, c := range containsSplit {
      if !exists(c, inSplit) {
        return false
      }
    }
    return true
  }

  //change this to maps
  diff := func(base string, diffFrom string) int {
    baseOrdered := strings.Split(base, "")
    miss := 0
    for _, b := range baseOrdered {
      if !strings.Contains(diffFrom, b) {
        miss += 1
      }
    }
    return miss
  }

  findDigit := func(digitOutput string, knowDigitsMap map[int]string) int {
    digitLength := len(digitOutput)
    digit, exists := uniqueDigitsByLength[digitLength]
    if exists {
      return digit
    } else {
      if digitLength == 5 {
        isThree := containsAll(digitOutput, knowDigitsMap[1]) && containsAll(digitOutput, knowDigitsMap[7])
        isFive := diff(digitOutput, knowDigitsMap[4]) == 2
        if isThree {
          return 3
        } else if isFive {
          return 5
        } else {
          return 2
        }
      } else {
        isNine := containsAll(digitOutput, knowDigitsMap[1]) && containsAll(digitOutput, knowDigitsMap[4]) && containsAll(digitOutput, knowDigitsMap[7])
        isZero := containsAll(digitOutput, knowDigitsMap[1]) && containsAll(digitOutput, knowDigitsMap[7])
        if isNine {
          return 9
        } else if isZero {
          return 0
        } else {
          return 6
        }
      }
    }
  }

  digitFactor := []int{1000, 100, 10, 1}
  digitsSum := 0

  for i, sig := range digitOutputs {
    finalDigit := 0
    knowDigitsMap := getKnowDigitsMapping(signalPatterns[i])
    for j, s := range sig {
      digit := findDigit(s, knowDigitsMap)
      finalDigit += digit * digitFactor[j]
    }
    digitsSum += finalDigit
  }

  return digitsSum
}

func getUniqueDigitsCount(signals [][]string) int {
  uniqueCount := 0
  for _, sig := range signals {
    for _, s := range sig {
      _, exists := uniqueDigitsByLength[len(s)]
      if exists {
        uniqueCount += 1
      }
    }
  }
  return uniqueCount
}

func getInput(path string) ([][]string, [][]string) {
  f, _ := os.ReadFile(path)
  lines := strings.Split(strings.TrimSpace(string(f)), "\n")

  outputSignals := make([][]string, len(lines))
  outputDigitOutputs := make([][]string, len(lines))
  for i, line := range lines {
    signalPatterns := strings.TrimSpace(strings.Split(strings.TrimSpace(line), "|")[0])
    digitOutputs := strings.TrimSpace(strings.Split(strings.TrimSpace(line), "|")[1])
    sps := strings.Split(signalPatterns, " ")
    dos := strings.Split(digitOutputs, " ")
    outputSignals[i] = sps
    outputDigitOutputs[i] = dos
  }
  return outputSignals, outputDigitOutputs
}
