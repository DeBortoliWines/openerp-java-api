package com.debortoliwines.odoo.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class VersionTest {
	private static final String DEFAULT_BUILD = "0";
	private static final int DEFAULT_MAJOR_MINOR = -1;
	private static final int MINOR = 2;
	private static final int MAJOR = 1;
	private static final String BUILD = "build";

	@Test
	public void should_extract_major_minor_build() throws Exception {
		Version version = new Version(Integer.toString(MAJOR) + "." + Integer.toString(MINOR) + "-" + BUILD);

		// Use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(version.getMajor()).isEqualTo(MAJOR);
		softAssertions.assertThat(version.getMinor()).isEqualTo(MINOR);
		softAssertions.assertThat(version.getBuild()).isEqualTo(BUILD);

		// Don't forget to call SoftAssertions global verification !
		softAssertions.assertAll();
	}

	@Test
	public void should_default_unrecognized_fields() throws Exception {
		Version version = new Version("A.B");

		// Use SoftAssertions instead of direct assertThat methods
		// to collect all failing assertions in one go
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(version.getMajor()).isEqualTo(DEFAULT_MAJOR_MINOR);
		softAssertions.assertThat(version.getMinor()).isEqualTo(DEFAULT_MAJOR_MINOR);
		softAssertions.assertThat(version.getBuild()).isEqualTo(DEFAULT_BUILD);

		// Don't forget to call SoftAssertions global verification !
		softAssertions.assertAll();
	}

	@Test
	public void should_keep_original_string_for_toString() throws Exception {
		String randomString = "whatever@#&./§?%£923";
		Version version = new Version(randomString);
		assertThat(version.toString()).isEqualTo(randomString);
	}
}
