package jiren.service.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.zaxxer.hikari.HikariDataSource
import jiren.data.entity.Database
import jiren.data.enum.SGBD
import jiren.security.credentials.CredentialsService
import org.springframework.stereotype.Service

@Service
class DatabasePicker(private val credentialsService: CredentialsService) {

    fun getConnection(db: Database): Any? {
        val dataSource = HikariDataSource()
        dataSource.username = db.user
        dataSource.password = credentialsService.externalDatabases.getString(db.secretName)
        dataSource.connectionTimeout = db.timeout.toLong()
        dataSource.maximumPoolSize = 1
        dataSource.maxLifetime = 600000

        lateinit var mongodb: MongoClient

        return when (db.sgbd) {
            SGBD.MariaDB -> {
                dataSource.jdbcUrl = "jdbc:mariadb://${db.host}:${db.port}/${db.schemaName}?"
                return dataSource.connection
            }
            SGBD.MySQL -> {
                dataSource.jdbcUrl = "jdbc:mysql://${db.host}:${db.port}?"//${db.schemaName}?
                return dataSource.connection
            }
            SGBD.PostgreSQL -> {
                dataSource.jdbcUrl = "jdbc:postgresql://${db.host}:${db.port}/${db.schemaName}?"
                return dataSource.connection
            }
            SGBD.Oracle -> {
                dataSource.jdbcUrl = "jdbc:oracle:thin:@${db.host}:${db.port}:${db.sid}"
                return dataSource.connection
            }
            SGBD.MongoDb -> {
                mongodb = MongoClients.create("mongodb://${db.user}:${credentialsService.externalDatabases.getString(db.secretName)}@${db.host}:${db.port}/?w=majority")
                return mongodb.getDatabase(db.schemaName!!)
            }
            else -> {
                null
            }
        }
    }

}