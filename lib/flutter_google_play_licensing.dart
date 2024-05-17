import 'flutter_google_play_licensing_platform_interface.dart';

class FlutterGooglePlayLicensing {
  Future<int> check({required String base64PublicKey, String? salt}) {
    return FlutterGooglePlayLicensingPlatform.instance
        .check(base64PublicKey: base64PublicKey, salt: salt);
  }

  Future<bool> isAllowed({required String base64PublicKey, String? salt}) {
    return FlutterGooglePlayLicensingPlatform.instance
        .isAllowed(base64PublicKey: base64PublicKey, salt: salt);
  }

  Future<String?> getUserId() {
    return FlutterGooglePlayLicensingPlatform.instance.getUserId();
  }
}
