package com.intux.plattendruck.calculation

import com.intux.plattendruck.model.MeasurementPoint
import kotlin.math.pow
import kotlin.math.round

data class RegressionCoefficients(
    val a0: Double,
    val a1: Double,
    val a2: Double
)

object EvCalculator {

    /**
     * Calculates the deformation modulus Ev in MN/m^2.
     * Formula according to DIN 18134: Ev = 1.5 * r / (a1 + a2 * sigma_max)
     */
    fun calculateEv(points: List<MeasurementPoint>, diameter: Double, sigmaMax: Double): Double {
        val coeffs = calculateRegression(points) ?: return 0.0
        
        // DIN 18134 / PHP formula parity
        // Ev = 1.5 * radius / (a1 + a2 * sigmaMax)
        // Note: coeffs are already rounded to 3 places in calculateRegression
        val result = (1.5 * (diameter / 2.0)) / (coeffs.a1 + coeffs.a2 * sigmaMax)
        
        // PHP rounds final result to 1 decimal place
        return Math.round(result * 10.0) / 10.0
    }

    /**
     * Generates a list of points on the fitted polynomial curve for visualization.
     */
    fun getPolynomialPoints(coeffs: RegressionCoefficients, maxSigma: Double, steps: Int = 50): List<MeasurementPoint> {
        val result = mutableListOf<MeasurementPoint>()
        for (i in 0..steps) {
            val sigma = (maxSigma / steps) * i
            val settlement = coeffs.a0 + coeffs.a1 * sigma + coeffs.a2 * sigma.pow(2)
            result.add(MeasurementPoint(sigma, settlement))
        }
        return result
    }

    /**
     * Fits a 2nd degree polynomial s = a0 + a1*sigma + a2*sigma^2
     * using the method of least squares.
     * PHP parity: uses exactly 6 points and rounds coefficients to 3 decimal places.
     */
    fun calculateRegression(points: List<MeasurementPoint>): RegressionCoefficients? {
        if (points.size < 3) return null

        var sumSigma = 0.0
        var sumSigma2 = 0.0
        var sumSigma3 = 0.0
        var sumSigma4 = 0.0
        var sumS = 0.0
        var sumSSigma = 0.0
        var sumSSigma2 = 0.0

        for (p in points) {
            val s = p.stress
            val h = p.settlement
            sumSigma += s
            sumSigma2 += s.pow(2)
            sumSigma3 += s.pow(3)
            sumSigma4 += s.pow(4)
            sumS += h
            sumSSigma += h * s
            sumSSigma2 += h * s.pow(2)
        }

        val n = points.size.toDouble()

        val matrix = arrayOf(
            doubleArrayOf(n, sumSigma, sumSigma2),
            doubleArrayOf(sumSigma, sumSigma2, sumSigma3),
            doubleArrayOf(sumSigma2, sumSigma3, sumSigma4)
        )
        val constants = doubleArrayOf(sumS, sumSSigma, sumSSigma2)

        val result = solve3x3(matrix, constants) ?: return null
        
        // PHP Parity: Round coefficients to 3 decimal places
        return RegressionCoefficients(
            Math.round(result[0] * 1000.0) / 1000.0,
            Math.round(result[1] * 1000.0) / 1000.0,
            Math.round(result[2] * 1000.0) / 1000.0
        )
    }

    private fun solve3x3(matrix: Array<DoubleArray>, constants: DoubleArray): DoubleArray? {
        val det = determinant3x3(matrix)
        if (det == 0.0) return null

        val x = determinant3x3(replaceColumn(matrix, constants, 0)) / det
        val y = determinant3x3(replaceColumn(matrix, constants, 1)) / det
        val z = determinant3x3(replaceColumn(matrix, constants, 2)) / det

        return doubleArrayOf(x, y, z)
    }

    private fun determinant3x3(m: Array<DoubleArray>): Double {
        return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
                m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
                m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0])
    }

    private fun replaceColumn(matrix: Array<DoubleArray>, column: DoubleArray, colIndex: Int): Array<DoubleArray> {
        val result = Array(3) { DoubleArray(3) }
        for (i in 0..2) {
            for (j in 0..2) {
                result[i][j] = if (j == colIndex) column[i] else matrix[i][j]
            }
        }
        return result
    }
}
