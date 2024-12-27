package com.example.cristianovsworldcup

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val matrix = Array(10){IntArray(3){0} }
    private val amberViews = mutableMapOf<Pair<Int, Int>, ImageView>()
    private var jackPosition = 1
    lateinit var jackImageView: ImageView
    lateinit var scoreText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var score = 0
    private var amberSpeed = 500L
    private var lives = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        jackImageView = findViewById(R.id.imageView)
        scoreText = findViewById(R.id.scoreText)
        initializeMatrix()
        startBallMovement()
        startAddingBalls()
        updateHearts()

    }

    private fun updateHearts() {
        val heart1 = findViewById<ImageView>(R.id.heart1)
        val heart2 = findViewById<ImageView>(R.id.heart2)
        val heart3 = findViewById<ImageView>(R.id.heart3)

        heart1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        heart2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        heart3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }


    private fun getOrCreateBallView(row: Int, col: Int): ImageView {
        val key = Pair(row, col)

        if (amberViews.containsKey(key)) {
            return amberViews[key]!!
        }

        val newBall = ImageView(this).apply {
            setImageResource(R.drawable.amberface)
            layoutParams = RelativeLayout.LayoutParams(
                dpToPx(100),
                dpToPx(100)
            )
        }

        findViewById<RelativeLayout>(R.id.main).addView(newBall)
        amberViews[key] = newBall

        return newBall
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun addNewBall() {
        val column = Random.nextInt(0, 3)
        matrix[0][column] = 1
        updateUI()
    }

    private fun updateScoreUI() {
        scoreText.text = "Score: $score"
    }

    private fun initializeMatrix() {
        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                matrix[row][col] = 0
            }
        }
        matrix[matrix.size - 1][jackPosition] = 2
    }

    private fun startBallMovement() {
        handler.post(object : Runnable {
            override fun run() {
                moveAllBallsDown()
                adjustSpeed()
                handler.postDelayed(this, amberSpeed)
            }
        })
    }

    private fun adjustSpeed() {
        amberSpeed = when {
            score < 20 -> 750L
            score < 30 -> 650L
            score < 40 -> 500L
            else -> 400L
        }
    }

    private fun moveAllBallsDown() {
        for (row in matrix.size - 1 downTo 1) {
            for (col in matrix[row].indices) {
                if (matrix[row - 1][col] == 1) {
                    matrix[row][col] = 1
                    matrix[row - 1][col] = 0
                }
            }
        }
        handleCollisions()
        updateUI()
    }

    fun move_left(view: View) {
        if (jackPosition > 0) {
            matrix[matrix.size - 1][jackPosition] = 0
            jackPosition -= 1
            matrix[matrix.size - 1][jackPosition] = 2
            updateGoalPosition()
        }
    }

    fun move_right(view: View) {
        if (jackPosition < 2) {
            matrix[matrix.size - 1][jackPosition] = 0
            jackPosition += 1
            matrix[matrix.size - 1][jackPosition] = 2
            updateGoalPosition()
        }
    }


    private fun updateGoalPosition() {
        val params = jackImageView.layoutParams as RelativeLayout.LayoutParams

        when (jackPosition) {
            0 -> {
                params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0)
                params.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
            }
            1 -> {
                params.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
                params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
                params.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
            }
            2 -> {
                params.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
                params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0)
                params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            }
        }
        jackImageView.layoutParams = params
    }

    private fun showCollisionEffect() {
        println("Collision effect triggered")
        jackImageView.setImageResource(R.drawable.pirates)
        Handler(Looper.getMainLooper()).postDelayed({
            jackImageView.setImageResource(R.drawable.jackrunning)
        }, 500)
    }

    private fun startAddingBalls() {
        handler.post(object : Runnable {
            override fun run() {
                addNewBall()

                val delay = when {
                    score < 10 -> 3000L
                    score < 20 -> 2000L
                    score < 35 -> 1500L
                    else -> 850L
                }

                handler.postDelayed(this, delay)
            }
        })
    }

    private fun updateUI() {

        val keysToRemove = mutableListOf<Pair<Int, Int>>()

        for ((key, amberView) in amberViews) {
            val (row, col) = key
            if (row >= matrix.size || matrix[row][col] != 1) {
                (amberView.parent as? RelativeLayout)?.removeView(amberView)
                keysToRemove.add(key)
            }
        }

        keysToRemove.forEach { amberViews.remove(it) }
        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                if (matrix[row][col] == 1) {
                    val ballView = getOrCreateBallView(row, col)
                    ballView.y = row * 200f
                    val params = ballView.layoutParams as RelativeLayout.LayoutParams
                    when (col) {
                        0 -> {
                            params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                            params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0)
                            params.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
                        }
                        1 -> {
                            params.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
                            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
                            params.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
                        }
                        2 -> {
                            params.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
                            params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0)
                            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                        }
                    }
                    ballView.layoutParams = params
                }
            }
        }
    }

    private fun handleCollisions() {
        val lastRow = matrix.size - 2

        for (col in matrix[lastRow].indices) {
            if (matrix[lastRow-1][col] == 1) {
                if (col == jackPosition) {
                    score -= 1
                    lives -= 1
                    updateHearts()
                    showCollisionEffect()

                    if (lives <= 0) {

                        //gameOver()
                    }
                } else {
                    score += 1
                }

                matrix[lastRow][col] = 0
            }
        }
        updateScoreUI()
    }

    private fun gameOver() {
        handler.removeCallbacksAndMessages(null)
        runOnUiThread {
            Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show()
        }
    }


}