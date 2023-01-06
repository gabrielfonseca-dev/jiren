package jiren.service.database

import jiren.controller.UserController
import jiren.data.entity.Database
import jiren.data.enum.SGBD
import org.springframework.stereotype.Component
import java.sql.Connection

@Component
class ScriptExecutor(var dao: DataAccessObject, var userController: UserController, var databasePicker: DatabasePicker) {

    fun execute(sql: String, db: Database, task: String): String {
        validate(sql, db).let { if (it !== null) return it }
        val batchList: MutableList<String> = sql.split(";").toMutableList()
        try {
            if (db.sgbd == SGBD.MongoDb) throw Exception("Banco de dados NoSQL não suportado")
            dao.executeScript(batchList, databasePicker.getConnection(db) as Connection, task).let { return "SUCCESS!#!$it" }
        } catch (e: Exception) {
            return "ERROR!#!${e.message}"
        }
    }

    private fun validate(sql: String, db: Database): String? {

        val cmd = sql.trimStart().substringBefore(" ").substringBefore("\n").substringBefore("\r").uppercase()
        if(cmd == "UPDATE") {
            val valuesCheck = sql.substringAfter("=").substringBefore("WHERE").substringBefore("where")
            val checkError = valuesCheck.contains(";")
            if (checkError) return "Não é possível executar UPDATES com ; nos valores"
        }

        val queryList = sql.split(";").toMutableList()
        if (queryList.last().trim() === ";" || queryList.last().trim().isEmpty()) queryList.removeAt(queryList.lastIndex)

        queryList.forEach { query ->
            val commandType = query.trimStart().substringBefore(" ").substringBefore("\n").substringBefore("\r").uppercase()

            if ((!userController.loggedUserHasPermission("${db}_$commandType")) && commandType != "SET") {
                return "Você não tem permissão para executar $commandType"
            }

            when (commandType) {
                "INSERT" -> {
                    if ((query.contains("SELECT ", ignoreCase = true)) || (query.contains("SELECT\n",ignoreCase = true))) {
                        if (!(query.contains("WHERE ", ignoreCase = true)) && !(query.contains("WHERE\n",ignoreCase = true))) {
                            return "Cannot execute select without WHERE closure!"
                        }
                    }
                }
                "UPDATE" -> {
                    if (!(query.contains("WHERE ", ignoreCase = true)) && !(query.contains("WHERE\n",ignoreCase = true)) && !(query.contains("WHERE\r",ignoreCase = true))) {
                        return "Cannot execute update without WHERE closure!"
                    }
                    if (!(query.contains("SET ", ignoreCase = true)) && !(query.contains("SET\n", ignoreCase = true)) && !(query.contains("SET\r",ignoreCase = true))) {
                        return "Cannot execute update without SET values"
                    }
                    if ((query.contains("LIMIT ", ignoreCase = true) || (query.contains("LIMIT\n",ignoreCase = true))) && !(query.contains("LIMIT\r",ignoreCase = true))) {
                        return "LIMIT is defined by system default, cannot define new limit"
                    }
                }
                "DELETE" -> {
                    if (!(query.contains("WHERE ", ignoreCase = true)) && !(query.contains("WHERE\n",ignoreCase = true)) && !(query.contains("WHERE\r",ignoreCase = true))) {
                        return "Cannot execute update without WHERE closure!"
                    }
                    if ((query.contains("LIMIT ", ignoreCase = true)) || (query.contains("LIMIT\n",ignoreCase = true)) && !(query.contains("LIMIT\r",ignoreCase = true))) {
                        return "LIMIT is defined by system default, cannot define new limit"
                    }
                }
                "SET" -> {}
                "CALL" -> {}
                "SELECT" -> {}
                else -> return "Command not found"
            }

        }
        return null
    }

}