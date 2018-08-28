package Infrastructure.Repositories

import Expenses.Model.{Claim, PendingClaim}
import Expenses.Repositories.ClaimRepository
import Expenses.Utils.ErrorManagement.ApplicationResult
import Infrastructure.Repositories.Doobie.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import cats.implicits._
import Expenses.Utils.ErrorManagement.implicits._

class DoobieClaimRepository extends ClaimRepository[ConnectionIO] {
  type ClaimType = String

  override def save(claim: Claim): ConnectionIO[ApplicationResult[Unit]] =
    sql"""insert into claims (id, type, employeeid, expenses)
            values (${claim.id}, ${claimType(claim)}, ${claim.employee.id}, ${claim.expenses.toList})"""
      .update.run.attempt.map(_.map(_ =>()).leftMap(_.toError))

  private def claimType(claim: Claim) : ClaimType = claim match {
    case PendingClaim(_, _, _) => "P"
  }
}
