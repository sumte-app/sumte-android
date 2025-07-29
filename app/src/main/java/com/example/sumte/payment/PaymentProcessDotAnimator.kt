package com.example.sumte.payment

import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.example.sumte.R

class PaymentProcessDotAnimator(
    private val dots: List<ImageView>
) {
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    private val dotAnimationRunnable = object : Runnable {
        override fun run() {

            dots.forEach { it.setImageResource(R.drawable.dot_gray) }


            dots[currentIndex].setImageResource(R.drawable.dot_green)


            currentIndex = (currentIndex + 1) % dots.size

            handler.postDelayed(this, 300)
        }
    }

    fun start() {
        handler.post(dotAnimationRunnable)
    }

    fun stop() {
        handler.removeCallbacks(dotAnimationRunnable)
    }
}
