package Expenses.TestUtils

import Expenses.Model.Claim
import Expenses.Repositories.ClaimRepository
import Expenses.TestUtils.AcceptanceTestUtils.{OrError, Test, TestState}
import cats.data.StateT.modify
import cats.implicits._

class InMemoryClaimRepository extends ClaimRepository[Test] {
  override def save(claim: Claim): Test[Unit] =
    for {
      _ <- modify[OrError, TestState](state => state.copy(claims = claim :: state.claims))
    } yield ()
}
