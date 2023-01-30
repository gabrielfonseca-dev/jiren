package jiren.service.database

import com.helger.commons.csv.CSVWriter
import jiren.controller.LogController
import jiren.data.enum.LogCode
import jiren.data.enum.LogType
import jiren.data.enum.Parameters
import jiren.data.repository.ParameterRepository
import org.json.JSONObject
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.time.Instant.now
import javax.annotation.PostConstruct

@Component
class DataAccessObject(private val logController: LogController, private val parameterRepository: ParameterRepository) {
    private lateinit var limit: String

    @PostConstruct
    fun setParameters() {
        try {
            this.limit = parameterRepository.findByCode(Parameters.SQL_UPDATE_LIMIT.toString())?.value ?: "100"
        } catch (e: Exception) {
            println(e.message)
        }
    }

    fun executeAutomation(sql: String, connection: Connection, name: String): List<Int> {
        val transact: Statement = connection.createStatement()
        val queryList = sql.split(";").toMutableList()
        queryList.removeAt(queryList.lastIndex)
        queryList.indices.forEach { transact.addBatch(queryList[it]) }
        val affectedRows: List<Int>
        val resultLog = StringBuilder()
        try {
            val start = now().toEpochMilli()
            affectedRows = transact.executeBatch().toList()
            val elapsedTime = (now().toEpochMilli() - start)
            affectedRows.indices.forEach { x -> resultLog.appendLine("Query ${x + 1} affected ${affectedRows[x]} lines") }
            logController.saveLog(LogCode.AUTOMATION_INFO, LogType.EXECUTE, elapsedTime, resultLog.toString(), name, sql)
        } catch (e: Exception) {
            throw IOException(e.message)
        } finally {
            transact.close()
            connection.close()
        }
        return affectedRows
    }

    fun executeMonitoring(sql: String, connection: Connection, name: String): String {
        lateinit var rs: ResultSet
        val transact = connection.prepareStatement(sql)
        val csv: String
        try {
            val start = now().toEpochMilli()
            rs = transact.executeQuery()
            val elapsedTime = (now().toEpochMilli() - start)
            logController.saveLog(LogCode.MONITORING_INFO, LogType.EXECUTE, elapsedTime, "SQL_SUCCESS", name, sql)
            csv = resulSetToCSV(rs)
        } catch (e: Exception) {
            throw (e)
        } finally {
            connection.close()
            transact.close()
            rs.close()
        }
        return csv
    }

    fun executeMonitoringToList(sql: String, connection: Connection, name: String): MutableList<String> {
        lateinit var rs: ResultSet
        val transact = connection.prepareStatement(sql)
        val objList: MutableList<String> = ArrayList()

        try {
            val start = now().toEpochMilli()
            rs = transact.executeQuery()
            val elapsedTime = (now().toEpochMilli() - start)
            logController.saveLog(LogCode.MONITORING_INFO, LogType.EXECUTE, elapsedTime, "SQL_SUCCESS", name, sql)
            while (rs.next()) {
                objList.add(rs.getObject(1).toString())
            }
        } catch (e: Exception) {
            throw (e)
        } finally {
            connection.close()
            transact.close()
            rs.close()
        }
        return objList
    }

    fun executeScript(batchList: List<String>, connection: Connection, task: String?): String {

        val response = StringBuilder()
        executeBackup(batchList.toMutableList(), connection, response)

        val transact: Statement = connection.createStatement()
        var sql = ""
        batchList.indices.forEach { i ->
            sql = batchList[i]
            val commandType = sql.trimStart().substringBefore(" ").substringBefore("\n").substringBefore("\r").uppercase()
            if (commandType == "UPDATE" || commandType == "DELETE") sql.plus(" LIMIT $limit")
            if(sql.isNotEmpty()) transact.addBatch(sql)
        }

        try {
            val start = now().toEpochMilli()
            transact.executeLargeBatch().let { rowCount ->
                rowCount.forEach { r -> response.appendLine("Query ${rowCount.indexOf(r) + 1} afetou $r linhas") }
                logController.saveLog(LogCode.SQL_INFO, LogType.EXECUTE, (now().toEpochMilli() - start), "$response", task, batchList.toString())
            }
        } catch (e: Exception) {
            response.appendLine(e.stackTraceToString())
            logController.saveLog(LogCode.SQL_ERROR, LogType.EXECUTE, null, "$response", task, sql)
            return response.toString()
        } finally {
            transact.close()
            connection.close()
        }

        return response.toString()

    }

