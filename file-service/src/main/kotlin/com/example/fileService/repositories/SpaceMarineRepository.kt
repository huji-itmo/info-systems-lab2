package com.example.fileService.repositories

import com.example.fileService.model.SpaceMarine
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SpaceMarineRepository : CrudRepository<SpaceMarine, Long>