import java.util.*

const val UNKNOWN_CELL = "."
const val MINE = "X"
const val USER_CHOOSE = "*"
const val SAFE_CELL = "/"

const val HOR_DEV = "│"
const val VER_DIV = "—"

const val COMMAND_FREE = "free"
const val COMMAND_MINE = "mine"

fun main() {

    println("How many mines do you want on the field?")
    val scanner = Scanner(System.`in`)
    val countMines = scanner.nextInt()
    val mineField = MineField(countMines)

    while (mineField.gameStatus == GameState.IN_PROCESS) {
        var correctChoose = false

        while (!correctChoose) {
            println("Set/delete mines marks (x and y coordinates):")
            val (x, y, command) = readLine()!!.split(" ")
            correctChoose = mineField.checkUserChoose(x.toInt(), y.toInt(), command)
        }
    }

    when (mineField.gameStatus) {
        GameState.WIN -> println("Congratulations! You found all the mines!")
        else -> println("You stepped on a mine and failed!")
    }
}

class MineField(
    private val quantityMines: Int,
    private val size: Int = 9
) {
    private val minefield: MutableList<MutableList<Cell>> = MutableList(size) { x ->
        MutableList(size) { y ->
            Cell(x = x, y = y, value = SAFE_CELL)
        }
    }

    private val viewMinefield: MutableList<MutableList<Cell>> = MutableList(size) { x ->
        MutableList(size) { y ->
            Cell(x = x, y = y, value = UNKNOWN_CELL)
        }
    }

    private val listOpenCells = mutableListOf<Cell>()
    private var quantitySafeCells = size * size - quantityMines
    private var chooseMine = 0
    private var allChoose = 0
    var gameStatus = GameState.IN_PROCESS

    init {
        setMines()
        setPlayerHint()
        printMineField()
    }

    private fun setMines() {
        var setMines = quantityMines

        while (setMines > 0) {
            var isSetMine = false

            while (!isSetMine) {
                val randomLine = minefield.random()
                val randomCell = randomLine.indices.random()
                if (randomLine[randomCell].value != MINE) {
                    randomLine[randomCell].value = MINE
                    isSetMine = true
                }
            }
            setMines--
        }
    }

    private fun setPlayerHint() {
        for (i in minefield.indices) {
            for (j in minefield[i].indices) {

                if (minefield[i][j].value != MINE) {
                    minefield[i][j].value = setHintInCell(i, j)
                }
            }
        }
    }

    private fun setHintInCell(x: Int, y: Int): String {
        var result = SAFE_CELL
        var numberOfMinesAroundTheCell = 0

        for (i in x - 1..x + 1) {
            if (i < 0 || i >= size) continue
            for (j in y - 1..y + 1) {
                if (j < 0 || j >= size) continue
                if (minefield[i][j].value == MINE) {
                    numberOfMinesAroundTheCell++
                }
            }
        }

        if (numberOfMinesAroundTheCell != 0) {
            result = numberOfMinesAroundTheCell.toString()
        }

        return result
    }

    private fun printMineField() {
        printRowNumber()
        printDivider()
        for (i in viewMinefield.indices) {
            print("${i + 1}$HOR_DEV")
            viewMinefield[i].forEach { cell ->
                print(cell.value)
            }
            println(HOR_DEV)
        }
        printDivider()
    }

    private fun printRowNumber() {
        print(" $HOR_DEV")
        for (i in 1..size) print(i)
        println(HOR_DEV)
    }

    private fun printDivider() {
        print("$VER_DIV$HOR_DEV")
        for (i in 1..size) print(VER_DIV)
        println(HOR_DEV)
    }

    fun checkUserChoose(_x: Int, _y: Int, command: String): Boolean {
        val x = _x - 1
        val y = _y - 1
        if (x < 0 || y < 0 || x > size || y > size) return false
        when (command) {
            COMMAND_FREE -> commandFree(x, y)
            COMMAND_MINE -> commandMine(x, y)
            else -> return false
        }
        return true
    }

    private fun commandMine(x: Int, y: Int) {

        when (viewMinefield[y][x].value) {
            UNKNOWN_CELL -> {
                allChoose++
                viewMinefield[y][x].value = USER_CHOOSE
                if (minefield[y][x].value == MINE) chooseMine++
                if (chooseMine == quantityMines) gameStatus = GameState.WIN
            }
            USER_CHOOSE -> {
                allChoose--
                viewMinefield[y][x].value = UNKNOWN_CELL
                if (minefield[y][x].value == MINE) chooseMine--
            }
        }

        printMineField()
    }

    private fun commandFree(x: Int, y: Int) {

        when (minefield[y][x].value) {
            SAFE_CELL -> openSafeCells(y, x)
            MINE -> loseGame()
            else -> openHintCell(y, x)
        }

        printMineField()
    }

    private fun openSafeCells(x: Int, y: Int) {
        for (i in x - 1..x + 1) {
            if (i < 0 || i >= size) continue
            for (j in y - 1..y + 1) {
                if (j < 0 || j >= size) continue

                if (!listOpenCells.contains(viewMinefield[i][j])) {
                    if (minefield[i][j].value == MINE) {
                        return
                    } else if (minefield[i][j].value.toIntOrNull() != null) {
                        openNextCells(i, j)
                        continue
                    } else {
                        openNextCells(i, j)
                        openSafeCells(i, j)
                    }
                }
            }
        }
    }

    private fun openNextCells(i: Int, j: Int) {
        val cell = minefield[i][j].value
        quantitySafeCells--

        if (cell == SAFE_CELL) {
            viewMinefield[i][j] = minefield[i][j]
            listOpenCells.add(viewMinefield[i][j])
        } else if (cell.toIntOrNull() != null) {
            viewMinefield[i][j] = minefield[i][j]
            listOpenCells.add(viewMinefield[i][j])
        }

        if (quantitySafeCells == 0) {
            gameStatus = GameState.WIN
        }

    }

    private fun openHintCell(x: Int, y: Int) {
        quantitySafeCells--
        viewMinefield[x][y].value = minefield[x][y].value
        listOpenCells.add(viewMinefield[x][y])
        if (quantitySafeCells == 0) {
            gameStatus = GameState.WIN
        }
    }

    private fun loseGame() {
        for (i in viewMinefield.indices) {
            for (j in viewMinefield.indices) {
                if (minefield[i][j].value == MINE) {
                    viewMinefield[i][j].value = MINE
                }
            }
        }
        gameStatus = GameState.LOSE
    }

}

enum class GameState {
    WIN, LOSE, IN_PROCESS
}

data class Cell(val x: Int, val y: Int, var value: String = ".")