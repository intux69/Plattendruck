package com.intux.plattendruck

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.intux.plattendruck.calculation.EvCalculator
import com.intux.plattendruck.model.MeasurementPoint

class MainActivity : AppCompatActivity() {

    private var currentStep = 1
    private val totalSteps = 5

    private lateinit var layoutStepSettings: LinearLayout
    private lateinit var layoutStepLoad1: LinearLayout
    private lateinit var layoutStepUnload: LinearLayout
    private lateinit var layoutStepLoad2: LinearLayout
    private lateinit var layoutStepResult: LinearLayout
    private lateinit var resultChart: LineChart

    private lateinit var txtStepTitle: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        // Initialize layouts
        layoutStepSettings = findViewById(R.id.layout_step_settings)
        layoutStepLoad1 = findViewById(R.id.layout_step_load1)
        layoutStepUnload = findViewById(R.id.layout_step_unload)
        layoutStepLoad2 = findViewById(R.id.layout_step_load2)
        layoutStepResult = findViewById(R.id.layout_step_result)
        resultChart = findViewById(R.id.result_chart)

        txtStepTitle = findViewById(R.id.txt_step_title)
        btnPrev = findViewById(R.id.btn_prev)
        btnNext = findViewById(R.id.btn_next)

        updateStepVisibility()

