package com.hms_networks.americas.sc.historicaldata;

import com.ewon.ewonitf.Exporter;

import com.hms_networks.americas.sc.datapoint.DataPoint;
import com.hms_networks.americas.sc.datapoint.DataPointBoolean;
import com.hms_networks.americas.sc.datapoint.DataPointDword;
import com.hms_networks.americas.sc.datapoint.DataPointFloat;
import com.hms_networks.americas.sc.datapoint.DataPointInteger;
import com.hms_networks.americas.sc.datapoint.DataPointString;
import com.hms_networks.americas.sc.string.QuoteSafeStringTokenizer;
import com.hms_networks.americas.sc.taginfo.TagInfo;
import com.hms_networks.americas.sc.taginfo.TagInfoManager;
import com.hms_networks.americas.sc.taginfo.TagType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class to manage retrieving tag information and historical logs using export block descriptors.
 *
 * @author HMS Networks, MU Americas Solution Center
 * @since 1.0
 */
public class HistoricalDataManager {

  /**
   * Exports the historical log for tags in tag groups A, B, C and D between <code>startTime</code>
   * and <code>endTime</code> to <code>destinationFileName</code>.
   *
   * @param startTime start time of export
   * @param endTime end time of export
   * @param destinationFileName path of destination file
   * @throws IOException if export block descriptor fails
   */
  public static void exportAllHistoricalToFile(
      String startTime, String endTime, String destinationFileName) throws IOException {
    final boolean includeTagGroupA = true;
    final boolean includeTagGroupB = true;
    final boolean includeTagGroupC = true;
    final boolean includeTagGroupD = true;
    exportHistoricalToFile(
        startTime,
        endTime,
        destinationFileName,
        includeTagGroupA,
        includeTagGroupB,
        includeTagGroupC,
        includeTagGroupD);
  }

  /**
   * Exports the historical log for tags in specified tag groups between <code>startTime</code> and
   * <code>endTime</code> to <code>destinationFileName</code>.
   *
   * @param startTime start time of export
   * @param endTime end time of export
   * @param destinationFileName path of destination file
   * @param includeTagGroupA include tag group A
   * @param includeTagGroupB include tag group B
   * @param includeTagGroupC include tag group C
   * @param includeTagGroupD include tag group D
   * @throws IOException if export block descriptor fails
   */
  public static void exportHistoricalToFile(
      String startTime,
      String endTime,
      String destinationFileName,
      boolean includeTagGroupA,
      boolean includeTagGroupB,
      boolean includeTagGroupC,
      boolean includeTagGroupD)
      throws IOException {
    // Check for valid group selection
    if (!includeTagGroupA && !includeTagGroupB && !includeTagGroupC && !includeTagGroupD) {
      throw new IllegalArgumentException(
          "Cannot generate historical logs with no tag groups selected.");
    }

    // Build string of tag groups for filter type
    String tagGroupFilterStr = "";
    if (includeTagGroupA) {
      tagGroupFilterStr += "A";
    }
    if (includeTagGroupB) {
      tagGroupFilterStr += "B";
    }
    if (includeTagGroupC) {
      tagGroupFilterStr += "C";
    }
    if (includeTagGroupD) {
      tagGroupFilterStr += "D";
    }

    /*
     * Build EBD string with parameters
     * dtHL: data type, historical logs
     * ftT: file type, text
     * startTime: start time for data
     * endTime: end time for data
     * flABCD: filter type, specified tag groups
     */
    final String ebdStr = "$dtHL$ftT$st" + startTime + "$et" + endTime + "$fl" + tagGroupFilterStr;

    // Perform EBD call
    Exporter exporter = new Exporter(ebdStr);
    exporter.ExportTo(HistoricalDataConstants.FILE_URL_PREFIX + destinationFileName);
    exporter.close();
  }

