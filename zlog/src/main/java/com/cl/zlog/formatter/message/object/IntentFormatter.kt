/*
 * Copyright 2016 cl
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

package com.cl.zlog.formatter.message.`object`

import android.content.Intent
import com.cl.zlog.internal.util.ObjectToStringUtil

/**
 * Format an Intent object to a string.
 *
 * @since 1.4.0
 */
class IntentFormatter : ObjectFormatter<Intent> {

    /**
     * Format an Intent object to a string.
     *
     * @param data the Intent object to format
     * @return the formatted string
     */
    override fun format(data: Intent): String {
        return ObjectToStringUtil.intentToString(data)
    }
}