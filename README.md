# Drone3D
[![Build Status](https://api.cirrus-ci.com/github/Drone3D-Team/Drone3D.svg)](https://cirrus-ci.com/github/Drone3D-Team/Drone3D)
[![Maintainability](https://api.codeclimate.com/v1/badges/7d12c4fd472569490788/maintainability)](https://codeclimate.com/github/Drone3D-Team/Drone3D/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/7d12c4fd472569490788/test_coverage)](https://codeclimate.com/github/Drone3D-Team/Drone3D/test_coverage)


## Table of contents
* [Description](#description)
* [Usage](#usage)
* [Set up](#set-up)
* [Developement](#developement)
* [License](#license)

## Description
Drone3D is the mobile application that let you plan and execute drone mission to take pictures of a location. These photos can then be used to reconstruct a 3D model of the area. You have the possibility to share the missions you've planned with other or see their missions. This app can be used with any drone or simulation supporting [MAVSDK](https://mavsdk.mavlink.io/main/en/index.html).

## Usage


## Set-up
This app uses [Mapbox](https://www.mapbox.com) and [OpenWeather](https://openweathermap.org/api) APIs. To be able to have a working build of Drone3D, you will have to set up your own keys. We explain in this section how to do that. Keep in mind that you don't want to share you private keys, be carefull !

### Mapbox
You'll first have to login or create an account [here](https://account.mapbox.com/auth/signin/). Update "mapbox_access_token" with your default public token in [strings.xml](app/src/main/res/values/strings.xml).
```
    <string name="mapbox_access_token">PASTE YOUR PUBLIC TOKEN HERE</string>
```
 Finally generate a private token and set it up in [build.gradle](./build.gradle) under password:
```
   credentials {
                // This should always be `mapbox` (not the real username).
                username = 'mapbox'
                //Use Mapbox's secret key
                password = PASTE YOUR PRIVATE KEY HERE
            }
```

### OpenWeather
You'll first have to login or create an account [here](https://home.openweathermap.org/users/sign_in). Then under "API keys" create a new key and copy it. All you have to do is add a new file secrets.xml in [res/values](app/src/main/res/values/) with the following code:
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="open_weather_api_key" type="string">PASTE YOUR KEY HERE</item>
</resources>
```

## Developement
Drone3D was developed as part of the CS-306 course "Software developement project" at EPFL. We have followed agile development methods by adopting the scrum framework. You cane find our scrum board [here](https://github.com/Drone3D-Team/Drone3D/projects/1). We have divided the semester into sprints of one week each. For each sprint we assigned ourselves tasks. Once the task has been implemented, tested and merged into the main branch, it is moved to the corresponding "Done is sprint X" column.  At the end of the week, a presentation of the application's functionality takes place, as well as a review of the last sprint to improve the next one. We summarise the work done in each sprint in a sprint summary available in our [wiki](https://github.com/Drone3D-Team/Drone3D/wiki).

We had a few imposed requirements for this app:

### Correctness & Utility
Drone3D let users plan and execute mission to create 3D model of a area.

### Split app model
Our app use firebase to let the users store and share their mission.

### Sensor usage
Every map in the app is zoomed on the user, based on his gps. Users can follow the live progression of the drone on the map when a mission is launched. At any time they can send an instruction so that the drone comes back to their location.

### User support
We use an email/password register and login. Login users have the possibility to store and share their mission. The app is still useable without being login.

### Local cache & offline mode
Users have the possibility to download parts on the map before entering the mission site in case there is little or no connection there. They can also store and share mapping mission locally, it'll be synchronised with the online database as soon as they regain connection.

### Testing
Our code is carefully tested. It has the following stats:
[![Build Status](https://api.cirrus-ci.com/github/Drone3D-Team/Drone3D.svg)](https://cirrus-ci.com/github/Drone3D-Team/Drone3D)
[![Maintainability](https://api.codeclimate.com/v1/badges/7d12c4fd472569490788/maintainability)](https://codeclimate.com/github/Drone3D-Team/Drone3D/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/7d12c4fd472569490788/test_coverage)](https://codeclimate.com/github/Drone3D-Team/Drone3D/test_coverage)

## License

[GNU Affero General Public License version 3](/LICENSE.txt)

Drone3D a mobile application that helps you plan drone flights to build 3D model of a scene.  
Copyright (C) 2021  Drone3D-Team

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

