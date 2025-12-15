package com.example.fileService.repositories

import org.example.shared.model.SpaceMarine
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SpaceMarineRepository : CrudRepository<SpaceMarine, Long>