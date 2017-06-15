**AppHome**

[Download latest release (v1.5.2)](https://github.com/PHPirates/AppHome/blob/master/AppHome/app/AppHome-release1.5.2.apk?raw=true)

[Download latest debug](https://github.com/PHPirates/AppHome/blob/master/apphome/app/build/outputs/apk/AppHome-debug.apk?raw=true) (see tags if this is ahead of the release)

We use this app in order to have quick access to departure and arrival times of trains on the trajectories we travel often. Selecting source and destination cities, it display departure times, possible delays in departure time, and if going via Breda from Eindhoven to Roosendaal or vice versa a possible departure delay of the train leaving Breda is displayed. Hitting the 'send' button will guide you to sending a Whatsapp with arrival time given the chosen departure time, and hitting 'send delay' will send an arrival delay if there is one.

It is possible to switch between users to have different messages or even arrival times, for example selecting user 'Thomas' adds bike time from Eindhoven to Roosendaal, or selecting 'Abby' changes the message from Eindhoven to Heeze (customizations are hard-coded, for now).

There is also a widget which displays three departure times given a trajectory, and sends an arrival time when you click on it. It works most of the time (sometimes if it keeps 'loading...' changing the trajectory helps to reload).

![Image of app](ScreenshotApp.png?raw=true "App Screenshot") ![Widget Screenshot](ScreenshotWidget.png?raw=true "Widget Screenshot")

<sub>This project was previously maintainted on [BitBucket](https://bitbucket.org/slideclimb/apphome/overview)</sub>
