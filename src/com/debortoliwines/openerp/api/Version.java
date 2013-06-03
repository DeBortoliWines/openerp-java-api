package com.debortoliwines.openerp.api;

/**
 * Breaks up the OpenERP server version into a major, minor and build number
 * @author Pieter van der Merwe
 * @since  May 30, 2013
 */
public class Version {

  private final String full_version;
  private final int major;
  private final int minor;
  private final String build;
  
  public Version(String version){
    this.full_version = version;
    int build_separator_idx = version.indexOf("-");
    
    String major_minor = version;
    
    // If there is a build number
    if (build_separator_idx >= 0 && version.length() > 1){
      major_minor = version.substring(0,build_separator_idx);
      this.build = version.substring(build_separator_idx + 1);
    }
    else this.build = "0";
    
    String[] major_minor_obj = major_minor.split("\\.");
    
    int maj = -1;
    try{
      maj = Integer.parseInt(major_minor_obj[0]);
    }
    catch(Exception e){
      maj = -1;
    }
    this.major = maj;
    
    int min = -1;
    try{
      if (major_minor_obj.length > 1)
        min = Integer.parseInt(major_minor_obj[1]);
    }
    catch(Exception e){
      min = -1;
    }
    this.minor = min;
    
  }

  /**
   * Get the major version of the OpenERP version string
   * @return Major version number
   */
  public int getMajor() {
    return major;
  }

  /**
   * Get the minor version of the OpenERP version string
   * @return Minor version number
   */
  public int getMinor() {
    return minor;
  }

  /**
   * Get the build number of the OpenERP version string
   * @return Build number
   */
  public String getBuild() {
    return build;
  }
  
  @Override
  public String toString() {
    return this.full_version;
  }
}