        btnPrev.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateStepVisibility()
            }
        }
    }

    private fun updateStepVisibility() {
        layoutStepSettings.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
        layoutStepLoad1.visibility = if (currentStep == 2) View.VISIBLE else View.GONE
        layoutStepUnload.visibility = if (currentStep == 3) View.VISIBLE else View.GONE
        layoutStepLoad2.visibility = if (currentStep == 4) View.VISIBLE else View.GONE
        layoutStepResult.visibility = if (currentStep == 5) View.VISIBLE else View.GONE

        btnPrev.visibility = if (currentStep > 1) View.VISIBLE else View.GONE
        btnNext.text = if (currentStep == totalSteps - 1) "Berechnen" else if (currentStep == totalSteps) "Neu starten" else "Weiter"
        
        btnNext.setOnClickListener {
            if (currentStep == totalSteps) {
                currentStep = 1
                updateStepVisibility()
            } else {
                currentStep++
                if (currentStep == totalSteps) {
                    calculateAndShowResults()
                }
                updateStepVisibility()
            }
        }

        txtStepTitle.text = when (currentStep) {
            1 -> "Schritt 1: Einstellungen"
            2 -> "Schritt 2: Erstbelastung"
            3 -> "Schritt 3: Entlastung"
            4 -> "Schritt 4: Zweitbelastung"
            5 -> "Schritt 5: Ergebnis"
            else -> ""
        }
    }

    private fun calculateAndShowResults() {
        val txtSummary = findViewById<TextView>(R.id.txt_result_summary)

        try {
            val diameter = findViewById<EditText>(R.id.edit_diameter).text.toString().toDoubleOrNull() ?: 300.0
            val leverRatio = findViewById<EditText>(R.id.edit_lever_ratio).text.toString().toDoubleOrNull() ?: 2.0

            // 1. Erstbelastung (User request: 0.08, 0.16, 0.25, 0.33, 0.42, 0.50)
            val firstLoading = getPoints(
                listOf(0.08, 0.16, 0.25, 0.33, 0.42, 0.50),
                listOf(R.id.edit_load_1, R.id.edit_load_2, R.id.edit_load_3, R.id.edit_load_4, R.id.edit_load_5, R.id.edit_load_6),
                leverRatio
            )
            
            // 2. Entlastung (0.25, 0.12, 0.00)
            val unloading = getPoints(
                listOf(0.25, 0.12, 0.0),
                listOf(R.id.edit_unload_1, R.id.edit_unload_2, R.id.edit_unload_3),
                leverRatio
            )
            
            // 3. Zweitbelastung (User request: 0.08, 0.16, 0.25, 0.33, 0.42)
            val secondLoadingRaw = getPoints(
                listOf(0.08, 0.16, 0.25, 0.33, 0.42),
                listOf(R.id.edit_reload_1, R.id.edit_reload_2, R.id.edit_reload_3, R.id.edit_reload_4, R.id.edit_reload_5),
                leverRatio
            )

            // For Ev2 calculation, DIN starts with residual settlement (stress 0)
            val residualSettlement = unloading.lastOrNull()?.settlement ?: 0.0
            val secondLoadingForRegression = listOf(MeasurementPoint(0.0, residualSettlement)) + secondLoadingRaw

            // CALCULATION (PHP PARITY)
            // Use max stress from FIRST cycle (0.50) for both Ev1 and Ev2 calculation
            val sigmaMax = 0.50 

            val ev1 = EvCalculator.calculateEv(firstLoading, diameter, sigmaMax)
            val ev2 = EvCalculator.calculateEv(secondLoadingForRegression, diameter, sigmaMax)
            val ratio = if (ev1 > 0) ev2 / ev1 else 0.0

            val summary = StringBuilder()
            summary.append("Durchmesser: ${diameter.toInt()} mm\n")
            summary.append("Verhältnis (Messeinrichtung): $leverRatio\n\n")
            summary.append(String.format("EV1 = %.1f MPa\n", ev1))
            summary.append(String.format("EV2 = %.1f MPa\n", ev2))
            summary.append(String.format("EV2 / EV1 = %.2f", ratio))

            txtSummary.text = summary.toString()

            updateChart(firstLoading, unloading, secondLoadingForRegression)

        } catch (e: Exception) {
            txtSummary.text = "Fehler: ${e.message}"
        }
    }

    private fun updateChart(
        firstLoading: List<MeasurementPoint>,
        unloading: List<MeasurementPoint>,
        secondLoading: List<MeasurementPoint>
    ) {
        val ubuntu = ResourcesCompat.getFont(this, R.font.ubuntu)

        resultChart.description.isEnabled = true
        resultChart.description.text = "Normalspannung [MN/m²] / Setzung [mm]"
        resultChart.description.typeface = ubuntu
        resultChart.setDrawGridBackground(true)
        resultChart.setBackgroundColor(Color.WHITE)

        val xAxis = resultChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.typeface = ubuntu
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 0.6f

        val leftAxis = resultChart.axisLeft
        leftAxis.isInverted = true
        leftAxis.setDrawGridLines(true)
        leftAxis.typeface = ubuntu
        leftAxis.axisMinimum = 0f

        resultChart.axisRight.isEnabled = false

        val dataSets = mutableListOf<LineDataSet>()

        // --- FIRST LOADING (BLUE) ---
        val coeffs1 = EvCalculator.calculateRegression(firstLoading)
        if (coeffs1 != null) {
            val curve1 = EvCalculator.getPolynomialPoints(coeffs1, 0.50)
            val set1Curve = LineDataSet(curve1.map { Entry(it.stress.toFloat(), it.settlement.toFloat()) }, "Erstbelastung (Kurve)")
            set1Curve.color = Color.BLUE
            set1Curve.lineWidth = 2.5f
            set1Curve.setDrawCircles(false)
            set1Curve.mode = LineDataSet.Mode.CUBIC_BEZIER
            dataSets.add(set1Curve)
        }
        val set1Points = LineDataSet(firstLoading.map { Entry(it.stress.toFloat(), it.settlement.toFloat()) }, "Points1")
        set1Points.lineWidth = 0f
        set1Points.setCircleColor(Color.BLUE)
        set1Points.circleRadius = 4f
        set1Points.setDrawCircleHole(false)
        dataSets.add(set1Points)

        // --- UNLOADING (RED) ---
        val lastLoad1 = firstLoading.lastOrNull()
        val unloadingPointsForFit = if (lastLoad1 != null) listOf(lastLoad1) + unloading else unloading
        val coeffsUnload = EvCalculator.calculateRegression(unloadingPointsForFit)
        if (coeffsUnload != null) {
            val curveUnload = EvCalculator.getPolynomialPoints(coeffsUnload, 0.50)
            val setUnloadCurve = LineDataSet(curveUnload.map { Entry(it.stress.toFloat(), it.settlement.toFloat()) }, "Entlastung (Kurve)")
            setUnloadCurve.color = Color.RED
            setUnloadCurve.lineWidth = 2.5f
            setUnloadCurve.setDrawCircles(false)
            setUnloadCurve.mode = LineDataSet.Mode.CUBIC_BEZIER
            setUnloadCurve.enableDashedLine(10f, 10f, 0f)
            dataSets.add(setUnloadCurve)
        }
        val setUnloadPoints = LineDataSet(unloading.map { Entry(it.stress.toFloat(), it.settlement.toFloat()) }, "PointsUnload")
        setUnloadPoints.lineWidth = 0f
        setUnloadPoints.setCircleColor(Color.RED)
        setUnloadPoints.circleRadius = 4f
        setUnloadPoints.setDrawCircleHole(false)
        dataSets.add(setUnloadPoints)

        // --- SECOND LOADING (BLACK) ---
        val coeffs2 = EvCalculator.calculateRegression(secondLoading)
        if (coeffs2 != null) {
            val curve2 = EvCalculator.getPolynomialPoints(coeffs2, 0.50)
            val set2Curve = LineDataSet(curve2.map { Entry(it.stress.toFloat(), it.settlement.toFloat()) }, "Zweitbelastung (Kurve)")
            set2Curve.color = Color.BLACK
            set2Curve.lineWidth = 2.5f
            set2Curve.setDrawCircles(false)
            set2Curve.mode = LineDataSet.Mode.CUBIC_BEZIER
            dataSets.add(set2Curve)
        }
        val set2Points = LineDataSet(secondLoading.map { Entry(it.stress.toFloat(), it.settlement.toFloat()) }, "Points2")
        set2Points.lineWidth = 0f
        set2Points.setCircleColor(Color.BLACK)
        set2Points.circleRadius = 4f
        set2Points.setDrawCircleHole(false)
        dataSets.add(set2Points)

        dataSets.forEach { set ->
            set.valueTextSize = 0f
        }

        resultChart.data = LineData(dataSets.toList())
        
        val entries = listOf(
            com.github.mikephil.charting.components.LegendEntry("Erstbelastung (Kurve)", com.github.mikephil.charting.components.Legend.LegendForm.LINE, 10f, 2f, null, Color.BLUE),
            com.github.mikephil.charting.components.LegendEntry("Entlastung (Kurve)", com.github.mikephil.charting.components.Legend.LegendForm.LINE, 10f, 2f, null, Color.RED),
            com.github.mikephil.charting.components.LegendEntry("Zweitbelastung (Kurve)", com.github.mikephil.charting.components.Legend.LegendForm.LINE, 10f, 2f, null, Color.BLACK)
        )
        resultChart.legend.setCustom(entries)
        resultChart.legend.typeface = ubuntu
        resultChart.invalidate()
    }

    private fun getPoints(stresses: List<Double>, ids: List<Int>, leverRatio: Double): List<MeasurementPoint> {
        return stresses.zip(ids).map { (stress, id) ->
            val value = findViewById<EditText>(id).text.toString().toDoubleOrNull() ?: 0.0
            MeasurementPoint(stress, (value / 100.0) * leverRatio)
        }
    }
}
