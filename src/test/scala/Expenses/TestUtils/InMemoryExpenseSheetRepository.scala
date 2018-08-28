package Expenses.TestUtils

import Expenses.Model.ExpenseSheet
import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Repositories.ExpenseSheetRepository
import Expenses.TestUtils.AcceptanceTestUtils.Test
import Expenses.Utils.ErrorManagement.ApplicationResult
import cats.data.{NonEmptyList, State}
import Expenses.Utils.ErrorManagement.implicits._

class InMemoryExpenseSheetRepository extends ExpenseSheetRepository[Test]{

  override def get(id: ExpenseSheetId): Test[ApplicationResult[ExpenseSheet]] =
    State.get.map(_.expenseSheets.find(_.id == id).orError(s"Unable to find expense sheet $id"))

  override def save(expenseSheet: ExpenseSheet): Test[ApplicationResult[Unit]] = {
    State {
      state => {
        if (!state.employees.exists(_.id == expenseSheet.employee.id))
          (state, Left(NonEmptyList(s"Unable to find employee ${expenseSheet.employee.id}", List())))
        else {
          val idx = state.expenseSheets.indexWhere(x => x.id == expenseSheet.id)

          if (idx == -1)
            (state.copy(expenseSheets = expenseSheet :: state.expenseSheets), Right(()))
          else
            (state.copy(expenseSheets = state.expenseSheets.patch(idx, Seq(expenseSheet), 1)), Right(()))
        }
      }
    }
  }
}
