import org.jtransforms.fft.DoubleFFT_1D;

/**
 * Class that uses Fast Fourier Transform to calculate frequency of the sound.<br>
 * Use the extractFrequency() method to find a frequency of a sound given in its parameters.<br><br>
 * The class is based on code from: https://stackoverflow.com/questions/7649003/jtransforms-fft-in-android-from-pcm-data?lq=1
 * written by Shirkrin (StackOverflow user).
 */
public class FrequencyScanner {
    private double[] window; //FFT Window

    /** default constructor */
    public FrequencyScanner() {
        window = null;
    }

    /** extract the dominant frequency from samples from a file.
     * @param sampleData an array containing the sound data (samples).
     * @param sampleRate the sample rate (in HZ) of sampleData
     * @return an approximation of the dominant frequency in sampleData
     */
    public double extractFrequency(int[] sampleData, int sampleRate) {
        //sampleData + zero padding
        DoubleFFT_1D fft = new DoubleFFT_1D(sampleData.length + 24L * sampleData.length);
        double[] a = new double[(sampleData.length + 24 * sampleData.length) * 2];

        System.arraycopy(applyWindow(sampleData), 0, a, 0, sampleData.length);
        fft.realForward(a);

        //finding the peak magnitude and it's index
        double maxMag = Double.MIN_VALUE;
        int maxInd = -1;

        for(int i = 0; i < a.length / 2; ++i) {
            double re  = a[2*i];
            double im  = a[2*i+1];
            double mag = Math.sqrt(re * re + im * im);

            if(mag > maxMag) {
                maxMag = mag;
                maxInd = i;
            }
        }
        //

        //calculating the frequency
        return (double)sampleRate * maxInd / (a.length / 2);
    }

    /** build a Hamming window filter for samples of a given size
     * See http://www.labbookpages.co.uk/audio/firWindowing.html#windows
     * @param size the sample size for which the filter will be created
     */
    private void buildHammWindow(int size) {
        if(window != null && window.length == size) {
            return;
        }
        window = new double[size];
        for(int i = 0; i < size; ++i) {
            window[i] = .54 - .46 * Math.cos(2 * Math.PI * i / (size - 1.0));
        }
    }

    /** apply a Hamming window filter to raw input data
     * @param input an array containing unfiltered input data
     * @return a double array containing the filtered data
     */
    private double[] applyWindow(int[] input) {
        double[] res = new double[input.length];

        buildHammWindow(input.length);
        for(int i = 0; i < input.length; ++i) {
            res[i] = (double)input[i] * window[i];
        }
        return res;
    }
}
