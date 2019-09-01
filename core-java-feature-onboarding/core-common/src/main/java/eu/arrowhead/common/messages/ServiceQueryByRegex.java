package eu.arrowhead.common.messages;

import javax.validation.constraints.NotNull;

public class ServiceQueryByRegex {

  public enum FieldType {systemName, serviceDefinition, interfaces}

  @NotNull(message = "Field name can not be null, valid values are: systemName, serviceDefinition, interfaces")
  private FieldType fieldName;
  @NotNull(message = "The regular expression can not be null")
  private String regularExpression;

  private Boolean partialMatch = true;

  public ServiceQueryByRegex() {
  }

  public ServiceQueryByRegex(FieldType fieldName, String regularExpression, Boolean partialMatch) {
    this.fieldName = fieldName;
    this.regularExpression = regularExpression;
    this.partialMatch = partialMatch;
  }

  public FieldType getFieldName() {
    return fieldName;
  }

  public void setFieldName(FieldType fieldName) {
    this.fieldName = fieldName;
  }

  public String getRegularExpression() {
    return regularExpression;
  }

  public void setRegularExpression(String regularExpression) {
    this.regularExpression = regularExpression;
  }

  public Boolean getPartialMatch() {
    return partialMatch;
  }

  public void setPartialMatch(Boolean partialMatch) {
    this.partialMatch = partialMatch;
  }
}
