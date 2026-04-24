package com.example.lr3bmimonitor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lr3bmimonitor.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lastResultText: String = ""
    private val historyItems = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadHistory()

        binding.btnCalculate.setOnClickListener {
            calculateBmi()
        }

        binding.btnSave.setOnClickListener {
            saveCurrentResult()
        }
    }

    private fun calculateBmi() {
        val heightText = binding.etHeight.text.toString().trim()
        val weightText = binding.etWeight.text.toString().trim()

        if (heightText.isEmpty() || weightText.isEmpty()) {
            binding.tvResult.text = "Введите рост и вес"
            return
        }

        val heightCm = heightText.toDoubleOrNull()
        val weightKg = weightText.toDoubleOrNull()

        if (heightCm == null || weightKg == null || heightCm <= 0 || weightKg <= 0) {
            binding.tvResult.text = "Введите корректные числовые значения"
            return
        }

        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)

        val category = when {
            bmi < 18.5 -> "Недостаточная масса тела"
            bmi < 25.0 -> "Нормальная масса тела"
            bmi < 30.0 -> "Избыточная масса тела"
            else -> "Ожирение"
        }

        lastResultText = String.format(
            Locale.US,
            "Рост: %.1f см\nВес: %.1f кг\nИМТ: %.2f\nКатегория: %s",
            heightCm,
            weightKg,
            bmi,
            category
        )

        binding.tvResult.text = lastResultText
    }

    private fun saveCurrentResult() {
        if (lastResultText.isBlank()) {
            binding.tvResult.text = "Сначала выполните расчёт ИМТ"
            return
        }

        historyItems.add(0, lastResultText)
        saveHistory()
        renderHistory()
    }

    private fun renderHistory() {
        if (historyItems.isEmpty()) {
            binding.tvHistory.text = "История пока пуста"
            return
        }

        binding.tvHistory.text = historyItems.joinToString(
            separator = "\n\n--------------------\n\n"
        )
    }

    private fun saveHistory() {
        val joined = historyItems.joinToString(separator = "|||")
        getSharedPreferences("bmi_monitor_prefs", MODE_PRIVATE)
            .edit()
            .putString("history", joined)
            .apply()
    }

    private fun loadHistory() {
        val joined = getSharedPreferences("bmi_monitor_prefs", MODE_PRIVATE)
            .getString("history", "") ?: ""

        historyItems.clear()

        if (joined.isNotBlank()) {
            historyItems.addAll(joined.split("|||").filter { it.isNotBlank() })
        }

        renderHistory()
    }
}