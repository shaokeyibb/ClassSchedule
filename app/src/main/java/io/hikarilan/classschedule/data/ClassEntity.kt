package io.hikarilan.classschedule.data

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(primaryKeys = ["week", "class_number"])
data class ClassEntity(
    @ColumnInfo(name = "week") val week: Int,
    @ColumnInfo(name = "class_number") val classNumber: Int,
    @ColumnInfo(name = "class_name") var className: String,
    @ColumnInfo(name = "location") var location: String,
    @ColumnInfo(name = "teacher") var teacher: String,
    @ColumnInfo(name = "availableWeeks") var availableWeeks: String,
) {
    companion object {
        fun fillEntities(
            origin: List<ClassEntity>,
            maxWeek: Int,
            maxClassNumber: Int
        ): List<ClassEntity> {
            val list = mutableListOf<ClassEntity>()
            for (classNumber in 1..maxClassNumber) {
                for (week in 1..maxWeek) {
                    origin.find { it.week == week && it.classNumber == classNumber }.let {
                        if (it != null) list.add(it)
                        else list.add(ClassEntity(week, classNumber, "", "", "", ""))
                    }
                }
            }
            return list
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassEntity

        if (week != other.week) return false
        if (classNumber != other.classNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = week
        result = 31 * result + classNumber
        return result
    }


}
