## Summary
TechMpire Measurement SDK for Android platform

## Integration into project
To integrate the SDK, add you have to add our Maven repository to your <b>app's build.gradle file</b>:
```
allprojects {
	repositories {
		jcenter()
        maven {
            url "http://maven.nxus.mobi/libs-release-local"
        }
        maven {
            url 'https://maven.google.com'
        }
    }
}
```

Open your <b>app</b> module's <b>build.gradle</b> file and add the following dependencies:
```
compile 'com.google.android.gms:play-services-base:9.2.0'
compile ('com.nxus.measurement:library:1.1.2@aar') {
	transitive = true
}
```

## AndroidManifest.xml modifications
Add the following permissions to your applications AndroidManifest.xml file:
```
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.INTERNET"/>
```

Also, inside the <b>application</b> tag, add following <b>meta-data</b> tag:
```
<meta-data 
	android:name="nxus.measurement.token"
	android:value="[YOUR_API_KEY]">
</meta-data>
```

To have Install Referrer resolved after application is downloaded, add the InstallReceiver within the <b>application</b> tag:
```
<receiver
	android:name="com.nxus.measurement.receivers.InstallReceiver"
	android:exported="true">
	<intent-filter>
		<action android:name="com.android.vending.INSTALL_REFERRER" />
	</intent-filter>
</receiver>
```

If you already have a BroadcastReceiver defined in your manifest file that is subscribed to the <b>com.android.vending.INSTALL_REFERRER</b> event, then instead of adding the <b>InstallReceiver</b>, add the following to your AndroidManifest.xml file:
```
<receiver 
	android:name="com.nxus.measurement.receivers.MultipleInstallReceiver"
	android:exported="true">
	<intent-filter>
		<action android:name="com.android.vending.INSTALL_REFERRER" />
	</intent-filter>
</receiver>
```

Please note that this should be added as the first receiver for INSTALL_REFERRER, or else it won't be called.

## SDK initialisation
After you completed the previous steps, you are ready to initialise the library and start sending events.
You can initialise it within your Application class if you have one, if not, then do it in the <b>onCreate</b> method of your starting Activity:
```
MpireNxusMeasurement.initializeLibrary(getApplication());
```
This will automatically also track activity transition events (activity started, activity paused, etc.). If you do not wish to track this, initialiase the library in the following way:
```
MpireNxusMeasurement.initializeLibrary(getApplication(), false);
```

Once initialisation is done, an <b>app_start</b> event is automatically sent. If the application is started for the first time after installation, instead of <b>app_start</b>, <b>first_app_launch</b> is sent.

## Sending custom events
You can send custom events by calling the method <b>trackEvent</b>:
```
MpireNxusMeasurement.trackEvent("event_name");
```

If you have any additional parameters you would like to send, pass in an instance of TrackingParams:
```
TrackingParams params = new TrackingParams();
params.put("key", "value");
MpireNxusMeasurement.trackEvent("event_name", params);
```

## Sending predefined events
You can send predefined events using the SDK, with following methods:
```
MpireNxusMeasurement.trackEventInstall(TrackingParams params);
MpireNxusMeasurement.trackEventOpen(TrackingParams params);
MpireNxusMeasurement.trackEventRegistration(TrackingParams params);
MpireNxusMeasurement.trackEventPurchase(TrackingParams params);
MpireNxusMeasurement.trackEventLevel(TrackingParams params);
MpireNxusMeasurement.trackEventTutorial(TrackingParams params);
MpireNxusMeasurement.trackEventAddToCart(TrackingParams params);
MpireNxusMeasurement.trackEventCheckout(TrackingParams params);
MpireNxusMeasurement.trackEventInvite(TrackingParams params);
MpireNxusMeasurement.trackEventAchievement(TrackingParams params);
```
Every method takes a TrackingParams object, but is not mandatory.
```
TrackingParams params = new TrackingParams();
params.put("key", "value");
```

## Logging
To enable logging, call the method setLogLevel before library initialisation:
```
MpireNxusMeasurement.setLogLevel(LogLevel.Debug);
```

## Author

TechMpire ltd.

## License

nxus_android_sdk is available under the MIT license. See the LICENSE file for more info.