    private fun executeBackup(batchList: MutableList<String>, connection: Connection, response: StringBuilder) {
        if (batchList.last().trim() === ";" || batchList.last().trim().isEmpty()) batchList.removeAt(batchList.lastIndex)

        batchList.forEach { query ->
            val cmd = query.trimStart().substringBefore(" ").substringBefore("\n").substringBefore("\r").uppercase()
            if (cmd == "UPDATE" || cmd == "DELETE") {

                var table = ""
                var where = ""

                when (cmd) {
                    "UPDATE" -> {
                        table = query.substringAfter("UPDATE").substringAfter("update").substringBefore("SET")
                            .substringBefore("set").substringBefore("JOIN").substringBefore("join")
                        where = query.substringAfter("WHERE").substringAfter("where")
                    }

                    "DELETE" -> {
                        table = query.substringAfter("FROM").substringAfter("from").substringBefore("WHERE")
                            .substringBefore("where")
                        where = query.substringAfter("WHERE").substringAfter("where")
                    }
                }

                val spaceSplit = query.split(" ", "\n")
                var joinCount = 0
                spaceSplit.forEach { if (it.contains("JOIN")) joinCount++ }
                val joinList: MutableList<String> = ArrayList()
                var cpQuery = query
                for (i in 0 until joinCount) {
                    joinList.add(
                        cpQuery.substringAfter("JOIN").substringAfter("join").substringBefore("JOIN")
                            .substringBefore("join").substringBefore("SET").substringBefore("set")
                            .substringBefore("WHERE").substringBefore("where")
                    )
                    cpQuery = cpQuery.replace("JOIN${joinList[i]}", "", true)
                }

                val fieldList: MutableList<String> = ArrayList()
                if (cmd == "UPDATE") {
                    val fields = query.substringAfter("SET").substringAfter("set")
                        //.substringBefore("=")
                        .substringBefore("WHERE").substringBefore("where").split(",")

                    fields.forEach { fieldList.add(it.substringBefore("=", "")) }
                }

                val backupQueryBuilder = StringBuilder()
                backupQueryBuilder.append("SELECT ")

                if (fieldList.size > 0) {
                    fieldList.forEach { if (it.isNotEmpty()) backupQueryBuilder.append("${it}, ") }
                } else {
                    backupQueryBuilder.append(" * ")
                }
                backupQueryBuilder.append("FROM $table ")
                if (cmd == "UPDATE") joinList.forEach { join -> backupQueryBuilder.append("JOIN $join ") }
                backupQueryBuilder.append("WHERE $where")

                val backupQuery = backupQueryBuilder.toString().replace(", FROM", " FROM")

                val transact = connection.prepareStatement(backupQuery)
                transact.queryTimeout = 300

                val rs: ResultSet
                try {
                    rs = transact.executeQuery()
                } catch (e: Exception) {
                    transact.close()
                    connection.close()
                    response.appendLine("Backup Error - Query ${batchList.indexOf(query)}\n${e.message}")
                    println(e.message)
                    return@forEach
                }

                response.appendLine("Backup - Query ${batchList.indexOf(query)}")
                while (rs.next()) {
                    val row = JSONObject()
                    for (i in 1..rs.metaData.columnCount) {
                        try {
                            if (rs.getObject(i) != null && rs.getObject(i).toString().isNotEmpty()) {
                                row.put(rs.metaData.getColumnLabel(i), rs.getObject(i).toString())
                            } else {
                                row.put(rs.metaData.getColumnLabel(i), " ")
                            }
                        } catch (e: Exception) {
                            row.put(rs.metaData.getColumnLabel(i), " ")
                        }
                    }
                    response.appendLine(row)
                }
                rs.close()
                transact.close()
            }
        }
    }

    private fun resulSetToCSV(rs: ResultSet): String {
        val file = File.createTempFile("${this.hashCode()}","csv")
        file.deleteOnExit()
        val csvWriter = CSVWriter(file.writer(Charsets.UTF_8))
        csvWriter.flushQuietly()
        val lines: MutableList<MutableList<String>> = ArrayList()
        var count = 0
        while (rs.next()) {
            if (count == 0) {
                val columns: MutableList<String> = ArrayList()
                for (i in 1..rs.metaData.columnCount) {
                    columns.add(rs.metaData.getColumnLabel(i))
                }
                lines.add(columns)
            }
            count++

            val columns: MutableList<String> = ArrayList()
            for (i in 1..rs.metaData.columnCount) {
                try {
                    if (rs.getObject(i) != null && rs.getObject(i).toString().isNotEmpty()) {
                        columns.add("${rs.getObject(i)}")
                    } else {
                        columns.add("")
                    }
                } catch (e: Exception) {
                    columns.add("${e.message}")
                }
            }
            lines.add(columns)
        }
        csvWriter.writeAll(lines)
        csvWriter.flush()
        return file.readText(Charsets.UTF_8)
    }

}