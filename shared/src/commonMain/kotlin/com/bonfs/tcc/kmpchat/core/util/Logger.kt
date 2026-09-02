package com.bonfs.tcc.kmpchat.core.util

import co.touchlab.kermit.Logger as KermitLogger

interface Logger {
    fun logInfo(
        tag: String? = null,
        throwable: Throwable? = null,
        message: () -> String
    )
    fun logWarn(
        tag: String? = null,
        throwable: Throwable? = null,
        message: () -> String
    )
    fun logError(
        tag: String? = null,
        throwable: Throwable? = null,
        message: () -> String
    )
}

class DefaultLogger : Logger {
    private val logger: KermitLogger = KermitLogger

    override fun logInfo(
        tag: String?,
        throwable: Throwable?,
        message: () -> String
    ) {
        if (tag != null) {
            logger.withTag(tag).i(throwable = throwable, message = message)
        } else {
            logger.i(throwable = throwable, message = message)
        }
    }

    override fun logWarn(
        tag: String?,
        throwable: Throwable?,
        message: () -> String
    ) {
        if (tag != null) {
            logger.withTag(tag).w(throwable = throwable, message = message)
        } else {
            logger.w(throwable = throwable, message = message)
        }
    }

    override fun logError(
        tag: String?,
        throwable: Throwable?,
        message: () -> String
    ) {
        if (tag != null) {
            logger.withTag(tag).e(throwable = throwable, message = message)
        } else {
            logger.e(throwable = throwable, message = message)
        }
    }
}
