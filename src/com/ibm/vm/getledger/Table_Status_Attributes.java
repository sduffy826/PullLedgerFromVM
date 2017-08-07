package com.ibm.vm.getledger;

/**
 * This class holds the 'table_status' attribues (in_use and in_use_by), when
 * you instantiate this object you pass in those values.
 * 
 * @author sduffy
 * 
 */
public class Table_Status_Attributes {
  private String inUse;
  private String inUseBy;

  Table_Status_Attributes(String _inUse, String _inUseBy) {
    inUse = _inUse;
    inUseBy = _inUseBy;
  }

  // Define getters (no setters, can only be set in constructor)
  public String getInUse() {
    return inUse;
  }

  public String getInUseBy() {
    return inUseBy;
  }

  // For output :)
  public String toString() {
    return "{inUse=" + getInUse() + ", inUseBy=" + getInUseBy() + "}";
  }
}
