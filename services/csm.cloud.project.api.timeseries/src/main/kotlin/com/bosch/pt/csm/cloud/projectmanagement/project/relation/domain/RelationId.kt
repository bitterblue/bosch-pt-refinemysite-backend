/*
 * ************************************************************************
 *
 *  Copyright:       Robert Bosch Power Tools GmbH, 2018 - 2023
 *
 * ************************************************************************
 */

package com.bosch.pt.csm.cloud.projectmanagement.project.relation.domain

import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class RelationId(@get:JsonValue val value: UUID) {

  override fun toString() = value.toString()
}

fun UUID.asRelationId() = RelationId(this)