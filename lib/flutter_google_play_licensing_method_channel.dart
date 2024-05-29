import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_google_play_licensing_platform_interface.dart';

/// An implementation of [FlutterGooglePlayLicensingPlatform] that uses method channels.
class MethodChannelFlutterGooglePlayLicensing extends FlutterGooglePlayLicensingPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_google_play_licensing');

  @override
  Future<String> check({String? salt}) async {
    final result = await methodChannel.invokeMethod<String>(
        'check', {'salt': salt});
    return result!;
  }

}
