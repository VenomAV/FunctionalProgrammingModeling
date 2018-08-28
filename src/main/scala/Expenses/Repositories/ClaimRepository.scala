package Expenses.Repositories

import Expenses.Model.Claim

trait ClaimRepository[F[_]] {
  def save(claim: Claim) : F[Unit]
}