package com.google.play.flutter_google_play_licensing

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.google.android.vending.licensing.AESObfuscator
import com.google.android.vending.licensing.LicenseChecker
import com.google.android.vending.licensing.LicenseCheckerCallback
import com.google.android.vending.licensing.LicenseCheckerCallback.ERROR_CHECK_IN_PROGRESS
import com.google.android.vending.licensing.LicenseCheckerCallback.ERROR_INVALID_PACKAGE_NAME
import com.google.android.vending.licensing.LicenseCheckerCallback.ERROR_INVALID_PUBLIC_KEY
import com.google.android.vending.licensing.LicenseCheckerCallback.ERROR_MISSING_PERMISSION
import com.google.android.vending.licensing.LicenseCheckerCallback.ERROR_NON_MATCHING_UID
import com.google.android.vending.licensing.LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED
import com.google.android.vending.licensing.ServerManagedPolicy
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.Locale

/** FlutterGooglePlayLicensingPlugin */
class FlutterGooglePlayLicensingPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_google_play_licensing")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
        "check" -> {
          check(call, result)
        }
        "isAllowed" -> {
          isAllowed(call, result)
        }
        else -> {
          result.notImplemented()
        }
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun Context.checker(base64PublicKey: String, salt: ByteArray? = null): LicenseChecker {
    return LicenseChecker(
      this,
      ServerManagedPolicy(
        this,
        AESObfuscator(
          salt ?: PlayLicensingConfig.salt,
          packageName,
          Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        )
      ),
      base64PublicKey
    )
  }

  private fun LicenseChecker.checkAccess(onAllow: (Int) -> Unit = { _ -> }, onDontAllow: (Int) -> Unit = { _ -> }, onApplicationError: (Int) -> Unit = { _ -> }) {
    checkAccess(object : LicenseCheckerCallback {
      override fun allow(reason: Int) {
        onAllow(reason)
      }

      override fun dontAllow(reason: Int) {
        onDontAllow(reason)
      }

      override fun applicationError(errorCode: Int) {
        onApplicationError(errorCode)
      }
    })
  }

  private fun check(call: MethodCall, result: Result) {
    val checker = context.checker(
      base64PublicKey = call.argument<String>("base64PublicKey")!!,
      salt = call.argument<String>("salt")?.toHexByteArray)
    checker.checkAccess(
      onAllow = { reason ->
        result.onMain().success(reason)
      },
      onDontAllow = { reason ->
        result.onMain().error(reason.toString(), errorMessage = "dontAllow:$reason", errorDetails = "dontAllow:$reason")
      },
      onApplicationError = { errorCode ->
        result.onMain().error(errorCode.toString(), errorMessage = "applicationError:${errorCodeToDes(errorCode)}", errorDetails = "applicationError:${errorCodeToDes(errorCode)}")
      }
    )
  }

  private fun isAllowed(call: MethodCall, result: Result) {
    val checker = context.checker(
      base64PublicKey = call.argument<String>("base64PublicKey")!!,
      salt = call.argument<String>("salt")?.toHexByteArray)
    checker.checkAccess(
      onAllow = {
        result.onMain().success(true)
      },
      onDontAllow = {
        result.onMain().success(false)
      },
      onApplicationError = { errorCode ->
        result.onMain().error(errorCode.toString(), errorMessage = "applicationError:${errorCodeToDes(errorCode)}", errorDetails = "applicationError:${errorCodeToDes(errorCode)}")
      }
    )
  }

  private fun errorCodeToDes(errorCode: Int) : String {
    return when (errorCode) {
      ERROR_INVALID_PACKAGE_NAME -> "ERROR_INVALID_PACKAGE_NAME"
      ERROR_NON_MATCHING_UID -> "ERROR_NON_MATCHING_UID"
      ERROR_NOT_MARKET_MANAGED -> "ERROR_NOT_MARKET_MANAGED"
      ERROR_CHECK_IN_PROGRESS -> "ERROR_CHECK_IN_PROGRESS"
      ERROR_INVALID_PUBLIC_KEY -> "ERROR_INVALID_PUBLIC_KEY"
      ERROR_MISSING_PERMISSION -> "ERROR_MISSING_PERMISSION"
      else -> "UNKNOWN $errorCode"
    }
  }

  private object PlayLicensingConfig {
    val salt: ByteArray = byteArrayOf(
      -46, 65, 30, -128, -103, -57, 74, -64, 51, 88,
      -95, -45, 77, -117, -36, -113, -11, 32, -64, 89
    )
  }

}

fun Result.onMain(): ResultOnMain {
  return if (this is ResultOnMain) {
    this
  } else {
    ResultOnMain(this)
  }
}

class ResultOnMain(private val result: Result) : Result {
  private val handler: Handler by lazy {
    Handler(Looper.getMainLooper())
  }

  override fun success(res: Any?) {
    handler.post {
      result.success(res)
    }
  }

  override fun error(
    errorCode: String, errorMessage: String?, errorDetails: Any?
  ) {
    handler.post {
      result.error(errorCode, errorMessage, errorDetails)
    }
  }

  override fun notImplemented() {
    handler.post {
      result.notImplemented()
    }
  }
}

val String.toHexByteArray inline get(): ByteArray? = try {
  chunked(2).map {
    it.uppercase(Locale.US).toInt(16).toByte()
  }.toByteArray()
} catch (e: Throwable) { null }
