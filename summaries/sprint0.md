# Summary for week 0

## Antoine

I added a serializable mapping mission object to store the drone mappings.
I implemented a local storage system for this object by creating files in the phone's storage but my difficulties to test it showed me that it was not modular enough.
After a few researches, I stumbled upon the Android Room API for storing local objects and I think it would be a better fit for this issue so I might have to start over the application.

I greatly underestimated the time needed for those tasks.

Next time, I will try to ask more advice from my teammates and the coaches about the modularity of the project.

## Gabriel (Scrum Master)

I setup the project for kotlin, and then started working on the firebase authentication system.
I spend a lot more time learning the library and Kotlin than expected.

But then the code writing went smoothly.

I estimated the correct amount of time for the test writting as I had to relearn how to use mockito.

I also helped Jonas fix some CI errors at setup and Nicolas with android test not passing on Cirrus 
as the screen size of the emulated device is very small.

## Loïc

## Jonas

I migrated the GitHub repo to an organisation one, meaning setting up (again) all the continuous integration.
I implemented a first basic version of the firebase database.

The firebase setup went well.

I underestimated the time needed for dealing with GitHub and continuous integration.
I started implementing too many function for database. I had to reduce the scope to be able to test them correctly.

## José

## Nicolas
I created a figma to have a plan we can use to create the applications of the app in the future.
I created the main menu of the app

The creation of the figma went well, without any hitch.
The creation of the main menu went well until I tried to merge it where I had two problems : first I had to change the setup of codeclimate
because the functions creating the added applications and the functions called on button press where too close to each other. Then I had a
problem because cirrus uses a smaller screen than the one I used, and I didn't setup the buttons to adapt their positions and sizes to the 
screen.

## Hubert

## Overall team
