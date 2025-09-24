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

package com.cl.zlog.internal.util

import android.content.ClipData
import android.content.ComponentName
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Utility for formatting object to string.
 */
object ObjectToStringUtil {

    /**
     * Bundle object to string, the string would be in the format of "Bundle[{...}]".
     */
    fun bundleToString(bundle: Bundle?): String {
        if (bundle == null) {
            return "null"
        }

        val b = StringBuilder(128)
        b.append("Bundle[{")
        bundleToShortString(bundle, b)
        b.append("}]")
        return b.toString()
    }

    /**
     * Intent object to string, the string would be in the format of "Intent { ... }".
     */
    fun intentToString(intent: Intent?): String {
        if (intent == null) {
            return "null"
        }

        val b = StringBuilder(128)
        b.append("Intent { ")
        intentToShortString(intent, b)
        b.append(" }")
        return b.toString()
    }

    private fun bundleToShortString(bundle: Bundle, b: StringBuilder) {
        var first = true
        for (key in bundle.keySet()) {
            if (!first) {
                b.append(", ")
            }
            b.append(key).append('=')
            val value = bundle.get(key)
            when (value) {
                is IntArray -> b.append(value.contentToString())
                is ByteArray -> b.append(value.contentToString())
                is BooleanArray -> b.append(value.contentToString())
                is ShortArray -> b.append(value.contentToString())
                is LongArray -> b.append(value.contentToString())
                is FloatArray -> b.append(value.contentToString())
                is DoubleArray -> b.append(value.contentToString())
                is Array<*> -> {
                    when {
                        value.javaClass.componentType == String::class.java -> {
                            @Suppress("UNCHECKED_CAST")
                            b.append((value as Array<String>).contentToString())
                        }
                        value.javaClass.componentType == CharSequence::class.java -> {
                            @Suppress("UNCHECKED_CAST")
                            b.append((value as Array<CharSequence>).contentToString())
                        }
                        value.javaClass.componentType == Parcelable::class.java -> {
                            @Suppress("UNCHECKED_CAST")
                            b.append((value as Array<Parcelable>).contentToString())
                        }
                        else -> b.append(value.contentToString())
                    }
                }
                is Bundle -> b.append(bundleToString(value))
                else -> b.append(value)
            }
            first = false
        }
    }

    private fun intentToShortString(intent: Intent, b: StringBuilder) {
        var first = true
        val mAction = intent.action
        if (mAction != null) {
            b.append("act=").append(mAction)
            first = false
        }
        val mCategories = intent.categories
        if (mCategories != null) {
            if (!first) {
                b.append(' ')
            }
            first = false
            b.append("cat=[")
            var firstCategory = true
            for (c in mCategories) {
                if (!firstCategory) {
                    b.append(',')
                }
                b.append(c)
                firstCategory = false
            }
            b.append("]")
        }
        val mData = intent.data
        if (mData != null) {
            if (!first) {
                b.append(' ')
            }
            first = false
            b.append("dat=")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                b.append(uriToSafeString(mData))
            } else {
                val scheme = mData.scheme
                if (scheme != null) {
                    when {
                        scheme.equals("tel", ignoreCase = true) -> b.append("tel:xxx-xxx-xxxx")
                        scheme.equals("smsto", ignoreCase = true) -> b.append("smsto:xxx-xxx-xxxx")
                        else -> b.append(mData)
                    }
                } else {
                    b.append(mData)
                }
            }
        }
        val mType = intent.type
        if (mType != null) {
            if (!first) {
                b.append(' ')
            }
            first = false
            b.append("typ=").append(mType)
        }
        val mFlags = intent.flags
        if (mFlags != 0) {
            if (!first) {
                b.append(' ')
            }
            first = false
            b.append("flg=0x").append(Integer.toHexString(mFlags))
        }
        val mPackage = intent.`package`
        if (mPackage != null) {
            if (!first) {
                b.append(' ')
            }
            first = false
            b.append("pkg=").append(mPackage)
        }
        val mComponent = intent.component
        if (mComponent != null) {
            if (!first) {
                b.append(' ')
            }
            first = false
            b.append("cmp=").append(mComponent.flattenToShortString())
        }
        val mSourceBounds = intent.sourceBounds
        if (mSourceBounds != null) {
            if (!first) {
                b.append(' ')
            }
            first = false
            b.append("bnds=").append(mSourceBounds.toShortString())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val mClipData = intent.clipData
            if (mClipData != null) {
                if (!first) {
                    b.append(' ')
                }
                first = false
                b.append("(has clip)")
            }
        }
        val mExtras = intent.extras
        if (mExtras != null) {
            if (!first) {
                b.append(' ')
            }
            b.append("extras={")
            bundleToShortString(mExtras, b)
            b.append('}')
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            val mSelector = intent.selector
            if (mSelector != null) {
                b.append(" sel=")
                intentToShortString(mSelector, b)
                b.append("}")
            }
        }
    }

    private fun uriToSafeString(uri: Uri): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                val toSafeString: Method = Uri::class.java.getDeclaredMethod("toSafeString")
                toSafeString.isAccessible = true
                return toSafeString.invoke(uri) as String
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        return uri.toString()
    }
}