  /**
   * Parse the specified historical file line by line and return an array list of data points
   * parsed.
   *
   * @param filename historical file to parse
   * @return data points parsed
   * @throws IOException if unable to access or read file
   */
  public static ArrayList parseHistoricalFile(String filename) throws IOException {
    final int sleepBetweenLinesMs = 5;
    final BufferedReader reader = new BufferedReader(new FileReader(filename));

    // Create line counter and variable to store current line
    int lineCount = 0;
    String line = reader.readLine();

    // Loop through lines in file until end and store data points
    ArrayList dataPoints = new ArrayList();
    while (line != null) {

      // Only parse lines 1 and greater, skip header
      if (lineCount > 0) {
        // Parse line
        DataPoint lineDataPoint = parseHistoricalFileLine(line);
        dataPoints.add(lineDataPoint);

        /*
         * Reading historical log EBD file can take a large amount of time.
         * Sleeping the thread allows the Flexy time to perform other tasks
         * and service its watchdog timers.
         */
        try {
          Thread.sleep(sleepBetweenLinesMs);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      // Increment line count
      lineCount++;

      // Read next line before looping again
      line = reader.readLine();
    }
    return dataPoints;
  }

  /**
   * Convert the supplied string representation of a boolean to its corresponding boolean value.
   *
   * @param strBool boolean string representation
   * @return converted boolean
   */
  private static boolean convertStrToBool(String strBool) {
    return strBool.equals("1");
  }

  /**
   * Parse the specified historical file line and return its corresponding data point.
   * @param line line to parse
   * @return data point
   * @throws IOException if unable to access tag information
   */
  private static DataPoint parseHistoricalFileLine(String line) throws IOException {
    /*
     * Example Line:
     * "TagId";"TimeInt";"TimeStr";"IsInitValue";"Value";"IQuality"
     * 247;1582557658;"24/02/2020 15:20:58";0;0;3
     */

    // Create variables to store line data
    int tagId = -1;
    String tagTimeInt = "";
    String tagValue = "";

    // Create DataPoint for returning
    DataPoint returnVal = null;

    // Create tokenizer to process line
    final boolean returnDelimiters = false;
    QuoteSafeStringTokenizer tokenizer =
        new QuoteSafeStringTokenizer(
            line, HistoricalDataConstants.EBD_LINE_DELIMITER, returnDelimiters);

    // Loop through each token
    while (tokenizer.hasMoreElements()) {

      // Get next token
      String currentToken = tokenizer.nextToken();

      /*
       * For each token in the correct spot, grab the data.
       * Add a TagInfo object to
       */
      switch (tokenizer.getPrevTokenIndex()) {
        case HistoricalDataConstants.EBD_LINE_TAG_ID_INDEX:
          tagId = Integer.parseInt(currentToken);
          break;
        case HistoricalDataConstants.EBD_LINE_TAG_TIMEINT_INDEX:
          tagTimeInt = currentToken;
          break;
        case HistoricalDataConstants.EBD_LINE_TAG_VALUE_INDEX:
          tagValue = currentToken;
        case (HistoricalDataConstants.EBD_LINE_LENGTH - 1):
          // Check if tag information list available, populate list if not
          boolean tagInfoListAvailable = TagInfoManager.isTagInfoListPopulated();
          if (!tagInfoListAvailable) {
            TagInfoManager.refreshTagList();
          }

          // Get corresponding tag info object for tag
          int tagInfoListIDOffset = TagInfoManager.getLowestTagIdSeen();
          TagInfo tagInfo =
              (TagInfo) TagInfoManager.getTagInfoList().get(tagId - tagInfoListIDOffset);

          // Create data point for tag type
          TagType tagType = tagInfo.getType();
          String tagName = tagInfo.getName();
          if (tagType == TagType.BOOLEAN) {
            boolean boolValue = convertStrToBool(tagValue);
            returnVal = new DataPointBoolean(tagName, boolValue, tagTimeInt);
          } else if (tagType == TagType.FLOAT) {
            float floatValue = Float.valueOf(tagValue).floatValue();
            returnVal = new DataPointFloat(tagName, floatValue, tagTimeInt);
          } else if (tagType == TagType.INTEGER) {
            int intValue = Integer.valueOf(tagValue).intValue();
            returnVal = new DataPointInteger(tagName, intValue, tagTimeInt);
          } else if (tagType == TagType.DWORD) {
            long dwordValue = Long.valueOf(tagValue).longValue();
            returnVal = new DataPointDword(tagName, dwordValue, tagTimeInt);
          } else if (tagType == TagType.STRING) {
            returnVal = new DataPointString(tagName, tagValue, tagTimeInt);
          }
      }
    }
    return returnVal;
  }
}