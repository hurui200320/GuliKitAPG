# GuliKitAPG
Study the Gulikit's AutoPilot Gaming.

## What is APG (AutoPilot Gaming)?

Frankly speaking, I don't know. GuliKit give the name. To describe it in simple
English, it's a feature on GuliKit's KingKong 2 pro controller that allows user
to record a sequence of input (like button press, joystick movements) for 10 minutes.
Then you can replay it at anytime with joystick override (so you can replay the
button press while moving around in the game).

The KingKong 2 pro controller also allows user to share this sequence. User can
export (copy from controller to PC) the sequence as a "APG" file, or import (copy
from PC to controller) other's sequence.

And you know what? You don't need an APP to do it. You just press a combination on
the controller, plug into a PC, it will become a USB storage device and you can
copy in and out. Huge appreciated :)

## But what's inside a APG file?

Unfortunately, GuliKit has no documentation on the format. But thankfully, it's not
encrypted, and well-formatted. After some try and guess, the format is mostly figured
out. I will call each unit of bytes a "control word", it's the basic unit of the APG
file. And a APG file is just a `Array<ControlWord>(65536)`. Each control word is
precisely 16 bytes.

There are two types of control word. The simplest one is padding word. This word
means this is not a valid movement/button press, where your sequence might be short,
like 10 control words, but the file is fixed at 65536 control words, then the rest
of 65525 words are padding words. The padding word is 
`FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF`. Yes, it's all `0xFF`.

The normal control words are more like this: `00 08 00 08 00 08 00 08 00 00 00 00 00 00 02 02`.
The format is: 
```
0x00                                                                                                     0x0F
┌──────────┬──────────┬──────────┬──────────┬────┬──────┬────┬──────┬────────────┬────────────┬─────────┬────┐
│LJH(2B,LE)│LJV(2B,LE)│RJH(2B,LE)│RJV(2B,LE)│0x00│ZL(1B)│0x00│ZR(1B)│buttons1(1B)│buttons2(1B)│D-Pad(1B)│0x02│
└──────────┴──────────┴──────────┴──────────┴────┴──────┴────┴──────┴────────────┴────────────┴─────────┴────┘
```

The `LJH` means "left joystick horizontal", `RJV` means "right joystick vertical".
They are unsigned shorts in little endian, where 0 means down (vertical) or left
(horizontal), 4095 means up (vertical) or right (horizontal), and 2048 means center.

The ZL and ZR is the linear movement of ZL and ZR. For platforms don't support
linear trigger (like nintendo switch), this value is still recorded, but to make
ZL or ZR clicked, there is a separate bit to control it. This linear movements
are range from 0 (not pressed) to 255 (fully pressed).

The `buttons1` are bits corresponded to some buttons:
```
MSB                   LSB
┌──┬──┬──┬──┬──┬──┬──┬──┐
│ZR│ZL│ R│ L│ X│ Y│ B│ A│
└──┴──┴──┴──┴──┴──┴──┴──┘
```

Note: ABXY are nintendo layout, which means button A is the right button,
Y is left, X is top and B is bottom.

The `buttons2` is same:
```
MSB                            LSB
┌──┬──┬──────────┬────┬──┬──┬──┬──┐
│??│??│Screenshot│Home│ +│ -│LJ│RJ│
└──┴──┴──────────┴────┴──┴──┴──┴──┘
```

The first two bits are unknown, just make them 0. Then is the screenshot button
and home button, then the plus and minus button. LJ and RJ are the left and right
joystick button, where you can press down the joystick.

Then is the D-Pad. This is a enum:
+ 00: not pressed
+ 01: UP
+ 02: UP and RIGHT
+ 03: RIGHT
+ 04: RIGHT and DOWN
+ 05: DOWN
+ 06: DOWN and LEFT
+ 07: LEFT
+ 08: LEFT and UP

Each control words represent 10 ms of operation. In theory, the nintendo console's
polling rate is 125HZ, aka 8 ms interval. But for some unknown reason, when I generate
sequence for splatoon 3, the game just ignore the input unless I use 60 ms as interval
(aka repeat each word 6 times). I think it's related to how game processed the input,
maybe the game got the input, then use 20ms to process it, thus the actual polling
rate is 50Hz. I don't know.

## What can we use it for?

Well, just for APG itself, it's pretty useful. For example, you need to do some
parkour to get somewhere in the game, but you can't make sure it's 100% working.
So you can record your success one and replay it. OR you can import other's APG
file to get there.

Or, the first idea comes into my mind, is auto drawing in splatoon. I don't need
to buy ESP 32 anymore! (Frankly speaking, to me, a Java developer, ESP 32 is just
useless) So I [tried](./src/main/kotlin/info/skyblond/gulikit/generator/SplatoonCanvas.kt).

While 10 minutes is long enough for human, it's not long enough to draw a 320 by
120 canvas. As I said, each stroke need 6 control words to perform, so we only
have 10922 actions. By doing the moving and drawing alternately, I can draw
16 lines in one APG file, which means I need to swap 8 different APG files. Each
file takes 656 seconds (about 11 minutes), and swapping the APG file take another
1 or 2 minutes. And you have to do it every 11 minutes, that defeat the whole point
of automation, where you run the code and watch it finish.

I'd still use a ESP 32 where I can click run and forget it.
