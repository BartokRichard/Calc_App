package com.example.calculatorapp

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.calculatorapp.api.CurrencyData
import com.example.calculatorapp.api.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class CurrencyExchangeActivity : AppCompatActivity() {

    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var tvFromAmount: TextView
    private lateinit var tvFromCurrency: TextView
    private lateinit var tvToAmount: TextView
    private lateinit var tvToCurrency: TextView

    private var amount: String = ""
    private var exchangeRates: Map<String, CurrencyData> = emptyMap()
    private val decimalFormat = DecimalFormat("#.####")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_exchange)

        spinnerFrom = findViewById(R.id.spinnerFrom)
        spinnerTo = findViewById(R.id.spinnerTo)
        tvFromAmount = findViewById(R.id.tvFromAmount)
        tvFromCurrency = findViewById(R.id.tvFromCurrency)
        tvToAmount = findViewById(R.id.tvToAmount)
        tvToCurrency = findViewById(R.id.tvToCurrency)

        setupSpinner(spinnerFrom, tvFromCurrency)
        setupSpinner(spinnerTo, tvToCurrency)

        tvFromAmount.text = amount

        // Navigate back to MainActivity on button click
        findViewById<ImageButton>(R.id.btnCalc).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Fetch exchange rates
        fetchExchangeRates()
    }

    private fun setupSpinner(spinner: Spinner, textView: TextView) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.currencies,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                textView.text = parent.getItemAtPosition(position).toString()
                updateExchangeAmount()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchExchangeRates() {
        lifecycleScope.launch {
            val response = RetrofitClient.apiService.getExchangeRates("cur_live_LP6wSUVD2roCLzXjESmj3cRJ53L1ZvZuTZtcR3y7") // API key
            if (response.isSuccessful && response.body() != null) {
                exchangeRates = response.body()!!.data
                updateExchangeAmount()
            }
        }
    }

    fun onNumberClick(view: android.view.View) {
        val button = view as Button
        val buttonText = button.text.toString()

        // Check if the button text is a decimal point
        if (buttonText == ".") {
            // Check if the amount already contains a decimal point
            if (!amount.contains(".")) {
                amount += buttonText
            }
        } else {
            amount += buttonText
        }

        tvFromAmount.text = amount
        updateExchangeAmount()
    }

    fun onClearClick(view: android.view.View) {
        amount = ""
        tvFromAmount.text = amount
        tvToAmount.text = "0"
    }

    private fun updateExchangeAmount() {
        val fromCurrency = tvFromCurrency.text.toString()
        val toCurrency = tvToCurrency.text.toString()

        val fromRate = exchangeRates[fromCurrency]?.value ?: 1.0
        val toRate = exchangeRates[toCurrency]?.value ?: 1.0

        val fromAmount = amount.toDoubleOrNull() ?: 0.0
        val toAmount = fromAmount * (toRate / fromRate)

        tvToAmount.text = decimalFormat.format(toAmount)
    }
}
