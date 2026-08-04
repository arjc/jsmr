# SMR: Project Java Sheet Music Reader

![Alpha screenshot](https://github.com/arjc/smr/blob/main/public/ss1.png)
> current indev build screenshot 4th Aug 26

## Idea
This java sheet music reader is a Image to music program which only uses vanila dependancies from the jdk pakages.
It works by taking in a raster image of a sheet music and then processing it into a monochromatic image which is mapped to a 
data structure as a binary bitmap.

### User Interface
SMR use Swing to build an simple and user firendly UI.
`swing.Box, swing.BoxLayout, swing.DefaultComboBoxModel, swing.JButton, swing.JComboBox, swing.JFileChooser, swing.JFrame, swing.JPanel, swing.SwingUtilities, swing.UIManager and swing.filechooser.FileNameExtensionFilter` are the dependancies imported from swing

![Outline of the final product](https://github.com/arjc/smr/blob/main/public/wire.png)
> Final interface 

# Sheet music image to expression
#### We first recive the image and store it as binary and convert it to monochromatic and represent it using a bitmap to perform calculations and determine symbols.
## Import Image
Get the image from swing and we store as binary using `imageio`'s BufferedImage
`BufferedImage image = ImageIO.read(file);`

## Process the image
Convert the image to monochrome (NOT GRAY SCALE!!!).
We need to do this since out binary bitmap cannot store range of grayness.
Assuming that the user shall only input a high resolution image for better accuracy.
> (Has experienced some issues while scaning lower quality images)
<!-- This may/ may not affect the staff symbol reading accuracy but we shall assume that the user has input a high resolution image -->
