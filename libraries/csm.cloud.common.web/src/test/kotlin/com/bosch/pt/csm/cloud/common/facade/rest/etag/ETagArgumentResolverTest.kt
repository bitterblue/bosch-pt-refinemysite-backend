/*
 * ************************************************************************
 *
 *  Copyright:       Robert Bosch Power Tools GmbH, 2018 - 2020
 *
 * ************************************************************************
 */
package com.bosch.pt.csm.cloud.common.facade.rest.etag

import com.bosch.pt.csm.cloud.common.SmartSiteMockKTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders.IF_MATCH
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.lang.Nullable
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer

/** Unit test to verify correctly resolving the 'IF-MATCH' header. */
@SmartSiteMockKTest
internal class ETagArgumentResolverTest {
  private val cut = ETagArgumentResolver()

  @MockK private lateinit var methodParameter: MethodParameter

  @MockK private lateinit var modelAndViewContainer: ModelAndViewContainer

  @MockK private lateinit var webRequest: NativeWebRequest

  @MockK private lateinit var webDataBinderFactory: WebDataBinderFactory

  /** Verify that resolver supports parameters of type [ETag]. */
  @Test
  fun verifySupportsParameter() {
    every { methodParameter.parameterType } returns ETag::class.java
    val supportsETagParameter = cut.supportsParameter(methodParameter)
    assertThat(supportsETagParameter).isTrue
  }

  /** Verify that resolver does not support any other parameter type than [ETag]. */
  @Test
  fun verifyNotSupportsParameter() {
    every { methodParameter.parameterType } returns String::class.java
    val supportsETagParameter = cut.supportsParameter(methodParameter)
    assertThat(supportsETagParameter).isFalse
  }

  /** Verify that resolver resolves 'IF-MATCH' http header correctly to [ETag] instance. */
  @Test
  fun verifyResolveArgumentForIfMatchHeader() {
    every { webRequest.getHeader(IF_MATCH) } returns "\"1\""
    val eTag =
        cut.resolveArgument(
            methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory)
    assertThat(eTag).isNotNull.isEqualTo(ETag.from("1"))
  }

  /** Verify that resolver reports an error when no 'IF-MATCH' header is given. */
  @Test
  fun verifyResolveArgumentForMissingIfMatchHeader() {
    every { webRequest.getHeader(IF_MATCH) } returns null
    every { methodParameter.hasParameterAnnotation(Nullable::class.java) } returns false
    assertThatThrownBy {
          cut.resolveArgument(
              methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory)
        }
        .isInstanceOf(HttpMessageNotReadableException::class.java)
  }

  /** Verify that resolver reports an error when empty 'IF-MATCH' header value is given. */
  @Test
  fun verifyResolveArgumentForEmptyIfMatchHeader() {
    every { webRequest.getHeader(IF_MATCH) } returns ""
    every { methodParameter.hasParameterAnnotation(Nullable::class.java) } returns false
    assertThatThrownBy {
          cut.resolveArgument(
              methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory)
        }
        .isInstanceOf(HttpMessageNotReadableException::class.java)
        .hasMessage("Value for request header field 'If-Match' is missing.")
  }

  /** Verify that resolver allows for optional ETags. */
  @Test
  fun verifyResolveArgumentForAbsentETag() {
    every { webRequest.getHeader(IF_MATCH) } returns ""
    every { methodParameter.hasParameterAnnotation(Nullable::class.java) } returns true

    val eTag =
        cut.resolveArgument(
            methodParameter, modelAndViewContainer, webRequest, webDataBinderFactory)

    assertThat(eTag).isNull()
  }
}
