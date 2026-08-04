# SMR: Project Java Sheet Music Reader

![Alpha screenshot](https://github.com/arjc/smr/blob/main/public/ss1.png)
current indev build screenshot 3rd Aug 26

## Idea
This java sheet music reader is a Image to music program which only uses vanila dependancies from the jdk pakages.
It works by taking in a raster image of a sheet music and then processing it into a monochromatic image which is mapped to a 
data structure as a binary bitmap.

### User Interface
SMR use Swing to build an simple and user firendly UI.
`swing.Box, swing.BoxLayout, swing.DefaultComboBoxModel, swing.JButton, swing.JComboBox, swing.JFileChooser, swing.JFrame, swing.JPanel, swing.SwingUtilities, swing.UIManager and swing.filechooser.FileNameExtensionFilter` are the dependancies imported from swing

![Outline of the final product](https://github.com/arjc/smr/blob/main/public/wire.png)
final interface outline