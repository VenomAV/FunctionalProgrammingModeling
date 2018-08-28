package Expenses.Model

import java.util.UUID

import Expenses.Model.Claim.ClaimId
import Expenses.Utils.ErrorManagement.ValidationResult
import cats.data.NonEmptyList
import cats.implicits._

sealed trait Claim {
  def id: ClaimId
  def employee: Employee
  def expenses: NonEmptyList[Expense]
}

case class PendingClaim private (id: ClaimId, employee: Employee, expenses: NonEmptyList[Expense]) extends Claim

object Claim {
  type ClaimId = UUID
}

object PendingClaim {
  def create(employee: Employee, expenses: NonEmptyList[Expense]) : ValidationResult[PendingClaim] =
    new PendingClaim(UUID.randomUUID(), employee, expenses).validNel
}