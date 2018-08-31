package Expenses.Model

import java.util.UUID

import Expenses.Utils.ErrorManagement.Validated
import cats.data.NonEmptyList
import cats.implicits._
import Claim.implicits._

sealed case class ClaimId(uuid: UUID)

sealed trait Claim {
  def id: ClaimId
  def employee: Employee
  def expenses: NonEmptyList[Expense]
}

case class PendingClaim private (id: ClaimId, employee: Employee, expenses: NonEmptyList[Expense]) extends Claim

object Claim {
  object implicits {
    import scala.language.implicitConversions

    implicit def uuidToClaimId(uuid: UUID) : ClaimId = ClaimId(uuid)
  }
}

object PendingClaim {
  def create(employee: Employee, expenses: NonEmptyList[Expense]) : Validated[PendingClaim] =
    new PendingClaim(UUID.randomUUID(), employee, expenses).validNel
}