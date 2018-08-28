package Expenses.TestUtils

import Expenses.Model.{Employee, ExpenseSheet}
import Expenses.Model.ExpenseSheet.ExpenseSheetId
import Expenses.Repositories.{ExpenseSheetRepository, ExpenseSheetRepositoryME}
import Expenses.TestUtils.AcceptanceTestUtils.{OrError, Test, TestME, TestState}
import Expenses.Utils.ErrorManagement.ApplicationResult
import Expenses.Utils.ErrorManagement.implicits._
import cats.data.StateT.{liftF, modify, pure}
import cats.data.{NonEmptyList, State, StateT}
import cats.implicits._

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

class InMemoryExpenseSheetRepositoryME extends ExpenseSheetRepositoryME[TestME]{

  override def get(id: ExpenseSheetId): TestME[ExpenseSheet] =
    for {
      state <- StateT.get[OrError, TestState]
      result <- state.expenseSheets.find(_.id == id) match {
        case Some(x) => pure[OrError, TestState, ExpenseSheet](x)
        case _ => liftF[OrError, TestState, ExpenseSheet](Left(new Error(s"Unable to find expense sheet $id")))
      }
    } yield result

  override def save(expenseSheet: ExpenseSheet): TestME[Unit] =
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
