package ru.glack.pedometer.data.model

import ru.glack.pedometer.data.EnumStepState

data class StepsModel(
    val date: String,
    val firstStep: StepModel,
    val lastStep: StepModel
)

data class StepModel(
    val stepState: EnumStepState,
    val timestamp: String
)