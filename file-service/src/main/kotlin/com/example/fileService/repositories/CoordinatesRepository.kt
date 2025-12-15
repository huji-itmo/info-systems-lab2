package com.example.fileService.repositories

import org.example.shared.model.Coordinates
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CoordinatesRepository : CrudRepository<Coordinates, Long> {}