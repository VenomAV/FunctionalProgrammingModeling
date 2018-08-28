package Expenses.TestUtils

import Expenses.Model.ExpenseSheet
import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Repositories.ExpenseSheetRepository
import Expenses.TestUtils.AcceptanceTestUtils.Test
import cats.data.State

class InMemoryExpenseSheetRepository extends ExpenseSheetRepository[Test]{

  override def get(id: ExpenseSheetId): Test[Option[ExpenseSheet]] =
    State.get.map(_.expenseSheets.find(_.id == id))

  override def save(expenseSheet: ExpenseSheet): Test[Unit] = {
    State {
      state => {
        if (!state.employees.exists(_.id == expenseSheet.employee.id))
          (state, ())
        else {
          val idx = state.expenseSheets.indexWhere(x => x.id == expenseSheet.id)

          if (idx == -1)
            (state.copy(expenseSheets = expenseSheet :: state.expenseSheets), ())
          else
            (state.copy(expenseSheets = state.expenseSheets.patch(idx, Seq(expenseSheet), 1)), ())
        }
      }
    }
  }
}
