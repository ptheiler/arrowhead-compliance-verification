/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

public class Event {

  @NotBlank
  @Size(max = 255, message = "Event type must be 255 character at max")
  private String type;
  private String payload;
  private ZonedDateTime timestamp;
  private Map<String, String> eventMetadata = new HashMap<>();

  public Event() {
  }

  public Event(String type, String payload, ZonedDateTime timestamp, Map<String, String> eventMetadata) {
    this.type = type;
    this.payload = payload;
    this.timestamp = timestamp;
    this.eventMetadata = eventMetadata;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(ZonedDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public Map<String, String> getEventMetadata() {
    return eventMetadata;
  }

  public void setEventMetadata(Map<String, String> eventMetadata) {
    this.eventMetadata = eventMetadata;
  }

}
