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

public class VideoManager {

	public void appendVideo() {
    	
		try {
			File file1 = new File(Environment.getExternalStorageDirectory() + "/Video/1373803987950.mp4");
	    	InputStream inputStream1 = new FileInputStream(file1);
			
			File file2 = new File(Environment.getExternalStorageDirectory() + "/Video/1373807736772.mp4");

	    	InputStream inputStream2 = new FileInputStream(file2);
	    	
	        MovieCreator mc = new MovieCreator();
			@SuppressWarnings("static-access")
			Movie video = mc.build(Channels.newChannel(inputStream1));
			@SuppressWarnings("static-access")
			Movie audio = mc.build(Channels.newChannel(inputStream2));

			List<Track> videoTracks = video.getTracks();
			
			Log.e("videoTracks", "videoTracks " +videoTracks.size());
			
			video.setTracks(new LinkedList<Track>());

			List<Track> audioTracks = audio.getTracks();
			Log.e("audioTracks", "audioTracks " +audioTracks.size());
			
			video.addTrack(new AppendTrack(videoTracks.get(0), audioTracks.get(0)));
			video.addTrack(new AppendTrack(videoTracks.get(1), audioTracks.get(1)));

	        IsoFile out = new DefaultMp4Builder().build(video);
	        FileOutputStream fos = new FileOutputStream(new File(String.format(
	        		Environment.getExternalStorageDirectory() +  "/Video/output.mp4"
	        		)));
	        out.getBox(fos.getChannel());
	        fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void cut() {
    	try {
    		 Movie movie = MovieCreator.build(new FileInputStream(
    				 new File(String.format(
    			        		Environment.getExternalStorageDirectory() +  "/Video/output.mp4"
    			        		))
    				 ).getChannel());

             List<Track> tracks = movie.getTracks();
             
             movie.setTracks(new LinkedList<Track>());
             // remove all tracks we will create new tracks from the old

             double startTime1 =2;
             double endTime1 = 4;
             double startTime2 =10;
             double endTime2 = 11;

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
                     startTime2 = correctTimeToSyncSample(track, startTime2, false);
                     endTime2 = correctTimeToSyncSample(track, endTime2, true);
                     timeCorrected = true;
                 }
             }

             for (Track track : tracks) {
                 long currentSample = 0;
                 double currentTime = 0;
                 double lastTime = 0;
                 long startSample1 = -1;
                 long endSample1 = -1;
                 long startSample2 = -1;
                 long endSample2 = -1;

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
                         if (currentTime > lastTime && currentTime <= startTime2) {
                             // current sample is still before the new starttime
                             startSample2 = currentSample;
                         }
                         if (currentTime > lastTime && currentTime <= endTime2) {
                             // current sample is after the new start time and still before the new endtime
                             endSample2 = currentSample;
                         }
                         lastTime = currentTime;
                         currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                         currentSample++;
                     }
                 }
                 movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1), new CroppedTrack(track, startSample2, endSample2)));
             }
             
             long start1 = System.currentTimeMillis();

             IsoFile out = new DefaultMp4Builder().build(movie);
             long start2 = System.currentTimeMillis();
             
             FileOutputStream fos = new FileOutputStream(new File(String.format(Environment.getExternalStorageDirectory() +  
 	         		"/Video/output-%f-%f--%f-%f.mp4", startTime1, endTime1, startTime2, endTime2)));
 	        out.getBox(fos.getChannel());
 	        fos.close();
             
             long start3 = System.currentTimeMillis();
             System.err.println("Building IsoFile took : " + (start2 - start1) + "ms");
             System.err.println("Writing IsoFile took  : " + (start3 - start2) + "ms");
             System.err.println("Writing IsoFile speed : " + (new File(String.format("output-%f-%f--%f-%f.mp4", startTime1, endTime1, startTime2, endTime2)).length() / (start3 - start2) / 1000) + "MB/s");
             
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
