package Expenses.Repositories

import Expenses.Model.ExpenseSheet
import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Utils.ErrorManagement.ApplicationResult

trait ExpenseSheetRepository[F[_]] {
  def get(id: ExpenseSheetId) : F[ApplicationResult[ExpenseSheet]]
  def save(expenseSheet: ExpenseSheet) : F[ApplicationResult[Unit]]
}

trait ExpenseSheetRepositoryME[F[_]] {
  def get(id: ExpenseSheetId) : F[ExpenseSheet]
  def save(expenseSheet: ExpenseSheet) : F[Unit]
}
