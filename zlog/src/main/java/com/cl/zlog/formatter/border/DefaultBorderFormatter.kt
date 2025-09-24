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

package com.cl.zlog.formatter.border

import com.cl.zlog.internal.SystemCompat

/**
 * String segments wrapped with borders look like:
 * ╔════════════════════════════════════════════════════════════════════════════
 * ║String segment 1
 * ╟────────────────────────────────────────────────────────────────────────────
 * ║String segment 2
 * ╟────────────────────────────────────────────────────────────────────────────
 * ║String segment 3
 * ╚════════════════════════════════════════════════════════════════════════════
 */
class DefaultBorderFormatter : BorderFormatter {

    override fun format(data: Array<String>): String {
        if (data.isEmpty()) {
            return ""
        }

        val nonNullSegments = data.filterNotNull()
        if (nonNullSegments.isEmpty()) {
            return ""
        }

        val msgBuilder = StringBuilder()
        msgBuilder.append(TOP_HORIZONTAL_BORDER).append(SystemCompat.lineSeparator)
        
        for (i in nonNullSegments.indices) {
            msgBuilder.append(appendVerticalBorder(nonNullSegments[i]))
            if (i != nonNullSegments.size - 1) {
                msgBuilder.append(SystemCompat.lineSeparator).append(DIVIDER_HORIZONTAL_BORDER)
                    .append(SystemCompat.lineSeparator)
            } else {
                msgBuilder.append(SystemCompat.lineSeparator).append(BOTTOM_HORIZONTAL_BORDER)
            }
        }
        return msgBuilder.toString()
    }

    companion object {
        private const val VERTICAL_BORDER_CHAR = '║'

        // Length: 100.
        private const val TOP_HORIZONTAL_BORDER =
            "╔═════════════════════════════════════════════════" +
                    "══════════════════════════════════════════════════"

        // Length: 99.
        private const val DIVIDER_HORIZONTAL_BORDER =
            "╟─────────────────────────────────────────────────" +
                    "──────────────────────────────────────────────────"

        // Length: 100.
        private const val BOTTOM_HORIZONTAL_BORDER =
            "╚═════════════════════════════════════════════════" +
                    "══════════════════════════════════════════════════"

        /**
         * Add [VERTICAL_BORDER_CHAR] to each line of msg.
         *
         * @param msg the message to add border
         * @return the message with [VERTICAL_BORDER_CHAR] in the start of each line
         */
        private fun appendVerticalBorder(msg: String): String {
            val borderedMsgBuilder = StringBuilder(msg.length + 10)
            val lines = msg.split(SystemCompat.lineSeparator)
            for (i in lines.indices) {
                if (i != 0) {
                    borderedMsgBuilder.append(SystemCompat.lineSeparator)
                }
                val line = lines[i]
                borderedMsgBuilder.append(VERTICAL_BORDER_CHAR).append(line)
            }
            return borderedMsgBuilder.toString()
        }
    }
}