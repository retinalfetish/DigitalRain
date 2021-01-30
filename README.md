# DigitalRain
A styleable widget that recreates the classic Matrix digital rain effect. It features a number of customizations for the animation and text.
## Screenshots
<img src="/art/screenshot-dark.gif" alt="Screenshot" height=600> <img src="/art/screenshot-light.gif" alt="Screenshot" height=600>

## Usage
The library is part of [JCenter](https://bintray.com/rogue/maven/com.unary:digitalrain) (a default repository) and can be included in your project by adding `implementation 'com.unary:digitalrain:1.0.0'` as a module dependency. The latest build can also be found at [JitPack](https://jitpack.io/#com.unary/digitalrain).
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
This widget has a number of options that can be configured in both the XML and code. An example app is provided in the project repository to illustrate its use and the ability to add a depth blur to the text.
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.unary.digitalrain.DigitalRain
        android:id="@+id/digitalrain"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#000000"
        android:textColor="#00FF00"
        app:rainDepth="10"
        app:rainIntensity="3"
        app:rainSpeed="80" />

</FrameLayout>
```
### XML attributes
The following optional attributes can be used to change the look and feel of the view:
```
app:rainAlpha="float"               // How quickly the character trails fade
app:rainAnimator="reference"        // Animator to use for the text rain
app:rainDepth="float"               // Layer depth blur. This is drawing intensive
app:rainIntensity="integer"         // Default number of layers is 3
app:rainSpeed="integer"             // Time interval in milliseconds for speed

android:autoStart="boolean"         // If animation should start automatically
android:enabled="boolean"           // Changes the view state
android:textColor="reference|color" // Reference to a color selector or simple color
android:textSize="dimension"        // Text size to use. Default is "14sp"
```
