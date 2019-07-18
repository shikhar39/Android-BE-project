# Android-BE-project
This is the 1st big android app that I have made. I have started to learn android development recently. Spoiler alert: the app has lots of bugs.

ABOUT THE APP: The project was to make a heads up display system that users can use while driving motorcycles. The augmented system would display directions and basic info like time, temp etc. on his helmet. The android app would be used to connect the HUD to his phone via bluetooth. The opening screen provides 2 options. 1) To connect to the Rpi HUD system(which requires a configured RPI) 2) To go to the maps activity where you can see the map, your location, search for a location on the search section and get a marker on that place.

It also has a code written to parse text messages received while driving to the HUD system. Rpi had a classifier model to separate urgent messages from not urgent and then display a notification icon on the user screen accordingly.

Instructions: To try the app on your phone download the app-debug.apk and run it on your device. IMPORTANT: make sure that the app has location permissions given if it does not ask for permission after starting the app. 
