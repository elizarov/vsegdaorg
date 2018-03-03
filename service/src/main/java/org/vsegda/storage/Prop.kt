package org.vsegda.storage

import com.google.appengine.api.datastore.*
import kotlin.reflect.*

object Prop {
    abstract class BaseProp<T> {
        abstract fun fromEntity(value: Any?): T?
        open fun toEntity(value: T?): Any? = value
        operator fun getValue(entity: Entity, meta: KProperty<*>) =
            fromEntity(entity.getProperty(meta.name))
        operator fun setValue(entity: Entity, meta: KProperty<*>, value: T?) =
            entity.setProperty(meta.name, toEntity(value))
    }

    val string = object : BaseProp<String>() {
        override fun fromEntity(value: Any?): String? = value as? String
    }

    val long = object : BaseProp<Long>() {
        override fun fromEntity(value: Any?): Long? = value as? Long
    }

    val int = object : BaseProp<Int>() {
        override fun fromEntity(value: Any?): Int? = (value as? Long)?.toInt()
        override fun toEntity(value: Int?): Any? = value?.toLong()
    }

    val double = object : BaseProp<Double>() {
        override fun fromEntity(value: Any?): Double? = value as? Double
    }

    val blob = object : BaseProp<ByteArray>() {
        override fun fromEntity(value: Any?): ByteArray? = when (value) {
            is Blob -> value.bytes
            is ShortBlob -> value.bytes
            else -> null /* unknown type or null */
        }
        override fun toEntity(value: ByteArray?): Any? = when {
            value == null -> null
            value.size <= DataTypeUtils.MAX_SHORT_BLOB_PROPERTY_LENGTH -> ShortBlob(value)
            else -> Blob(value)
        }
    }
}
