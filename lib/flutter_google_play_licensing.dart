import 'flutter_google_play_licensing_platform_interface.dart';

class FlutterGooglePlayLicensing {
  Future<String> check({String? salt}) {
    return FlutterGooglePlayLicensingPlatform.instance
        .check(salt: salt);
  }
}
