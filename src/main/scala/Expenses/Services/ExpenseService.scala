package Expenses.Services

import Expenses.Model._
import Expenses.Utils.ErrorManagement.ValidationResult
import cats.data.NonEmptyList
import cats.implicits._

object ExpenseService {
  def openFor(employee: Employee): ValidationResult[OpenExpenseSheet] =
    ExpenseSheet.createOpen(employee, List[Expense]())

  def addExpenseTo(expense: Expense, expenseSheet: OpenExpenseSheet): ValidationResult[OpenExpenseSheet] =
    ExpenseSheet.createOpen(expenseSheet.id, expenseSheet.employee, expenseSheet.expenses :+ expense)

  def claim(expenseSheet: OpenExpenseSheet): ValidationResult[(ClaimedExpenseSheet, PendingClaim)] =
    expenseSheet.expenses match {
      case h::t =>
        (ExpenseSheet.createClaimed(expenseSheet.id, expenseSheet.employee, expenseSheet.expenses),
          PendingClaim.create(expenseSheet.employee, NonEmptyList(h, t))).mapN((_, _))
      case _ => "Cannot claim empty expense sheet".invalidNel
    }
}


