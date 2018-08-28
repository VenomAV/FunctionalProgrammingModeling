package Expenses.ApplicationServices

import Expenses.Model.Employee.EmployeeId
import Expenses.Model.{Expense, ExpenseSheet, OpenExpenseSheet}
import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Repositories.{ClaimRepository, EmployeeRepository, ExpenseSheetRepository}
import Expenses.Services.ExpenseService
import Expenses.Utils.ErrorManagement.{ApplicationResult, Error, ErrorList}
import Expenses.Utils.ErrorManagement.implicits._
import cats._
import cats.implicits._

import scala.reflect.ClassTag

object ExpenseApplicationService {
  def openFor[F[_]](id: EmployeeId)
                   (implicit M:Monad[F],
                    er: EmployeeRepository[F],
                    esr: ExpenseSheetRepository[F]) : F[ApplicationResult[Unit]] =
    for {
      maybeEmployee <- er.get(id)
      result <- execute(for {
        employee <- maybeEmployee.orError("Unable to find employee")
        openExpenseSheet <- ExpenseService.openFor(employee).toEither
        result <- esr.save(openExpenseSheet).asRight
      } yield result)
    } yield result

  def addExpenseTo[F[_]](expense: Expense, id: ExpenseSheetId)
                        (implicit M:Monad[F],
                         esr: ExpenseSheetRepository[F]) : F[ApplicationResult[Unit]] =
    for {
      maybeExpenseSheet <- esr.get(id)
      result <- execute(for {
        openExpenseSheet <- checkOpenExpenseSheet(maybeExpenseSheet, id)
        newOpenExpenseSheet <- ExpenseService.addExpenseTo(expense, openExpenseSheet).toEither
        result <- esr.save(newOpenExpenseSheet).asRight
      } yield result)
    } yield result

  def claim[F[_]](id: ExpenseSheetId)
                 (implicit M:Monad[F],
                  esr: ExpenseSheetRepository[F],
                  cr: ClaimRepository[F]) : F[ApplicationResult[Unit]] =
    for {
      maybeExpenseSheet <- esr.get(id)
      result <- execute(for {
        openExpenseSheet <- checkOpenExpenseSheet(maybeExpenseSheet, id)
        pair <- ExpenseService.claim(openExpenseSheet).toEither
        (claimedExpenseSheet, pendingClaim) = pair
        result <- (for {
          _ <- esr.save(claimedExpenseSheet)
          _ <- cr.save(pendingClaim)
        } yield ()).asRight
      } yield result)
    } yield result

  private def checkOpenExpenseSheet(maybeExpenseSheet: Option[ExpenseSheet],
                                  id: ExpenseSheetId) : ApplicationResult[OpenExpenseSheet] =
    for {
      expenseSheet <- maybeExpenseSheet
        .orError("Unable to find expense sheet")
      openExpenseSheet <- tryCastTo[OpenExpenseSheet](expenseSheet)
        .orError(s"$id is not an open expense sheet")
    } yield openExpenseSheet

  private def tryCastTo[A : ClassTag](a: Any) : Option[A] = a match {
    case b: A => Some(b)
    case _ => None
  }

  private def execute[F[_]](either: ApplicationResult[F[Unit]])
                           (implicit A:Applicative[F]): F[ApplicationResult[Unit]] =
    either.traverse(x => x)
}