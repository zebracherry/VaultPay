package nz.co.vaultpay.data

import androidx.room.Dao
import androidx.room.Query
import androidx.sqlite.db.SupportSQLiteQuery

/**
 * [M4 / MASVS-CODE-4] SQL injection via a raw, string-built query.
 *
 * loginRaw() concatenates untrusted input straight into SQL. Input like
 *   username = "' OR '1'='1' --"
 * returns every row. Exercise it through the login screen or a Frida call.
 *
 * SECURE: parameterised @Query with :bind arguments (see loginSafe()).
 */
@Dao
interface VaultDao {

    // The insecure path: caller builds the SQL string and passes it in.
    @Query("SELECT * FROM users") // placeholder; real call uses rawQuery below
    fun getAll(): List<UserEntity>

    // Parameterised = safe. Kept as the "correct" reference implementation.
    @Query("SELECT * FROM users WHERE username = :u AND password = :p LIMIT 1")
    fun loginSafe(u: String, p: String): UserEntity?
}

/**
 * The deliberately injectable helper. Room's SupportSQLiteQuery lets you run raw SQL;
 * building it by concatenation is the vulnerability.
 */
object VaultRawQueries {
    fun loginRawSql(username: String, password: String): String =
        "SELECT * FROM users WHERE username = '$username' AND password = '$password'"
}
