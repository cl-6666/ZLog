/*
 * Copyright 2015 cl
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

package com.cl.zlog.formatter.message.json

import com.cl.zlog.internal.Platform
import org.json.JSONArray
import org.json.JSONObject

/**
 * Simply format the JSON using [JSONObject] and [JSONArray].
 */
class DefaultJsonFormatter : JsonFormatter {

    override fun format(data: String): String {
        if (data.isBlank()) {
            Platform.get().warn("JSON empty.")
            return ""
        }
        
        return try {
            when {
                data.startsWith("{") -> {
                    val jsonObject = JSONObject(data)
                    jsonObject.toString(JSON_INDENT)
                }
                data.startsWith("[") -> {
                    val jsonArray = JSONArray(data)
                    jsonArray.toString(JSON_INDENT)
                }
                else -> {
                    Platform.get().warn("JSON should start with { or [")
                    data
                }
            }
        } catch (e: Exception) {
            Platform.get().warn(e.message ?: "JSON parsing error")
            data
        }
    }

    companion object {
        private const val JSON_INDENT = 4
    }
}