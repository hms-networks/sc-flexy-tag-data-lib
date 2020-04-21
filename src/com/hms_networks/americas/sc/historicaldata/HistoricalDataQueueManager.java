package com.hms_networks.americas.sc.historicaldata;

import com.hms_networks.americas.sc.fileutils.FileAccessManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Class to manage queueing historical tag data and retrieving it in chunks based on a configurable
 * time span of data.
 *
 * @author HMS Networks, MU Americas Solution Center
 * @since 1.0
 */
public class HistoricalDataQueueManager {

  /** Time span for fetching FIFO queue data. Default is 1 minute. */
  private static long queueFifoTimeSpanMins = 1;

  /**
   * Get the current configured FIFO queue time span in milliseconds.
   *
   * @return FIFO queue time span in ms
   */
  private static synchronized long getQueueFifoTimeSpanMillis() {
    return queueFifoTimeSpanMins
        * HistoricalDataConstants.TIME_SECS_PER_MIN
        * HistoricalDataConstants.TIME_MS_PER_SEC;
  }

  /**
   * Get the current configured FIFO queue time span in minutes.
   *
   * @return FIFO queue time span in mins
   */
  public static synchronized long getQueueFifoTimeSpanMins() {
    return queueFifoTimeSpanMins;
  }

  /**
   * Set the FIFO queue time span in minutes.
   *
   * @param timeSpanMins new FIFO queue time span in minutes
   */
  public static synchronized void setQueueFifoTimeSpanMins(long timeSpanMins) {
    queueFifoTimeSpanMins = timeSpanMins;
  }

  /**
   * Convert a <code>long</code> time value to format required for EDB calls.
   *
   * @param time <code>long</code> time value to format
   * @return formatted time string for EBD calls
   */
  private static String convertToEBDTimeFormat(long time) {
    return new SimpleDateFormat(HistoricalDataConstants.EBD_TIME_FORMAT).format(new Date(time));
  }

  /**
   * Gets a boolean representing if the time tracker file exists.
   *
   * @return true if time tracker file exists
   */
  public static boolean doesTimeTrackerExist() {
    final String timeTrackerFileName =
        HistoricalDataConstants.QUEUE_FILE_FOLDER
            + "/"
            + HistoricalDataConstants.QUEUE_TIME_FILE_NAME
            + HistoricalDataConstants.QUEUE_FILE_EXTENSION;
    return new File(timeTrackerFileName).isFile();
  }

  /**
   * Get the historical log data for all tag groups within the next FIFO queue time span.
   *
   * @param startNewTimeTracker if new time tracker should be generated, not read from storage
   * @return historical log data
   * @throws IOException if unable to read or write files
   */
  public static synchronized ArrayList getFifoNextSpanDataAllGroups(boolean startNewTimeTracker)
      throws IOException {
    final boolean includeTagGroupA = true;
    final boolean includeTagGroupB = true;
    final boolean includeTagGroupC = true;
    final boolean includeTagGroupD = true;
    return getFifoNextSpanData(
        startNewTimeTracker,
        includeTagGroupA,
        includeTagGroupB,
        includeTagGroupC,
        includeTagGroupD);
  }

  /**
   * Get the historical log data for the specified tag groups within the next FIFO queue time span.
   *
   * @param startNewTimeTracker if new time tracker should be generated, not read from storage
   * @param includeTagGroupA if tag group A data should be included
   * @param includeTagGroupB if tag group B data should be included
   * @param includeTagGroupC if tag group C data should be included
   * @param includeTagGroupD if tag group D data should be included
   * @return historical log data
   * @throws IOException if unable to read or write files
   */
  public static synchronized ArrayList getFifoNextSpanData(
      boolean startNewTimeTracker,
      boolean includeTagGroupA,
      boolean includeTagGroupB,
      boolean includeTagGroupC,
      boolean includeTagGroupD)
      throws IOException {
    // Get start time from file, or start new time tracker if startNewTimeTracker is true.
    final String timeMarkerFileName =
        HistoricalDataConstants.QUEUE_FILE_FOLDER
            + "/"
            + HistoricalDataConstants.QUEUE_TIME_FILE_NAME
            + HistoricalDataConstants.QUEUE_FILE_EXTENSION;
    String startTimeTrackerMs;
    long startTimeTrackerMsLong;
    if (startNewTimeTracker) {
      startTimeTrackerMsLong = System.currentTimeMillis();
      startTimeTrackerMs = Long.toString(startTimeTrackerMsLong);
      FileAccessManager.writeStringToFile(timeMarkerFileName, startTimeTrackerMs);
    } else {
      startTimeTrackerMs = FileAccessManager.readFileToString(timeMarkerFileName);
      startTimeTrackerMsLong = Long.parseLong(startTimeTrackerMs);
    }

    /*
     * Calculate end time from start time + time span. Use current time if calculated
     * end time is in the future.
     */
    long startTimeTrackerMsPlusSpan = startTimeTrackerMsLong + getQueueFifoTimeSpanMillis();
    long endTimeTrackerMsLong = Math.min(startTimeTrackerMsPlusSpan, System.currentTimeMillis());

    // Run EBD export call
    final String ebdFileName =
        HistoricalDataConstants.QUEUE_FILE_FOLDER
            + "/"
            + HistoricalDataConstants.QUEUE_EBD_FILE_NAME
            + HistoricalDataConstants.QUEUE_FILE_EXTENSION;
    final String ebdStartTime = convertToEBDTimeFormat(startTimeTrackerMsLong);
    final String ebdEndTime = convertToEBDTimeFormat(endTimeTrackerMsLong);
    HistoricalDataManager.exportHistoricalToFile(
        ebdStartTime,
        ebdEndTime,
        ebdFileName,
        includeTagGroupA,
        includeTagGroupB,
        includeTagGroupC,
        includeTagGroupD);

    // Parse EBD export call
    ArrayList queueData = HistoricalDataManager.parseHistoricalFile(ebdFileName);

    // Store end time +1 ms (to prevent duplicate data)
    final String newTimeTrackerVal = Long.toString(endTimeTrackerMsLong + 1);
    FileAccessManager.writeStringToFile(timeMarkerFileName, newTimeTrackerVal);

    // Return data
    return queueData;
  }
}
