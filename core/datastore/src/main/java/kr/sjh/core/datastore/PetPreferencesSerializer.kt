package kr.sjh.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object PetPreferencesSerializer : Serializer<PetPreferences> {
    override val defaultValue: PetPreferences
        get() = PetPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PetPreferences {
        try {
            return PetPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: PetPreferences, output: OutputStream) = t.writeTo(output)
}