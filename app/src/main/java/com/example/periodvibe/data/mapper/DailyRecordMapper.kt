package com.example.periodvibe.data.mapper

import com.example.periodvibe.data.local.entity.DailyRecordEntity
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.domain.model.FlowLevel
import com.example.periodvibe.domain.model.Symptom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyRecordMapper @Inject constructor() {

    fun toDomain(entity: DailyRecordEntity): DailyRecord {
        return DailyRecord(
            id = entity.id,
            date = entity.date,
            cycleId = entity.cycleId,
            isPeriod = entity.isPeriod,
            flowLevel = entity.flowLevel?.let { FlowLevel.valueOf(it) },
            symptoms = parseSymptoms(entity.symptoms),
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: DailyRecord): DailyRecordEntity {
        return DailyRecordEntity(
            id = domain.id,
            date = domain.date,
            cycleId = domain.cycleId,
            isPeriod = domain.isPeriod,
            flowLevel = domain.flowLevel?.name,
            symptoms = serializeSymptoms(domain.symptoms),
            notes = domain.notes,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    fun toDomainList(entities: List<DailyRecordEntity>): List<DailyRecord> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(domains: List<DailyRecord>): List<DailyRecordEntity> {
        return domains.map { toEntity(it) }
    }

    private fun serializeSymptoms(symptoms: List<Symptom>): String {
        return symptoms.joinToString(",") { it.name }
    }

    private fun parseSymptoms(symptomsString: String): List<Symptom> {
        if (symptomsString.isBlank()) return emptyList()
        return symptomsString.split(",")
            .mapNotNull { Symptom.values().find { symptom -> symptom.name == it.trim() } }
    }
}
