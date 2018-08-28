package Expenses.TestUtils

import Expenses.Model.Claim
import Expenses.Repositories.ClaimRepository
import Expenses.TestUtils.AcceptanceTestUtils.Test
import Expenses.Utils.ErrorManagement.ApplicationResult
import cats.data.State

class InMemoryClaimRepository extends ClaimRepository[Test] {
  override def save(claim: Claim): Test[ApplicationResult[Unit]] =
    State {
      state => (state.copy(claims = claim :: state.claims), Right(()))
    }
}
