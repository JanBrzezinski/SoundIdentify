# SoundIdentify
A simple program to identify the music tone form a .wav file (containing 1 sound).

The program was made as student project.
The program allows user to open a .wav file. Then it analyzes data from the file using Fast Fourier Transform to find the sound's frequency. Then it finds what music tone in which octava is it.
The project was created using IntelliJ IDEA Ultimate IDE. GUI was created using JFormDesigner tool (as a plug-in to IntelliJ).

HOW TO RUN THE PROGRAM
To run the programm, You need Java version 16 or higher installed on your computer.
You can run the programm by running jar file 'SoundIdentify.jar'. It is in directory: out > artifacts > SoundIdentify_jar.

NOTICE! #1
The program works correctly only with files containing only one sound! If the file contains multiple sounds (full melody or some big noice in the background) the result might not be correct!
But if the file is empty (contains just silence), the program displays an appropriate message.

NOTICE! #2
The program works on both mono and stereo files, but if the file is stereo, then the app analyzes only the first (0th) channel!

The code and comments are written in English. The user interface is written in Polish!
In the future, the programm will probably allow user to choose the language.

NOTICE! #3
As the GUI is written in Polish, the sound and octava notation is appropriate to the music notation convention established in Poland (e.g. the sound below C is H and B is the sound between A and H).
