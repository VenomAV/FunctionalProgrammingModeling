package Expenses.Repositories

import Expenses.Model.{ExpenseSheet, ExpenseSheetId}

trait ExpenseSheetRepository[F[_]] {
  def get(id: ExpenseSheetId) : F[ExpenseSheet]
  def save(expenseSheet: ExpenseSheet) : F[Unit]
}
