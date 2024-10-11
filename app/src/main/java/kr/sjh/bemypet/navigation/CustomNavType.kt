package kr.sjh.bemypet.navigation

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.sjh.core.model.adoption.Pet

object CustomNavType {
    val petType = object : NavType<Pet>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): Pet? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): Pet {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun put(bundle: Bundle, key: String, value: Pet) {
            bundle.putString(key, Json.encodeToString(value))
        }

        override fun serializeAsValue(value: Pet): String {
            return Uri.encode(Json.encodeToString(value))
        }

    }
}