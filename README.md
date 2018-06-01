# icapps Android Architecture Components

[ ![Download](https://api.bintray.com/packages/icapps/maven/icapps-android-architecture/images/download.svg) ](https://bintray.com/icapps/maven/icapps-android-architecture/_latestVersion)

Library containing architecture components for android apps. The components in this library should be loosely coupled and
most of the dependencies being to external libraries referenced in the code should be added manually to the consuming project's 
build.gradle file.


### Setup
```
//Include dependency to base library
implementation "com.icapps.android:architecture:${archComponentsVersion}"

//Include used library component dependencies.
//In this case we use retrofit and leakcanary helpers from the architecture library
implmentation "com.squareup.retrofit2:retrofit:${retrofitVersion}"
implmentation "com.squareup.leakcanary:leakcanary-android:${leakCanaryVersion}"

```


### Components

#### ObservableFuture
Concept of a 'Future' (something that will deliver the result of an operation in some undefined point in the future) that
can be easily observed on android. The api is designed to be as fluent as possible

Example 1. In this case the callbacks will be executed on the main thread and the future will cancel itself when the lifecycle 
enters the `STOPPED` state
```
    val future = createBackgroundFuture()
    future onSuccess {  //Lambda function reiving the value in case the future completes successfully
        ...
    } onFailure {       //Lambda function reiving the error in case the future completes with an exception
        ...
    } observe lifecycle
```

Example 2. In this case the callbacks will be executed on the tread that is posting the result (success or failure)
```
    createBackgroundFuture() onSuccess {
        ...
    } onFailure {
        ...
    } observe onCaller
```