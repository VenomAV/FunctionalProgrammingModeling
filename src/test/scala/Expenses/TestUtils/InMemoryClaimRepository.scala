package Expenses.TestUtils

import Expenses.Model.Claim
import Expenses.Repositories.{ClaimRepository, ClaimRepositoryME}
import Expenses.TestUtils.AcceptanceTestUtils.{OrError, Test, TestME, TestState}
import Expenses.Utils.ErrorManagement.ApplicationResult
import cats.data.State
import cats.data.StateT.modify
import cats.implicits._

class InMemoryClaimRepository extends ClaimRepository[Test] {
  override def save(claim: Claim): Test[ApplicationResult[Unit]] =
    State {
      state => (state.copy(claims = claim :: state.claims), Right(()))
    }
}

class InMemoryClaimRepositoryME extends ClaimRepositoryME[TestME] {
  override def save(claim: Claim): TestME[Unit] =
    for {
      _ <- modify[OrError, TestState](state => state.copy(claims = claim :: state.claims))
    } yield ()
}
