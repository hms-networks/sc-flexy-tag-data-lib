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

  /** Local time offset in milliseconds. */
  private static long timeOffsetMilliseconds = 0;

  /** Boolean flag indicating if string history data should be included in queue data. */
  private static boolean stringHistoryEnabled = false;

  /** File path for time marker */
  private static final String timeMarkerFileName =
      HistoricalDataConstants.QUEUE_FILE_FOLDER
          + "/"
          + HistoricalDataConstants.QUEUE_TIME_FILE_NAME
          + HistoricalDataConstants.QUEUE_FILE_EXTENSION;

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
   * Configures the local time offset of the historical data queue.
   *
   * @param timeOffsetMilliseconds local time offset in milliseconds
   */
  public static void setLocalTimeOffset(long timeOffsetMilliseconds) {
    HistoricalDataQueueManager.timeOffsetMilliseconds = timeOffsetMilliseconds;
  }

  /**
   * Gets the current time with the configured time offset.
   *
   * @return current time minus local time offset
   */
  public static long getCurrentTimeWithOffset() {
    return System.currentTimeMillis() - timeOffsetMilliseconds;
  }

  /**
   * Sets the flag indicating if string history data should be included in queue data.
   *
   * @param stringHistoryEnabled true if string history should be include, false if not
   */
  public static void setStringHistoryEnabled(boolean stringHistoryEnabled) {
    HistoricalDataQueueManager.stringHistoryEnabled = stringHistoryEnabled;
  }

  /**
   * Gets a boolean representing if the time tracker file exists.
   *
   * @return true if time tracker file exists
   */
  public static boolean doesTimeTrackerExist() {
    return new File(timeMarkerFileName).isFile();
  }

  /**
   * Gets the current value in the time tracker file.
   *
   * @throws IOException if unable to read file
   * @return time tracker file value
   */
  public static long getCurrentTimeTrackerValue() throws IOException {
    String startTimeTrackerMs = FileAccessManager.readFileToString(timeMarkerFileName);
    return Long.parseLong(startTimeTrackerMs);
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
    String startTimeTrackerMs;
    long startTimeTrackerMsLong;
    if (startNewTimeTracker) {
      startTimeTrackerMsLong = getCurrentTimeWithOffset();
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
    long endTimeTrackerMsLong = Math.min(startTimeTrackerMsPlusSpan, getCurrentTimeWithOffset());

    // Calculate EBD start and end time
    final String ebdStartTime = convertToEBDTimeFormat(startTimeTrackerMsLong);
    final String ebdEndTime = convertToEBDTimeFormat(endTimeTrackerMsLong);

    // Run standard EBD export call (int, float, ...)
    final String ebdFileName =
        HistoricalDataConstants.QUEUE_FILE_FOLDER
            + "/"
            + HistoricalDataConstants.QUEUE_EBD_FILE_NAME
            + HistoricalDataConstants.QUEUE_FILE_EXTENSION;
    boolean stringHistorical = false;
    HistoricalDataManager.exportHistoricalToFile(
        ebdStartTime,
        ebdEndTime,
        ebdFileName,
        includeTagGroupA,
        includeTagGroupB,
        includeTagGroupC,
        includeTagGroupD,
        stringHistorical);

    // Parse standard EBD export call
    ArrayList queueData = HistoricalDataManager.parseHistoricalFile(ebdFileName);

    // Run string EBD export call if enabled
    if (stringHistoryEnabled) {
      final String ebdStringFileName =
          HistoricalDataConstants.QUEUE_FILE_FOLDER
              + "/"
              + HistoricalDataConstants.QUEUE_EBD_STRING_FILE_NAME
              + HistoricalDataConstants.QUEUE_FILE_EXTENSION;
      stringHistorical = true;
      HistoricalDataManager.exportHistoricalToFile(
          ebdStartTime,
          ebdEndTime,
          ebdFileName,
          includeTagGroupA,
          includeTagGroupB,
          includeTagGroupC,
          includeTagGroupD,
          stringHistorical);

      // Parse string EBD export call and combine with standard EBD call results
      ArrayList queueStringData = HistoricalDataManager.parseHistoricalFile(ebdStringFileName);
      queueData.addAll(queueStringData);
    }

    // Store end time +1 ms (to prevent duplicate data)
    final String newTimeTrackerVal = Long.toString(endTimeTrackerMsLong + 1);
    FileAccessManager.writeStringToFile(timeMarkerFileName, newTimeTrackerVal);

    // Return data
    return queueData;
  }
}
