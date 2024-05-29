import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_google_play_licensing_method_channel.dart';

abstract class FlutterGooglePlayLicensingPlatform extends PlatformInterface {
  /// Constructs a FlutterGooglePlayLicensingPlatform.
  FlutterGooglePlayLicensingPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterGooglePlayLicensingPlatform _instance = MethodChannelFlutterGooglePlayLicensing();

  /// The default instance of [FlutterGooglePlayLicensingPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterGooglePlayLicensing].
  static FlutterGooglePlayLicensingPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterGooglePlayLicensingPlatform] when
  /// they register themselves.
  static set instance(FlutterGooglePlayLicensingPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String> check({String? salt}) {
    throw UnimplementedError('check() has not been implemented.');
  }

}
