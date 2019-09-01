package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonSetter;
import eu.arrowhead.common.exception.BadPayloadException;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.validator.constraints.NotEmpty;

public class OrchestrationStorePriorities {

  /*
    Key: Orchestration Store entry ID
    Value: the new priority the entry should have
   */
  @NotEmpty
  private Map<Long, Integer> priorities = new HashMap<>();

  public OrchestrationStorePriorities() {
  }

  public OrchestrationStorePriorities(Map<Long, Integer> priorities) {
    this.priorities = priorities;
  }

  public Map<Long, Integer> getPriorities() {
    return priorities;
  }

  @JsonSetter
  public void setPriorities(Map<Long, Integer> priorities) {
    for (Map.Entry<Long, Integer> entry : priorities.entrySet()) {
      Long key = entry.getKey();
      if (key == null || key < 1) {
        throw new BadPayloadException("Store entry ID must be at least 1");
      }
      Integer value = entry.getValue();
      if (value == null || value < 1) {
        throw new BadPayloadException("Store entry priority must be at least 1");
      }
    }
    this.priorities = priorities;
  }
}
