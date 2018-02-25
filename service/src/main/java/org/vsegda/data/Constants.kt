package org.vsegda.data

import org.vsegda.util.*

/**
 * Each archive keeps data for one day.
 */
const val ARCHIVE_INTERVAL = TimeUtil.DAY

/**
 * Data items usually come every 5 minutes and we round times in archive to it.
 */
const val TIME_PRECISION = 5 * TimeUtil.MINUTE

/**
 * Estimated number of items per archive.
 */
const val ARCHIVE_COUNT_ESTIMATE = (ARCHIVE_INTERVAL / TIME_PRECISION).toInt()

/**
 * Data is considered recent for 2 days (then it is archived or deleted).
 */
const val RECENT_TIME_INTERVAL = 2 * TimeUtil.DAY

/**
 * Maximal number of items in one archive
 */
const val MAX_ARCHIVE_ITEMS = 1000

/**
 * Limit for max size of data archive blob
 */
const val MAX_ARCHIVE_ENCODED_SIZE = 10_000

/**
 * Now many items to pre-load into cache.
 */
const val PRELOAD_CACHED_ITEMS = 40 * 24 * 12 // load into cache 40 days assuming reading every 5 mins

/**
 * Max number of items to keep in cache
 */
const val MAX_CACHED_ITEMS = (1.5 * PRELOAD_CACHED_ITEMS).toInt()

