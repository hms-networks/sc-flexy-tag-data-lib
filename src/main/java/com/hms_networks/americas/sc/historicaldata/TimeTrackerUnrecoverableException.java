package com.hms_networks.americas.sc.historicaldata;

/**
 * Class to throw custom exception in the case both primary and secondary time tracking files are
 * corrupted. This exception is only thrown when we lose data.
 *
 * @author HMS Networks, MU Americas Solution Center
 * @since 2.2
 */
public class TimeTrackerUnrecoverableException extends Exception {

  /**
   * Constructor for the TimeTrackerUnrecoverableException.
   *
   * @param errorMessage the message to associate with this exception
   */
  public TimeTrackerUnrecoverableException(String errorMessage) {
    super(errorMessage);
  }
}
