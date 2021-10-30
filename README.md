
# JavaScreenSnipper
An open source screen snipping tool written in Java.

## Usage

Just run the Main file and select an area from your screen with mouse. After you release the button selected area will be saved and copied to your clipboard.

(You can paste/send it with `ctrl+v`)

## How To Run

#### Automated
- Run `compile.bat`
- Run `run.bat` each time you want to take a screen snip.

(You can also make a shortcut for `run.bat` and create a keyboard shortcut for it)

#### Manual
- Compile all `.java` extension files in `src` folder

 `javac src/*.java`
- Run the snipper with Main.class file each time you want to take a screen snip.

 `java -Dsun.java2d.uiScale=1 src.Main [<output_path> <brightness_ratio>]`

(I recommend that you create a script and assign a keyboard shortcut to it for easy use)

#### Optional Arguments
- <output_path>:

Folder to save images. If not provided it will be assigned to current directory.
- <brightness_ratio>:

Controls the amount of shadowing applied before taking the snip.

(Supports values between and including 0 and 1)

## Future Plans
- Accelerating shadowing and drawing the screen. Currently is not accelerated by anything. Although, I tried to optimize it as much as I can to reduce on the fly computation, it is still not as smooth as I wanted it to be.
- Support for more shapes other than rectangle.
- A better way of applying the settings.
