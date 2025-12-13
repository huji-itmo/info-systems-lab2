package org.example.model.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class WeaponFilterRequest(
    @field:NotEmpty(message = "At least one weapon type must be specified")
    @field:Size(max = 10, message = "Maximum 10 weapon types allowed")
    val weaponTypes: List<String>,
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
