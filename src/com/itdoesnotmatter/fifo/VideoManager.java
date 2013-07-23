package com.itdoesnotmatter.fifo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.itdoesnotmatter.fifo.model.VideoFile;
import com.itdoesnotmatter.fifo.model.VideoQueue;

public class VideoManager {
	public static final String RECORD_MODE = 	"RECORD_MODE";
	
	public static final int DEFAULT_BITRATE = 	2000000;
	public static final int DEFAULT_FPS = 		30;
	
	public static final int MODE_VIDEO_REG = 	3423;
	public static final int MODE_REPORT = 		3545;
	
	public static final int MAX_VIDEO_LEN = 		(int) 10 * 1000;
	public static final int MAX_VIDEO_LEN_REPORT = 	(int) 1.0 * 60 * 1000;
	
	public void createVideoFromQueue(VideoQueue videoQueue) {
		int count = videoQueue.getVideoQueue().size();
		
		if (count == 1) {
			//видео готово
		} else if (count == 2) {
			this.joinVideo(videoQueue.getVideoQueue().get(0), videoQueue.getVideoQueue().get(1));
//			this.joinVideo(
//					new VideoFile(Environment.getExternalStorageDirectory() + "/Video/1374545060131.mp4"), 
//					new VideoFile(Environment.getExternalStorageDirectory() + "/Video/1374545362316.mp4"));
		}
	}
	
	@SuppressWarnings("static-access")
	public VideoFile joinVideo(VideoFile videoFile1, VideoFile videoFile2) {
    	VideoFile outputVideoFile = null;
		try {
			File file1 = videoFile1.getFile();
			Log.e("LOG", "test " + videoFile1.filePath);
	    	InputStream inputStream1 = new FileInputStream(file1);
			
			File file2 = videoFile2.getFile();
	    	InputStream inputStream2 = new FileInputStream(file2);
	    	
	        MovieCreator mc = new MovieCreator();
			Movie video1 = mc.build(Channels.newChannel(inputStream1));
			Movie video2 = mc.build(Channels.newChannel(inputStream2));
			
			List<Track> videoTracks1 = video1.getTracks();
			video1.setTracks(new LinkedList<Track>());

			List<Track> videoTracks2 = video2.getTracks();
			
			long video1Length = (long) Math.floor(this.getDuration(videoTracks1.get(0))/100);
			long video2Length = (long) Math.floor(this.getDuration(videoTracks2.get(0))/100);
			
			if (video2Length >= MAX_VIDEO_LEN) {
				return videoFile2;
			} else if (video2Length < video1Length) {
				long delta = video2Length;
				
				VideoFile cuttedFile = this.cut(videoFile1, delta/1000, video1Length/1000);
				if (cuttedFile != null) {
					inputStream1 = new FileInputStream(cuttedFile.getFile());
					video1 = mc.build(Channels.newChannel(inputStream1));
					videoTracks1 = video1.getTracks();
					video1.setTracks(new LinkedList<Track>());
				}
			}
			
			video1.addTrack(new AppendTrack(videoTracks1.get(0), videoTracks2.get(0)));
			video1.addTrack(new AppendTrack(videoTracks1.get(1), videoTracks2.get(1)));

	        IsoFile out = new DefaultMp4Builder().build(video1);
	        outputVideoFile = new VideoFile(String.format(
	        		Environment.getExternalStorageDirectory() +  "/Video/output1.mp4"
	        		));
	        FileOutputStream fos = new FileOutputStream(outputVideoFile.getFile());
	        out.getBox(fos.getChannel());
	        fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return outputVideoFile;
    }
    
    public VideoFile cut(VideoFile videoFile, long from, long to) {
    	VideoFile outputFile = null;
    	
    	try {
    		 Movie movie = MovieCreator.build(new FileInputStream(videoFile.getFile()).getChannel());

             List<Track> tracks = movie.getTracks();
             
             movie.setTracks(new LinkedList<Track>());
             // remove all tracks we will create new tracks from the old

             double startTime1 = from;
             double endTime1 = to;
             
             boolean timeCorrected = false;

             // Here we try to find a track that has sync samples. Since we can only start decoding
             // at such a sample we SHOULD make sure that the start of the new fragment is exactly
             // such a frame
             for (Track track : tracks) {
                 if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                     if (timeCorrected) {
                         // This exception here could be a false positive in case we have multiple tracks
                         // with sync samples at exactly the same positions. E.g. a single movie containing
                         // multiple qualities of the same video (Microsoft Smooth Streaming file)

                         throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                     }
                     startTime1 = correctTimeToSyncSample(track, startTime1, false);
                     endTime1 = correctTimeToSyncSample(track, endTime1, true);
                     timeCorrected = true;
                 }
             }

             for (Track track : tracks) {
                 long currentSample = 0;
                 double currentTime = 0;
                 double lastTime = 0;
                 long startSample1 = -1;
                 long endSample1 = -1;

                 for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
                     TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
                     for (int j = 0; j < entry.getCount(); j++) {

                         if (currentTime > lastTime && currentTime <= startTime1) {
                             // current sample is still before the new starttime
                             startSample1 = currentSample;
                         }
                         if (currentTime > lastTime && currentTime <= endTime1) {
                             // current sample is after the new start time and still before the new endtime
                             endSample1= currentSample;
                         }

                         lastTime = currentTime;
                         currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                         currentSample++;
                     }
                 }
                 movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
             }
             
             long start1 = System.currentTimeMillis();

             IsoFile out = new DefaultMp4Builder().build(movie);
             long start2 = System.currentTimeMillis();
             
             outputFile = new VideoFile(String.format(Environment.getExternalStorageDirectory() +  
  	         		"/Video/output23-%f-%f.mp4", startTime1, endTime1));
             
             FileOutputStream fos = new FileOutputStream(outputFile.getFile());
 	         out.getBox(fos.getChannel());
 	         fos.close();
             
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return outputFile;
     }

     protected static long getDuration(Track track) {
         long duration = 0;
         for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
             duration += entry.getCount() * entry.getDelta();
         }
         return duration;
     }

     private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
         double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
         long currentSample = 0;
         double currentTime = 0;
         for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
             TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
             for (int j = 0; j < entry.getCount(); j++) {
                 if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                     // samples always start with 1 but we start with zero therefore +1
                     timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
                 }
                 currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                 currentSample++;
             }
         }
         double previous = 0;
         for (double timeOfSyncSample : timeOfSyncSamples) {
             if (timeOfSyncSample > cutHere) {
                 if (next) {
                     return timeOfSyncSample;
                 } else {
                     return previous;
                 }
             }
             previous = timeOfSyncSample;
         }
         return timeOfSyncSamples[timeOfSyncSamples.length - 1];
     }
	
}
