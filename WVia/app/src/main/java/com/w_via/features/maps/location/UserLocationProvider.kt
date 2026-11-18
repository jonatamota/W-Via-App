package com.w_via.features.maps.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class UserLocationProvider(private val context: Context) {

  private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

  fun temPermissao(): Boolean =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED ||
          ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
              PackageManager.PERMISSION_GRANTED

  suspend fun obterLocalizacao(): Pair<Double, Double>? {
    if (!temPermissao()) {
      Log.w(TAG, "Sem permissão de localização")
      return null
    }

    // lastLocation costuma responder mais rápido com o app em segundo plano (Rappi aberto)
    obterLastLocation()?.let {
      Log.d(TAG, "Localização via lastLocation: ${it.first}, ${it.second}")
      return it
    }

    return withTimeoutOrNull(LOCATION_TIMEOUT_MS) { obterCurrentLocation() }?.also {
      Log.d(TAG, "Localização via getCurrentLocation: ${it.first}, ${it.second}")
    } ?: run {
      Log.w(TAG, "Nenhuma localização disponível (lastLocation e getCurrentLocation)")
      null
    }
  }

  private suspend fun obterLastLocation(): Pair<Double, Double>? =
      suspendCancellableCoroutine { cont ->
        try {
          fusedClient.lastLocation
              .addOnSuccessListener { location ->
                cont.resume(location?.let { Pair(it.latitude, it.longitude) })
              }
              .addOnFailureListener {
                Log.w(TAG, "lastLocation falhou: ${it.message}")
                cont.resume(null)
              }
        } catch (e: SecurityException) {
          Log.w(TAG, "lastLocation SecurityException: ${e.message}")
          cont.resume(null)
        }
      }

  private suspend fun obterCurrentLocation(): Pair<Double, Double>? =
      suspendCancellableCoroutine { cont ->
        try {
          fusedClient
              .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
              .addOnSuccessListener { location -> cont.resume(location?.let { Pair(it.latitude, it.longitude) }) }
              .addOnFailureListener {
                Log.w(TAG, "getCurrentLocation falhou: ${it.message}")
                cont.resume(null)
              }
        } catch (e: SecurityException) {
          Log.w(TAG, "getCurrentLocation SecurityException: ${e.message}")
          cont.resume(null)
        }
      }

  companion object {
    private const val TAG = "UserLocationProvider"
    private const val LOCATION_TIMEOUT_MS = 8_000L
  }
}
