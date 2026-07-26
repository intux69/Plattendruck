package com.intux.plattendruck.calculation

import com.intux.plattendruck.model.MeasurementPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class EvCalculatorTest {

    @Test
    fun testEvCalculationWithSampleData() {
        val diameter = 300.0 // 300mm plate
        val sigmaMax = 0.5
        val leverRatio = 2.0

        // Sample data points from typical test (input in 0.01 mm)
        // Values here are already multiplied by 2 (leverRatio) for the test
        val firstLoadingPoints = listOf(
            MeasurementPoint(0.0, 0.0),
            MeasurementPoint(0.08, 0.35 * leverRatio),
            MeasurementPoint(0.16, 0.68 * leverRatio),
            MeasurementPoint(0.25, 1.05 * leverRatio),
            MeasurementPoint(0.33, 1.45 * leverRatio),
            MeasurementPoint(0.42, 1.88 * leverRatio),
            MeasurementPoint(0.50, 2.30 * leverRatio)
        )

        val ev1 = EvCalculator.calculateEv(firstLoadingPoints, diameter, sigmaMax)
        
        // Expected Ev1 adjusted for lever ratio 2.0 and DIN factor 0.75 * D
        // This should match the "full height" expected results now.
        assertEquals(90.96, ev1, 1.0)

        val secondLoadingPoints = listOf(
            MeasurementPoint(0.0, 1.60 * leverRatio),
            MeasurementPoint(0.08, 1.75 * leverRatio),
            MeasurementPoint(0.16, 1.90 * leverRatio),
            MeasurementPoint(0.25, 2.05 * leverRatio),
            MeasurementPoint(0.33, 2.20 * leverRatio),
            MeasurementPoint(0.42, 2.35 * leverRatio)
        )

        val ev2 = EvCalculator.calculateEv(secondLoadingPoints, diameter, sigmaMax)
        
        // Expected Ev2
        assertEquals(247.52, ev2, 1.0)
    }
}
