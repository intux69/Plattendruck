package com.intux.plattendruck.model

data class MeasurementPoint(
    val stress: Double, // sigma in MN/m^2 (MPa)
    val settlement: Double // s in mm
)

data class PlateLoadTest(
    val diameter: Double = 300.0, // mm
    val firstLoading: List<MeasurementPoint> = emptyList(),
    val unloading: List<MeasurementPoint> = emptyList(),
    val secondLoading: List<MeasurementPoint> = emptyList()
) {
    val radius: Double get() = diameter / 2.0
}
