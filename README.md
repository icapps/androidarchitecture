# icapps Android Architecture Components

[ ![Download](https://api.bintray.com/packages/icapps/maven/icapps-android-architecture/images/download.svg) ](https://bintray.com/icapps/maven/icapps-android-architecture/_latestVersion)

Library containing architecture components for android apps. The components in this library should be loosely coupled and
most of the dependencies being to external libraries referenced in the code should be added manually to the consuming project's 
build.gradle file.

**Note**: Versions ending their code in -x target the androidx versions of the support library


## Setup
```
// Include dependency to base library
implementation "com.icapps.android:architecture:${archComponentsVersion}"

// Include used library component dependencies.
// In this case we use retrofit and leakcanary helpers from the architecture library
implementation "com.squareup.retrofit2:retrofit:${retrofitVersion}"
implementation "com.squareup.leakcanary:leakcanary-android:${leakCanaryVersion}"

```

## Components

### ObservableFuture

#### Introduction

An `ObservableFuture` represents an operation that will deliver a result / multiple results at some point in the future. They are designed so that they can easily be observed on Android, keeping in mind the threading challenges of the platform. The API is designed to be as fluent as possible, while still being pretty concise.

The result of ObservableFutures can be posted on different threads:

- On the main thread by using `future observe onMain`.
- On the caller thread by using `future observe onCaller`
- Using a lifecycle object, which will observe the ObservableFuture on the main thread, and cancel the ObservableFuture once the lifecycle hits `Lifecycle.Event.ON_STOP`. You can do this by using `future observe lifecycle`

A future has two main callbacks: `onSuccess` and `onFailure`. Each callback can only be attached once! Callbacks can and will be called more than once if multiple results are posted.

Exceptions thrown in `onSuccess` will be caught and propagated to `onFailure`. Warning: exceptions thrown in `onFailure` are not caught and will crash you application. Be sure to wrap dangerous method calls in `onFailure` in a try catch.

#### Cancelling

You can cancel an `ObservableFuture` by calling `future.cancel()`. This will ensure that any callbacks will be cleared from memory and will not be called.

Example 1. In this example the callbacks will be executed on the main thread and the future will cancel itself when the lifecycle 
enters the `STOPPED` state
```
val coffeeFuture = coffeeMachine.makeCoffee() onSuccess { coffee ->
    // Lambda function reiving the value in case the future completes successfully
    ...
} onFailure { throwable ->
    // Lambda function reiving the error in case the future completes with an exception
    ...
} observe lifecycle
```

Example 2. In this example the callbacks will be executed on the thread that is posting the result (success or failure).
```
val coffeeFuture = coffeeMachine.makeCoffee() onSuccess { coffee ->
    ...
} onFailure {
    ...
} observe onCaller

override fun onStop() {
    coffeeFuture.cancel()
}
```

Example 3. In this example the callbacks will be executed on the main thread.
```
val coffeeFuture = coffeeMachine.makeCoffee() onSuccess { coffee ->
    ...
} onFailure {
    ...
} observe onMain

override fun onStop() {
    coffeeFuture.cancel()
}
```

#### Executing synchronously

`ObservableFuture` has a method called `execute()` that blocks the calling thread until the result of the future is ready to be delivered.
*WARNING:* This method is generally dangerous as it has subtle pitfalls.

Except for `RetrofitObservableFuture` this method will use a latch to wait for the results that are delivered using direct observe. If the underlying future cannot be executed for whatever reason (eg thread could not be started, ...),
this method will block. Also, since this is being executed in the context of the future in the first place, special care should be taken with regards to reentrant locks (eg: synchronized(...), ...) as the thread calling [execute] will not be the same thread that is doing the actual executing.

Example 4. Executing a future synchronously
```
val future = coffeeMachine.makeCoffee() // returns an ObservableFuture
val coffee = future.execute(10000) // Will block the current thread for 10 seconds, and return coffee (or throw the throwable from onFailure)
```

#### Creating Futures

There are a few implementations of the `(Mutable)ObservableFuture`-interface. The simplest one being `ConcreteMutableObservableFuture`.

It allows you to pass results (either success or failure) to the future with the `onResult(T)` and `onFailure(throwable)` methods. The result will be posted on the correct thread for you (depending on which thread the future is observed)

Example 5. Creating a `ConcreteMutableObservableFuture` (If you do something similar, make sure to unsubscribe from the timer at some point!)
```
val timeFuture = ConcreteMutableObservableFuture<Long>()
someTimer.addTickListener(intervalMs = 1000) { timeFuture.onResult(System.currentTimeMillis()) }
return future
```

A shortcut for creating a future of something that runs on a (background) thread is `onBackground { }`. It runs the supplied lambda on a new thread (you can pass an `Executor` if you want) and posts the result of the lambda to the `ConcreteMutableObservableFuture` that it returns.
An alternative also exists for making sure something is not running on the main thread: `offMain { }`. It achieves the same thing as `onBackground { }`, but will not start a new thread if the current thread is not the main thread.

Example 6. Creating a future with `onBackground`
```
class CoffeeMachine @Inject constructor(heater: WaterHeater, grinder: CoffeeGrinder, pump: Pump) {

    fun makeCoffee(): ObservableFuture<Coffee> {
        return onBackground {
            val warmWater = heater.heat(temp = 100)
            val groundCoffee = grinder.grind(amount = 42)
            pump.pumpCoffee(warmWater, groundCoffee)
        }
    }

}

```

There are also some shortcuts for directly returning a future with a certain result:

- `ObservableFuture.withData(t)` or `t.asObservable()`: returns an `Observable<T>` which will immediately call `onSuccess` with the provided data
- `ObservableFuture.withError<T>(throwable)` returns an `ObservableFuture<T>` which will immediately call `onFailure` with the provided throwable

Our library also contains extensions for creating futures of Retrofit calls: `Call.wrapToFuture()`.

Example 7: Creating a future of a Retrofit `Call`
```
fun getChannels(): ObservableFuture<List<Channel>> {
    return retrofitChannelService.getChannels().wrapToFuture()    
}
```

#### Peeking

`ObservableFuture`s also have another result/failure callback, which we call peeking. You can add a callback which will also be called on success and/or failure. You can either listen only for success: `peek {}` or for both success and failure: `peekBoth { success, failure -> ... }`

Peeking is most easily explained with a practical example:

Example 8. Using `peek`

```
class ChannelRepository(val channelService: ChannelService) {

    private val memCache = mutableListOf<Channel>()

    fun getChannels(): ObservableFuture<Channel> {
        synchronized(this) {
            if(memCache.isNotEmpty()) return memCache.asObservable()
        }

        return channelService.getChannels().wrapToFuture() peek { channels ->
            synchronized(this) {
                memCache.clear()
                memCache.putAll(channels)
            }
        }
    }

}
```
In this example, we keep a memory cache of channels. When there is no cache available and the channels call succeeds, the memCache is updated.

#### Combining Futures

`ObservableFuture`s can be combined in multiple ways:

- `ObservableFuture.of(a, b, ...)`  n futures will be executed asynchronously. Once both futures have reached `onSuccess`, the result (a `Pair<firstT, secondT>`) will be posted to onSuccess.

Example 9. `ObservableFuture.of`
```
ObservableFuture.of(sessionRepository.getSession(), channelRepository.getChannels()) onSuccess { (session, channels) ->
    ...
} observe lifecycle
```

- `future andThen { future2 }` : The 2 futures will be executed after one another. Once the first future has reached `onSuccess`, the next `ObservableFuture` will be created with the result from the previous. The result of the second future will be posted to `onSuccess`.

Example 10. usage of `andThen`
```
sessionRepository.getSession() andThen { session ->
    session -> channelRepository.getChannels(session.profileId)
} onSuccess { channels ->
    ...
} observe lifecycle
```

- `future andThenAlso { future2 }` : The 2 futures that will be executed after one another. Once the first future has reached `onSuccess`, the next `ObservableFuture` will be created with the result from the previous. The result of both futures (a `Pair<firstT, secondT>`) will be posted to `onSuccess`.

Example 11. usage of `andThenAlso`
```
sessionRepository.getSession() andThenAlso { session ->
    session -> channelRepository.getChannels(session.profileId)
} onSuccess { (session, channels) ->
    ...
} observe lifecycle
```

### ViewModels, Repositories, Dagger setup

#### BaseRepository

`BaseRepository` is an abstract base class for repositories. It mostly provides helper methods for transforming retrofit `Call`s into ObservableFutures (`makeCall()`).

#### BaseViewModel

`BaseViewModel` is an abstract base class for ViewModels. It extends the `ViewModel` class from Google's Architecture Components.

The only thing that `BaseViewModel` does over a `ViewModel` is providing `saveInstanceState`, `restoreInstanceState` methods and a var which allows you to see whether viewModels are fresh instances or recreated. If you use our `ViewModelLifecycleController`, state saving and restoring for these viewmodels is handled for you.

#### ViewModelLifecycleController and Dagger setup

`ViewModelLifecycleController` is an injectable wrapper class around `ViewModelProviders.of`. It handles the creation of viewModels for activities/fragments and handles the saving and restoring of instance state into the viewmodels.

For injecting ViewModels with dagger, the classes `ViewModelKey` and `ViewModelFactory` are included. For a clear dagger setup example, check out our project template: https://github.com/icapps/android-template-kotlin-viewmodel

### Other extensions and utils

Other extensions and utilities can be found in the `com.icapps.architecture.utils.ext` package:

- `UIExtensions.kt` contains many shortcuts for inflating views, converting dp/px units and loading resources
- `LifecycleExt.kt` contains a shortcut for adding a stop observer to a `Lifecycle`
- `ObservableExt.kt` contains utils for observing `ObservableField<T>`s, `ObservableInt`s, `ObservableBoolean`s and `ObservableList`s with `lifecycle`