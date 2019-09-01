/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.base.MoreObjects;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.json.support.ArrowheadServiceSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Representation of a service within Arrowhead.
 *
 * @author uzoltan
 * @since 4.2
 */
@Entity
@Table(name = "arrowhead_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"service_definition"})})
public class ArrowheadService {

  @Id
  @GenericGenerator(name = "table_generator", strategy = "org.hibernate.id.enhanced.TableGenerator")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "table_generator")
  private Long id;

  @NotBlank
  @Size(max = 255, message = "Service serviceDefinition must be 255 character at max")
  @Column(name = "service_definition")
  private String serviceDefinition;

  @Size(max = 100, message = "Service can only have 100 interfaces at max")
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "arrowhead_service_interfaces", joinColumns = @JoinColumn(name = "arrowhead_service_id"))
  private Set<String> interfaces = new HashSet<>();

  @Transient
  @JsonInclude(Include.NON_EMPTY)
  @Size(max = 100, message = "Service can only have 100 serviceMetadata key-value pairs at max")
  private Map<String, String> serviceMetadata = new HashMap<>();

  public ArrowheadService() {
  }

  /**
   * Constructor with all the fields of the ArrowheadService class.
   *
   * @param serviceDefinition A descriptive name for the service
   * @param interfaces The set of interfaces that can be used to consume this service (helps interoperability between
   *     ArrowheadSystems). Concrete meaning of what is an interface is service specific (e.g. JSON, I2C)
   * @param serviceMetadata Arbitrary additional serviceMetadata belonging to the service, stored as key-value pairs.
   */
  public ArrowheadService(String serviceDefinition, Set<String> interfaces, Map<String, String> serviceMetadata) {
    this.serviceDefinition = serviceDefinition;
    this.interfaces = interfaces;
    this.serviceMetadata = serviceMetadata;
  }

  public ArrowheadService(ArrowheadServiceSupport service) {
    this.serviceDefinition = service.getServiceGroup() + "_" + service.getServiceDefinition();
    this.interfaces = new HashSet<>(service.getInterfaces());
    this.serviceMetadata = service.getServiceMetadata();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getServiceDefinition() {
    return serviceDefinition;
  }

  public void setServiceDefinition(String serviceDefinition) {
    this.serviceDefinition = serviceDefinition;
  }

  public Set<String> getInterfaces() {
    return interfaces;
  }

  @JsonSetter
  public void setInterfaces(Set<String> interfaces) {
    for (String serviceInterface : interfaces) {
      if (serviceInterface == null || serviceInterface.trim().isEmpty()) {
        throw new BadPayloadException("ArrowheadService interface can not be blank!");
      }
    }
    this.interfaces = interfaces;
  }

  public Map<String, String> getServiceMetadata() {
    return serviceMetadata;
  }

  @JsonSetter
  public void setServiceMetadata(Map<String, String> serviceMetadata) {
    for (Map.Entry<String, String> entry : serviceMetadata.entrySet()) {
      String key = entry.getKey();
      if (key == null || key.trim().isEmpty()) {
        throw new BadPayloadException("ArrowheadService metadata key can not be blank!");
      }
      String value = entry.getValue();
      if (value == null || value.trim().isEmpty()) {
        throw new BadPayloadException("ArrowheadService metadata value can not be blank!");
      }
    }
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArrowheadService)) {
      return false;
    }
    ArrowheadService that = (ArrowheadService) o;

    if (!Objects.equals(serviceDefinition, that.serviceDefinition)) {
      return false;
    }

    //2 services can be equal if they have at least 1 common interface
    if (interfaces == null || that.interfaces == null) {
      return true;
    } else {
      Set<String> intersection = new HashSet<>(interfaces);
      intersection.retainAll(that.interfaces);
      return !intersection.isEmpty();
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceDefinition);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("serviceDefinition", serviceDefinition).toString();
  }

  @SuppressWarnings("unchecked")
  public void partialUpdate(ArrowheadService other) {
    this.serviceDefinition = other.getServiceDefinition() != null ? other.getServiceDefinition() : this.serviceDefinition;
    //Making deep copies of the collections with the help of JSON (de)serialization
    this.interfaces = other.getInterfaces().isEmpty() ? this.interfaces : Utility
        .fromJson(Utility.toPrettyJson(null, other.getInterfaces()), Set.class);
    this.serviceMetadata = other.getServiceMetadata().isEmpty() ? this.serviceMetadata : Utility.fromJson(Utility.toPrettyJson(null, other.getServiceMetadata()), Map.class);
  }
}