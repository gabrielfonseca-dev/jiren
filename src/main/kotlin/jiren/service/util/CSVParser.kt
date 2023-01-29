package jiren.service.util

import com.helger.commons.csv.CSVWriter
import java.io.File
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

class CSVParser {

    fun parse(objects: List<Any>?): File {
        val file = File.createTempFile("${this.hashCode()}", "csv")
        file.deleteOnExit()
        val csvWriter = CSVWriter(file.writer(Charsets.UTF_8))
        csvWriter.separatorChar = ';'
        csvWriter.flushQuietly()
        val lines: MutableList<MutableList<String>> = ArrayList()
        var count = 0
        objects?.forEach { obj ->
            if (count == 0) {
                val columns: MutableList<String> = ArrayList()
                obj::class.memberProperties.forEach { prop ->
                    columns.add(prop.name)
                }
                lines.add(columns)
            }
            count++
            val columns: MutableList<String> = ArrayList()
            obj::class.memberProperties.forEach { prop ->
                if (prop.visibility == KVisibility.PUBLIC) {
                    columns.add("${prop.getter.call(obj)}")
                } else {
                    columns.add("***")
                }
            }
            lines.add(columns)
        }
        csvWriter.writeAll(lines)
        csvWriter.flush()
        return file
    }

}