package thederpgamer.startunes.utils;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class AudioUtils {

	public static byte[] convertAudioBytesToWave(byte[] sourceBytes) {
		byte[] wave = new byte[44 + sourceBytes.length];
		long totalAudioLen = sourceBytes.length;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = 44100;
		int channels = 2;
		long byteRate = 16 * 44100 * channels / 8;
		wave[0] = 'R';  // RIFF/WAVE header
		wave[1] = 'I';
		wave[2] = 'F';
		wave[3] = 'F';
		wave[4] = (byte) (totalDataLen & 0xff);
		wave[5] = (byte) ((totalDataLen >> 8) & 0xff);
		wave[6] = (byte) ((totalDataLen >> 16) & 0xff);
		wave[7] = (byte) ((totalDataLen >> 24) & 0xff);
		wave[8] = 'W';
		wave[9] = 'A';
		wave[10] = 'V';
		wave[11] = 'E';
		wave[12] = 'f';  // 'fmt ' chunk
		wave[13] = 'm';
		wave[14] = 't';
		wave[15] = ' ';
		wave[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		wave[17] = 0;
		wave[18] = 0;
		wave[19] = 0;
		wave[20] = 1;  // format = 1
		wave[21] = 0;
		wave[22] = (byte) channels;
		wave[23] = 0;
		wave[24] = (byte) (longSampleRate & 0xff);
		wave[25] = (byte) ((longSampleRate >> 8) & 0xff);
		wave[26] = (byte) ((longSampleRate >> 16) & 0xff);
		wave[27] = (byte) ((longSampleRate >> 24) & 0xff);
		wave[28] = (byte) (byteRate & 0xff);
		wave[29] = (byte) ((byteRate >> 8) & 0xff);
		wave[30] = (byte) ((byteRate >> 16) & 0xff);
		wave[31] = (byte) ((byteRate >> 24) & 0xff);
		wave[32] = (2 * 16 / 8);  // block align
		wave[33] = 0;
		wave[34] = 16;  // bits per sample
		wave[35] = 0;
		wave[36] = 'd';
		wave[37] = 'a';
		wave[38] = 't';
		wave[39] = 'a';
		wave[40] = (byte) (totalAudioLen & 0xff);
		wave[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		wave[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		wave[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		System.arraycopy(sourceBytes, 0, wave, 44, sourceBytes.length);
		return wave;
	}
}
