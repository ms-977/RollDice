package com.example.rolldice
import android.view.View
import android.os.Bundle
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
    private var score1 = 0
    private var score2 = 0
    private var currentJackpot = 5
    private var currentPlayer = "P1"
    private var gotjackpot = false
    private var gotdouble = false
    private val winingscore = 20

    private val rollDieButtonClickListener = View.OnClickListener {
        val dieNumber = rollDie()
        val problemInfo = when (dieNumber) {
            1, 2, 3 -> generateProblemText(dieNumber, getSymbol(dieNumber))
            4 -> {
                gotdouble = true
                Triple(0, 0, "Roll again for 2x points")
            }
            5 -> {
                handleLoseTurn()
                return@OnClickListener
            }
            6 -> {
                gotjackpot = true
                Triple(0, 0, "Roll again to try for jackpot")
            }
            else -> Triple(0, 0, "")
        }

        binding.mathproblem.text = problemInfo.third
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

        //resetGameValues()
        checkWinner()
    }

    private fun setAllGameValues() {
        binding.tvtotalpoints1.text = score1.toString()
        binding.tvtotalpoints2.text = score2.toString()
        binding.jackpotamount.text = currentJackpot.toString()
        binding.currentplayervalue.text = currentPlayer
    }

    private fun rollDie(): Int {
        val dieNumber = Random.nextInt(1, 7)
        binding.maindie.setImageResource(dieFaceIds[dieNumber - 1])
        return dieNumber
    }

    private fun generateProblemText(operation: Int, symbol: String): Triple<Int, Int, String> {
        val maxRange = when (operation) {
            1, 2 -> 100
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
        mainscore = 0
        gotdouble = false
        gotjackpot = false
        switchPlayer()
        setAllGameValues()
        binding.mathproblem.text = "You Lose!  " +currentPlayer+"turn"
    }

    private fun handleCorrectGuess() {
        addPoints()
        switchPlayer()
        binding.mathproblem.text = "Correct!" + currentPlayer+"turn"
    }

    private fun handleWrongGuess() {
        currentJackpot += mainscore
        switchPlayer()
        binding.mathproblem.text = "Wrong Guess!"+ currentPlayer+"turn"
    }

    private fun addPoints() {
        when {
            currentPlayer == "P1" -> score1 += mainscore
            gotjackpot -> score1 += currentJackpot
            else -> score2 += mainscore
        }

        if (gotjackpot) {
            if (currentPlayer == "P1") {
                score1 += currentJackpot
            } else {
                score2 += currentJackpot
            }

            currentJackpot = 5
        }
    }

    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == "P1") "P2" else "P1"
    }



    private fun checkWinner() {
        when {
            score1 >= winingscore -> {
                binding.mathproblem.text = "Player 1 Wins!"
                disableButtons()
            }
            score2 >= winingscore -> {
                binding.mathproblem.text = "Player 2 Wins!"
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