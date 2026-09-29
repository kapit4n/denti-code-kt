package com.denticode.kt.data

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the packaging invariant relied upon by the `.deb` installer:
 *
 *   application files -> /opt/denti-code (installed and replaced by dpkg)
 *   user data         -> ~/.denti-code-kt/denti-clinic.db (never packaged, never touched by an upgrade)
 *
 * Every test points `user.home` at a throwaway sandbox, so the developer's real database is never read,
 * written or deleted by the test suite.
 */
class DatabasePreservationTest {

    private lateinit var sandboxHome: Path
    private var realUserHome: String? = null

    @BeforeTest
    fun setUp() {
        realUserHome = System.getProperty("user.home")
        sandboxHome = Files.createTempDirectory("denti-code-db-test")
        System.setProperty("user.home", sandboxHome.toString())
    }

    @AfterTest
    fun tearDown() {
        realUserHome?.let { System.setProperty("user.home", it) }
        sandboxHome.toFile().deleteRecursively()
    }

    private fun databaseFile(): Path = DentiDatabase.localDatabasePath()

    @Test
    fun `database lives under the user home and outside any installation directory`() {
        val db = databaseFile()

        assertEquals(
            sandboxHome.resolve(".denti-code-kt").resolve("denti-clinic.db"),
            db,
            "The database must stay at ~/.denti-code-kt/denti-clinic.db",
        )
        assertFalse(
            db.toString().contains("/opt/"),
            "The database must never live inside the /opt installation directory managed by the .deb",
        )
    }

    @Test
    fun `first startup creates the database file and its parent directory`() {
        assertFalse(Files.exists(databaseFile()), "Precondition: no database exists yet")

        DentiDatabase.connectAndMigrate(resetLocalDatabase = false)

        assertTrue(Files.exists(databaseFile()), "Startup must create ~/.denti-code-kt/denti-clinic.db")
        assertTrue(Files.size(databaseFile()) > 0L, "A freshly created database must not be empty")
    }

    @Test
    fun `restart and upgrade preserve the existing database`() {
        DentiDatabase.connectAndMigrate(resetLocalDatabase = false)
        val patientsBefore = transaction { PatientsTable.selectAll().count() }

        val markerId =
            transaction {
                PatientsTable.insert {
                    it[firstName] = "Upgrade"
                    it[lastName] = "Survivor"
                    it[dateOfBirth] = "1999-12-31"
                    it[contactPhone] = "+34 600 000 999"
                    it[email] = "upgrade.survivor@mail.test"
                    it[createdAtEpochMs] = 1_700_000_000_000L
                } get PatientsTable.id
            }

        // Second launch of the application — this is what an install or an upgrade of the .deb does.
        DentiDatabase.connectAndMigrate(resetLocalDatabase = false)

        assertTrue(Files.exists(databaseFile()), "Startup must not delete an existing database")

        val patientsAfter = transaction { PatientsTable.selectAll().count() }
        assertEquals(patientsBefore + 1, patientsAfter, "Startup must not drop user rows")

        val survivor =
            transaction {
                PatientsTable.selectAll().where { PatientsTable.id eq markerId }.singleOrNull()
            }
        assertTrue(survivor != null, "The row written before the upgrade must still be present")
        assertEquals("upgrade.survivor@mail.test", survivor?.get(PatientsTable.email))
    }

    @Test
    fun `reset is opt-in and only the explicit reset path drops existing rows`() {
        DentiDatabase.connectAndMigrate(resetLocalDatabase = false)

        val markerId =
            transaction {
                PatientsTable.insert {
                    it[firstName] = "Reset"
                    it[lastName] = "Victim"
                    it[dateOfBirth] = "1999-12-31"
                    it[contactPhone] = "+34 600 000 998"
                    it[email] = "reset.victim@mail.test"
                    it[createdAtEpochMs] = 1_700_000_000_000L
                } get PatientsTable.id
            }
        assertEquals(1L, patientRows(markerId), "Precondition: the marker row exists")

        DentiDatabase.connectAndMigrate(resetLocalDatabase = true)

        assertEquals(
            0L,
            patientRows(markerId),
            "Only resetLocalDatabase = true (--reset-local-db / --fresh-db / -Ddenti.resetLocalDb=true) may drop rows",
        )
        assertTrue(
            Files.exists(databaseFile()),
            "Even the reset path recreates the file, so a fresh install always ends up with a usable database",
        )
    }

    private fun patientRows(id: Int): Long =
        transaction { PatientsTable.selectAll().where { PatientsTable.id eq id }.count() }
}
