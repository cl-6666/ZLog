/*
 * Copyright 2018 cl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cl.zlog.flattener

/**
 * The flattener used to flatten log elements(log time milliseconds, level, tag and message) to
 * a single CharSequence.
 *
 * @since 1.6.0
 */
interface Flattener2 {

    /**
     * Flatten the log.
     *
     * @param timeMillis the time milliseconds of log
     * @param logLevel  the level of log
     * @param tag       the tag of log
     * @param message   the message of log
     * @return the formatted final log Charsequence
     */
    fun flatten(timeMillis: Long, logLevel: Int, tag: String, message: String): CharSequence
}