package kr.sjh.feature.adoption_detail.navigation

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.sjh.core.model.Screen
import kr.sjh.core.model.adoption.Pet
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.reflect.typeOf

@Serializable
data class PetDetail(
    val pet: Pet
) : Screen {
    companion object {
        val typeMap = mapOf(
            typeOf<Pet>() to PetType
        )
    }
}

val PetType = object : NavType<Pet>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Pet? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, Pet::class.java)
        } else {
            @Suppress("DEPRECATION") bundle.getParcelable(key)
        }
    }

    override fun parseValue(value: String): Pet {
        return Json.decodeFromString<Pet>(value)
    }

    override fun put(bundle: Bundle, key: String, value: Pet) {
        bundle.putParcelable(key, value)
    }

    override fun serializeAsValue(value: Pet): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            URLEncoder.encode(Json.encodeToString(value), StandardCharsets.UTF_8)
        } else {
            @Suppress("DEPRECATION")
            URLEncoder.encode(Json.encodeToString(value))
        }
    }
}

@Serializable
@Stable
data class PinchZoom(
    val imageUrl: String
) : Screen

fun NavController.navigateToPinchZoom(imageUrl: String, navOptions: NavOptions? = null) {
    navigate(PinchZoom(imageUrl), navOptions)
}

fun NavController.navigateToPetDetail(pet: Pet, navOptions: NavOptions? = null) {
    navigate(PetDetail(pet), navOptions)
}