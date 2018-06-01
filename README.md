# icapps Android Architecture components

[ ![Download](https://api.bintray.com/packages/icapps/maven/icapps-android-architecture/images/download.svg) ](https://bintray.com/icapps/maven/icapps-android-architecture/_latestVersion)

Library containing architecture components for android apps. The components in this library should be loosely coupled and
most of the dependencies being to external libraries referenced in the code should be added manually to the consuming project's 
build.gradle file.


### Setup
```
//Include dependency to base library
implementation "com.icapps.android:architecture:${archComponentsVersion}"

//Include used library component dependencies. In this case we use retrofit and leakcanary helpers from the architecture library
implmentation "com.squareup.retrofit2:retrofit:${retrofitVersion}"
implmentation "com.squareup.leakcanary:leakcanary-android:${leakCanaryVersion}"

```


### Components