package com.hnexperts.cosmetics.concurrency

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ApplicationScope(
    dispatchers: AppDispatchers
) {
    val coroutineScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + dispatchers.io + CoroutineName("cosmetics-app")
    )
}
