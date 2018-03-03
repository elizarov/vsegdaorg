package org.vsegda.storage

import com.google.appengine.api.datastore.*
import org.vsegda.util.*
import kotlin.reflect.*

private val dataStore: DatastoreService = DatastoreServiceFactory.getDatastoreService()

typealias Transaction = com.google.appengine.api.datastore.Transaction

abstract class BaseStorage<T> : Logged {
    protected val ds = dataStore

    protected open val chunkSize: Int = 10000
    protected abstract val kind: String

    protected abstract fun T.toKey(): Key
    protected abstract fun T.toEntity(): Entity
    protected abstract fun Entity.toObject(): T

    private fun List<Entity>.toObjectList(): List<T> = map { it.toObject() }

    private fun List<T>.toEntityList(): List<Entity> = map { it.toEntity() }

    protected fun Long.toKey(): Key = KeyFactory.createKey(kind, this)

    protected fun store(obj: T): Key = ds.put(obj.toEntity())

    protected fun Transaction.store(obj: T): Key = ds.put(this, obj.toEntity())

    protected fun Transaction.storeList(list: List<T>): List<Key> =
        ds.put(this, list.toEntityList())

    protected fun delete(obj: T) = ds.delete(obj.toKey())

    protected fun Sequence<Entity>.store() = chunked(chunkSize).forEach { ds.put(it) }

    protected fun Sequence<Key>.delete() = chunked(chunkSize).forEach { ds.delete(it) }

    protected fun load(key: Key) =
        try { ds.get(key).toObject() }
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

    protected fun <T> Query.filterGreater(prop: KProperty<T>, value: T) {
        filter(Query.FilterPredicate(prop.name, Query.FilterOperator.GREATER_THAN, value))
    }

    protected fun <T> Query.filterLess(prop: KProperty<T>, value: T) {
        filter(Query.FilterPredicate(prop.name, Query.FilterOperator.LESS_THAN, value))
    }

    protected fun PreparedQuery.asList(): List<T> =
        asList(FetchOptions.Builder.withChunkSize(DataStreamStorage.chunkSize)).toObjectList()

    protected fun PreparedQuery.asList(n: Int, offset: Int = 0): List<T> =
        asList(FetchOptions.Builder.withChunkSize(n).limit(n).offset(offset)).toObjectList()

    protected fun PreparedQuery.asObject(): T? = asSingleEntity()?.toObject()

    protected fun PreparedQuery.asEntitySequence(chunkSize: Int = this@BaseStorage.chunkSize): Sequence<Entity> =
        asIterable(FetchOptions.Builder.withChunkSize(chunkSize)).asSequence()

    protected fun PreparedQuery.asSequence(chunkSize: Int = this@BaseStorage.chunkSize): Sequence<T> =
       asEntitySequence(chunkSize).map { it.toObject() }

    fun <T> transaction(block: Transaction.() -> T): T {
        val tx = ds.beginTransaction(TransactionOptions.Builder.withXG(true))
        log.info("BEGIN Transaction $tx")
        try {
            val result = block(tx)
            tx.commit()
            log.info("COMMITTED Transaction $tx")
            return result
        } catch (e: Throwable) {
            log.info("ROLLBACK Transaction $tx")
            tx.rollback()
            throw e
        }
    }
}



