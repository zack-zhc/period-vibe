package com.example.periodvibe.data.mapper

import com.example.periodvibe.data.local.entity.CycleEntity
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.FlowLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleMapper @Inject constructor() {

    fun toDomain(entity: CycleEntity): Cycle {
        return Cycle(
            id = entity.id,
            startDate = entity.startDate,
            endDate = entity.endDate,
            cycleLength = entity.cycleLength,
            periodLength = entity.periodLength,
            averageFlowLevel = entity.averageFlowLevel?.let { FlowLevel.valueOf(it) },
            isCompleted = entity.isCompleted,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: Cycle): CycleEntity {
        return CycleEntity(
            id = domain.id,
            startDate = domain.startDate,
            endDate = domain.endDate,
            cycleLength = domain.cycleLength,
            periodLength = domain.periodLength,
            averageFlowLevel = domain.averageFlowLevel?.name,
            isCompleted = domain.isCompleted,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    fun toDomainList(entities: List<CycleEntity>): List<Cycle> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<Cycle>): List<CycleEntity> {
        return domains.map { toEntity(it) }
    }
}
