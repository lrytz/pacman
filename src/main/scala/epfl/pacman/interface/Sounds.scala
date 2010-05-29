package epfl.pacman
package interface

import java.io.File
import maze.MVC
import javax.sound.sampled._

trait Sounds { this: MVC =>
    class SoundPlayer(filename: String) extends Thread {

        val buffer_size = 524288


        override def run = {
            val soundFile = new File("src/main/resources/" + filename)
            val audioIS = AudioSystem.getAudioInputStream(soundFile)

            val audioFormat = audioIS.getFormat()
            val info = new DataLine.Info(classOf[SourceDataLine], audioFormat)

            val audioLine: SourceDataLine = AudioSystem.getLine(info).asInstanceOf[SourceDataLine]

            audioLine.open(audioFormat)
            audioLine.start();
            var nBytesRead = 0;

            var abData = new Array[Byte](buffer_size);

            while (nBytesRead != -1) {
                nBytesRead = audioIS.read(abData, 0, abData.length);
                if (nBytesRead >= 0) {
                    audioLine.write(abData, 0, nBytesRead);
                }
            }
        }
    }
}
