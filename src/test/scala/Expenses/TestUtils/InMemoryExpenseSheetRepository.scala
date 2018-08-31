package Expenses.TestUtils

import Expenses.Model.{ExpenseSheet, ExpenseSheetId}
import Expenses.Repositories.ExpenseSheetRepository
import Expenses.TestUtils.AcceptanceTestUtils.{OrError, Test, TestState}
import cats.data.StateT
import cats.data.StateT.{liftF, modify, pure}
import cats.implicits._

class InMemoryExpenseSheetRepository extends ExpenseSheetRepository[Test]{

  override def get(id: ExpenseSheetId): Test[ExpenseSheet] =
    for {
      state <- StateT.get[OrError, TestState]
      result <- state.expenseSheets.find(_.id == id) match {
        case Some(x) => pure[OrError, TestState, ExpenseSheet](x)
        case _ => liftF[OrError, TestState, ExpenseSheet](Left(new Error(s"Unable to find expense sheet $id")))
      }
    } yield result

  override def save(expenseSheet: ExpenseSheet): Test[Unit] =
    for {
      state <- StateT.get[OrError, TestState]
      _ <- if(!state.employees.exists(_.id == expenseSheet.employee.id))
          liftF[OrError, TestState, Unit](Left(new Error(s"Unable to find employee ${expenseSheet.employee.id}")))
        else pure[OrError, TestState, Unit](())
      idx = state.expenseSheets.indexWhere(x => x.id == expenseSheet.id)
      _ <- if (idx == -1) modify[OrError, TestState](state => state.copy(expenseSheets = expenseSheet :: state.expenseSheets))
        else modify[OrError, TestState](state => state.copy(expenseSheets = state.expenseSheets.patch(idx, Seq(expenseSheet), 1)))
    } yield ()
}
