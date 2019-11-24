[![Build Status](https://travis-ci.org/PHPirates/AppHome.svg?branch=master)](https://travis-ci.org/PHPirates/AppHome) 
[![codecov](https://codecov.io/gh/phpirates/apphome/branch/master/graph/badge.svg)](https://codecov.io/gh/phpirates/apphome)
[![TODO board](https://imdone.io/api/1.0/projects/5ae048b775e50c7dfbd31049/badge)](https://imdone.io/app#/board/PHPirates/AppHome)

# AppHome

Download latest release: [![Release](https://img.shields.io/github/release/PHPirates/AppHome.svg)](https://github.com/PHPirates/AppHome/releases/latest)

We use this app in order to have quick access to departure and arrival times of trains on the trajectories we travel often. Selecting source and destination cities, it display departure times, possible delays in departure time, and if going via Breda from Eindhoven to Roosendaal or vice versa a possible departure delay of the train leaving Breda is displayed. Hitting the 'send' button will guide you to sending a Whatsapp with arrival time given the chosen departure time, and hitting 'send delay' will send an arrival delay if there is one.

It is possible to switch between users to have different messages or even arrival times, for example selecting user 'Thomas' adds bike time from Eindhoven to Roosendaal, or selecting 'Abby' changes the message from Eindhoven to Heeze (customizations are hard-coded, for now).

<img src="https://github.com/PHPirates/AppHome/raw/master/Screenshot1.6.0.jpg" width="300">

In previous versions there was also a widget which displays three departure times given a trajectory, and sends an arrival time when you click on it. It works most of the time (sometimes if it keeps 'loading...' changing the trajectory helps to reload).

<img src="ScreenshotWidget.png?raw=true" width="250">

<sub>This project was previously maintainted on [BitBucket](https://bitbucket.org/slideclimb/apphome/overview)</sub>


## Getting the NS API key
- Go to the [NS API Portal](https://apiportal.ns.nl) and log in
- Go to our profile
- Copy the primary key from the `Ns-App`
- In the android project, create a Kotlin file `NsKey.kt` in the `ns` folder, and add the following content

```
package com.abbyberkers.apphome.ns

const val key: String = <primary key>
```
