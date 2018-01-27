package org.vsegda.storage

import com.google.appengine.api.datastore.*
import java.util.logging.*
import kotlin.reflect.*
import kotlin.system.*

private val dataStore: DatastoreService = DatastoreServiceFactory.getDatastoreService()

abstract class BaseStorage<T> {
    protected val log: Logger = Logger.getLogger(this::class.java.name)
    protected val ds = dataStore

    protected open val chunkSize: Int = 10000
    protected abstract val kind: String

    protected abstract fun T.toKey(): Key
    protected abstract fun T.toEntity(): Entity
    protected abstract fun Entity.toObject(): T

    protected fun List<Entity>.toObjectList(): List<T> = map { it.toObject() }

    protected fun Long.toKey(): Key = KeyFactory.createKey(kind, this)

    protected inline fun <T> logged(msg: String, body: () -> T): T =
        logged({ msg }, body)

    @Suppress("UNCHECKED_CAST")
    protected inline fun <T> logged(msg: (T) -> String, body: () -> T): T {
        var result: T? = null
        val time = measureTimeMillis {
            result = body()
        }
        log.info("${msg(result as T)} in $time ms")
        return result as T
    }

    protected fun store(obj: T): Key = ds.put(obj.toEntity())

    protected fun delete(obj: T) = ds.delete(obj.toKey())

    protected fun Sequence<Entity>.store() = chunked(chunkSize).forEach { ds.put(it) }

    protected fun Sequence<Key>.delete() = chunked(chunkSize).forEach { ds.delete(it) }

    protected fun loadById(id: Long) =
        try { ds.get(id.toKey()).toObject() }
        catch (e: EntityNotFoundException) { null }

    protected inline fun query(config: Query.() -> Unit): PreparedQuery {
        val query = Query(kind)
        config(query)
        return ds.prepare(query)
    }

    protected fun Query.sortAscByKey() {
        addSort(Entity.KEY_RESERVED_PROPERTY, Query.SortDirection.ASCENDING)
    }

    protected fun Query.sortDescByKey() {
        addSort(Entity.KEY_RESERVED_PROPERTY, Query.SortDirection.DESCENDING)
    }

    protected fun Query.sortAscBy(prop: KProperty<*>) {
        addSort(prop.name, Query.SortDirection.ASCENDING)
    }

    protected fun Query.sortDescBy(prop: KProperty<*>) {
        addSort(prop.name, Query.SortDirection.DESCENDING)
    }

    protected fun Query.filter(predicate: Query.FilterPredicate) {
        val curFilter = filter
        filter = if (curFilter == null)
            predicate
        else Query.CompositeFilter(Query.CompositeFilterOperator.AND,
            if (curFilter is Query.CompositeFilter && curFilter.operator == Query.CompositeFilterOperator.AND)
                curFilter.subFilters + predicate
            else
                listOf(curFilter, predicate)
        )
    }

    protected fun <T> Query.filterEq(prop: KProperty<T>, value: T) {
        filter(Query.FilterPredicate(prop.name, Query.FilterOperator.EQUAL, value))
    }

    protected fun <T> Query.filterGreaterEq(prop: KProperty<T>, value: T) {
        filter(Query.FilterPredicate(prop.name, Query.FilterOperator.GREATER_THAN_OR_EQUAL, value))
    }

    protected fun <T> Query.filterLess(prop: KProperty<T>, value: T) {
        filter(Query.FilterPredicate(prop.name, Query.FilterOperator.LESS_THAN, value))
    }

    protected fun PreparedQuery.asList(): List<T> =
        asList(FetchOptions.Builder.withChunkSize(DataStreamStorage.chunkSize)).toObjectList()

    protected fun PreparedQuery.asList(n: Int): List<T> =
        asList(FetchOptions.Builder.withChunkSize(n).limit(n)).toObjectList()

    protected fun PreparedQuery.asObject(): T? = asSingleEntity()?.toObject()

    protected fun PreparedQuery.asEntitySequence(chunkSize: Int = this@BaseStorage.chunkSize): Sequence<Entity> =
        asIterable(FetchOptions.Builder.withChunkSize(chunkSize)).asSequence()

    protected fun PreparedQuery.asSequence(chunkSize: Int = this@BaseStorage.chunkSize): Sequence<T> =
       asEntitySequence(chunkSize).map { it.toObject() }
}



