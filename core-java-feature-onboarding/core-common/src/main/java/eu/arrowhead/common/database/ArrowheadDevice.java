/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "arrowhead_device")
public class ArrowheadDevice {

  @Id
  @GenericGenerator(name = "table_generator", strategy = "org.hibernate.id.enhanced.TableGenerator")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "table_generator")
  private Long id;

  @NotBlank
  @Column(name = "device_name")
  @Size(max = 255, message = "System name must be 255 character at max")
  private String deviceName;

  public ArrowheadDevice() {
  }

  public ArrowheadDevice(String deviceName) {
    this.deviceName = deviceName;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArrowheadDevice)) {
      return false;
    }
    ArrowheadDevice that = (ArrowheadDevice) o;
    return Objects.equals(deviceName, that.deviceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("deviceName", deviceName).toString();
  }
}
