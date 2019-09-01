package eu.arrowhead.common.database;

import java.sql.Timestamp;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "logs")
public class Logs {

  private int id;
  private Timestamp date;
  private String origin;
  private String level;
  private String message;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Basic
  @Column(name = "date")
  public Timestamp getDate() {
    return date;
  }

  public void setDate(Timestamp date) {
    this.date = date;
  }

  @Basic
  @Column(name = "origin")
  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  @Basic
  @Column(name = "level")
  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  @Basic
  @Column(name = "message", length = 2047)
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Logs logs = (Logs) o;

    if (id != logs.id) {
      return false;
    }
    if (date != null ? !date.equals(logs.date) : logs.date != null) {
      return false;
    }
    if (origin != null ? !origin.equals(logs.origin) : logs.origin != null) {
      return false;
    }
    if (level != null ? !level.equals(logs.level) : logs.level != null) {
      return false;
    }
    if (message != null ? !message.equals(logs.message) : logs.message != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (date != null ? date.hashCode() : 0);
    result = 31 * result + (origin != null ? origin.hashCode() : 0);
    result = 31 * result + (level != null ? level.hashCode() : 0);
    result = 31 * result + (message != null ? message.hashCode() : 0);
    return result;
  }
}
