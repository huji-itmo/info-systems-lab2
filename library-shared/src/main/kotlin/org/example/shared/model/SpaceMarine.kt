package org.example.shared.model

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.example.shared.model.Weapon
import java.time.LocalDateTime

@Entity
@Table(name = "space_marines")
data class SpaceMarine(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,
    @field:NotBlank
    @Column(nullable = false)
    var name: String,
    @field:NotNull
    @Column(name = "coordinates_id", nullable = false)
    var coordinatesId: Long,
    @field:NotNull
    @Column(name = "creation_date", nullable = false, updatable = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    var creationDate: LocalDateTime = LocalDateTime.now(),
    @field:NotNull
    @Column(name = "chapter_id", nullable = false)
    var chapterId: Long,
    @field:NotNull
    @field:Positive
    @Column(nullable = false)
    var health: Long,
    @Column
    var loyal: Boolean? = null,
    @Enumerated(EnumType.STRING)
    @Column
    var category: AstartesCategory? = null,
    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "weapon_type", nullable = false)
    var weaponType: Weapon,
)
