package com.hnexperts.cosmetics.concurrency

import com.hnexperts.cosmetics.failure.toVerboseString
import com.hnexperts.cosmetics.logging.AppLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ApplicationScope(
    dispatchers: AppDispatchers
) {
    val coroutineScope: CoroutineScope = CoroutineScope(
        SupervisorJob() +
            dispatchers.io +
            CoroutineName("cosmetics-app") +
            CoroutineExceptionHandler { _, error ->
                AppLog.e("ApplicationScope", error.toVerboseString(), error)
            }
    )
}
