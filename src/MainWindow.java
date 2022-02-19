import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import net.miginfocom.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Jan Brzeziński
 * This is the main class of the project.
 */
public class MainWindow extends JFrame {

    private int[][] samples; //data from wav file
    private double timeLength; //length of file in seconds
    private double samplingFrequency; //sample rate of wav file

    /** default constructor */
    public MainWindow() {
        initComponents();
        setTitle("Analiza dźwięku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        samples = null;
        timeLength = 0;
        samplingFrequency = 0;
    }

    /**
     * opens a WAV file chosen by the user
     * if file is not null, calls methods for sound analyzing
     * @param e unused parameter of the event
     */
    private void fileOpen(ActionEvent e) {
        //setting title to original
        setTitle("Analiza dźwięku");

        //clearing labels in window
        InfoFreq.setText(" ");
        InfoFreqVal.setText(" ");
        InfoSound.setText(" ");
        SoundName.setText(" ");
        InfoOctava.setText(" ");
        OctavaName.setText(" ");
        //

        //dialog window to choose a file
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    return (f.getName().toLowerCase().endsWith(".wav"));
                }
            }

            @Override
            public String getDescription() {
                return "Sound file (*.wav)";
            }
        });
        fc.setCurrentDirectory(new File(fc.getFileSystemView().getHomeDirectory().getAbsolutePath())); //file choosing from home directory
        fc.showOpenDialog(this);
        File f = fc.getSelectedFile();
        //main file choosing process ends here
        ///////////////////

        //below, if file is not null, the sound from file is analyzed
        if (f!=null) {
            setTitle("Analizowanie pliku " + f.getName()); //title with the file name
            analyzeSound(f); //getting frequency and displaying it in the main window
        }
    }

    /**
     * Gets data from the file and assigns it to the field 'samples' by calling getSamplesFromWavFile() method.
     * If file contains sound, the method finds its frequency (with FrequencyScanner object),
     * then the method analyzes the frequency to find the music tone and displays sound information in the main window.<br><br>
     * WARNING! This methods works only on the 1st [0th] channel in stereo files!
     * @param f WAV file to analyze
     */
    private void analyzeSound(File f) {
        getSamplesFromWavFile(f);
        try {
            //checking if file contains any sound
            boolean isSilent = true;
            for (int sample : samples[0]) {
                if (sample != 0) {
                    isSilent = false;
                    break;
                }
            }
            if (isSilent){
                throw new Exception();
            }
            //

            //getting frequency
            FrequencyScanner fs = new FrequencyScanner();
            double frequency = fs.extractFrequency(samples[0], (int) samplingFrequency);
            //

            //finding a tone
            double tempFreq = frequency;

            double[] frequencies = {16.35, 17.32, 18.35, 19.45, 20.60, 21.83, 23.12, 24.59, 25.96, 27.50, 29.14, 30.87}; //array with frequencies of specific tones in the lowest octave
            String[] notes = {"C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/B", "H"}; //array with note names - NOTICE! Sound name's index = its frequency's index in array 'frequencies'
            String[] octaves = {"subkontra", "kontra", "wielka", "mała", "-kreślna"}; //array with Polish names of octaves (last element is a suffix of octaves' names for one-line octave and higher ones - e. g. 1-kreślna, 2-kreślna etc.)

            double diff = Double.MAX_VALUE; //for finding the best approximation of the frequency
            int octave = 0; //octave number
            int noteIndex = 0; //index of note in arrays 'notes' and 'frequencies'

            //finding the best approximation of the frequency and its tone
            while (diff >= 1) {
                for (int i = 0; i < frequencies.length; i++) {
                    double temp = Math.abs(tempFreq - frequencies[i]);
                    if (temp < diff) {
                        diff = temp;
                        noteIndex = i;
                    }
                }
                if (diff >= 1) {
                    tempFreq /= 2.0;
                    octave++;
                }
            }
            ////

            String note = notes[noteIndex]; //note name to display
            String octaveName = octave < 4 ? octaves[octave] : ((octave - 3) + octaves[4]); //octave name to display

            //displaying all sound info in the main window
            InfoFreq.setText("Częstotliwość dźwięku to");
            InfoFreqVal.setText((int) frequency + " Hz");
            InfoSound.setText("Jest to dźwięk");
            SoundName.setText(note);
            InfoOctava.setText("Oktawa");
            OctavaName.setText(octaveName);
            //
        }
        catch (Exception e) {
            //if the file is only silence
            SoundName.setText("Plik nie zawiera dźwięku!");
        }
    }

    /**
     * Gets data from the file and assigns it to the 'samples' field.
     * Also gets file's length and saves it in the field 'timeLength' in seconds.
     * Gets sample rate (sampling frequency) of the file and assigns it to the 'samplingFrequency' field.
     * @param f - WAV file
     */
    private void getSamplesFromWavFile(File f){
        AudioInputStream stream = null;
        try {
            stream = AudioSystem.getAudioInputStream(f);
        } catch (UnsupportedAudioFileException | IOException exception) {
            exception.printStackTrace();
        }
        try {
            assert stream != null;
            stream.mark(Integer.MAX_VALUE); //to reset the stream later in order to get clip and time length from it
            int frameLength = (int) stream.getFrameLength();
            int frameSize = stream.getFormat().getFrameSize();
            byte[] eightBitByteArray = new byte[frameLength * frameSize];

            int result = stream.read(eightBitByteArray);

            int channels = stream.getFormat().getChannels();
            samples = new int[channels][frameLength];

            int sampleIndex = 0;
            for (int t = 0; t < eightBitByteArray.length; ) {
                for (int channel = 0; channel < channels; channel++) {
                    int low = eightBitByteArray[t];
                    t++;
                    int high = eightBitByteArray[t];
                    t++;
                    int sample = getSixteenBitSample(high, low);
                    samples[channel][sampleIndex] = sample;
                }
                sampleIndex++;
            }
            stream.reset();
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        //getting time length of the file
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            timeLength = clip.getMicrosecondLength()/1000000.0;
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }

        //calculating the sample rate (sampling frequency) in Hz
        samplingFrequency = samples[0].length/timeLength;
    }

    /**
     * Temporary method for getting samples from a wav file.
     * @param high 8-bit byte array element
     * @param low 8-bit byte array element
     * @return 16-bit sample combined from the parameters
     */
    private int getSixteenBitSample(int high, int low) {
        return (high << 8) + (low & 0x00ff);
    }

    /**
     * Automatically generated (by JFormDesigner) method to initialize the GUI.
     */
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        FileChoose = new JButton();
        InfoFreq = new JLabel();
        InfoFreqVal = new JLabel();
        hSpacer1 = new JPanel(null);
        label2 = new JLabel();
        InfoSound = new JLabel();
        SoundName = new JLabel();
        hSpacer2 = new JPanel(null);
        InfoOctava = new JLabel();
        OctavaName = new JLabel();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "hidemode 3,alignx center",
            // columns
            "[fill]" +
            "[fill]" +
            "[fill]",
            // rows
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]"));

        //---- FileChoose ----
        FileChoose.setText("Wybierz plik...");
        FileChoose.addActionListener(e -> fileOpen(e));
        contentPane.add(FileChoose, "cell 1 0");

        //---- InfoFreq ----
        InfoFreq.setText(" ");
        InfoFreq.setHorizontalAlignment(SwingConstants.RIGHT);
        InfoFreq.setMaximumSize(new Dimension(165, 16));
        InfoFreq.setMinimumSize(new Dimension(165, 16));
        contentPane.add(InfoFreq, "cell 0 2,alignx right,growx 0");

        //---- InfoFreqVal ----
        InfoFreqVal.setText(" ");
        InfoFreqVal.setHorizontalAlignment(SwingConstants.LEFT);
        contentPane.add(InfoFreqVal, "cell 1 2");
        contentPane.add(hSpacer1, "cell 1 2");

        //---- label2 ----
        label2.setMinimumSize(new Dimension(165, 0));
        contentPane.add(label2, "cell 2 2");

        //---- InfoSound ----
        InfoSound.setText(" ");
        InfoSound.setHorizontalAlignment(SwingConstants.RIGHT);
        InfoSound.setMaximumSize(new Dimension(165, 16));
        InfoSound.setMinimumSize(new Dimension(165, 16));
        contentPane.add(InfoSound, "cell 0 3,alignx right,growx 0");

        //---- SoundName ----
        SoundName.setText(" ");
        contentPane.add(SoundName, "cell 1 3");
        contentPane.add(hSpacer2, "cell 1 3");

        //---- InfoOctava ----
        InfoOctava.setText(" ");
        InfoOctava.setHorizontalAlignment(SwingConstants.RIGHT);
        InfoOctava.setMaximumSize(new Dimension(165, 16));
        InfoOctava.setMinimumSize(new Dimension(165, 16));
        contentPane.add(InfoOctava, "cell 0 4,alignx right,growx 0");

        //---- OctavaName ----
        OctavaName.setText(" ");
        contentPane.add(OctavaName, "cell 1 4");
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JButton FileChoose;
    private JLabel InfoFreq;
    private JLabel InfoFreqVal;
    private JPanel hSpacer1;
    private JLabel label2;
    private JLabel InfoSound;
    private JLabel SoundName;
    private JPanel hSpacer2;
    private JLabel InfoOctava;
    private JLabel OctavaName;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    /**
     * Program main method.
     * @param args Arguments to call with the program. Not needed.
     */
    public static void main(String[] args) {
        MainWindow MW = new MainWindow();
    }
}
