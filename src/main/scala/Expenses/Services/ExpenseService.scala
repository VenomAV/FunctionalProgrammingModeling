package Expenses.Services

import Expenses.Model._
import Expenses.Utils.ErrorManagement.implicits.ValidationResultToApplicativeError
import cats.ApplicativeError
import cats.data.NonEmptyList
import cats.implicits.catsSyntaxTuple2Semigroupal

object ExpenseService {
  def openFor[F[_]](employee: Employee)(implicit AE: ApplicativeError[F, Throwable]): F[OpenExpenseSheet] =
    ExpenseSheet.createOpen(employee, List[Expense]()).orRaiseError

  def addExpenseTo[F[_]](expense: Expense, expenseSheet: OpenExpenseSheet)
                        (implicit AE: ApplicativeError[F, Throwable]): F[OpenExpenseSheet] =
    ExpenseSheet.createOpen(expenseSheet.id, expenseSheet.employee, expenseSheet.expenses :+ expense).orRaiseError

  def claim[F[_]](expenseSheet: OpenExpenseSheet)
                 (implicit AE: ApplicativeError[F, Throwable]): F[(ClaimedExpenseSheet, PendingClaim)] =
    expenseSheet.expenses match {
      case h::t =>
        (ExpenseSheet.createClaimed(expenseSheet.id, expenseSheet.employee, expenseSheet.expenses),
          PendingClaim.create(expenseSheet.employee, NonEmptyList(h, t))).mapN((_, _)).orRaiseError
      case _ => AE.raiseError(new Error("Cannot claim empty expense sheet"))
    }
}


