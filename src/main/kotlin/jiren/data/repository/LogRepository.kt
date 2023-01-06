package jiren.data.repository

import jiren.data.entity.Log
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface LogRepository : JpaRepository<Log, Long>, JpaSpecificationExecutor<Log>