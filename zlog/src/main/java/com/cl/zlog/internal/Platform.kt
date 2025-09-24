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

package com.cl.zlog.internal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.cl.zlog.formatter.message.`object`.BundleFormatter
import com.cl.zlog.formatter.message.`object`.IntentFormatter
import com.cl.zlog.formatter.message.`object`.ObjectFormatter
import com.cl.zlog.printer.AndroidPrinter
import com.cl.zlog.printer.ConsolePrinter
import com.cl.zlog.printer.Printer

open class Platform {

    @SuppressLint("NewApi")
    open fun lineSeparator(): String {
        return System.lineSeparator()
    }

    open fun defaultPrinter(): Printer {
        return ConsolePrinter()
    }

    open fun builtinObjectFormatters(): Map<Class<*>, ObjectFormatter<*>> {
        return emptyMap()
    }

    open fun warn(msg: String) {
        println(msg)
    }

    open fun error(msg: String) {
        println(msg)
    }

    class Android : Platform() {

        override fun lineSeparator(): String {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                "\n"
            } else {
                System.lineSeparator()
            }
        }

        override fun defaultPrinter(): Printer {
            return AndroidPrinter()
        }

        override fun builtinObjectFormatters(): Map<Class<*>, ObjectFormatter<*>> {
            return BUILTIN_OBJECT_FORMATTERS
        }

        override fun warn(msg: String) {
            android.util.Log.w("ZLog", msg)
        }

        override fun error(msg: String) {
            android.util.Log.e("ZLog", msg)
        }

        companion object {
            private val BUILTIN_OBJECT_FORMATTERS: Map<Class<*>, ObjectFormatter<*>> = mapOf(
                Bundle::class.java to BundleFormatter(),
                Intent::class.java to IntentFormatter()
            )
        }
    }

    companion object {
        private val PLATFORM = findPlatform()

        @JvmStatic
        fun get(): Platform {
            return PLATFORM
        }

        private fun findPlatform(): Platform {
            return try {
                Class.forName("android.os.Build")
                if (Build.VERSION.SDK_INT != 0) {
                    Android()
                } else {
                    Platform()
                }
            } catch (ignored: ClassNotFoundException) {
                Platform()
            }
        }
    }
}