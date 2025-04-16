package kr.sjh.core.common.ads

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kr.sjh.core.common.BuildConfig


@SuppressLint("MissingPermission")
@Composable
fun AdMobBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center
    ) {
        AndroidView(modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp), factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BuildConfig.AD_MOB_BANNER_ID
                loadAd(
                    AdRequest.Builder().build()
                )
            }
        }, update = { adView ->
            adView.loadAd(AdRequest.Builder().build())
        })
    }

}