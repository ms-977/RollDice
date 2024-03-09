package com.example.rolldice

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.rolldice.databinding.ActivityMainBinding
import kotlin.properties.Delegates
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val dieFaceIds by lazy {
        arrayOf(
            R.drawable.die_one, R.drawable.die_two,
            R.drawable.die_three, R.drawable.die_four,
            R.drawable.die_five, R.drawable.die_six
        )
    }
    private var expectedAnswer by Delegates.notNull<Int>()
    private var mainscore = 0
    private var totalScore1 = 0
    private var totalScore2 = 0
    private var curJackpot = 5
    private var currentPlayer = "P1"
    private var gotJackpot = false
    private val winningScore = 20

    private val rollDieButtonClickListener = View.OnClickListener {
        val dieNumber = rollDie()
        val problemInfo = when (dieNumber) {
            in 1..3 -> generateProblemText(dieNumber, getSymbol(dieNumber))
            4 -> {
                gotJackpot = true
                Triple(0, 0, "Roll again to try for jackpot")
            }
            5 -> {
                handleLoseTurn()
                return@OnClickListener
            }
            6 -> {
                gotJackpot = true
                Triple(0, 0, "Roll again to try for jackpot")
            }
            else -> Triple(0, 0, "")
        }

        updateUI(problemInfo.third)
    }

    private fun getSymbol(operation: Int): String {
        return when (operation) {
            1 -> "+"
            2 -> "-"
            3 -> "*"
            else -> ""
        }
    }

    private val guessButtonClickListener = View.OnClickListener {
        val curAnswer = binding.answercontainer.text.toString()

        if (curAnswer == expectedAnswer.toString()) {
            handleCorrectGuess()
        } else {
            handleWrongGuess()
        }

        checkWinner()
    }

    private fun updateUI(message: String) {
        setAllGameValues()
        binding.mathproblem.text = message
    }

    private fun setAllGameValues() {
        binding.tvtotalpoints1.text = totalScore1.toString()
        binding.tvtotalpoints2.text = totalScore2.toString()
        binding.jackpotamount.text = curJackpot.toString()
        binding.currentplayervalue.text = currentPlayer
    }

    private fun rollDie(): Int {
        val dieNumber = Random.nextInt(1, 7)
        binding.maindie.setImageResource(dieFaceIds[dieNumber - 1])
        return dieNumber
    }

    private fun generateProblemText(operation: Int, symbol: String): Triple<Int, Int, String> {
        val maxRange = when (operation) {
            in 1..2 -> 100
            3 -> 20
            else -> 0
        }

        val num1 = Random.nextInt(maxRange + 1)
        val num2 = Random.nextInt(maxRange + 1)

        expectedAnswer = when (operation) {
            1 -> num1 + num2
            2 -> num1 - num2
            3 -> num1 * num2
            else -> 0
        }

        return Triple(num1, num2, "$num1 $symbol $num2 = ")
    }

    private fun handleLoseTurn() {
        gotJackpot = false
        switchPlayer()
        updateUI("You Lose! $currentPlayer turn")
    }

    private fun handleCorrectGuess() {
        addPoints()
        switchPlayer()
        updateUI("Correct! $currentPlayer turn")
    }

    private fun handleWrongGuess() {
        curJackpot += mainscore
        switchPlayer()
        updateUI("Wrong Guess! $currentPlayer turn")
    }

    private fun addPoints() {
        // Determine the correct player's total score
        val currentPlayerScore = if (currentPlayer == "P1") totalScore1 else totalScore2

        // Generate a random problem (1 for addition, 2 for subtraction, 3 for multiplication)
        val problemType = Random.nextInt(1, 4)

        // Generate the problem text and get the expected answer
        val (num1, num2, problemText) = generateProblemText(problemType, getSymbol(problemType))

        // Update the player's score based on the type of problem
        val pointsToAdd = when (problemType) {
            1 -> 1 // Addition problem, add 1 point
            2 -> 2 // Subtraction problem, add 2 points
            3 -> 3 // Multiplication problem, add 3 points
            else -> 0 // Default case
        }

        // Update the player's score, considering the jackpot
        val updatedScore = currentPlayerScore + pointsToAdd

        // Update the total score for the current player
        if (currentPlayer == "P1") {
            totalScore1 = updatedScore
        } else {
            totalScore2 = updatedScore
        }

        // Display the problem text
        updateUI(problemText)

        // Reset mainscore for the next round
        mainscore = 0
    }

    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == "P1") "P2" else "P1"
    }

    private fun checkWinner() {
        when {
            totalScore1 >= winningScore -> {
                updateUI("Player 1 Wins!")
                disableButtons()
            }
            totalScore2 >= winningScore -> {
                updateUI("Player 2 Wins!")
                disableButtons()
            }
        }
    }

    private fun disableButtons() {
        binding.guessbutton.isEnabled = false
        binding.RolleDiceButton.isEnabled = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAllGameValues()

        binding.RolleDiceButton.setOnClickListener(rollDieButtonClickListener)
        binding.guessbutton.setOnClickListener(guessButtonClickListener)
    }
}