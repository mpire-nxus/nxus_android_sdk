## Summary
TechMpire nxus platform SDK for Android platform

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
compile ('com.nxus.dsp:library:1.0.20@aar') {
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
	android:name="nxus.dsp.token"
	android:value="[YOUR_NXUS_DSP_TOKEN]">
</meta-data>
```

To have Install Referrer resolved after application is downloaded, add the InstallReceiver within the <b>application</b> tag:
```
<receiver
	android:name="com.nxus.dsp.receivers.InstallReceiver"
	android:exported="true">
	<intent-filter>
		<action android:name="com.android.vending.INSTALL_REFERRER" />
	</intent-filter>
</receiver>
```

If you already have a BroadcastReceiver defined in your manifest file that is subscribed to the <b>com.android.vending.INSTALL_REFERRER</b> event, then instead of adding the <b>InstallReceiver</b>, add the following to your AndroidManifest.xml file:
```
<receiver 
	android:name="com.nxus.dsp.receivers.MultipleInstallReceiver"
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
NxusDSPTracker.initializeLibrary(getApplication());
```
This will automatically also track activity transition events (activity started, activity paused, etc.). If you do not wish to track this, initialiase the library in the following way:
```
NxusDSPTracker.initializeLibrary(getApplication(), false);
```

Once initialisation is done, an <b>app_start</b> event is automatically sent. If the application is started for the first time after installation, instead of <b>app_start</b>, <b>first_app_launch</b> is sent.

## Sending custom events
You can send custom events by calling the method <b>trackEvent</b>:
```
NxusDSPTracker.trackEvent("event_name");
```

If you have any additional parameters you would like to send, pass in an instance of TrackingParams:
```
TrackingParams params = new TrackingParams();
params.put("key", "value");
NxusDSPTracker.trackEvent("event_name", params);
```

## Logging
To enable logging, call the method setLogLevel before library initialisation:
```
NxusDSPTracker.setLogLevel(LogLevel.Debug);
```

## Author

TechMpire ltd.

## License

nxus_android_sdk is available under the MIT license. See the LICENSE file for more info.
