import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_google_play_licensing_platform_interface.dart';

/// An implementation of [FlutterGooglePlayLicensingPlatform] that uses method channels.
class MethodChannelFlutterGooglePlayLicensing extends FlutterGooglePlayLicensingPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_google_play_licensing');

  @override
  Future<int> check({required String base64PublicKey, String? salt}) async {
    final result = await methodChannel.invokeMethod<int>(
        'check', {'base64PublicKey': base64PublicKey, 'salt': salt});
    return result!;
  }

  @override
  Future<bool> isAllowed(
      {required String base64PublicKey, String? salt}) async {
    final result = await methodChannel.invokeMethod<bool>(
        'isAllowed', {'base64PublicKey': base64PublicKey, 'salt': salt});
    return result!;
  }

  @override
  Future<String?> getUserId() async {
    final result = await methodChannel.invokeMethod<String?>('getUserId');
    return result;
  }

}
