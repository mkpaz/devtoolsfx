# devtoolsfx

DevToolsFX is a tool for navigating your application's scene graph and exploring node properties. It aims to be similar
to Chrome DevTools, but for JavaFX.

It's lightweight, around 250 KB, with no dependencies, allowing you to easily embed it into your app. The only JavaFX
dependency is `javafx.controls`, which your app will need regardless.

<p align="center">
<img src="https://raw.githubusercontent.com/mkpaz/devtoolsfx/master/.screenshots/inspector.png" alt="inspector"/>
</p>

Find more screenshots [here](https://github.com/mkpaz/devtoolsfx/tree/master/.screenshots).

## Getting started

Maven:

```xml

<dependency>
    <groupId>io.github.mkpaz</groupId>
    <artifactId>devtoolsfx-gui</artifactId>
    <version>1.0.0</version>
</dependency>
```

Gradle:

```groovy
dependencies {
    implementation 'io.github.mkpaz:devtoolsfx-gui:1.0.0'
}
```

After the primary stage is shown, you can launch the dev tools GUI at any time with:

```java
primaryStage.setOnShown(
    e -> GUI.openToolStage(primaryStage, getHostServices())
);
```

Check the `devtoolsfx.gui.GUI` class for additional ways to launch the dev tools, such as embedding it at the top or
bottom. Also, refer to the demo for a more detailed example.
