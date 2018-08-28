package Expenses.Repositories

import Expenses.Model.Claim
import Expenses.Utils.ErrorManagement.ApplicationResult

trait ClaimRepository[F[_]] {
  def save(claim: Claim) : F[ApplicationResult[Unit]]
}

trait ClaimRepositoryME[F[_]] {
  def save(claim: Claim) : F[Unit]
}