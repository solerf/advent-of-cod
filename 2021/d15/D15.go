package main

import (
	"fmt"
	"math"
	"os"
	"strconv"
	"strings"
)

func getInput() *[][]int {
	f, _ := os.ReadFile("D15_input_or.txt")
	input := strings.Split(strings.TrimSpace(string(f)), "\n")
	rows := make([][]int, len(input))

	for i, line := range input {
		cols := make([]int, len(input))
		for j, l := range strings.Split(line, "") {
			asInt, _ := strconv.Atoi(l)
			cols[j] = asInt
		}
		rows[i] = cols
	}
	return &rows
}

type Coord struct {
	x int
	y int
}

type Cheap struct {
	coord Coord
	value int
}

func cheapeastWalk(cavern *[][]int, startCoord Coord) int {

	maxX := len(*cavern) - 1
	maxY := len((*cavern)[0]) - 1

	var getLowCostNeighbor func(Coord, int) Coord
	getLowCostNeighbor = func(atCoord Coord, maxNeighbors int) Coord {
		costX, costY := 0, 0
		for inc := 1; inc <= maxNeighbors; inc++ {
			newX, newY := atCoord.x+inc, atCoord.y+inc
			if newY <= maxY {
				costY += (*cavern)[atCoord.x][atCoord.y+inc]
			} else if costY == 0 {
				costY = math.MaxInt64
			}

			if newX <= maxX {
				costX += (*cavern)[atCoord.x+inc][atCoord.y]
			} else if costX == 0 {
				costX = math.MaxInt64
			}
		}

		if costX != costY {
			if costX < costY {
				return Coord{x: atCoord.x + 1, y: atCoord.y}
			} else {
				return Coord{x: atCoord.x, y: atCoord.y + 1}
			}
		} else {
			return getLowCostNeighbor(atCoord, maxNeighbors+1)
		}
	}

	var walk func(Coord, *[]Cheap) *[]Cheap
	walk = func(visitCoord Coord, cheapeastPaths *[]Cheap) *[]Cheap {
		if visitCoord.x == maxX && visitCoord.y == maxY {
			return cheapeastPaths
		} else {
			nextCoord := getLowCostNeighbor(visitCoord, 1)
			cheap := Cheap{coord: nextCoord, value: (*cavern)[nextCoord.x][nextCoord.y]}
			fmt.Println(cheap)
			*cheapeastPaths = append(*cheapeastPaths, cheap)
			return walk(nextCoord, cheapeastPaths)
		}
	}

	var path []Cheap
	result := *walk(startCoord, &path)

	travelCost := 0
	for _, r := range result {
		travelCost += int(r.value)
	}

	return travelCost
}

func main() {

	fmt.Println(cheapeastWalk(getInput(), Coord{x: 0, y: 0}))

}
