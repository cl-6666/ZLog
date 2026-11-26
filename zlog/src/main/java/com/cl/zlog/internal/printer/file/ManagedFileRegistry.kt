package com.cl.zlog.internal.printer.file

import com.cl.zlog.internal.Platform
import java.io.File

/** Tracks files created by FilePrinter so cleanup never touches unrelated files. */
internal class ManagedFileRegistry(private val folder: File) {

    private val registryFile = File(folder, REGISTRY_FILE_NAME)
    private val managedFileNames = linkedSetOf<String>()

    init {
        if (registryFile.isFile) {
            try {
                registryFile.readLines()
                    .filter(::isSafeFileName)
                    .forEach(managedFileNames::add)
            } catch (e: Exception) {
                Platform.get().warn("Load managed log files failed: ${e.message}")
            }
        }
    }

    fun register(file: File) {
        if (!isManagedLocation(file) || !managedFileNames.add(file.name)) {
            return
        }
        persist()
    }

    fun register(files: Iterable<File>) {
        var changed = false
        for (file in files) {
            if (isManagedLocation(file)) {
                changed = managedFileNames.add(file.name) || changed
            }
        }
        if (changed) {
            persist()
        }
    }

    fun unregister(file: File) {
        if (managedFileNames.remove(file.name)) {
            persist()
        }
    }

    fun files(): List<File> {
        return managedFileNames.map { File(folder, it) }
    }

    fun isReserved(fileName: String): Boolean {
        return fileName == REGISTRY_FILE_NAME
    }

    private fun isManagedLocation(file: File): Boolean {
        return try {
            file.canonicalFile.parentFile == folder && isSafeFileName(file.name)
        } catch (_: Exception) {
            false
        }
    }

    private fun isSafeFileName(fileName: String): Boolean {
        return fileName.isNotBlank() &&
                fileName != REGISTRY_FILE_NAME &&
                fileName == File(fileName).name
    }

    private fun persist() {
        try {
            registryFile.writeText(managedFileNames.joinToString(separator = "\n", postfix = "\n"))
        } catch (e: Exception) {
            Platform.get().warn("Persist managed log files failed: ${e.message}")
        }
    }

    private companion object {
        const val REGISTRY_FILE_NAME = ".zlog-managed-files"
    }
}
