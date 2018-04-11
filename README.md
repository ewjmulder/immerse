# Immerse
 
Immerse yourself in the waves of dynamic surround sound!


Open question about combining multiple sounds: should they always go through the 'combine-mix-and-match' flow?
- I guess yes, cause beforehand you do not know which speakers will be used for which sounds.
So to be sure all sounds can be mixed, they should go through the flow. Result might be that one sound is 100% on speaker 1
and another 100% on speaker 2, resulting in a clean separation, but that can be a perfectly legal outcome of the flow, so
net result is the same a no flow.


Braindump about volume:
There are 5 volume 'dials' that effect the final volume:
  1. The physical volume dial of the speaker
     This should be tweaked in such a way that it is set to a value where outputting sound with the max volume of the OS
     and a loud sound source (with peaks of max amplitude) still sounds good. That means every other dial (#2, #3 and #4) can be freely set
     without risk of distortion.
  2. The OS master volume 'dial'. This can be used to get a general louder/softer effect for instance with regard to distance
     from listener to source. But this is slightly dangerous, cause it will effect all sounds played. Only usable when there is just one sound playing.
  3. The volume gauge of the sound 'line' in the Java Sound API. Although this seems to be unavailable in Linux. Still has the same issue as #2,
     cause it will effect all sounds played over that line.
  4. The amplitude of the individual sound source. This is in our full control and can be manipulated individually for each sound source.
     Also, it can be changed independently of the speaker.
  5. The recording volume of the sound source. This is the unchanged amplitude in the sound source (file). If this is quite low, #4 can be used to
     increase the volume. But if it is quite high, dialing #4 up can cause the amplitude to be higher than 1, causing sound distortion.
     But there is no way to know at one particular point in the sound stream if dailing #4 up will cause issues later on.

Conclusions:
- #1 and #2 and #3 must always be set to the max possible without distortion
- #5 must already be or be remixed to be as high as possible without distorting - this is called 'normalizing' and can be done with the audacity GUI or sox CLI
- #4 is the only 'dial' we should use and it should only be used to dial 'down' (lower than 1).
     Default value can be something like 0.5 and value can be increased or decreased per sound source as applicable in specific situation.
