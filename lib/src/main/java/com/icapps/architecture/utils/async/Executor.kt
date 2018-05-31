/*
 * Copyright 2018 icapps
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.icapps.architecture.utils.async

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


/**
 * @author Nicola Verbeeck
 * @version 1
 */
class ScalingThreadPoolExecutor(coreSize: Int, maxSize: Int, keepAliveTime: Long, keepAliveUnit: TimeUnit, queue: ScalingBlockingQueue = ScalingBlockingQueue(),
                                threadFactory: ThreadFactory = Executors.defaultThreadFactory())
    : ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, keepAliveUnit, queue, threadFactory) {

    private val activeCount = AtomicInteger()

    init {
        rejectedExecutionHandler = RejectedExecutionHandler { r, executor -> executor.queue.put(r) }
        queue.executor = this
    }

    override fun getActiveCount(): Int {
        return activeCount.get()
    }

    override fun beforeExecute(t: Thread, r: Runnable) {
        activeCount.incrementAndGet()
    }

    override fun afterExecute(r: Runnable, t: Throwable?) {
        activeCount.decrementAndGet()
    }

}

class ScalingBlockingQueue : LinkedBlockingQueue<Runnable>() {

    lateinit var executor: ThreadPoolExecutor

    override fun offer(e: Runnable): Boolean {
        val allWorkingThreads = executor.activeCount + super.size
        return allWorkingThreads < executor.poolSize && super.offer(e)
    }

}