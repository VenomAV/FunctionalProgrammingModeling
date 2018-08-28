package Expenses.TestUtils

import Expenses.Model.Claim
import Expenses.Repositories.ClaimRepository
import Expenses.TestUtils.AcceptanceTestUtils.Test
import cats.data.State

class InMemoryClaimRepository extends ClaimRepository[Test] {
  override def save(claim: Claim): Test[Unit] =
    State {
      state => (state.copy(claims = claim :: state.claims), ())
    }
}
