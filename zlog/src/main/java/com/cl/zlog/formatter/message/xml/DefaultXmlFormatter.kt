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

package com.cl.zlog.formatter.message.xml

import com.cl.zlog.internal.Platform
import com.cl.zlog.internal.SystemCompat
import java.io.StringReader
import java.io.StringWriter
import javax.xml.XMLConstants
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * Simply format the XML with a indent of [XML_INDENT].
 * TODO: Make indent size and enable/disable state configurable.
 */
class DefaultXmlFormatter : XmlFormatter {

    override fun format(data: String): String {
        if (data.isBlank()) {
            Platform.get().warn("XML empty.")
            return ""
        }

        if (data.contains("<!DOCTYPE", ignoreCase = true)) {
            Platform.get().warn("XML DOCTYPE is not supported.")
            return data
        }
        
        return try {
            val xmlInput = StreamSource(StringReader(data))
            val xmlOutput = StreamResult(StringWriter())
            val transformerFactory = TransformerFactory.newInstance().apply {
                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                disableExternalAccess(this, ACCESS_EXTERNAL_DTD)
                disableExternalAccess(this, ACCESS_EXTERNAL_STYLESHEET)
                uriResolver = javax.xml.transform.URIResolver { _, _ ->
                    throw TransformerException("External XML resources are not supported")
                }
            }
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                XML_INDENT.toString()
            )
            transformer.transform(xmlInput, xmlOutput)
            xmlOutput.writer.toString().replaceFirst(">", ">" + SystemCompat.lineSeparator)
        } catch (e: Exception) {
            Platform.get().warn(e.message ?: "XML parsing error")
            data
        }
    }

    private fun disableExternalAccess(factory: TransformerFactory, property: String) {
        try {
            factory.setAttribute(property, "")
        } catch (_: IllegalArgumentException) {
            // Some Android TransformerFactory implementations do not expose JAXP properties.
        }
    }

    companion object {
        private const val XML_INDENT = 4
        private const val ACCESS_EXTERNAL_DTD =
            "http://javax.xml.XMLConstants/property/accessExternalDTD"
        private const val ACCESS_EXTERNAL_STYLESHEET =
            "http://javax.xml.XMLConstants/property/accessExternalStylesheet"
    }
}
