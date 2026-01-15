package com.example.periodvibe.data.local.converter

import androidx.room.TypeConverters

@TypeConverters(
    LocalDateConverter::class,
    LocalDateTimeConverter::class
)
class Converters
