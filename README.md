# AVD-Gradle-Plugin
Gradle plugin to assist in managing android virtual devices

`android list sdk -a -e`

```groovy
AVD.configs {
    tablet_25 {
        avd {
            abi "x86" | "x86_64" | "armeabi-v7a" | "arm64-v8a" //(default x86_64)
            api 25 //(default highest stable api)
            type 'google_apis_playstore' | 'google_apis' | 'default' | 'android-wear' | 'android-tv' //(default google_apis)
            deviceId "pixel" //from avdmanager list device
            sdSize "1000M" //optional

        }
        emu {
            skin "nexus_5x"
            port 12345 //optional auto assign default
            launch_options ""
            wipe_data false //(default true)
            use_data file('path') //optional
        }
    }
}
```

## Tasks 
 - (per unique sys image) install/update sys-img 
    - This file has the rev number etc to match against the sys-img.xml file.
    - `sdkmanager --install "system-images;android-25;google_apis_playstore;x86"`
 
 - (per defined config) start emulator //figure out how to check if already running and skip?
 
 - (per defined config) stop emulator //only if it is running?   
 
 ## Usage
 Don't. It isn't usable yet. This readme is currently a scratch pad for the concept
 
 Going to hold out for an update to the new `sdkmanager` cli that should help make the implementation of this plugin much more simple.
 https://commonsware.com/blog/2016/12/12/sdkmanager-command-line-sdk-installs.html
 
 Giving up on holding out. Turns out you can get the class files for the sdkmanager cli tool by depending on the android gradle plugin. 
 This is better because I won't have to run exec tasks and parse anything I can just mimic it while using it as a guide to do the work I need done, I think...


 `avdmanager create avd --name 'O_6P_Playstore' --package 'system-images;android-O;google_apis_playstore;x86' --device 'Nexus 6P' --tag 'google_apis_playstore'`