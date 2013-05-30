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
    
    String major_minor = version.substring(0,build_separator_idx);
    this.major = Integer.parseInt(major_minor.split("\\.")[0]);
    this.minor = Integer.parseInt(major_minor.split("\\.")[1]);
    
    this.build = version.substring(build_separator_idx + 1);
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
