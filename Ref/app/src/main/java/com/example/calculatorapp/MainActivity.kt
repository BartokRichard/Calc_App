package com.example.calculatorapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var tvFormula: TextView
    private lateinit var tvResult: TextView
    private var formula: String = ""
    private var result: String = ""
    private var lastNumeric: Boolean = false
    private var stateError: Boolean = false
    private var lastDot: Boolean = false
    private var isCleared: Boolean = false


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set status- and nav-bar colors
        window.statusBarColor = getColor(R.color.background_color)
        window.navigationBarColor = getColor(R.color.nav_background_color)

        tvFormula = findViewById(R.id.tvFormula)
        tvResult = findViewById(R.id.tvResult)

        findViewById<View>(R.id.btnCurrency).setOnClickListener {
            val intent = Intent(this, CurrencyExchangeActivity::class.java)
            startActivity(intent)
        }

        setNumericOnClickListener()
        setOperatorOnClickListener()
    }
    private fun setNumericOnClickListener() {
        val listener = View.OnClickListener { v ->
            val button = v as MaterialButton
            if (stateError) {
                tvResult.text = button.text
                stateError = false
            } else {
                if (tvResult.text == "0" || isCleared) {
                    tvResult.text = button.text
                    isCleared = false
                } else {
                    tvResult.append(button.text)
                }
            }
            lastNumeric = true
            lastDot = false
        }

        findViewById<MaterialButton>(R.id.zero).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.one).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.two).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.three).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.four).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.five).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.six).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.seven).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.eight).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.nine).setOnClickListener(listener)
    }

    private fun setOperatorOnClickListener() {
        val listener = View.OnClickListener { v ->
            val button = v as MaterialButton
            if (lastNumeric && !stateError) {
                tvResult.append(button.text)
                lastNumeric = false
                lastDot = false
            } else if (!lastNumeric && !stateError) {
                // Replace the operator with a new one, if the last character was an operator
                val text = tvResult.text.toString()
                tvResult.text = text.substring(0, text.length - 1) + button.text
            }
        }

        findViewById<MaterialButton>(R.id.plus).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.mines).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.multiply).setOnClickListener(listener)
        findViewById<MaterialButton>(R.id.devide).setOnClickListener(listener)

        findViewById<MaterialButton>(R.id.dot).setOnClickListener {
            if (lastNumeric && !stateError && !lastDot) {
                val text = tvResult.text.toString()
                val lastNumber = text.split(Regex("[+\\-*/]")).lastOrNull()
                if (lastNumber != null && !lastNumber.contains(".")) {
                    tvResult.append(".")
                    lastNumeric = false
                    lastDot = true
                }
            }
        }

        findViewById<MaterialButton>(R.id.clear).setOnClickListener {
            tvResult.text = "0"
            tvFormula.text = ""
            stateError = false
            lastDot = false
            lastNumeric = false
            formula = ""
            result = ""
            isCleared = true
            tvResult.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 50f)
        }

        findViewById<MaterialButton>(R.id.equal).setOnClickListener {
            onEqual()
        }
    }

    private fun onEqual() {
        if (lastNumeric && !stateError) {
            val txt = tvResult.text.toString()
            formula = txt
            tvFormula.text = formula
            try {
                val result = evaluate(formula)
                tvResult.text = result.toString()
                lastDot = true
            } catch (e: Exception) {
                tvResult.text = "Error"
                stateError = true
                lastNumeric = false
            }
        }
    }

    private fun evaluate(formula: String): Double {
        return object {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < formula.length) formula[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                return if (ch == charToEat) {
                    nextChar()
                    true
                } else {
                    false
                }
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < formula.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+'.toInt()) -> x + parseTerm()
                        eat('-'.toInt()) -> x - parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('*'.toInt()) -> x * parseFactor()
                        eat('/'.toInt()) -> x / parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor()
                if (eat('-'.toInt())) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.toInt())) {
                    x = parseExpression()
                    eat(')'.toInt())
                } else if ((ch >= '0'.toInt() && ch <= '9'.toInt()) || ch == '.'.toInt()) {
                    while ((ch >= '0'.toInt() && ch <= '9'.toInt()) || ch == '.'.toInt()) nextChar()
                    x = formula.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                return x
            }
        }.parse()
    }
}
