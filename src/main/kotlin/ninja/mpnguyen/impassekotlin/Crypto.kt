package ninja.mpnguyen.impassekotlin

import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class FileRepresentation {
    class VersionMismatchException(message: String) : Exception(message)

    val version : String

    val encryption_salt_encoded : String
    val hmac_salt_encoded : String

    /** The actual hash output for signing the encrypted kv store */
    val database_hmac_encoded : String

    /** Base64 encoded representation of the encrypted kv store */
    val database_encrypted_encoded : String

    /** Random 256 bit initialization vector for encrypting the kv store */
    val database_encryption_iv_encoded : String

    constructor(store : KeyValueStore, password : CharArray) {
        this.version = "0.0"

        // Get the byte representation of the key-value store to persist
        val store_bytes = store.toBytes()

        // Grab some instances of utility objects
        val random = SecureRandom.getInstanceStrong()
        val base64_encoder = Base64.getEncoder()

        // Generate some nice random bits for encryption IV
        val database_encryption_iv = ByteArray(16) // 16 * 8 bits
        random.nextBytes(database_encryption_iv)
        database_encryption_iv_encoded = base64_encoder
                .encodeToString(database_encryption_iv)

        // Generate some nice random bits for the salt
        val encryption_salt = ByteArray(512)
        random.nextBytes(encryption_salt)
        encryption_salt_encoded = base64_encoder
                .encodeToString(encryption_salt)

        // Generate some nice random bits for the salt
        val hmac_salt = ByteArray(256)
        random.nextBytes(hmac_salt)
        hmac_salt_encoded = base64_encoder.encodeToString(hmac_salt)

        // Encrypt the key value store
        val database_encrypted = cipher(
                Cipher.ENCRYPT_MODE,
                store_bytes,
                password,
                encryption_salt,
                database_encryption_iv
        )
        database_encrypted_encoded = base64_encoder
                .encodeToString(database_encrypted)

        // We do this after encrypting the DB because we want to
        // authentication the encrypted DB before performing the decryption
        val database_hmac = hmac(password, hmac_salt, database_encrypted)
        database_hmac_encoded = base64_encoder.encodeToString(database_hmac)
    }

    fun cipher(
            mode : Int,
            input : ByteArray,
            password : CharArray,
            salt : ByteArray,
            iv : ByteArray
    ) : ByteArray {
        val encryption_key_spec = PBEKeySpec(password, salt, 100000, 128)
        val encryption_key = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA512")
                .generateSecret(encryption_key_spec)
        val encryption_secret_key_spec = SecretKeySpec(
                encryption_key.encoded,
                "AES"
        )
        val encryption_iv_spec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(mode, encryption_secret_key_spec, encryption_iv_spec)
        return cipher.doFinal(input)
    }

    fun hmac(
            password : CharArray,
            salt : ByteArray,
            input : ByteArray
    ) : ByteArray {
        val hmac_algorithm = "HmacSHA512"
        val hmac_machine = Mac.getInstance(hmac_algorithm)
        val hmac_spec = PBEKeySpec(password, salt, 100000, 512)
        val hmac_key = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA512")
                .generateSecret(hmac_spec)
        hmac_machine.init(hmac_key)
        hmac_machine.update(input)
        return hmac_machine.doFinal()
    }

    fun open(password: CharArray) : KeyValueStore {
        // Unencode all the datum to get the bytes
        if (version == "0.0") {
            throw VersionMismatchException("We cannot open version " + version)
        }
        val base64_decoder = Base64.getDecoder()
        val encryption_salt = base64_decoder.decode(encryption_salt_encoded)
        val hmac_salt = base64_decoder.decode(hmac_salt_encoded)
        val database_hmac = base64_decoder.decode(database_hmac_encoded)
        val database_encryption_iv = base64_decoder
                .decode(database_encryption_iv_encoded)
        val database_encrypted = base64_decoder
                .decode(database_encrypted_encoded)


        // Authenticate the database first
        val derived_hmac = hmac(password, hmac_salt, database_encrypted)
        if (!equals(derived_hmac, database_hmac)) {
            throw Exception("Failed to authenticate")
        }

        // Decrypt the database after it's been authenticated
        val database_bytes = cipher(
                Cipher.DECRYPT_MODE,
                database_encrypted,
                password,
                encryption_salt,
                database_encryption_iv
        )
        return KeyValueStore(database_bytes)
    }

    private fun equals(
            derived_hmac: ByteArray,
            database_hmac: ByteArray
    ) : Boolean {
        if (derived_hmac.size != database_hmac.size) {
            return false
        }

        return derived_hmac.mapIndexed {
            index, ours ->
                val other = database_hmac[index]
                return ours == other
        }.all { it }
    }
}